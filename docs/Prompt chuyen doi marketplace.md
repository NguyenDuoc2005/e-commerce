# PROMPT CHO CODEX: Chuyển đổi triệt để sang Marketplace đa gian hàng chuẩn (Shopee/Tiki-like)

> Đây là bản THAY THẾ hoàn toàn `docs/Prompt chuyen doi marketplace.md`. Copy toàn bộ nội dung dưới đây,
> dán đè vào file đó. Từ phiên thứ 2 trở đi, chỉ cần nói với Codex:
> **"Đọc file `docs/PROGRESS.md` và `HE_THONG_HIEN_TAI_MARKETPLACE.md`, làm tiếp theo đúng kế hoạch trong `docs/Prompt chuyen doi marketplace.md`"**

Prompt này được viết dựa trên audit thật của source code hiện tại (`HE_THONG_HIEN_TAI_MARKETPLACE.md`, cập nhật 2026-08-24) và DB thật đã kiểm tra qua DBeaver (schema `ecommerce_catalog` đã có đủ bảng thuộc tính động: `product_attribute_definition`, `product_attribute_option`, `product_attribute_value`, `product_attribute_moderation_audit`, `product_variant_axis`, `product_variant_axis_value`, `product_variant_axis_value_mapping`, `category_attribute_suggestion`, `variant_axis_name_suggestion`). Mọi tên bảng/cột/route/file nhắc trong prompt này đều lấy nguyên văn từ audit — KHÔNG được đổi tên tùy tiện khi code.

---

## 0. QUY TẮC BẮT BUỘC — ĐỌC TRƯỚC KHI LÀM BẤT CỨ VIỆC GÌ

Dự án làm qua nhiều phiên rời rạc (mỗi lần chat là 1 phiên, không nhớ phiên trước).

### Đầu phiên (bắt buộc)
1. Đọc `docs/PROGRESS.md` — biết đã làm tới đâu, task nào dở, câu hỏi nào đang treo.
2. Đọc `HE_THONG_HIEN_TAI_MARKETPLACE.md` — đối chiếu lại với source thật vì có thể đã đổi khác.
3. Đọc đúng Mục 6 (Thứ tự PR) của file này để biết đang ở PR nhóm nào, KHÔNG nhảy cóc.
4. Không suy đoán trạng thái code — luôn `view`/`grep` lại source thật ở phần liên quan trước khi sửa.
5. Nếu nghi ngờ dữ liệu bất thường (file mất, git status lạ) — dừng, ghi chú vào PROGRESS.md, hỏi người dùng, KHÔNG tự khôi phục hàng loạt.

### Cuối phiên (bắt buộc, kể cả khi chưa xong task)
1. Cập nhật `docs/PROGRESS.md` đúng format ở Mục 9 của file cũ (giữ nguyên format đang dùng: Trạng thái tổng quan, Câu hỏi cần xác nhận, Checklist tính năng, Nhật ký chi tiết theo phiên).
2. Nếu 1 task bị chặn vì thiếu quyết định nghiệp vụ — dừng, ghi vào "Câu hỏi/quyết định cần xác nhận", không đoán bừa.
3. Không kết thúc phiên mà không cập nhật PROGRESS.md.

---

## 1. QUYẾT ĐỊNH NGHIỆP VỤ ĐÃ CHỐT (trả lời dứt điểm Mục 20 và Mục 30.1 của `HE_THONG_HIEN_TAI_MARKETPLACE.md`)

Đây là câu trả lời chính thức cho toàn bộ câu hỏi đang treo. Nếu người dùng muốn đổi quyết định nào, phải sửa lại mục này trước khi code tiếp — Codex không được tự đổi.

### 1.1 Mô hình seller/shop: GIỮ NGUYÊN "1 owner_customer_id = 1 seller record đang active"
Đúng như source hiện tại đang làm (Mục 5.2, Mục 26.1 của audit). Bảng `seller` tiếp tục vừa là hồ sơ seller vừa là hồ sơ shop — KHÔNG tách thành bảng `shop` riêng, KHÔNG đổi sang model "1 owner nhiều shop". Đây đúng chuẩn thị trường: Shopee/Lazada/Tiki đều gắn 1 tài khoản bán hàng với 1 gian hàng; muốn nhiều gian hàng phải có nhiều tài khoản riêng biệt.

**Hệ quả kỹ thuật — KHÔNG được làm:**
- Không thêm bảng `shop` mới.
- Không đổi JWT claim `sellerId` thành `sellerIds` (mảng).
- Không đổi header `X-Seller-Id` thành multi-value.
- Không xây shop switcher.

**Glossary bắt buộc dùng đúng khi code/đặt tên biến/API mới:** `sellerId` trong toàn bộ hệ thống hiện tại **chính là** shop ID. Khi UI hiển thị chữ "Shop", backend vẫn dùng `seller_id`/`sellerId`. Không tạo khái niệm `shopId` mới song song gây nhầm.

### 1.2 Platform Admin CÓ ĐƯỢC CRUD sản phẩm thay seller không: KHÔNG
Đúng theo Mục 26.2 và Mục 29.1 của audit. Vai trò Admin với sản phẩm CHỈ gồm:
- Xem toàn bộ sản phẩm (`GET` danh sách, không có quyền `POST`/`PUT` nội dung sản phẩm shop).
- Ẩn/gỡ sản phẩm vi phạm (thêm hành động mới, không phải sửa nội dung).
- Hậu kiểm/chuẩn hóa/gộp/ẩn ở tầng **attribute** và **variant axis** (đã có sẵn API tại `AdminProductAttributeController`, `AdminVariantAxisController` — Mục 6.5, 6.6 audit) — đây là quyền admin có với "thuộc tính", KHÔNG phải với "sản phẩm".
- Xem report/complaint về sản phẩm (module mới, xem Mục 5 file này).

Route `/api/v1/admin/san-pham/**` và `/api/v1/admin/san-pham-chi-tiet/**` hiện đang route qua gateway (Mục 3.2 audit) — đây là dấu vết admin-tự-bán kiểu cũ, PHẢI audit xem controller có đang cho tạo/sửa sản phẩm thay seller không, nếu có thì gỡ bỏ (xem PR nhóm 2, Mục 6).

### 1.3 Voucher: Admin = Voucher sàn (`seller_id = null`), Seller = Voucher shop (`seller_id = X-Seller-Id`)
Đúng theo Mục 8.1, 23.7, 26.4 của audit — bảng `voucher` đã có sẵn cột `seller_id` phân biệt đúng 2 loại. **Không cần đổi schema.** Việc cần làm là đảm bảo:
- FE `/admin/voucher` chỉ tạo/sửa voucher với `seller_id = null`, không cho chọn seller cụ thể để tạo hộ.
- Route `/api/v1/admin/them-phieu-giam-gia` (Mục 2.3 audit, route legacy) cần audit lại — nếu đây là màn tạo voucher kiểu "phiếu giảm giá cửa hàng cũ" thì gộp vào `/admin/voucher` chuẩn, không giữ 2 luồng song song.
- Đổi nhãn hiển thị: "Quản lý phiếu giảm giá" → "Voucher sàn" (chỉ đổi label FE, backend giữ nguyên endpoint `/api/v1/admin/voucher/**`).

