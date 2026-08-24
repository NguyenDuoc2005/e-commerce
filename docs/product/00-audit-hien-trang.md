# Audit hiện trạng domain Sản phẩm

## 1. Phạm vi và bằng chứng audit

Audit này được thực hiện ngày 2026-08-24 theo Mục 0 của `PROMPT-CHUAN-HOA-SAN-PHAM.md`, dựa trên source và dữ liệu đang chạy, không suy luận từ tài liệu cũ.

Phạm vi đã đọc:

- Toàn bộ entity, repository, request/response model, controller và service trong `backend-microservice/catalog-service/src/main`.
- Hai script thủ công `m10_dynamic_attributes_up.sql` và `m10_dynamic_attributes_down.sql`.
- Hai test hiện có của `catalog-service`.
- FE Seller `SellerProducts.vue` và `services/api/seller/product.api.ts`.
- FE Platform Admin `ProductAttributes.vue` và API client tương ứng.
- FE Buyer `ProductsView.vue`, `FilterBox.vue`, `ProductDetail.vue` và hai API client public sản phẩm.
- Feign client và điểm dùng contract catalog trong `cart-service`, `order-service`, `promotion-service`.
- Outbox, `ProductDocument`, Elasticsearch mapping và seed SQL hiện tại.
- Schema MySQL thật trong container `backend-microservice-mysql-1`, database `ecommerce_catalog`.

Không chạy lệnh DROP/CREATE/reset dữ liệu trong phiên audit này.

## 2. Ảnh chụp DB thật

Schema thật có 16 bảng:

| Nhóm | Bảng | Số dòng tại thời điểm audit |
|---|---|---:|
| Cứng theo ngành giày | `brand`, `color`, `material`, `origin`, `size`, `sole_type` | 2, 3, 2, 2, 3, 2 |
| Lõi sản phẩm | `category`, `product`, `product_variant`, `outbox` | 3, 4, 6, 4 |
| Thông số động | `category_attribute_suggestion`, `product_attribute_definition`, `product_attribute_option`, `product_attribute_value`, `product_attribute_value_option` | 5, 5, 0, 20, 0 |
| Hậu kiểm | `product_attribute_moderation_audit` | 0 |

Các ràng buộc chứng minh model đang “half-migrated”:

- `product.brand_id -> brand.id`.
- `product.origin_id -> origin.id`.
- `product.material_id -> material.id`.
- `product.sole_type_id -> sole_type.id`.
- `product.category_id -> category.id`.
- `product_variant.size_id -> size.id`.
- `product_variant.color_id -> color.id`.
- Đồng thời `product_attribute_value.product_id -> product.id` và `product_attribute_value.attribute_id -> product_attribute_definition.id` cũng đã tồn tại.

Bốn sản phẩm demo đều vừa giữ FK cứng `brand/origin/material/sole_type`, vừa có 5 dòng thông số động tương ứng. Ví dụ `Nike Air Demo` giữ `brand_id = Nike`, đồng thời có `ATTR-BRAND = Nike`; giữ `sole_type_id = Đế cao su`, đồng thời có `ATTR-SOLE = Đế cao su`. Đây là dữ liệu trùng nghĩa ở hai mô hình khác nhau.

Sáu variant thật đều dùng `size_id` và `color_id`; chưa có bảng trục phân loại, giá trị trục, mapping tổ hợp hay cờ `is_default`.

## 3. Hiện trạng schema và entity

### 3.1. Phần hard-code phải bỏ

| Bảng/entity | Chỗ bị hard-code | Hệ quả |
|---|---|---|
| `Brand`, `Origin`, `Material`, `SoleType` | `Product` có bốn quan hệ `@ManyToOne` riêng | Sản phẩm điện tử/mỹ phẩm vẫn bị ép mang khái niệm của giày; cùng dữ liệu lại được lưu ở thông số động. |
| `Color`, `Size` | `ProductVariant` có hai quan hệ cố định `color` và `size` | Chỉ sinh được variant Màu × Size; không biểu diễn Dung lượng × Phiên bản hoặc Mùi hương × Dung tích. |
| `CatalogAttribute`/`AttributeDefinitions` | Một CRUD generic nhưng đăng ký cứng bảy route `mau-sac`, `size`, `thuong-hieu`, `xuat-xu`, `chat-lieu`, `danh-muc`, `loai-de` | Cơ chế CRUD trông generic nhưng danh sách domain vẫn đóng trong code. |
| `ProductRequest` | Có `idBrand`, `idXuatXu`, `idSoleType`, `idMaterial` | Contract tạo/sửa sản phẩm vẫn yêu cầu field theo ngành giày. |
| `ProductDetailRequest` | Có thêm `idMau`, `idSize` | Contract variant chỉ hiểu hai trục cố định. |

### 3.2. Phần động đã có nhưng chưa đúng prompt mới

Điểm có thể tái sử dụng:

