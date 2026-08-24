# Nghiệp vụ thuộc tính và phân loại sản phẩm

## 1. Nguyên tắc domain

Domain sản phẩm có hai khái niệm tách biệt:

| Khái niệm | Mục đích | Có sinh SKU/variant | Phạm vi |
|---|---|---:|---|
| Thông số mô tả | Mô tả đặc tính dùng chung của sản phẩm | Không | Gắn với `product` |
| Trục phân loại hàng | Xác định các lựa chọn làm thay đổi SKU, giá, tồn kho hoặc ảnh | Có | Gắn với `product_variant` qua tổ hợp giá trị trục |

Không dùng một cờ trên cùng bảng để nhập nhằng hai loại. Một tên có thể xuất hiện ở cả hai cơ chế tùy bản chất hàng hóa. Ví dụ “Màu sắc” thường là trục nếu buyer phải chọn màu để lấy đúng SKU, nhưng có thể chỉ là thông số mô tả đối với một món đồ độc bản không có lựa chọn màu.

Mọi nghiệp vụ giá, tồn kho, giỏ hàng, khuyến mại và đơn hàng luôn tham chiếu `product_variant.id`.

- Sản phẩm có phân loại: có nhiều variant theo tổ hợp tối đa hai trục.
- Sản phẩm không có phân loại: vẫn có đúng một variant ẩn `is_default = true`.
- Không có nhánh nghiệp vụ nào thao tác giá/tồn trực tiếp trên `product`.

## 2. Thông số mô tả

### 2.1. Kiểu dữ liệu

- `TEXT`: chuỗi tự do, ví dụ “Da tổng hợp”, “Snapdragon 8 Gen 3”.
- `NUMBER`: số và đơn vị, ví dụ `5000 mAh`, `30 ml`, `0.8 kg`. Definition có `default_unit` nullable làm đơn vị chuẩn dùng chung; value chỉ lưu `unit` khi sản phẩm cần override bằng đơn vị khác mặc định.
- `SELECT_ONE`: chọn đúng một option, ví dụ “Xuất xứ = Việt Nam”.
- `SELECT_MULTI`: chọn nhiều option, ví dụ “Loại da phù hợp = Da dầu, Da hỗn hợp”.

Thông số mô tả không giới hạn số lượng theo nghiệp vụ mới. Backend vẫn phải áp dụng giới hạn an toàn cho kích thước request/body và độ dài tên/giá trị, nhưng không dùng lại quy tắc “tối đa 50 thông số” của model tạm trước đây.

### 2.2. Gợi ý theo danh mục

Platform Admin gắn definition vào một hoặc nhiều category với:

- `required`: seller bắt buộc nhập hay không.
- `filterable`: buyer có được lọc theo thông số này hay không.
- `display_order`: thứ tự form Seller và trang chi tiết Buyer.

Gợi ý không phải danh sách đóng. Seller vẫn có thể thêm thông số mới ngay khi tạo/sửa sản phẩm.

### 2.3. Seller tự thêm definition và option

Khi seller gõ tên thông số:

1. FE gọi autocomplete theo category và chuỗi đã chuẩn hóa không dấu/chữ thường.
2. Kết quả ưu tiên definition đã verified, sau đó là definition chưa verified đang được dùng trong category.
3. Seller chọn definition có sẵn hoặc tiếp tục tạo mới.
4. Definition mới được dùng ngay, `is_verified = false`; không chờ Admin duyệt.
5. Với `SELECT_ONE/SELECT_MULTI`, seller được thêm option mới ngay. Option mới `is_verified = false` và xuất hiện trong autocomplete chung của definition để tránh nhập lặp.

Tên/option được so trùng bằng `normalized_name`/`normalized_value`, nhưng vẫn giữ nguyên nhãn hiển thị seller nhập.

## 3. Trục phân loại hàng

### 3.1. Quy tắc

- Mỗi sản phẩm có từ 0 đến 2 trục.
- Seller tự đặt tên trục; không hard-code Màu/Size.
- Mỗi trục có ít nhất một giá trị, các giá trị không trùng nhau sau normalize.
- `display_order` chỉ nhận 1 hoặc 2 và không trùng trong cùng sản phẩm.
- Mỗi variant không mặc định phải có đúng một giá trị ở mỗi trục của sản phẩm.
- Không cho hai variant có cùng tổ hợp giá trị trục.
- Mỗi variant bắt buộc có `combination_key`; key `NOT NULL` và unique theo `(product_id, combination_key)`. Product không trục dùng `DEFAULT`, product có trục tạo key canonical theo thứ tự trục và axis value ID.
- Seller có thể bỏ một số tổ hợp không bán trước khi lưu.

### 3.2. Sinh ma trận variant

Ví dụ giày có:

- Trục 1 “Màu”: Trắng, Đen, Đỏ.
- Trục 2 “Size”: 39, 40, 41, 42.

