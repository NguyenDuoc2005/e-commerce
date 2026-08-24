# Thay đổi contract API domain Sản phẩm

## 1. Chiến lược cutover

Áp dụng quyết định Mục 3.5.e: đổi contract trực tiếp trong `/api/v1`, không mở `/v2` và không duy trì hai response cũ/mới song song.

Một pull request/task có thể chia commit nhỏ, nhưng trạng thái deploy chỉ hợp lệ khi các phần sau cùng tương thích:

- `catalog-service` và schema mới.
- `cart-service`, `order-service`, `promotion-service`.
- Outbox, Debezium sink, Elasticsearch mapping/index mới.
- FE Seller, Platform Admin và Buyer.

Trong thời gian code chưa hoàn tất, không chạy stack hỗn hợp lên dữ liệu shared. Cutover dev thực hiện reset/seed một lần sau khi source compile đồng bộ.

## 2. Endpoint cũ phụ thuộc bảng cứng

### 2.1. Admin attribute CRUD phải xóa

- `/api/v1/admin/mau-sac/**`
- `/api/v1/admin/size/**`
- `/api/v1/admin/thuong-hieu/**`
- `/api/v1/admin/xuat-xu/**`
- `/api/v1/admin/chat-lieu/**`
- `/api/v1/admin/loai-de/**`

`/api/v1/admin/danh-muc/**` được giữ về nghiệp vụ nhưng tách khỏi `AdminAttributeController`, chuyển thành Category API có parent/slug/tree.

### 2.2. Public/Seller option endpoint phải xóa

- `/api/v1/permitall/san-pham/list-thuong-hieu`
- `/api/v1/permitall/san-pham/list-xuat-xu`
- `/api/v1/permitall/san-pham/list-size`
- `/api/v1/permitall/san-pham/list-mau`
- `/api/v1/permitall/san-pham/list-loai-de`
- `/api/v1/permitall/san-pham/list-chat-lieu`
- Các route tương ứng dưới `/api/v1/seller/products`.
- `/api/v1/seller/product-variants/list-mau`
- `/api/v1/seller/product-variants/list-size`

### 2.3. Endpoint trả field cứng phải đổi response

- `GET /api/v1/admin/san-pham`
- `GET /api/v1/seller/products`
- `GET /api/v1/seller/products/{id}`
- `POST /api/v1/seller/products`
- Toàn bộ `/api/v1/seller/product-variants/**` hiện tại.
- `GET /api/v1/permitall/san-pham/get-all/danh-sach-san-pham`
- `GET /api/v1/permitall/san-pham/get-all/san-pham-moi`
- `GET /api/v1/permitall/san-pham/get-all/san-pham-giam-gia`
- `GET /api/v1/permitall/san-pham-chi-tiet/get-all/san-pham-chi-tiet`
- Các endpoint `/internal/catalog/**` trả product/variant map.

Field/alias phải bỏ: `idBrand`, `tenBrand`, `brandId`, `brand`, `thuongHieu`, `idXuatXu`, `xuatXu`, `idMaterial`, `tenMaterial`, `chatLieu`, `idSoleType`, `tenSoleType`, `loaiDe`, `idMau/idMS/idColor`, `tenMau/mauSac`, `idSize/idKT`, `kichCo`.

## 3. Canonical model dùng chung

### 3.1. Product detail response