### 1.4 "Quản lý khách hàng" → "Người dùng/Tài khoản"
Đúng Mục 4.2, 23.2, 29.1 audit. Backend giữ nguyên `user-service` bảng `customer` và toàn bộ endpoint `/api/v1/admin/khach-hang/**` — **không đổi route backend**, chỉ đổi:
- Label menu FE: "Quản lý khách hàng" → "Người dùng" (hoặc "Người dùng/Tài khoản").
- Bổ sung UI: hiển thị thêm cột "Đã đăng ký seller?" (join `seller.owner_customer_id`), trạng thái shop nếu có, thay vì chỉ có info CRUD kiểu POS.
- KHÔNG cho tạo "khách hàng mới" theo kiểu nhập tay cho bán tại quầy (nếu form hiện tại có nút "Thêm khách hàng" kiểu vãng lai, xem lại mục đích — nếu chỉ dùng cho admin tạo tài khoản hỗ trợ thì giữ nhưng đổi nhãn rõ, nếu là tàn dư POS thì gỡ).

### 1.5 "Quản lý nhân viên" → "Quản trị viên/Phân quyền"
Đúng Mục 4.3, 23.2, 29.1 audit. Backend giữ nguyên `user-service` bảng `staff` và endpoint `/api/v1/admin/nhan-vien/**` — **không đổi route backend**, chỉ:
- Đổi label FE: "Quản lý nhân viên" → "Quản trị viên/Phân quyền".
- Đảm bảo các role gán cho `staff` phản ánh đúng vai trò platform operator (admin tổng, operator duyệt seller, operator dispute, operator payout, content moderator) — không còn field/role mang nghĩa "nhân viên bán hàng của 1 cửa hàng".
- Endpoint `PUT /api/v1/admin/nhan-vien/{id}/change-role` đã có sẵn — audit lại danh sách role hợp lệ đang cho phép gán, loại bỏ role kiểu "nhân viên quầy" nếu có.

### 1.6 POS `ban-hang` và `hoa-don` offline: XÓA HOÀN TOÀN, làm theo 3 bước an toàn
Đúng theo Mục 14.3, 17.2, 21 audit — đây là nhóm việc rủi ro cao nhất vì đụng tới bảng `orders` đang dùng chung cho cả buyer thật (cột `staff_id`, `debt_amount`, `refund_amount` trong `orders` là dấu vết POS — Mục 23.6 audit). Xử lý theo đúng 3 bước, mỗi bước 1 PR riêng, **không gộp**:

- **Bước 1 (an toàn, làm trước):** Ẩn/xóa hoàn toàn khỏi FE — route `/admin/ban-hang`, `/admin/hoa-don` và mọi liên kết trong `AdminSidebar.vue`. Xóa constant liên quan trong `FE/src/constants/path.ts`. Xóa khai báo route trong `FE/src/routes/router.ts`. Có thể xóa hẳn folder `admin/banhang`, `admin/hoadon` (Mục 13.2 audit) sau khi xác nhận không route nào khác import.
- **Bước 2 (kiểm tra kỹ trước khi làm):** Audit gateway route `/api/v1/admin/ban-hang/**`, `/api/v1/admin/hoa-don/**` trong `api-gateway/src/main/resources/application.yml` (Mục 22 audit) — kiểm tra controller đứng sau route này trong `order-service` có bị bất kỳ luồng nào khác gọi tới không (VD: có bị buyer checkout hay seller order dùng chung code không). Nếu độc lập hoàn toàn, xóa route gateway + controller.
- **Bước 3 (chỉ làm sau khi bước 1-2 xong và đã chạy ổn định ít nhất 1 thời gian, hoặc theo xác nhận người dùng):** Xử lý cột legacy trong bảng `orders`: `staff_id`, `debt_amount`, `refund_amount` (Mục 23.6 audit). Các cột này có thể đang rỗng/không dùng với order thật của buyer — kiểm tra dữ liệu thực tế trước:
  ```sql
  SELECT COUNT(*) AS total, COUNT(staff_id) AS has_staff, COUNT(debt_amount) AS has_debt, COUNT(refund_amount) AS has_refund
  FROM orders;
  ```
  Nếu toàn bộ order thật (có `customer_id` hợp lệ) đều có các cột này = NULL/0, có thể đánh dấu deprecated (không xóa cột ngay, tránh vỡ code cũ) và loại bỏ khỏi mọi DTO/response mới. Xóa cột vật lý là việc CHỈ làm khi có xác nhận rõ từ người dùng.

### 1.7 Thuộc tính seller tự thêm có hiện ngay cho seller khác dùng chung không: CÓ, nhưng ở trạng thái `is_verified = false`
Đúng theo Mục 20 câu 7 và Mục 24.8 audit — chốt theo đúng cơ chế đã build: khi seller tạo attribute mới, `product_attribute_definition` được tạo với `created_by_seller_id = X-Seller-Id`, `is_verified = false`. Definition này **hiện ngay trong autocomplete/suggestion cho seller khác** (để tránh loạn tên trùng nghĩa), nhưng admin hậu kiểm sau (`verify`/`standardize`/`merge`/`hide` — đã có đủ API tại `AdminProductAttributeController`). Đây là cơ chế **hậu kiểm (post-moderation)**, không phải **tiền kiểm (pre-moderation)** — không được đổi sang chặn seller tạo tự do rồi chờ duyệt mới hiện, vì sẽ làm chậm luồng đăng sản phẩm của seller.

### 1.8 Product canonical DB — reset/cutover hay migration an toàn: MIGRATION AN TOÀN, không reset
Vì DB hiện tại (`ecommerce_catalog`, xem ảnh DBeaver) đã đúng schema canonical (đã có đủ 12 bảng thuộc tính động + variant), không cần reset. Chỉ cần:
- Kiểm tra live schema đã khớp hoàn toàn với entity source chưa (chạy migration tool đang dùng, VD Flyway/Liquibase — audit lại thư mục migration trong `catalog-service`).
- Nếu có bảng/cột thừa từ hệ thống giày cũ còn sót trong `ecommerce_catalog` (khác 12 bảng đã liệt kê ở đầu file này), liệt kê ra và xin xác nhận trước khi xóa.
---

## 2. KIẾN TRÚC PHÂN QUYỀN CHUẨN (đối chiếu bắt buộc mọi task với bảng này)