- `product_attribute_definition`, `product_attribute_option`, `category_attribute_suggestion` và `product_attribute_value` đã tách khỏi `product`.
- Có kiểu `TEXT`, `NUMBER`, `SINGLE_SELECT`, `MULTI_SELECT` và có hậu kiểm `PENDING/STANDARDIZED/MERGED/HIDDEN`.
- Seller lấy quyền từ `X-Seller-Id`; thuộc tính `PENDING` chỉ hiện với shop tạo trong source hiện tại.
- Buyer đã có nested attributes trong Elasticsearch và filter theo category.
- Admin đã có standardize/merge/hide và audit.

Khoảng cách so với model đích:

- Chưa tách “thông số mô tả” khỏi “trục sinh variant”. Toàn bộ phần động hiện tại chỉ là thông số mô tả; variant vẫn cứng Màu/Size.
- `product_attribute_value` hiện dùng `text_value`, `number_value` và bảng nối `product_attribute_value_option`; model đích yêu cầu một bảng value duy nhất với `value_text`, `value_number`, `attribute_option_id`.
- Tên enum hiện là `SINGLE_SELECT/MULTI_SELECT`; model đích chốt `SELECT_ONE/SELECT_MULTI`. Đây là breaking schema/API change cần làm đồng bộ.
- Definition dùng `normalization_status` và chuỗi `merged_into_attribute_id`; model đích cần `is_verified` và FK tự tham chiếu `merged_into_definition_id`.
- Option chưa có `is_verified`; giá trị mới chỉ được tạo khi definition `PENDING` thuộc chính seller, trong khi quyết định mới cho seller thêm option ngay và dùng chung để gợi ý.
- Source đang giới hạn 50 thông số mô tả/sản phẩm. Prompt sản phẩm mới chốt thông số mô tả không giới hạn; giới hạn hai chỉ áp dụng cho trục phân loại. Quy tắc 50 phải được gỡ trong đợt chuẩn hóa này.
- `Category` hiện chỉ có `id/code/name/status`, không có `parent_id`; DB thật là danh sách phẳng, chưa phải cây danh mục như nghiệp vụ yêu cầu.

## 4. Hiện trạng service/controller catalog

### 4.1. Endpoint hard-code

`AdminAttributeController` công khai CRUD riêng cho:

- `/api/v1/admin/mau-sac`
- `/api/v1/admin/size`
- `/api/v1/admin/thuong-hieu`
- `/api/v1/admin/xuat-xu`
- `/api/v1/admin/chat-lieu`
- `/api/v1/admin/loai-de`

`PublicProductController` và `SellerProductController` tiếp tục có các endpoint `list-thuong-hieu`, `list-xuat-xu`, `list-size`, `list-mau`, `list-loai-de`, `list-chat-lieu`.

`SellerProductVariantController` tách CRUD product và variant thành hai modal/luồng độc lập, đọc option từ `/list-mau` và `/list-size`. Điều này không thể tự sinh ma trận tối đa hai trục tùy tên.

### 4.2. Query và response hard-code

- `ProductRepository.getAllProductByFilter` JOIN trực tiếp `Brand`, `Origin`, `SoleType`, `Material`; projection trả `tenBrand`, `tenXuatXu`, `tenSoleType`, `tenMaterial`.
- `ProductVariantRepository` JOIN `Size` và `Color`, filter bằng `idKT/idMS`, kiểm tra trùng bằng tổ hợp `idMau + idSize + idProduct`.
- `ProductServiceImpl.applyRequest` nạp năm repository cứng vào `Product`.
- `ProductServiceImpl.filteredPublicProductRows` vẫn filter `brandIds/materialIds/soleTypeIds`, trả `brand/thuongHieu`, `material/chatLieu`, `xuatXu`, đồng thời mới trả thêm `attributes`.
- `ProductDetailServiceImpl.getPublicDetail` trả hai lớp alias cũ (`brand/thuongHieu`, `material/chatLieu`, `soleType/loaiDe`) và variant `mauSac/kichCo`.
- `InternalCatalogController.searchProductDetails` nhận các filter `idColor`, `idKichThuoc`, `idMaterial`, `idBrand`, `idSoleType`; response nội bộ chứa `tenBrand`, `tenMaterial`, `tenSoleType`, `tenMau`, `kichThuoc`.

### 4.3. Lỗi/độ lệch kỹ thuật cần xử lý trong implementation