```json
{
  "id": "PROD-PHONE-001",
  "code": "PHONE-X1",
  "sellerId": "SELLER-002",
  "name": "Acme X1 5G",
  "description": "...",
  "status": "ACTIVE",
  "category": {
    "id": "CAT-PHONES",
    "name": "Điện thoại",
    "slug": "dien-thoai",
    "path": [
      { "id": "CAT-ELECTRONICS", "name": "Điện tử" },
      { "id": "CAT-PHONES", "name": "Điện thoại" }
    ]
  },
  "attributes": [
    {
      "definitionId": "ATTR-BATTERY",
      "name": "Dung lượng pin",
      "dataType": "NUMBER",
      "valueText": null,
      "valueNumber": 5000,
      "unit": "mAh",
      "selectedOptions": [],
      "displayOrder": 1
    }
  ],
  "variantAxes": [
    {
      "id": "AXIS-CAPACITY",
      "name": "Dung lượng",
      "displayOrder": 1,
      "values": [
        { "id": "CAP-128", "value": "128 GB", "displayOrder": 1 },
        { "id": "CAP-256", "value": "256 GB", "displayOrder": 2 }
      ]
    }
  ],
  "variants": [
    {
      "id": "VAR-PHONE-128-BLACK",
      "sku": "X1-128-BLK",
      "salePrice": 8990000,
      "quantity": 10,
      "imageUrl": "https://...",
      "isDefault": false,
      "status": "ACTIVE",
      "selections": [
        { "axisId": "AXIS-CAPACITY", "axisName": "Dung lượng", "valueId": "CAP-128", "value": "128 GB" }
      ]
    }
  ]
}
```

`selectedOptions` luôn là mảng để cùng biểu diễn `SELECT_ONE` và `SELECT_MULTI`; `SELECT_ONE` có đúng một phần tử.

### 3.2. Product summary response

Danh sách/search không trả toàn bộ ma trận nếu không cần. Summary tối thiểu:

```json
{
  "id": "PROD-PHONE-001",
  "sellerId": "SELLER-002",
  "sellerName": "Acme Store",
  "sellerSlug": "acme-store",
  "name": "Acme X1 5G",
  "category": { "id": "CAT-PHONES", "name": "Điện thoại" },
  "minPrice": 8990000,
  "maxPrice": 10990000,
  "thumbnailUrl": "https://...",
  "ratingAverage": 4.8,
  "ratingCount": 20,
  "soldCount": 100,
  "attributePreview": [
    { "definitionId": "ATTR-CHIP", "name": "Chip xử lý", "displayValue": "Snapdragon X" }
  ],
  "axisPreview": [
    { "axisName": "Dung lượng", "values": ["128 GB", "256 GB"] }
  ]
}
```

## 4. Seller API đích

### 4.1. Category và autocomplete

- `GET /api/v1/seller/products/categories/tree`
- `GET /api/v1/seller/products/categories/{categoryId}/attribute-suggestions?q=`
- `GET /api/v1/seller/products/variant-axis-name-suggestions?q=`

Attribute suggestion trả definition, option, `required`, `filterable`, `displayOrder`, `isVerified`; không lọc theo owner shop như model tạm. Definition/option chưa verified được dùng chung để autocomplete theo quyết định mới.

### 4.2. Tạo/sửa sản phẩm atomically

- `POST /api/v1/seller/products`
- `PUT /api/v1/seller/products/{productId}`
- `GET /api/v1/seller/products/{productId}`
- `GET /api/v1/seller/products`
- `PUT /api/v1/seller/products/{productId}/status`

Không còn CRUD variant rời `/api/v1/seller/product-variants` làm luồng chính. Create/update nhận một aggregate hoàn chỉnh để validate ma trận trong một transaction.

Request mẫu:

```json
{
  "categoryId": "CAT-PHONES",
  "name": "Acme X1 5G",
  "description": "...",
  "attributes": [
    {
      "definitionId": "ATTR-BATTERY",
      "dataType": "NUMBER",
      "valueNumber": 5000,
      "unit": "mAh",
      "displayOrder": 1
    },
    {
      "name": "Chuẩn kháng nước",
      "dataType": "SELECT_ONE",
      "newOptions": ["IP68"],
      "selectedOptionValues": ["IP68"],
      "displayOrder": 2
    }
  ],
  "variantAxes": [
    {
      "clientKey": "capacity",
      "name": "Dung lượng",
      "displayOrder": 1,
      "values": [
        { "clientKey": "128", "value": "128 GB", "displayOrder": 1 },
        { "clientKey": "256", "value": "256 GB", "displayOrder": 2 }
      ]
    }
  ],
  "variants": [
    {
      "sku": "X1-128",
      "salePrice": 8990000,
      "quantity": 10,
      "imageUrl": "https://...",
      "isDefault": false,
      "selectionValueKeys": ["128"]
    },
    {
      "sku": "X1-256",
      "salePrice": 9990000,
      "quantity": 8,
      "imageUrl": "https://...",
      "isDefault": false,
      "selectionValueKeys": ["256"]
    }
  ]
}
```