| Thực thể | Bảng/Service sở hữu | Seller Center | Platform Admin |
|---|---|---|---|
| Tài khoản buyer | `user-service.customer` | — | Xem, khóa/mở, xem lịch sử vi phạm |
| Tài khoản seller/shop | `seller-service.seller` | Cập nhật hồ sơ shop mình | Duyệt/từ chối/khóa/mở, xem lịch sử (`seller_status_history`) |
| Category (taxonomy) | `catalog-service.category` | Chỉ đọc (chọn khi tạo SP) | CRUD toàn quyền |
| Product | `catalog-service.product` | CRUD toàn quyền SP của shop mình | CHỈ xem + ẩn/gỡ vi phạm, KHÔNG sửa nội dung |
| Attribute definition/option/value | `catalog-service.product_attribute_*` | Tạo mới khi cần (chưa verify), dùng suggestion có sẵn | Hậu kiểm: verify/standardize/merge/hide |
| Variant axis | `catalog-service.product_variant_axis*` | Tạo/chọn tối đa 2 axis khi đăng SP | Xem insight, quản lý `variant_axis_name_suggestion` |
| Cart | `cart-service.cart*` | — | — |
| Order (gốc) | `order-service.orders` | — | Giám sát toàn sàn, KHÔNG thao tác thay seller |
| Order (sub theo seller) | `order-service.order_seller` | Xử lý đơn thuộc shop mình (confirm/ship/complete/cancel) | Giám sát SLA, xử lý tranh chấp |
| Voucher sàn | `promotion-service.voucher` (`seller_id = null`) | — | Tạo/sửa/xóa |
| Voucher shop | `promotion-service.voucher` (`seller_id = X`) | Tạo/sửa/xóa (của shop mình) | Chỉ giám sát, không tạo hộ |
| Campaign/Flash sale sàn | `promotion-service.promotion_campaign*` | Đăng ký SP tham gia | Tạo/cấu hình/duyệt SP tham gia |
| Ví/Đối soát | `payout-service.seller_wallet`, `seller_receivable` | Xem số dư, lịch sử, yêu cầu rút | Duyệt payout, cấu hình `commission_config` |
| Review | `seller-service.danh_gia` | Reply review của shop mình | Kiểm duyệt report gian lận (module mới) |
| Follow shop | `seller-service.shop_follow` | — (buyer thao tác) | — |
| Banner | `seller-service.platform_banner` | — | CRUD toàn quyền |
| Dispute/Tranh chấp | **Bảng mới cần tạo** (xem Mục 5) | Phản hồi khiếu nại | Phân xử, quyết định hoàn tiền |
| Report nội dung | **Bảng mới cần tạo** (xem Mục 5) | — | Xử lý report |
| Platform staff/RBAC | `user-service.staff` | — | CRUD, gán role/permission |

---

## 3. CHECKLIST ĐỒNG BỘ VỚI `docs/PROGRESS.md` (KHÔNG làm lại phần đã DONE)

### Đã hoàn thành — audit xác nhận đúng, chỉ kiểm tra không đổi hướng
- [x] Đăng ký/đăng nhập buyer, seller, platform admin (Mục 4.1, 24.1 audit)
- [x] Đăng ký & duyệt shop (Mục 5.2, 5.3, 24.2 audit)
- [x] Trang chủ storefront gộp nhiều shop
- [x] Trang riêng từng shop (`/shop/:sellerSlug`, Mục 5.4 audit)
- [x] Tìm kiếm/filter sản phẩm toàn sàn (Mục 6.2 audit)
- [x] Giỏ hàng multi-seller (đã có `seller_id`, `shop_name`, `seller_slug` snapshot trong `cart_detail` — Mục 23.5 audit)
- [x] Checkout tách theo shop, thanh toán một lần (Mục 7.2, 24.6 audit)
- [x] Split-order thành sub-order theo seller (`orders` → `order_seller` → `order_item`, Mục 7.3, 23.6 audit)
- [x] Quản lý đơn hàng riêng theo từng seller (`SellerOrderController`, Mục 15.2 audit)
- [x] Voucher 2 tầng (`voucher.seller_id`, Mục 8, 23.7 audit)
- [x] Đánh giá sản phẩm & shop sau khi nhận hàng (`danh_gia`, Mục 10.1 audit)
- [x] Theo dõi shop (`shop_follow`, Mục 10.2 audit)
- [x] Ví & đối soát cho seller (`seller_wallet`, `seller_receivable`, Mục 9, 23.8 audit)
- [x] Thống kê riêng theo seller + thống kê tổng toàn sàn
- [x] Duyệt/khóa seller bởi Platform Admin (Mục 5.3 audit)
- [x] Thông báo qua notification-service (Mục 12 audit)

### Đang dở / chưa làm — ưu tiên theo đúng thứ tự Mục 6 (Thứ tự PR) của file này
1. **[ ] Chuẩn hóa UI thuộc tính động phía Admin** — backend/DB đã có đủ (đã xác nhận qua DBeaver), việc còn lại thuần là hoàn thiện UI hậu kiểm tại `/admin/product-attributes` theo đúng Mục 6.5 audit (tab Thuộc tính/Option/Danh mục gợi ý/Gộp-lịch sử/Trục biến thể).
2. **[ ] Chuẩn hóa UI thuộc tính động phía Seller** — form đăng sản phẩm cần autocomplete/tự thêm thuộc tính đúng luồng Mục 18.2 audit (chọn danh mục → gợi ý thuộc tính → seller tự thêm → khai báo trục biến thể → sinh SKU).
3. **[ ] Chuẩn hóa UI thuộc tính động phía Buyer** — filter theo danh mục (`categories/{id}/attribute-suggestions`) và hiển thị bảng thông số ở trang chi tiết sản phẩm (Mục 19.3 audit).
4. **[ ] Xóa module POS `ban-hang`/`hoa-don` offline** — theo đúng 3 bước ở Mục 1.6 file này.
5. **[ ] Đổi tên menu Admin**: "Quản lý khách hàng" → "Người dùng", "Quản lý nhân viên" → "Quản trị viên/Phân quyền", "Quản lý phiếu giảm giá" → "Voucher sàn" (Mục 1.3-1.5 file này).
6. **[ ] Tách layout/sidebar Admin và Seller** — hiện dùng chung `Admin.vue` + `AdminSidebar.vue` (Mục 13.2 audit) gây dễ lẫn logic. Tách thành `PlatformAdminLayout` + `AdminSidebar.vue` riêng, và `SellerCenterLayout` + `SellerSidebar.vue` riêng.
7. **[ ] Category Management UI mới cho Admin** — hiện category tree chỉ được dùng ngầm trong product-attributes, chưa có màn quản lý category độc lập, đúng vai trò (Mục 6.4, 17.1 mục 5 audit).
8. **[ ] Module Dispute (Tranh chấp đơn hàng)** — CHƯA CÓ, phải tạo mới hoàn toàn (xem Mục 5 file này).
9. **[ ] Module Report/Kiểm duyệt nội dung** — CHƯA CÓ, phải tạo mới hoàn toàn (xem Mục 5 file này).
10. **[ ] Chat buyer-seller** — làm sau khi các mục trên ổn định.
11. **[ ] Flash sale toàn sàn** — cần hoàn thiện `promotion_campaign`, `promotion_campaign_product` (đã có bảng theo Mục 8.3 audit, cần UI đăng ký/duyệt).

**Việc audit phát hiện thêm, chưa có trong checklist gốc, cần bổ sung:**
- [ ] Đổi tên/dọn route legacy catalog: `mau-sac`, `chat-lieu`, `loai-de`, `loai-giay`, `size`, `thuong-hieu`, `dot-giam-gia`, `add-dot-giam-gia`, `update-dot-giam-gia` (Mục 2.3 audit) — folder/route vẫn còn dù sidebar đã ẩn, cần dọn dứt điểm.
- [ ] Audit toàn bộ route FE thiếu `meta.requiresRole` (Mục 16.3, 21.1 audit).
- [ ] Đổi tên path `/permitall/cart`, `/permitall/don-mua`, `/permitall/reviews`, `/permitall/shops/{id}/follow` — các path này có tên "permitall" (ngụ ý public) nhưng thực tế cần `X-User-Id` (buyer đã login) — dễ gây hiểu lầm khi audit bảo mật (Mục 26.6 audit).
- [ ] Xác minh `soldCount` trong Public shop response đang hard-code = 0 (Mục 5.4 audit) — cần tính thật từ `order_seller`/`order_item` completed.
- [ ] Xác minh luồng payout có thực sự tự động tạo `seller_receivable` khi `order_seller` complete hay mới là skeleton (Mục 24.7 audit ghi "cần kiểm tra service implementation").
---

