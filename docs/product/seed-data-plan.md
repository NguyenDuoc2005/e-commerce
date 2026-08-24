# Kế hoạch seed data domain Sản phẩm mới

## 1. Nguyên tắc

- Không chuyển đổi bốn product/sáu variant demo hiện tại.
- Không backup/rollback dữ liệu catalog demo cũ trong migration sản phẩm mới.
- Xóa sạch dữ liệu product/variant/attribute/outbox catalog cũ rồi seed lại bằng model đích.
- ID seller phải dùng seller demo đã tồn tại để các flow marketplace khác tiếp tục hoạt động.
- Seed consumer (`cart`, `order`, `promotion`) phải đổi đồng bộ sang variant ID mới; không để dangling reference.
- Mọi outbox document seed phải sinh từ cùng builder/contract với runtime, không hand-write payload khác shape.

Việc thực thi reset database local là thao tác phá hủy và nằm ở task migration, không thực hiện trong phiên tài liệu hóa này.

## 2. Cây danh mục mẫu

| Code | Tên | Parent | Loại |
|---|---|---|---|
| `CAT-FASHION` | Thời trang | null | Node cha |
| `CAT-SHOES` | Giày | `CAT-FASHION` | Node cha |
| `CAT-RUNNING-SHOES` | Giày chạy bộ | `CAT-SHOES` | Lá |
| `CAT-ELECTRONICS` | Điện tử | null | Node cha |
| `CAT-PHONES` | Điện thoại | `CAT-ELECTRONICS` | Lá |
| `CAT-BEAUTY` | Mỹ phẩm | null | Node cha |
| `CAT-SERUM` | Serum | `CAT-BEAUTY` | Lá |

Product chỉ gắn vào ba category lá.

## 3. Definition mô tả mẫu

| Code | Tên | Type | Verified | Category gợi ý | Required | Filterable |
|---|---|---|---:|---|---:|---:|
| `ATTR-BRAND` | Thương hiệu | `SELECT_ONE` | true | Cả ba category lá | true | true |
| `ATTR-ORIGIN` | Xuất xứ | `SELECT_ONE` | true | Cả ba | false | true |
| `ATTR-MATERIAL` | Chất liệu | `TEXT` | true | Giày | false | true |
| `ATTR-SOLE-TYPE` | Loại đế | `SELECT_ONE` | true | Giày | false | true |
| `ATTR-HEEL-DROP` | Độ drop | `NUMBER` | true | Giày chạy bộ | false | true |
| `ATTR-CHIP` | Chip xử lý | `TEXT` | true | Điện thoại | true | true |
| `ATTR-BATTERY` | Dung lượng pin | `NUMBER` | true | Điện thoại | true | true |
| `ATTR-WARRANTY` | Bảo hành | `NUMBER` | true | Điện thoại | false | true |
| `ATTR-ACTIVE-INGREDIENT` | Thành phần chính | `TEXT` | true | Serum | true | true |
| `ATTR-CONCENTRATION` | Nồng độ | `NUMBER` | true | Serum | false | true |
| `ATTR-SKIN-TYPE` | Loại da phù hợp | `SELECT_MULTI` | true | Serum | false | true |

Option mẫu:

- Thương hiệu: Nike, Acme Mobile, PureLab.
- Xuất xứ: Việt Nam, Hoa Kỳ, Hàn Quốc.
- Loại đế: Đế foam, Đế cao su.
- Loại da phù hợp: Da dầu, Da khô, Da hỗn hợp, Da nhạy cảm.

Thêm một case hậu kiểm:

- Definition seller tạo `Chat lieu vai`, `is_verified=false`.
- Definition chuẩn `Chất liệu`, `is_verified=true`.
- Sau khi test merge, `Chat lieu vai.merged_into_definition_id = ATTR-MATERIAL`; product vẫn đọc ra definition chuẩn mà không rewrite hàng loạt trước.

Thêm một option chưa verified do seller tạo, ví dụ “Việt Nam - nội địa”, để test autocomplete dùng ngay và Admin gộp option sau.

## 4. Sản phẩm và variant mẫu

### 4.1. Giày — hai trục

Product `PROD-SHOE-001`: “Giày chạy AeroRun Pro”

- Seller: shop demo 1.
- Category: `CAT-RUNNING-SHOES`.
- Thông số: Thương hiệu=Nike; Xuất xứ=Việt Nam; Chất liệu=Vải mesh; Loại đế=Đế foam; Độ drop=`8 mm`.
- Axis 1 `Màu sắc`: Trắng, Đen.
- Axis 2 `Kích cỡ`: 39, 40, 41.
- Sáu tổ hợp sinh ra, bỏ tổ hợp Đen/39 để kiểm tra unavailable combination.

Năm variant lưu:

| SKU | Tổ hợp | Giá | Tồn |
|---|---|---:|---:|
| `AR-PRO-W-39` | Trắng / 39 | 1.200.000 | 20 |
| `AR-PRO-W-40` | Trắng / 40 | 1.200.000 | 15 |
| `AR-PRO-W-41` | Trắng / 41 | 1.250.000 | 10 |
| `AR-PRO-B-40` | Đen / 40 | 1.250.000 | 12 |
| `AR-PRO-B-41` | Đen / 41 | 1.300.000 | 0 |

Case kiểm chứng:

- Buyer chọn Đen thì Size 39 disabled.
- Đen/41 vẫn tồn tại nhưng hết hàng.
- “Loại đế” không xuất hiện như trục.

### 4.2. Điện thoại — hai trục khác hoàn toàn giày

Product `PROD-PHONE-001`: “Acme X1 5G”

- Seller: shop demo 2.
- Category: `CAT-PHONES`.
- Thông số: Thương hiệu=Acme Mobile; Chip=Snapdragon X; Pin=`5000 mAh`; Bảo hành=`24 tháng`.
- Axis 1 `Dung lượng`: 128 GB, 256 GB.
- Axis 2 `Màu`: Đen, Xanh.
- Bốn variant, giá 8.990.000–10.990.000, tồn khác nhau.

Case kiểm chứng: source/UI không có bất kỳ điều kiện “nếu điện thoại thì dùng size/color table”; tên trục hoàn toàn do data quyết định.

### 4.3. Serum — không có trục

Product `PROD-SERUM-001`: “PureLab Niacinamide 10%”

- Seller: shop demo 2.
- Category: `CAT-SERUM`.
- Thông số: Thương hiệu=PureLab; Thành phần chính=Niacinamide; Nồng độ=`10 %`; Loại da phù hợp=Da dầu + Da hỗn hợp.
- Không có row `product_variant_axis`.
- Có đúng một variant `SERUM-NIA-30`, `is_default=true`, giá 320.000, tồn 50, không có mapping axis value.

Case kiểm chứng: FE Buyer không hiện selector phân loại nhưng add cart/order vẫn gửi variant ID.

### 4.4. Một sản phẩm một trục

Product `PROD-PHONE-002`: “Acme Charger GaN”

- Category: điện tử phù hợp (có thể thêm lá `Phụ kiện điện thoại`).
- Axis duy nhất `Công suất`: 30 W, 65 W.
- Hai variant và mỗi variant có đúng một mapping.

Case này giúp test riêng logic một trục, không chỉ 0 và 2 trục.

## 5. Tổng số tối thiểu sau seed

| Dữ liệu | Số lượng tối thiểu |
|---|---:|
| Category | 7 |
| Definition mô tả | 11 + 1 chưa verified |
| Option | 12 trở lên |
| Product | 4 |
| Axis | 5 (2 + 2 + 0 + 1) |
| Axis value | 11 |
| Variant | 12 (5 + 4 + 1 + 2) |
| Default variant | 1 |

## 6. Seed cho service phụ thuộc

Sau khi variant ID mới cố định:

- `promotion_campaign_product.product_variant_id` trỏ tới ít nhất một variant giày và một variant điện thoại.
- Cart demo có hai shop và dùng variant ID mới; snapshot hiển thị `variantLabel` tổng quát.
- Order demo tham chiếu variant ID mới nhưng giữ snapshot tên trục/value để lịch sử không đổi khi catalog sửa tên.
- Review vẫn gắn `product_id`, không gắn các bảng cứng đã xóa.
- Outbox tạo một `ProductCreated`/document mới cho mỗi product; Elasticsearch reindex có đúng bốn document.

## 7. Câu SQL/check runtime bắt buộc

1. `SELECT COUNT(*)` sáu bảng cũ phải lỗi “table does not exist” hoặc information_schema trả 0.
2. Không có product thiếu variant.
3. Product không axis có đúng một default variant; product có axis có 0 default variant.
4. Không product nào có quá hai axis.
5. Mỗi non-default variant map đủ số axis.
6. `PROD-SHOE-001` có 5 chứ không phải 6 tổ hợp.
7. `PROD-SERUM-001` có 1 default variant, mapping count 0.
8. Filter category Giày có “Loại đế/Độ drop/Màu sắc/Kích cỡ” nhưng không có “Chip”.
9. Filter category Điện thoại có “Chip/Pin/Dung lượng/Màu” nhưng không có “Loại đế/Size giày”.
10. Detail Serum trả `variantAxes=[]`, `variants.length=1`, `variants[0].isDefault=true`.
11. Cart, checkout, stock adjust và promotion lookup đều pass bằng variant ID mới.
12. Elasticsearch có đúng bốn document và không còn top-level `brandId/brand`.