Hệ thống sinh 12 tổ hợp. Seller có thể bỏ “Đỏ / 42”, còn 11 variant. Mỗi dòng có SKU, giá, tồn kho, ảnh và trạng thái riêng.

Ảnh tổng thể/gallery được lưu ở `product_image` (`product_id`, `url`, `display_order`, `status`). `product_variant.image_url` chỉ là ảnh đại diện riêng của tổ hợp; hai nguồn ảnh không thay thế lẫn nhau.

Nếu chỉ có một trục “Dung lượng” gồm 128 GB, 256 GB, 512 GB thì sinh ba variant, mỗi variant mapping đúng một axis value.

Nếu không có trục, backend tự tạo một variant mặc định, không yêu cầu seller nhìn thấy/đặt tên phân loại.

### 3.3. Sửa trục và giá trị

- Đổi nhãn trục/giá trị không làm đổi variant ID nếu quan hệ logic không đổi.
- Thêm giá trị trục: sinh các tổ hợp mới còn thiếu, seller phải nhập giá/tồn trước khi activate.
- Xóa giá trị trục đang được variant/order tham chiếu: không hard-delete; chuyển inactive và ẩn khỏi lựa chọn mới. Variant lịch sử vẫn đọc được nhãn snapshot.
- Đổi từ hai trục xuống một trục hoặc về không trục là thao tác phá vỡ tổ hợp; FE phải hiển thị diff variant sẽ mất/tạo và yêu cầu xác nhận.
- SKU active phải unique toàn catalog hoặc tối thiểu unique theo seller; roadmap chọn unique toàn catalog để Feign/stock lookup đơn giản.

## 4. Luồng Seller tạo sản phẩm

1. Gateway xác thực JWT và truyền `X-Seller-Id`; request body không có `sellerId`.
2. Seller chọn category lá trong cây danh mục.
3. FE tải gợi ý thông số mô tả của category.
4. Seller nhập các thông số cần thiết, có thể thêm definition/option tự do. Với NUMBER, FE dùng `default_unit` của definition và chỉ gửi/lưu `unit` khi seller chọn đơn vị override khác mặc định.
5. Seller chọn “Có phân loại” hoặc “Không phân loại”.
6. Nếu có phân loại:
   - Nhập tối đa hai tên trục.
   - Nhập danh sách giá trị mỗi trục.
   - FE sinh ma trận, seller bỏ tổ hợp không bán và nhập SKU/giá/tồn/ảnh.
7. Nếu không phân loại:
   - FE chỉ hiển thị khối Giá, Tồn kho, SKU và ảnh riêng của variant; gallery tổng thể vẫn là khối ảnh sản phẩm độc lập.
   - Backend lưu thành một `product_variant.is_default = true`.
8. Backend validate toàn bộ request trong một transaction: category, ảnh tổng thể, definition, option, axis, tổ hợp, SKU, giá và tồn.
9. Backend lưu product + `product_image` + thông số + axes + variants atomically.
10. Sau commit, ghi một outbox event chứa document search hoàn chỉnh; không dual-write trực tiếp Elasticsearch.

Khi đổi category sau khi đã nhập thông số, FE phải cảnh báo. Seller xác nhận thì:

- Giữ các thông số custom hoặc definition vẫn hợp lệ.
- Đánh dấu rõ các gợi ý cũ không còn thuộc category mới để seller quyết định giữ/xóa.
- Không xóa trục phân loại vì trục do seller tự định nghĩa, không phụ thuộc category.

## 5. Luồng Platform Admin

### 5.1. Tab “Thông số mô tả”

Admin có thể:

- Tìm theo tên/code/status/category/seller tạo.
- Xem số sản phẩm và category đang dùng.
- Verify definition/option.
- Chuẩn hóa tên, cấu hình required/filterable/display order theo category.
- Gộp definition bằng `merged_into_definition_id` đúng một tầng: source trỏ trực tiếp đến definition chuẩn và target bắt buộc có `merged_into_definition_id = NULL`.
- Gộp option bằng `merged_into_option_id` theo cùng quy tắc một tầng: target bắt buộc có `merged_into_option_id = NULL`.
- Service từ chối chọn một row đã gộp làm target; tầng đọc chỉ áp dụng trực tiếp một mapping source → target, không đi qua chuỗi trung gian.
- Ẩn rác/vi phạm khỏi gợi ý và filter công khai; dữ liệu lịch sử không bị mất.

Hậu kiểm không chặn luồng đăng bán. Mọi thao tác phải ghi moderation audit với actor, lý do, source/target và số product ảnh hưởng, sau đó enqueue reindex các product liên quan.

### 5.2. Tab “Phân loại hàng”

Admin xem thống kê tên trục và giá trị seller đang dùng, ví dụ `Màu`, `Màu sắc`, `Color`, `Mau`. Admin có thể:

- Tạo nhãn gợi ý chuẩn “Màu sắc”.
- Gộp các nhãn gợi ý trùng nghĩa.
- Verify gợi ý phổ biến.
- Ẩn nhãn vi phạm.

Đây chỉ là từ điển autocomplete. Không đổi tên cưỡng bức trục đang tồn tại và không chặn seller nhập tên khác.

## 6. Luồng Buyer

### 6.1. Danh sách và filter

Khi buyer chọn category:

1. Backend xác định tập product active thực sự trong category (bao gồm descendant nếu category là node cha).
2. Chỉ trả filter có ít nhất một product/variant active sử dụng.
3. Filter gồm:
   - Thông số mô tả được Admin đánh dấu `filterable`.
   - Tên trục phân loại và giá trị thực tế đang xuất hiện.
4. Một product khớp khi thỏa tất cả nhóm filter; trong cùng một nhóm nhiều giá trị là OR.
5. Với variant-axis filter, kết quả product chỉ cần có ít nhất một variant active thỏa toàn bộ lựa chọn trục và khoảng giá. Không ghép Màu của variant A với Size của variant B.

### 6.2. Chi tiết và chọn variant

Response trả `variantAxes` theo `displayOrder`, mỗi axis có danh sách value. FE render động 0/1/2 khối chọn:

Response cũng trả `productImages` theo `displayOrder` làm gallery tổng thể, độc lập với `imageUrl` của variant.

- Không trục: chọn sẵn default variant, không hiện khối phân loại.
- Một trục: chọn một value rồi resolve variant.
- Hai trục: chọn lần lượt; disable tổ hợp không tồn tại/hết hàng; resolve đúng variant theo mapping.

Khi variant đổi, FE cập nhật giá, ảnh, tồn và promotion của chính variant đó.

Giỏ hàng chỉ gửi `variantId` và quantity. Server lấy giá/tồn/seller từ catalog, không tin snapshot giá từ client.

## 7. Ví dụ ba ngành hàng

### 7.1. Giày

- Category: Giày > Giày chạy bộ.
- Thông số: Thương hiệu=`Nike`, Chất liệu=`Vải mesh`, Xuất xứ=`Việt Nam`, Loại đế=`Đế foam`, Độ drop=`8 mm`.
- Trục: Màu × Size.
- `Loại đế` chỉ là thông số, không sinh SKU.

### 7.2. Điện tử

- Category: Điện tử > Điện thoại.
- Thông số: Thương hiệu=`Acme`, Chip=`Snapdragon X`, Dung lượng pin=`5000 mAh`, Bảo hành=`24 tháng`.
- Trục: Dung lượng (`128 GB`, `256 GB`) × Màu (`Đen`, `Xanh`).
- Không có field Size giày, Chất liệu giày hay Loại đế trong schema/product API.

### 7.3. Mỹ phẩm

- Category: Mỹ phẩm > Serum.
- Thông số: Thương hiệu=`PureLab`, Thành phần chính=`Niacinamide`, Nồng độ=`10 %`, Loại da phù hợp=`Da dầu, Da hỗn hợp`.
- Trục: Dung tích (`15 ml`, `30 ml`) hoặc không trục nếu chỉ bán một dung tích.
- Sản phẩm không trục vẫn có một default variant để cart/order dùng thống nhất.

## 8. Invariant và lỗi phải trả

- Category không tồn tại/inactive: `400 CATEGORY_INVALID`.
- Hơn hai trục: `400 VARIANT_AXIS_LIMIT_EXCEEDED`.
- Trục trùng tên hoặc value trùng sau normalize: `400 DUPLICATE_AXIS_OR_VALUE`.
- Tổ hợp variant trùng: `400 DUPLICATE_VARIANT_COMBINATION`.
- `combination_key` rỗng hoặc trùng trong cùng product: transaction bị từ chối; DB unique `(product_id, combination_key)` là lớp chống ghi đồng thời.
- Không trục nhưng có 0 hoặc hơn 1 default variant: transaction bị từ chối.
- Có trục nhưng variant thiếu mapping hoặc mapping hai value cùng axis: transaction bị từ chối.
- SKU trùng, giá âm, tồn âm: transaction bị từ chối.
- TEXT/NUMBER/SELECT_ONE có tối đa một giá trị cho mỗi `(product, definition)`. Đây là business rule bắt buộc được enforce trong transaction ở service bằng lock/serialization phù hợp, không chỉ ghi chú trong code hoặc trông chờ dữ liệu đầu vào tuần tự.
- Phải có test concurrency riêng cho invariant singleton trên: hai request đồng thời cùng ghi TEXT, NUMBER hoặc SELECT_ONE cho một `(product, definition)` không được commit thành hai giá trị logic.
- Seller sửa product không thuộc shop: `403`.
- Buyer/internal consumer yêu cầu variant inactive/không tồn tại: `404` hoặc business error rõ ràng, không trả map rỗng giả thành công.