## 4. THIẾT KẾ CHI TIẾT MODULE MỚI — DISPUTE (TRANH CHẤP ĐƠN HÀNG)

Module này **hoàn toàn chưa tồn tại** trong source hiện tại (đối chiếu Mục 15.3, 17.1 mục 6 audit — chỉ có "Order goc", "Sub-order theo seller" ở tầng giám sát, chưa có tầng xử lý tranh chấp/khiếu nại/hoàn tiền). Đây là module BẮT BUỘC phải có với marketplace đa gian hàng chuẩn, vì buyer/seller sẽ phát sinh mâu thuẫn (hàng lỗi, giao sai, không nhận được hàng, seller không giao...).

### 4.1 Service đặt module này ở đâu
Đặt trong `order-service` (vì tranh chấp luôn gắn với 1 `order_seller` cụ thể, và order-service đã có sẵn context order/payment). Không tạo microservice mới riêng cho việc này ở giai đoạn này.

### 4.2 Schema — bảng mới trong DB `ecommerce_order`

```sql
CREATE TABLE dispute (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_seller_id     BIGINT NOT NULL,
    order_id            BIGINT NOT NULL,
    seller_id           BIGINT NOT NULL,
    customer_id         BIGINT NOT NULL,
    raised_by           VARCHAR(16) NOT NULL,         -- 'BUYER' | 'SELLER'
    dispute_type        VARCHAR(32) NOT NULL,          -- 'ITEM_NOT_RECEIVED' | 'ITEM_DAMAGED' | 'WRONG_ITEM' | 'NOT_AS_DESCRIBED' | 'REFUND_REQUEST' | 'OTHER'
    reason              VARCHAR(500) NOT NULL,
    description         TEXT,
    evidence_urls       JSON,                          -- mảng URL ảnh/video bằng chứng
    status              VARCHAR(24) NOT NULL DEFAULT 'OPEN',
                        -- 'OPEN' | 'SELLER_RESPONDED' | 'UNDER_ADMIN_REVIEW' | 'RESOLVED_REFUND_BUYER' | 'RESOLVED_REJECT_BUYER' | 'RESOLVED_PARTIAL_REFUND' | 'CLOSED'
    requested_amount    DECIMAL(15,2),                 -- số tiền buyer yêu cầu hoàn (nếu có)
    resolved_amount     DECIMAL(15,2),                 -- số tiền admin quyết định hoàn (nếu có)
    resolution_note     TEXT,
    resolved_by_staff_id BIGINT,
    resolved_at         DATETIME,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dispute_order_seller (order_seller_id),
    INDEX idx_dispute_status (status),
    INDEX idx_dispute_seller (seller_id),
    INDEX idx_dispute_customer (customer_id)
);

CREATE TABLE dispute_message (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispute_id          BIGINT NOT NULL,
    sender_type         VARCHAR(16) NOT NULL,          -- 'BUYER' | 'SELLER' | 'ADMIN'
    sender_id           BIGINT NOT NULL,
    message             TEXT NOT NULL,
    attachment_urls     JSON,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dispute_message_dispute (dispute_id)
);
```

Lý do có `dispute_message` riêng: tranh chấp thường cần qua lại nhiều lượt (buyer nêu vấn đề → seller phản hồi → admin hỏi thêm bằng chứng → ra quyết định), tách bảng message giúp hiển thị timeline rõ ràng thay vì nhồi hết vào 1 cột text.

### 4.3 API — Buyer (thêm vào path buyer protected, không dùng `/permitall` theo đúng khuyến nghị Mục 26.6 audit)

```
POST   /api/v1/buyer/disputes
       Body: { orderSellerId, disputeType, reason, description, evidenceUrls[] }
       Điều kiện: order_seller.order_status phải là DELIVERED hoặc COMPLETED (không cho mở dispute khi đơn chưa giao)
       Response: dispute object, status = OPEN

GET    /api/v1/buyer/disputes
       Query: status (optional)
       Trả về danh sách dispute của customer hiện tại (theo X-User-Id)

GET    /api/v1/buyer/disputes/{id}
       Trả về chi tiết dispute + toàn bộ dispute_message (kiểm tra dispute.customer_id == X-User-Id)

POST   /api/v1/buyer/disputes/{id}/messages
       Body: { message, attachmentUrls[] }
       Chỉ cho phép khi status không phải CLOSED/RESOLVED_*
```

### 4.4 API — Seller

```
GET    /api/v1/seller/disputes
       Query: status (optional)
       Trả về dispute của order_seller thuộc X-Seller-Id

GET    /api/v1/seller/disputes/{id}
       Kiểm tra dispute.seller_id == X-Seller-Id

POST   /api/v1/seller/disputes/{id}/respond
       Body: { message, attachmentUrls[] }
       Set status: OPEN → SELLER_RESPONDED
```

### 4.5 API — Admin

```
GET    /api/v1/admin/disputes
       Query: status, sellerId, dateFrom, dateTo
       Trả về toàn bộ dispute toàn sàn, ưu tiên sort theo status=OPEN/SELLER_RESPONDED lên đầu

GET    /api/v1/admin/disputes/{id}
       Chi tiết đầy đủ + toàn bộ message + thông tin order_seller/order_item liên quan

POST   /api/v1/admin/disputes/{id}/take-review
       Set status: SELLER_RESPONDED → UNDER_ADMIN_REVIEW, gán resolved_by_staff_id = X-User-Id (admin)

POST   /api/v1/admin/disputes/{id}/resolve
       Body: { decision: 'REFUND_BUYER' | 'REJECT_BUYER' | 'PARTIAL_REFUND', resolvedAmount, resolutionNote }
       - REFUND_BUYER: set status RESOLVED_REFUND_BUYER, trigger hoàn tiền toàn bộ (gọi payment/payout service để xử lý hoàn tiền, KHÔNG cộng vào seller_receivable của seller đó)
       - REJECT_BUYER: set status RESOLVED_REJECT_BUYER, không hoàn tiền
       - PARTIAL_REFUND: set status RESOLVED_PARTIAL_REFUND, hoàn resolvedAmount

POST   /api/v1/admin/disputes/{id}/close
       Đóng dispute (sau khi đã resolve và không còn khiếu nại thêm)
```

### 4.6 Tích hợp với payout-service
Khi dispute resolve = REFUND_BUYER hoặc PARTIAL_REFUND, cần gọi internal API sang `payout-service` để:
- Nếu `seller_receivable` cho `order_seller_id` đó đã ở status PENDING (chưa payout) → giảm/hủy receivable tương ứng.
- Nếu đã payout rồi → tạo bản ghi `ADJUSTMENT` âm (trừ vào lần payout tiếp theo của seller đó) — cần thêm 1 giá trị `type = 'DISPUTE_ADJUSTMENT'` nếu bảng payout có cột phân loại transaction, hoặc tạo bảng `payout_adjustment` mới nếu payout-service hiện chưa có cơ chế này (audit lại `payout-service` thực tế trước khi quyết định thêm bảng).

### 4.7 FE cần thêm
- Buyer: màn "Khiếu nại đơn hàng" — nút "Yêu cầu hỗ trợ/Khiếu nại" trên trang chi tiết đơn mua (`/don-mua-detail/:maHoaDon/:id`), mở form tạo dispute.
- Seller: menu mới "Tranh chấp/Khiếu nại" trong Seller Center (Mục 18.1 audit đã đề xuất, giờ hiện thực hóa).
- Admin: menu mới "Xử lý tranh chấp" trong nhóm "Đơn hàng" (theo đề xuất Mục 17.1 mục 6 audit).