Request không có `sellerId`. Gateway/header là nguồn quyền duy nhất.

Sản phẩm không trục gửi `variantAxes=[]` và đúng một variant `isDefault=true`, `selectionValueKeys=[]`.

Ảnh trong contract aggregate dùng `imageUrl`. Upload binary, nếu cần, là endpoint riêng trả URL; không tiếp tục nhận `MultipartFile` nhưng bỏ qua việc lưu như source hiện tại.

## 5. Platform Admin API đích

### 5.1. Thông số mô tả

Giữ prefix `/api/v1/admin/product-attributes` nhưng đổi model:

- `GET /api/v1/admin/product-attributes?q=&verified=&status=&categoryId=&creatorSellerId=`
- `PUT /{id}/verify`
- `PUT /{id}/standardize`
- `POST /{id}/merge`
- `PUT /{id}/hide`
- `GET /{id}/options`
- `PUT /options/{optionId}/verify`
- `POST /options/{optionId}/merge`
- `POST /reindex`

Merge chỉ set self-reference trong transaction; response luôn có `resolvedDefinitionId`/`resolvedOptionId`.

### 5.2. Tab phân loại hàng

- `GET /api/v1/admin/product-variant-axes/insights?q=&verified=&status=`: aggregate tên trục, biến thể spelling, usage count và top values.
- `POST /api/v1/admin/product-variant-axes/suggestions`: tạo gợi ý chuẩn.
- `PUT /api/v1/admin/product-variant-axes/suggestions/{id}/verify`.
- `POST /api/v1/admin/product-variant-axes/suggestions/{id}/merge`.
- `PUT /api/v1/admin/product-variant-axes/suggestions/{id}/hide`.

Không cho Admin rewrite hàng loạt axis/product trong request đồng bộ.

### 5.3. Category

- `GET /api/v1/admin/categories/tree`
- `POST /api/v1/admin/categories`
- `PUT /api/v1/admin/categories/{id}`
- `PUT /api/v1/admin/categories/{id}/status`
- `PUT /api/v1/admin/categories/{id}/attribute-suggestions`

## 6. Public Buyer API đích

- `GET /api/v1/permitall/products`: list/search/filter canonical.
- `GET /api/v1/permitall/products/{productId}`: full detail canonical.
- `GET /api/v1/permitall/categories/tree`.
- `GET /api/v1/permitall/categories/{categoryId}/filters`.

Filter response:

```json
{
  "descriptiveAttributes": [
    {
      "definitionId": "ATTR-BATTERY",
      "name": "Dung lượng pin",
      "dataType": "NUMBER",
      "min": 4000,
      "max": 6000,
      "unit": "mAh",
      "options": []
    }
  ],
  "variantAxes": [
    {
      "normalizedName": "dung luong",
      "displayName": "Dung lượng",
      "values": [
        { "normalizedValue": "128 gb", "displayValue": "128 GB", "productCount": 3 }
      ]
    }
  ]
}
```

Request list dùng hai object JSON hoặc query convention rõ ràng:

- `attributeFilters`: key definition ID, value option/value/range.
- `variantFilters`: key normalized axis name/suggestion ID, value normalized axis values.

Backend/Elasticsearch phải đảm bảo các variant filter khác nhau khớp trên cùng một nested variant.

## 7. Internal contract và consumer

### 7.1. Canonical variant snapshot