- `Product.description` map `varchar(255)` trong DB thật trong khi seed tạo `text`; schema đang lệch giữa Hibernate update và seed.
- `sale_price` dùng `double`; tiền nên chuyển sang `decimal`/`BigDecimal` trong model chuẩn hóa để tránh sai số.
- `seller_id` lặp ở cả `product` và `product_variant`; có nguy cơ lệch chủ sở hữu. Model đích chỉ lấy seller từ product khi đọc variant.
- Ảnh upload trong `ProductDetailServiceImpl.saveImageIfPresent` chỉ đọc bytes, không lưu URL/file; `image_url` không được cập nhật.
- `Product`/`ProductVariant` sinh code bằng `Random(0..9999)`, không có unique constraint và có khả năng trùng.
- `SecurityConfig` của service permit all; quyền hiện phụ thuộc gateway. Các internal route cần xác định rõ chỉ được gọi nội bộ khi triển khai.
- `searchProductIds` trả `null` khi Elasticsearch trả zero hit, sau đó fallback MySQL có thể làm xuất hiện kết quả không khớp keyword/filter. Cần phân biệt “index unavailable” với “query hợp lệ nhưng zero hit”.

## 5. Hiện trạng FE

### 5.1. Seller

Phần thông số mô tả đã có UI động và autocomplete, nhưng vẫn có giới hạn 50 và chỉ cho option mới khi tạo definition mới.

Phần “Phân loại” vẫn là form một dòng:

- Chọn sản phẩm.
- Chọn Màu từ bảng `color`.
- Chọn Kích cỡ từ bảng `size`.
- Nhập tồn kho, giá và ảnh.

Không có UI đặt tên trục, nhập danh sách giá trị, sinh tích Descartes, xóa tổ hợp hay tạo default variant cho sản phẩm không phân loại.

### 5.2. Platform Admin

Màn hiện tại chỉ có một bảng thông số mô tả. Chưa có hai tab. Chưa có view tổng hợp tên trục phân loại và giá trị seller đã dùng. Các menu CRUD `Thương hiệu/Chất liệu/Loại đế/Size/Màu` cũ vẫn tồn tại ở router/sidebar/source.

### 5.3. Buyer

- `FilterBox.vue` đã tải filter động theo category, nhưng chỉ từ `category_attribute_suggestion.filterable`; chưa đưa tên trục phân loại thực tế vào filter.
- `ProductsView.vue` vẫn có type/alias cứng `thuongHieu`, `chatLieu`, `kichCo`, `mauSac`.
- `ProductDetail.vue` render đầy đủ `attributes`, nhưng chọn variant được viết cứng bằng `uniqueColors`, `filteredSizes`, `colorSelected`, `sizeSelected` và query param `colorId/sizeId`.
- Payload local cart/checkout vẫn snapshot `mauSac` và `kichCo`, chưa có `variantLabel`/`selections` tổng quát.

## 6. Ảnh hưởng tới service khác và search

| Consumer | Phụ thuộc hiện tại | Mức ảnh hưởng |
|---|---|---|
| `cart-service` | Gọi `/internal/catalog/product-details/{id}`; cần `id`, `name`, `imageUrl`, `salePrice`, `quantity`, `sellerId`; FE cart còn đọc alias màu/size | Giữ nguyên nguyên tắc cart luôn dùng variant ID, đổi phần mô tả phân loại sang danh sách selection/label tổng quát. |
| `order-service` | Checkout/stock dùng variant ID và `quantity`; lịch sử/thống kê đọc `tenBrand`, `tenMau`, `kichThuoc`; một search endpoint còn filter idColor/idSize/idMaterial/idBrand/idSoleType | Stock flow tương thích với quyết định “mọi product có variant”, nhưng contract hiển thị và search phải đổi đồng bộ. Order snapshot phải lưu label phân loại tại thời điểm mua. |
| `promotion-service` | Chọn variant qua product; còn có `/colors`, `/sizes` và FE campaign filter theo màu/size | Promotion vẫn gắn `product_variant_id`; bỏ endpoint màu/size cứng, nếu cần filter thì dùng trục/giá trị động. |
| Outbox/Debezium/Elasticsearch | Payload và mapping còn field `brandId`, `brand`; mới có `attributes` nested, chưa có `variantAxes`/variant selections | Đổi mapping và payload trong cùng đợt breaking change; reindex toàn bộ seed mới. |

## 7. Kết luận audit

Không thể hoàn thiện bằng cách giữ sáu bảng cũ rồi thêm `product_variant_axis`. Phải thực hiện một cutover đồng bộ:

1. Thiết kế schema đích và contract đích.
2. Reset dữ liệu demo catalog, bỏ FK và source cứng.
3. Tạo trục/value/mapping và default variant.
4. Cập nhật catalog API cùng consumer cart/order/promotion và Elasticsearch.
5. Cập nhật FE Seller, Admin, Buyer.
6. Chỉ xóa sáu bảng cũ sau khi source scan không còn tham chiếu và smoke test contract mới pass.

Prompt sản phẩm cho phép xóa data demo, nhưng thao tác reset database local vẫn là hành động phá hủy. Tài liệu/DDL có thể chuẩn bị trước; việc chạy DROP/reset trên DB local phải được thực hiện ở task migration với mục tiêu và bằng chứng rõ ràng.