---

## 5. THIẾT KẾ CHI TIẾT MODULE MỚI — REPORT (KIỂM DUYỆT NỘI DUNG)

Cũng hoàn toàn chưa tồn tại. Dùng để buyer/seller report sản phẩm vi phạm, review giả, shop lừa đảo — tách biệt với Dispute (Dispute là tranh chấp giao dịch cụ thể, Report là báo cáo vi phạm chính sách chung).

### 5.1 Service: đặt trong `catalog-service` cho report PRODUCT, hoặc `seller-service` cho report SHOP/REVIEW
Vì `danh_gia` (review) đã nằm trong `seller-service` (Mục 10.1 audit), đặt bảng `report` trong `seller-service` để tiện join, xử lý report cả PRODUCT/SHOP/REVIEW/USER qua polymorphic `target_type`.

### 5.2 Schema — bảng mới trong DB `ecommerce_seller`

```sql
CREATE TABLE report (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id     BIGINT NOT NULL,               -- customer_id của người report
    reporter_type   VARCHAR(16) NOT NULL,           -- 'BUYER' | 'SELLER'
    target_type     VARCHAR(16) NOT NULL,           -- 'PRODUCT' | 'SHOP' | 'REVIEW' | 'USER'
    target_id       BIGINT NOT NULL,
    reason_code     VARCHAR(48) NOT NULL,           -- 'FAKE_PRODUCT' | 'PROHIBITED_ITEM' | 'COPYRIGHT' | 'FAKE_REVIEW' | 'SCAM' | 'OFFENSIVE_CONTENT' | 'OTHER'
    description     TEXT,
    evidence_urls   JSON,
    status          VARCHAR(24) NOT NULL DEFAULT 'PENDING',
                    -- 'PENDING' | 'REVIEWING' | 'ACTION_TAKEN' | 'DISMISSED'
    action_taken    VARCHAR(48),                    -- 'PRODUCT_DELISTED' | 'SHOP_SUSPENDED' | 'REVIEW_HIDDEN' | 'WARNING_SENT' | 'NO_ACTION'
    reviewed_by_staff_id BIGINT,
    reviewed_at     DATETIME,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_target (target_type, target_id),
    INDEX idx_report_status (status)
);
```

### 5.3 API — Buyer/Seller (tạo report)

```
POST   /api/v1/buyer/reports
       Body: { targetType, targetId, reasonCode, description, evidenceUrls[] }

POST   /api/v1/seller/reports
       Body: giống trên — seller cũng có thể report (VD report review giả trên sản phẩm mình)
```

### 5.4 API — Admin

```
GET    /api/v1/admin/reports
       Query: status, targetType, dateFrom, dateTo

GET    /api/v1/admin/reports/{id}

POST   /api/v1/admin/reports/{id}/review
       Set status PENDING → REVIEWING, gán reviewed_by_staff_id

POST   /api/v1/admin/reports/{id}/resolve
       Body: { actionTaken, note }
       Tùy actionTaken, gọi API tương ứng:
       - PRODUCT_DELISTED → gọi catalog-service ẩn product (dùng cơ chế ẩn sản phẩm ở Mục 1.2 file này)
       - SHOP_SUSPENDED → gọi seller-service `/api/v1/admin/sellers/{id}/suspend` (đã có sẵn — Mục 5.3 audit)
       - REVIEW_HIDDEN → set `danh_gia` status ẩn (cần thêm cột status nếu bảng `danh_gia` hiện chưa có, audit lại trước)
       - WARNING_SENT → gọi notification-service gửi cảnh báo
       - NO_ACTION → set status DISMISSED
```

### 5.5 FE cần thêm
- Nút "Báo cáo vi phạm" trên trang sản phẩm, trang shop, dưới mỗi review (buyer-facing).
- Admin: menu mới "Kiểm duyệt nội dung" trong nhóm "Sản phẩm & Nội dung" (Mục 17.1 mục 4 audit).
---

## 6. THỨ TỰ TRIỂN KHAI — 11 NHÓM PR, MỖI NHÓM LÀ 1 PHIÊN LÀM VIỆC RIÊNG

Nguyên tắc: mỗi PR nhóm phải build/chạy được độc lập, không phá luồng đang chạy tốt. Không gộp PR nhóm. Trong mỗi nhóm, làm đúng thứ tự bước liệt kê.

### PR NHÓM 1 — Dọn legacy POS an toàn (rủi ro thấp, làm trước để giảm nhiễu code)
1. Xóa/ẩn route FE `/admin/ban-hang`, `/admin/hoa-don` khỏi `FE/src/constants/path.ts`.
2. Xóa khai báo route trong `FE/src/routes/router.ts`.
3. Xóa mục menu tương ứng trong `FE/src/components/custom/Sidebar/AdminSidebar.vue`.
4. Grep toàn bộ FE tìm import từ folder `admin/banhang`, `admin/hoadon` — nếu không còn ai import, xóa hẳn 2 folder này.
5. Xóa route legacy catalog không dùng: `/admin/mau-sac`, `/admin/chat-lieu`, `/admin/loai-de`, `/admin/loai-giay`, `/admin/size`, `/admin/thuong-hieu` — kiểm tra các route này đã redirect về `/admin/product-attributes` (Mục 2.3 audit ghi rõ đã redirect) → xóa hẳn route + folder cũ `admin/mausac`, `admin/size`, `admin/chatlieu`, `admin/loaide`, `admin/thuonghieu`, `admin/sanpham`, `admin/sanphamchitiet` (Mục 2.3 audit).
6. **Acceptance:** FE build thành công (`npm run build` hoặc lệnh build đang dùng), `vue-tsc --noEmit` không lỗi liên quan các file đã xóa, không còn link chết trong sidebar khi đăng nhập admin.
7. Cập nhật PROGRESS.md: đánh dấu DONE mục "Xóa module POS ban-hang/hoa-don offline" bước 1.

### PR NHÓM 2 — Audit & xóa route gateway/backend POS
1. Đọc `backend-microservice/api-gateway/src/main/resources/application.yml`, liệt kê chính xác route `/api/v1/admin/ban-hang/**`, `/api/v1/admin/hoa-don/**` đang trỏ tới service nào.
2. Đọc controller tương ứng trong `order-service`, kiểm tra có bị luồng buyer checkout hoặc seller order dùng chung method/class không.
3. Nếu độc lập hoàn toàn: xóa route khỏi gateway config, xóa controller, xóa service method chỉ phục vụ POS.
4. Nếu dùng chung 1 phần: tách riêng phần POS ra, không xóa phần dùng chung.
5. Chạy lại toàn bộ test của `order-service` (nếu có) đảm bảo checkout/seller-order không bị ảnh hưởng.
6. **Acceptance:** `order-service` compile/test pass, checkout buyer thật vẫn hoạt động bình thường (browser smoke test), gateway không còn route POS.
7. Cập nhật PROGRESS.md.