`GET /internal/catalog/product-variants/{variantId}`:

```json
{
  "id": "VAR-PHONE-128-BLACK",
  "productId": "PROD-PHONE-001",
  "sellerId": "SELLER-002",
  "sku": "X1-128-BLK",
  "productName": "Acme X1 5G",
  "variantLabel": "Dung lượng: 128 GB · Màu: Đen",
  "selections": [
    { "axisName": "Dung lượng", "value": "128 GB" },
    { "axisName": "Màu", "value": "Đen" }
  ],
  "salePrice": 8990000,
  "quantity": 10,
  "imageUrl": "https://...",
  "status": "ACTIVE"
}
```

Các endpoint cần có:

- `GET /internal/catalog/products`
- `GET /internal/catalog/products/{productId}/variants`
- `GET /internal/catalog/product-variants?ids=...`
- `GET /internal/catalog/product-variants/{variantId}`
- `POST /internal/catalog/product-variants/{variantId}/stock/adjust?delta=`

Xóa `/internal/catalog/colors`, `/sizes` và filter search cứng `idColor/idKichThuoc/idMaterial/idBrand/idSoleType`.

### 7.2. `cart-service`

Hiện chỉ gọi detail theo variant ID. Cập nhật:

- DTO typed thay `Map<String,Object>`.
- Server tự lấy `salePrice`, `quantity`, `sellerId`; không tin `price` client gửi.
- Response cart trả `variantLabel` và `selections`, FE không dò nhiều alias màu/size.

### 7.3. `order-service`

Giữ stock adjust theo variant ID. Cập nhật:

- Checkout typed DTO canonical.
- `order_item` snapshot `product_name`, `sku`, `variant_label`, `variant_selections_json`, `image_url`, `sale_price`; lịch sử không phụ thuộc catalog mutable.
- Thống kê đọc `productName/sku/imageUrl`, không `tenBrand/tenMau/kichThuoc`.
- Xóa hoặc thay search admin cũ đang nhận idColor/idSize/idMaterial/idBrand/idSoleType.

### 7.4. `promotion-service`

Promotion vẫn gắn variant ID.

- Bỏ Feign `/colors` và `/sizes`.
- UI chọn campaign tải product rồi variants canonical; filter tùy chọn dựa `variantAxes`, không bảng màu/size.
- Validation seller ownership dùng `sellerId` trong canonical variant snapshot.

## 8. Outbox và Elasticsearch

### 8.1. Product document mới

Xóa top-level `brandId`, `brand`. Document gồm:

- Product/seller/category summary.
- `attributes` nested: definition ID chuẩn đã resolve, normalized values, number/unit, option IDs/normalized values.
- `variants` nested: variant ID, price, stock/status và toàn bộ selection của cùng variant.
- `variantAxes` dùng cho render/facet metadata nếu cần, không thay thế `variants` khi filter tổ hợp.

### 8.2. Quy tắc đồng bộ

- Catalog transaction chỉ ghi outbox.
- Mọi create/update/status/attribute merge/axis change đều enqueue product document mới.
- Schema cutover tạo concrete index version mới, mapping `dynamic: strict`, bulk reindex seed mới, verify count rồi switch alias `products`.
- Zero hit là kết quả rỗng hợp lệ; chỉ fallback MySQL khi Elasticsearch thật sự unavailable. Không fallback khi query ES thành công nhưng không có hit.

## 9. Thứ tự triển khai contract

1. Tạo DTO/schema target và test contract catalog.
2. Cập nhật internal canonical variant endpoint.
3. Cập nhật cart/order/promotion compile với DTO mới.
4. Cập nhật outbox document + mapping + reindex script.
5. Cập nhật Seller aggregate API.
6. Cập nhật Admin API.
7. Cập nhật Public Buyer API.
8. Cập nhật toàn bộ FE và xóa alias cũ.
9. Scan source không còn six-table token/endpoint; compile/build/test; mới cutover DB/seed/runtime.