### PR NHÓM 3 — Đổi tên menu Admin (Người dùng, Quản trị viên, Voucher sàn)
1. FE: đổi label trong `AdminSidebar.vue` — "Quản lý khách hàng" → "Người dùng", "Quản lý nhân viên" → "Quản trị viên/Phân quyền", "Quản lý phiếu giảm giá" → "Voucher sàn". KHÔNG đổi route path backend, chỉ đổi label hiển thị và có thể đổi route path FE nếu cần (VD `/admin/khach-hang` → `/admin/nguoi-dung`) — nếu đổi path FE, giữ backward-compat redirect từ path cũ.
2. FE trang "Người dùng" (`KhachHang.vue` cũ): bổ sung cột hiển thị "Trạng thái seller" — gọi thêm API lấy thông tin seller theo `owner_customer_id` (cần thêm 1 API mới ở `seller-service`: `GET /internal/sellers/by-owner-ids?ids=1,2,3` trả về map customerId → seller status, dùng để hiển thị hàng loạt không gọi N+1 request).
3. FE trang "Quản trị viên" (`NhanVien.vue` cũ): audit danh sách role đang cho gán qua `PUT /api/v1/admin/nhan-vien/{id}/change-role`, loại bỏ role không còn ý nghĩa (nếu có role kiểu "nhân viên bán hàng"/"thu ngân"), thêm role mới nếu cần: `SELLER_APPROVAL_OPERATOR`, `DISPUTE_OPERATOR`, `PAYOUT_OPERATOR`, `CONTENT_MODERATOR`, `SUPER_ADMIN`.
4. FE trang "Voucher sàn" (`admin/voucher` hiện tại): xóa mọi UI cho phép chọn `sellerId` cụ thể khi tạo voucher (đảm bảo request luôn gửi `seller_id = null`). Nếu form hiện tại có field chọn seller, xóa field đó.
5. Audit route `/admin/them-phieu-giam-gia` — nếu là form riêng biệt tạo voucher kiểu cũ, gộp logic vào flow `/admin/voucher` chuẩn, xóa route riêng.
6. **Acceptance:** Admin đăng nhập thấy đúng 3 label mới, tạo voucher sàn không còn chọn được seller, trang người dùng hiển thị được trạng thái seller.
7. Cập nhật PROGRESS.md.

### PR NHÓM 4 — Tách layout/sidebar Admin và Seller
1. Tạo `FE/src/layout/PlatformAdminLayout.vue` (copy từ `Admin.vue` hiện tại, bỏ toàn bộ nhánh logic `role === 'SELLER'`).
2. Tạo `FE/src/layout/SellerCenterLayout.vue` (layout mới riêng cho seller, không dùng chung `Admin.vue` nữa).
3. Tách `AdminSidebar.vue` hiện tại thành `AdminSidebar.vue` (chỉ menu admin) và `SellerSidebar.vue` (chỉ menu seller, menu items theo đúng Mục 18.1 audit: Tổng quan shop, Sản phẩm, Đơn hàng, Marketing shop, Đánh giá, Ví & đối soát, Hồ sơ shop — bổ sung "Tranh chấp/Khiếu nại" mới từ Mục 4 file này).
4. Cập nhật `router.ts`: route `/admin/**` dùng `PlatformAdminLayout`, route `/seller/**` dùng `SellerCenterLayout`.
5. **Acceptance:** Đăng nhập admin chỉ thấy sidebar admin, đăng nhập seller chỉ thấy sidebar seller, không còn logic rẽ nhánh role trong 1 component dùng chung.
6. Cập nhật PROGRESS.md.

### PR NHÓM 5 — Category Management UI mới cho Admin
1. Tạo trang FE mới `FE/src/pages/admin/category/CategoryManagement.vue`, route `/admin/categories`.
2. Dùng lại API đã có: `GET /api/v1/admin/categories/tree`, `POST /api/v1/admin/categories`, `PUT /api/v1/admin/categories/{id}`, `PUT /api/v1/admin/categories/{id}/status`, `PUT /api/v1/admin/categories/{id}/attribute-suggestions` (Mục 6.4 audit — API đã có sẵn, chỉ thiếu UI).
3. UI cần: cây danh mục dạng tree view có thể expand/collapse, form thêm/sửa category (tên, danh mục cha, trạng thái), tab con để gán attribute suggestions cho category đang chọn (kéo từ danh sách `product_attribute_definition` đã verified).
4. Thêm mục menu "Danh mục & Thuộc tính" trong `AdminSidebar.vue`, gộp chung với `/admin/product-attributes` thành 1 nhóm menu có sub-item.
5. **Acceptance:** Admin tạo/sửa/ẩn category qua UI mới, gán attribute suggestion cho category thành công, category tree hiển thị đúng phân cấp.
6. Cập nhật PROGRESS.md.

### PR NHÓM 6 — Hoàn thiện UI Admin hậu kiểm thuộc tính (`/admin/product-attributes`)
Backend đã đủ API (Mục 6.5 audit liệt kê 9 endpoint). Chỉ cần hoàn thiện UI theo đúng cấu trúc:
1. Tab "Thuộc tính" (Attribute definitions): bảng liệt kê `code`, `name`, `data_type`, `is_verified`, tên category (không phải ID — đã có bug từng bị lộ UUID, đảm bảo không tái diễn), nguồn tạo (system/admin/seller — dựa vào `created_by_seller_id` null hay có giá trị), số sản phẩm đang dùng, ngày tạo. Actions: Verify, Standardize (sửa tên/đơn vị/category), Hide, Merge (chọn definition đích để gộp).
2. Tab "Option" (cho attribute kiểu SELECT_ONE/SELECT_MULTI): liệt kê theo attribute đang chọn, actions Verify/Merge.
3. Tab "Danh mục gợi ý" (`category_attribute_suggestion`): xem/sửa gán attribute nào gợi ý cho category nào, cờ `filterable`, `required_value`.
4. Tab "Gộp & Lịch sử": hiển thị `product_attribute_moderation_audit` — ai làm gì, khi nào (verify/standardize/merge/hide).
5. Tab "Trục biến thể" (tách biệt hẳn khỏi thuộc tính mô tả — đúng nguyên tắc Mục 26.3 audit): dùng API `GET /api/v1/admin/product-variant-axes/insights`, `.../suggestions`, verify/merge/hide suggestion.
6. **Acceptance:** Toàn bộ 5 tab hoạt động, verify/standardize/merge/hide chạy đúng, không còn hiện UUID thay vì tên category.
7. Cập nhật PROGRESS.md.

### PR NHÓM 7 — Hoàn thiện Seller Product Form (thuộc tính động phía seller)
Theo đúng luồng 7 bước Mục 18.2 audit:
1. Bước chọn category: dropdown/tree chọn `category_id`.
2. Sau khi chọn category, gọi `GET /api/v1/seller/products/categories/{categoryId}/attribute-suggestions` hiển thị danh sách thuộc tính gợi ý (đã filterable/required được đánh dấu rõ).
3. Cho phép seller tự thêm thuộc tính mới không có trong gợi ý (tạo `product_attribute_definition` mới với `created_by_seller_id`, `is_verified = false` — theo đúng Mục 1.7 file này).
4. Bước khai báo trục biến thể: dùng `GET /api/v1/seller/products/variant-axis-name-suggestions` cho autocomplete tên trục, giới hạn tối đa 2 axis (validate ở cả FE và BE).
5. Nhập giá trị cho từng axis, hệ thống tự sinh bảng tổ hợp combination (Cartesian product của các axis value).
6. Với mỗi combination, seller nhập SKU (validate unique trong phạm vi product), giá, tồn kho, ảnh riêng (optional).
7. Preview toàn bộ trước khi submit, sau đó `POST /api/v1/seller/products` gửi aggregate request đầy đủ.
8. **Acceptance:** Seller tạo được sản phẩm đa ngành hàng bất kỳ (không chỉ giày) với thuộc tính tự chọn, sinh đúng SKU theo tổ hợp axis, seller sửa sản phẩm (`PUT`) giữ đúng dữ liệu cũ khi chỉnh sửa 1 phần.
9. Cập nhật PROGRESS.md.

### PR NHÓM 8 — Hoàn thiện Buyer Dynamic Filters + Product Detail
1. Trang `/san-pham`: bỏ hoàn toàn filter hard-code (nếu còn sót filter "màu sắc"/"size" cứng cho ngành giày), thay bằng filter động lấy từ `categories/{categoryId}/attribute-suggestions` khi buyer chọn category.
2. Card sản phẩm trong danh sách: hiển thị shop name, rating, sold count (đã audit `soldCount` đang hard-code 0 — PR này phải fix tính thật từ `order_seller`/`order_item` completed, xem PR nhóm 11).
3. Trang `/san-pham-chi-tiet/:idsp`: hiển thị block riêng "Thông số sản phẩm" đọc từ `product_attribute_value`, block "Chọn phân loại" đọc từ `product_variant_axis` (tối đa 2 dropdown), block shop card, block review.
4. **Acceptance:** Buyer filter đúng theo thuộc tính động của từng category khác nhau (test với ít nhất 2 category khác ngành để đảm bảo không còn hard-code), trang chi tiết hiển thị đúng thông số + chọn biến thể đúng logic.
5. Cập nhật PROGRESS.md.

### PR NHÓM 9 — Module Dispute (theo thiết kế chi tiết Mục 4 file này)
1. Backend: tạo bảng `dispute`, `dispute_message` trong `order-service` theo đúng SQL Mục 4.2.
2. Backend: implement toàn bộ API Mục 4.3 (buyer), 4.4 (seller), 4.5 (admin).
3. Backend: tích hợp gọi sang `payout-service` khi resolve REFUND_BUYER/PARTIAL_REFUND theo Mục 4.6 — audit trước cấu trúc `payout-service` thật để biết cách điều chỉnh receivable/thêm bảng `payout_adjustment` nếu cần.
4. Gateway: thêm route `/api/v1/buyer/disputes/**`, `/api/v1/seller/disputes/**`, `/api/v1/admin/disputes/**` vào `application.yml`, cấu hình `AdminAuthorizationFilter` đúng role cho từng nhóm route.
5. FE Buyer: nút "Khiếu nại đơn hàng" tại `/don-mua-detail/:maHoaDon/:id`, form tạo dispute, trang xem danh sách + chi tiết + gửi message.
6. FE Seller: trang mới `/seller/disputes` trong `SellerSidebar.vue` mới (PR nhóm 4).
7. FE Admin: trang mới `/admin/disputes` trong nhóm menu "Đơn hàng".
8. **Acceptance:** Buyer tạo được dispute từ đơn đã DELIVERED, seller phản hồi được, admin xem/resolve/đóng dispute, số tiền hoàn được phản ánh đúng ở seller_receivable liên quan.
9. Cập nhật PROGRESS.md.

### PR NHÓM 10 — Module Report (theo thiết kế chi tiết Mục 5 file này)
1. Backend: tạo bảng `report` trong `seller-service` theo SQL Mục 5.2.
2. Backend: implement API Mục 5.3 (buyer/seller tạo report), 5.4 (admin xử lý).
3. Backend: với action `REVIEW_HIDDEN`, kiểm tra bảng `danh_gia` hiện có cột trạng thái ẩn/hiện chưa — nếu chưa, thêm cột `status` (mặc định `VISIBLE`, có thể set `HIDDEN`).
4. Gateway: thêm route `/api/v1/buyer/reports/**`, `/api/v1/seller/reports/**`, `/api/v1/admin/reports/**`.
5. FE: nút "Báo cáo vi phạm" trên trang sản phẩm/shop/dưới review (buyer-facing, có thể đặt trong dropdown menu "..." cạnh mỗi review).
6. FE Admin: trang mới `/admin/reports` trong nhóm menu "Sản phẩm & Nội dung".
7. **Acceptance:** Report tạo được từ 3 nguồn (product/shop/review), admin xử lý resolve đúng action tương ứng (delist product / suspend shop / hide review / gửi cảnh báo), trạng thái report cập nhật đúng.
8. Cập nhật PROGRESS.md.

### PR NHÓM 11 — Hoàn thiện Payout workflow đầy đủ + soldCount thật
1. Audit thật code `payout-service`: xác nhận khi `order_seller` chuyển status COMPLETE có tự động tạo `seller_receivable` không (gọi từ `order-service` sang `payout-service` qua event/internal API nào). Nếu chưa có, implement: `order-service` khi set `order_seller.order_status = COMPLETED` → gọi internal API `payout-service` tạo `seller_receivable` (gross_amount từ tổng `order_item`, commission_rate từ `commission_config` theo category, net_amount = gross - commission).
2. Implement cập nhật `seller_wallet.pending_amount` khi receivable tạo mới, chuyển sang `available_amount` sau chu kỳ đối soát (theo quy tắc thời gian đã có sẵn hoặc cần thêm cấu hình chu kỳ nếu chưa có).
3. Admin payout: đảm bảo UI duyệt payout batch, chuyển `available_amount` → `paid_amount`, tạo lịch sử thanh toán.
4. Fix `soldCount` hard-code 0 trong seller-service public shop response: implement tính thật bằng cách hoặc (a) query trực tiếp sang order-service qua internal API đếm `order_item` completed theo `seller_id`, hoặc (b) denormalize: order-service publish event khi order_seller COMPLETE, seller-service subscribe và tăng dần cột đếm sẵn trong bảng `seller` (khuyến nghị cách b để tránh gọi đồng bộ cross-service mỗi lần load trang shop — kiểm tra cơ chế event/outbox đã có `outbox` table ở catalog-service, có thể áp dụng pattern tương tự).
5. **Acceptance:** Sau khi 1 đơn hàng của seller chuyển COMPLETE, kiểm tra `seller_receivable` được tạo tự động với số tiền đúng, `seller_wallet.pending_amount` tăng đúng, trang shop hiển thị `soldCount` đúng số thật (không còn hard-code 0).
6. Cập nhật PROGRESS.md.
---

## 7. RÀNG BUỘC KỸ THUẬT XUYÊN SUỐT

- **Không viết lại từ đầu** entity/API/luồng nghiệp vụ đang hoạt động đúng (đã audit xác nhận DONE ở Mục 3). Chỉ sửa/thêm/xóa đúng phần cần thiết.
- **Không đổi glossary `sellerId`** — xem Mục 1.1. Mọi bảng/API mới đặt tên nhất quán với hệ thống cũ (VD `seller_id` không phải `shop_id`).
- **Attribute mô tả khác Variant axis** — không bao giờ nhầm 2 khái niệm này (Mục 26.3 audit). Thuộc tính mô tả (`product_attribute_*`) dùng để hiển thị thông số/filter, KHÔNG tạo SKU. Trục biến thể (`product_variant_axis*`) dùng để tạo tổ hợp SKU/giá/tồn kho, tối đa 2 trục.
- **Voucher sàn và voucher shop dùng chung 1 bảng `voucher`**, phân biệt bằng `seller_id` null hay không — không tách bảng.
- **Order gốc (`orders`) và sub-order (`order_seller`) khác nhau** — mọi thao tác vận hành (confirm/ship/complete/cancel) chỉ tác động `order_seller`, không sửa trực tiếp `orders` trừ khi là field tổng hợp (tổng tiền, trạng thái thanh toán tổng).
- **`/permitall` không có nghĩa là không cần user context** (Mục 26.6 audit) — khi thêm route mới, đặt tên rõ ràng: route thực sự public (không cần login) mới dùng `/permitall`, route cần buyer đã login thì đặt path buyer protected riêng (VD `/api/v1/buyer/...` như đã dùng cho module Dispute/Report mới ở Mục 4, 5).
- Trước khi xóa bất kỳ bảng/cột/route nào, kiểm tra ràng buộc FK và dữ liệu lịch sử thực tế bằng query trước — ưu tiên "ẩn/deprecate" trước, "xóa hẳn" sau khi chắc chắn an toàn và có xác nhận người dùng với các bảng chứa dữ liệu lịch sử (đặc biệt `orders` cột `staff_id/debt_amount/refund_amount` — Mục 1.6 bước 3).
- FE build/typecheck: nếu shell không có `node`/`npm`, ghi rõ vào PROGRESS.md là chưa verify được, KHÔNG báo DONE khi chưa chạy build/typecheck thành công thật sự.
- Mỗi PR nhóm ở Mục 6 phải có bước "Acceptance" được xác nhận (build pass, browser smoke test, hoặc ít nhất giải thích rõ vì sao chưa verify được) trước khi đánh dấu DONE trong PROGRESS.md.
- Nếu phát hiện dữ liệu/file bị mất bất thường ngoài phạm vi task đang làm — dừng lại, ghi chú, hỏi người dùng, không tự khôi phục hàng loạt (bài học từ sự cố phiên #28 — folder `docs` từng bị mất file tracked ngoài ý muốn).

---

## 8. FORMAT BẮT BUỘC CHO `docs/PROGRESS.md` MỖI CUỐI PHIÊN

Giữ nguyên format đang dùng hiện tại (không đổi cấu trúc file, chỉ tiếp tục ghi thêm entry mới):

```
### [YYYY-MM-DD HH:MM] Phien #N
**Da lam:**
- ...

**File da tao/sua:**
- ...

**Ket qua:** DONE / CHUA DONE (neu ro ly do neu chua xong)

**Ghi chu/vuong mac:**
- ...

**Viec tiep theo can lam ngay:**
- ...
```

Luôn cập nhật lại phần đầu file "Trạng thái tổng quan hiện tại", "Câu hỏi/quyết định cần người dùng xác nhận" (xóa câu hỏi đã được trả lời dứt điểm ở Mục 1 file này khỏi danh sách treo), và "Checklist tính năng" sau mỗi phiên.

---

## 9. PHỤ LỤC — THAM CHIẾU NHANH TÊN FILE/BẢNG/API THẬT (copy nguyên từ audit, dùng để tra cứu nhanh khi code)

### Frontend
```
Routes:          FE/src/constants/path.ts, FE/src/routes/router.ts
Sidebar:         FE/src/components/custom/Sidebar/AdminSidebar.vue
Navbar buyer:    FE/src/components/custom/layouts/NavBar.vue
API constants:   FE/src/constants/url.ts
Admin attributes: FE/src/pages/admin/product-attributes/ProductAttributes.vue
Seller registration: FE/src/pages/users/seller/SellerRegistration.vue
Shop detail:     FE/src/pages/users/seller/ShopDetail.vue
Seller products: FE/src/pages/seller/products/SellerProducts.vue
Seller orders:   FE/src/pages/seller/orders/SellerOrders.vue
Seller vouchers: FE/src/pages/seller/vouchers/SellerVouchers.vue
Seller payout:   FE/src/pages/seller/payout/SellerPayout.vue
Seller reviews:  FE/src/pages/seller/reviews/SellerReviews.vue
Admin seller approval: FE/src/pages/admin/seller/SellerApproval.vue
Admin payout:    FE/src/pages/admin/payout/AdminPayout.vue
Admin banners:   FE/src/pages/admin/banner/PlatformBanners.vue
Admin customer (đổi tên): FE/src/pages/admin/khachhang/KhachHang.vue
Admin staff (đổi tên):    FE/src/pages/admin/nhanvien/NhanVien.vue
```

### Backend
```
Gateway routes/security: backend-microservice/api-gateway/src/main/resources/application.yml
                          backend-microservice/api-gateway/src/main/java/com/ecommerce/gateway/security/AdminAuthorizationFilter.java
Auth JWT enrich:          backend-microservice/auth-service/.../security/TokenProvider.java
Seller service:           backend-microservice/seller-service/.../service/SellerService.java
Seller entity:            backend-microservice/seller-service/.../entity/Seller.java
Seller controller:        backend-microservice/seller-service/.../controller/SellerController.java
Catalog seller product:   backend-microservice/catalog-service/.../controller/SellerProductController.java
Catalog public:           backend-microservice/catalog-service/.../controller/PublicCatalogController.java
Admin attributes:         backend-microservice/catalog-service/.../controller/AdminProductAttributeController.java
Admin variant axes:       backend-microservice/catalog-service/.../controller/AdminVariantAxisController.java
Cart:                     backend-microservice/cart-service/.../controller/CartController.java
Seller orders:            backend-microservice/order-service/.../controller/SellerOrderController.java
Payout:                   backend-microservice/payout-service
Promotion/voucher:        backend-microservice/promotion-service
User/customer/staff:      backend-microservice/user-service
```

### Databases (đã xác nhận qua DBeaver — mỗi service 1 DB riêng)
```
ecommerce_auth
ecommerce_user       -> customer, staff
ecommerce_catalog    -> category, category_attribute_suggestion, product, product_attribute_definition,
                         product_attribute_moderation_audit, product_attribute_option, product_attribute_value,
                         product_image, product_variant, product_variant_axis, product_variant_axis_value,
                         product_variant_axis_value_mapping, variant_axis_name_suggestion, outbox
ecommerce_cart       -> cart, cart_detail
ecommerce_order      -> orders, order_seller, order_item, order_status_history, payment_history
                         [+ mới: dispute, dispute_message]
ecommerce_promotion  -> voucher, voucher_customer, promotion_campaign, promotion_campaign_product
ecommerce_seller     -> seller, seller_status_history, shop_follow, danh_gia, platform_banner
                         [+ mới: report]
ecommerce_payout     -> seller_wallet, seller_receivable, commission_config
                         [+ mới, nếu cần: payout_adjustment]
```

### Bảng cột chốt cần nhớ khi code (tránh nhầm tên field)
```
seller.owner_customer_id       -- ai sở hữu shop
seller.status                  -- DRAFT/PENDING_APPROVAL/APPROVED/REJECTED/SUSPENDED/CLOSED
product.seller_id              -- sản phẩm thuộc shop nào
product_variant.sale_price     -- giá bán (không phải product.price)
product_variant.quantity       -- tồn kho
product_attribute_definition.created_by_seller_id  -- null = system/admin tạo, có giá trị = seller tự tạo
product_attribute_definition.is_verified           -- đã hậu kiểm chưa
voucher.seller_id              -- null = voucher sàn, có giá trị = voucher shop
orders.staff_id / debt_amount / refund_amount       -- dấu vết POS cũ, xử lý ở Mục 1.6 bước 3
order_seller.order_status      -- trạng thái sub-order (khác order.order_status tổng)
cart_detail.seller_id / shop_name / seller_slug     -- đã có sẵn snapshot để group theo shop
```