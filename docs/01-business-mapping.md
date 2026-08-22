# 01-business-mapping.md

## 1. Mục tiêu nghiệp vụ marketplace

Chuyển hệ thống từ mô hình một cửa hàng bán giày online + POS sang marketplace nhiều gian hàng giống Shopee/Tiki:

| Trước đây | Sau chuyển đổi |
|---|---|
| Một chủ shop/platform bán toàn bộ sản phẩm | Nhiều seller độc lập đăng ký shop, platform vận hành sàn |
| Admin quản lý toàn bộ sản phẩm, voucher, hóa đơn | Tách Platform Admin và Seller Admin |
| Giỏ hàng/checkout/hóa đơn theo một cửa hàng | Giỏ hàng nhiều seller, checkout một lần, split thành sub-order theo seller |
| Voucher không phân sàn/shop | Voucher 2 tầng: voucher toàn sàn và voucher riêng shop |
| Không có shop profile, follow, review shop | Có trang shop riêng, follow shop, đánh giá sản phẩm/shop |
| Có POS/bán hàng tại quầy | Bỏ hẳn POS/offline invoice |

Nguyên tắc tái sử dụng: giữ lại code còn phù hợp như đăng nhập buyer/admin, catalog CRUD, cart core, checkout/VNPay, notification, search outbox. Chỉ thay đổi domain cần thiết để đạt marketplace.

## 2. Vai trò người dùng

| Vai trò | Mô tả | Nguồn xác thực |
|---|---|---|
| Buyer | Người mua hàng, có thể mua trước khi đăng ký seller | `khach_hang`, JWT role `USER` hoặc role hiện có tương đương |
| Seller | Buyer đã đăng ký shop và được platform duyệt | Vẫn cùng tài khoản login, JWT bổ sung role `SELLER` và claim `sellerId` |
| Platform Admin | Nhân viên/admin sàn, duyệt seller, quản lý danh mục/voucher sàn/đối soát | `nhan_vien`, JWT role `ADMIN` |

Một tài khoản buyer có thể nâng cấp thành seller. Không tạo hai tài khoản login tách biệt cho cùng một người. FE phải có entry "Kênh Người Bán" để user đổi context từ Buyer sang Seller Admin nếu shop đã được duyệt.

## 3. Seller onboarding và quản lý tài khoản seller

### 3.1 Trạng thái shop

| Trạng thái | Ý nghĩa | Ai chuyển | Điều kiện |
|---|---|---|---|
| `DRAFT` | Hồ sơ đang nhập, chưa nộp | Buyer/seller owner | Lưu nháp form nếu cần |
| `PENDING_APPROVAL` | Đã nộp, chờ sàn duyệt | Buyer/seller owner | Form đủ thông tin bắt buộc |
| `APPROVED` | Shop hoạt động, được đăng sản phẩm | Platform Admin | Hồ sơ hợp lệ |
| `REJECTED` | Bị từ chối, có lý do | Platform Admin | Hồ sơ thiếu/sai/không hợp lệ |
| `SUSPENDED` | Bị sàn khóa tạm thời | Platform Admin | Vi phạm chính sách, tranh chấp, rủi ro |
| `CLOSED` | Shop đóng vĩnh viễn/tự đóng | Seller owner hoặc Platform Admin | Không còn order đang xử lý hoặc xử lý theo chính sách đóng shop |

### 3.2 Form đăng ký shop

Trang mới: "Đăng ký bán hàng" hoặc "Trở thành Người bán".

| Field | Bắt buộc | Ghi chú |
|---|---|---|
| `shopName` | Có | Tên hiển thị, unique theo seller-service |
| `sellerSlug` | Có | Sinh từ tên shop, unique, dùng URL `/shop/:sellerSlug` |
| `description` | Có | Mô tả shop |
| `logoUrl` | Có thể bắt buộc ở phase sau | Có thể dùng upload/cloudinary hiện có |
| `coverImageUrl` | Không bắt buộc phase đầu | Ảnh bìa shop |
| `pickupAddress` | Có | Địa chỉ lấy hàng |
| `contactPhone` | Có | Số liên hệ shop |
| `identityType` | Có | `CCCD`, `CMND`, `TAX_CODE`, mock ở phase đầu |
| `identityNumber` | Có | Mã định danh |
| `bankName`, `bankAccountNo`, `bankAccountHolder` | Có | Tài khoản nhận đối soát |
| `mainCategoryId` | Có | Ngành/danh mục chính |

### 3.3 Luồng đăng ký shop

1. Buyer đã đăng nhập vào storefront.
2. Buyer mở trang "Đăng ký bán hàng".
3. FE hiển thị form shop, không cho nhập `sellerId`.
4. FE gửi hồ sơ tới `seller-service`.
5. `seller-service` lấy `ownerCustomerId/userId` từ JWT hoặc internal user context, tạo row `seller` trạng thái `PENDING_APPROVAL`.
6. Seller chưa đăng được sản phẩm, chỉ thấy màn hình chờ duyệt.
7. Platform Admin mở màn "Duyệt Seller", xem danh sách `PENDING_APPROVAL`.
8. Admin duyệt: chuyển `APPROVED`, ghi `approvedBy`, `approvedAt`, phát notification.
9. Admin từ chối: chuyển `REJECTED`, lưu `rejectionReason`, seller có thể sửa hồ sơ và nộp lại.
10. Khi `APPROVED`, `auth-service`/`user-service` phải trả role `SELLER` và claim `sellerId` trong JWT ở lần login/refresh tiếp theo.

### 3.4 Chuyển vai trò buyer/seller

| Ngữ cảnh FE | Điều kiện | Dữ liệu dùng |
|---|---|---|
| Buyer storefront | Mọi user, kể cả seller | User token thường |
| Seller Admin | JWT có role `SELLER`, seller status `APPROVED` | `sellerId` từ JWT/server-side |
| Platform Admin | JWT role `ADMIN` | Admin filter/gateway hiện có, cần giữ |

Seller Admin API tuyệt đối không nhận `sellerId` từ client. Service lấy `sellerId` từ JWT claim hoặc header nội bộ do gateway set sau khi verify token.

## 4. Seller Admin - Kênh Người Bán

Tái sử dụng layout `FE/src/layout/Admin.vue` nhưng tách menu và route seller, ví dụ `/seller/*`. Dữ liệu luôn filter theo `sellerId` server-side.

| Màn hình | Chức năng chi tiết | Backend liên quan |
|---|---|---|
| Dashboard seller | Doanh thu hôm nay/tuần/tháng, số đơn chờ xử lý, đang giao, sản phẩm sắp hết hàng, biểu đồ doanh thu | `order-service`, `catalog-service`, sau này `payout-service` |
| Quản lý sản phẩm | CRUD `san_pham`/`san_pham_chi_tiet` của shop mình; chọn danh mục trước, dùng thuộc tính gợi ý hoặc tự tạo thuộc tính động; không thấy/sửa sản phẩm seller khác | `catalog-service`, thêm seller filter và dynamic attributes |
| Quản lý đơn hàng | Danh sách sub-order của shop, trạng thái riêng: Chờ xác nhận -> Đã xác nhận/Đang đóng gói -> Đang giao -> Hoàn thành/Đã hủy | `order-service` |
| Voucher shop | Tạo voucher chỉ áp dụng sản phẩm của shop mình | `promotion-service`, `seller_id` bắt buộc với seller voucher |
| Đợt giảm giá shop | Tạo campaign discount cho product-detail thuộc shop mình | `promotion-service` + `catalog-service` validate ownership |
| Ví & đối soát | Số dư chờ đối soát, kỳ đã nhận, doanh thu gộp, hoa hồng sàn, tiền thực nhận | `payout-service` |
| Hồ sơ shop | Sửa logo, cover, mô tả, địa chỉ lấy hàng, cấu hình vận chuyển cơ bản | `seller-service` |
| Đánh giá & phản hồi | Xem review sản phẩm/shop, seller reply | `review` domain trong `seller-service` hoặc module mới tùy phase |
| Thống kê seller | Top sản phẩm, tỉ lệ hủy, doanh thu theo danh mục chỉ trong shop | `order-service`/`catalog-service` filter seller |

Quy tắc bảo mật seller:

| Quy tắc | Áp dụng |
|---|---|
| Không tin `sellerId` client gửi | Tất cả API `/api/v1/seller/**` |
| Validate ownership khi thao tác product/order/voucher | `catalog-service`, `order-service`, `promotion-service` |
| Không cho seller chuyển trạng thái shop sang `APPROVED`/`SUSPENDED` | Chỉ Platform Admin |
| Seller `SUSPENDED/CLOSED` không được tạo/sửa sản phẩm, voucher, xác nhận đơn mới | Gateway/service guard |

Quyết định thuộc tính động đã chốt ngày 2026-08-22:

| Chính sách | Quyết định |
|---|---|
| Phạm vi thuộc tính seller tự tạo | Thuộc tính `PENDING` chỉ được gợi ý/tái sử dụng trong chính shop tạo. Seller khác chỉ thấy sau khi Platform Admin chuẩn hóa thành `STANDARDIZED` và gắn vào danh mục |
| Giới hạn | Tối đa 50 thuộc tính động trên một sản phẩm; FE cảnh báo và backend bắt buộc validate |
| Kiểu dữ liệu | Seller được chọn `TEXT`, `NUMBER`, `SINGLE_SELECT`, `MULTI_SELECT`; dropdown phải có danh sách option hợp lệ |
| Hậu kiểm | Seller dùng thuộc tính mới ngay, không chờ duyệt. Admin chuẩn hóa, gộp hoặc ẩn sau |

## 5. Buyer storefront

### 5.1 Trang chủ

| Hiện tại | Marketplace |
|---|---|
| Danh sách sản phẩm một cửa hàng | Gộp sản phẩm nhiều seller |
| Không có shop nổi bật | Thêm "Shop nổi bật" |
| Banner tĩnh hoặc chưa rõ platform config | Banner do Platform Admin quản lý |

Trang chủ mới cần các block:

| Block | Dữ liệu |
|---|---|
| Banner | `platform_banner` từ `seller-service` hoặc platform config module |
| Gợi ý cho bạn | Product public có seller info, sort theo relevance/sold/rating |
| Shop nổi bật | Seller `APPROVED`, rating/follower/sales cao |
| Sản phẩm giảm giá | Discount active từ `promotion-service`, gồm seller/shop info |

### 5.2 Danh sách sản phẩm/tìm kiếm

Mở rộng filter hiện tại:

| Filter/sort | Nguồn dữ liệu |
|---|---|
| Thuộc tính theo danh mục | Lấy động từ `danh_muc_thuoc_tinh`; không cố định Màu/Size cho mọi ngành hàng |
| Giá | `catalog-service`/Elasticsearch |
| Shop | `seller-service` |
| Đánh giá sao | Review aggregate |
| Bán chạy nhất | Order aggregate theo product/seller |
| Tên shop trên card | `sellerId` + seller snapshot/lookup |

Elasticsearch payload cần có: `sellerId`, `sellerName`, `sellerSlug`, `sellerLogoUrl`, `ratingAverage`, `soldCount` và mảng nested `attributes` gồm `attributeId`, type và typed value. Thuộc tính `PENDING` vẫn được index để tìm product của shop nhưng chỉ các thuộc tính được gắn filter cho danh mục mới tạo facet công khai.

### 5.3 Trang shop riêng

Route mới: `/shop/:sellerSlug`.

| Thành phần | Nội dung |
|---|---|
| Header shop | Logo, cover, tên shop, mô tả ngắn |
| Chỉ số | Số sản phẩm, rating trung bình, lượt theo dõi, ngày tham gia |
| Hành động | Theo dõi Shop, vào danh sách sản phẩm shop |
| Product list | Chỉ product của `sellerId` tương ứng slug |
| Voucher shop | Voucher active của shop nếu có |

### 5.4 Trang chi tiết sản phẩm

Bổ sung:

| Block | Nội dung |
|---|---|
| Shop seller | Tên shop, avatar, rating, nút "Xem shop", nút "Theo dõi" |
| Review sản phẩm | Sao, bình luận, ảnh từ buyer đã mua |
| Chính sách shop | Địa chỉ lấy hàng/vận chuyển cơ bản nếu cần |
| Thuộc tính sản phẩm | Hiển thị toàn bộ thuộc tính động còn visible, gồm thuộc tính chuẩn và thuộc tính riêng shop dùng trên sản phẩm đó |

### 5.5 Giỏ hàng

Giỏ hàng phải nhóm theo shop:

```text
Shop A
  - Product 1
  - Product 2
Shop B
  - Product 3
```

Quy tắc:

| Quy tắc | Mô tả |
|---|---|
| Chọn shop | Checkbox shop chọn/bỏ tất cả item của shop |
| Chọn item | Checkbox item độc lập |
| Tổng tiền | Tính theo item đã chọn, grouped subtotal theo shop |
| Voucher | Chọn voucher shop ở checkout, không áp dụng trực tiếp sai scope trong cart |

### 5.6 Checkout marketplace

Checkout vẫn thanh toán một lần:

1. FE gửi selected cart items.
2. Backend group item theo `sellerId`.
3. Tạo một order cha buyer nhìn thấy.
4. Tạo nhiều sub-order, mỗi sub-order thuộc một seller.
5. Áp voucher shop ở từng sub-order.
6. Áp voucher toàn sàn ở order cha.
7. Tạo một payment/VNPay request tổng tiền cuối.
8. Khi payment success, order cha cập nhật paid, sub-order chuyển chờ seller xác nhận.

### 5.7 Lịch sử đơn mua

| Hiện tại | Marketplace |
|---|---|
| Một danh sách hóa đơn phẳng | Nhóm theo order cha |
| Một trạng thái đơn | Mỗi sub-order theo shop có trạng thái riêng |
| Không review sau nhận | Buyer review product/shop sau khi sub-order hoàn thành |

## 6. Platform Admin

Platform Admin là admin sàn, không phải seller.

| Màn hình | Chức năng |
|---|---|
| Duyệt Seller | Danh sách chờ duyệt, xem hồ sơ, duyệt/từ chối, khóa/mở shop |
| Danh mục chung | Quản lý `danh_muc` toàn sàn, seller chỉ chọn |
| Cấu hình hoa hồng | % hoa hồng theo danh mục/ngành hàng |
| Voucher toàn sàn | Voucher `seller_id = null`, áp dụng toàn platform |
| Banner trang chủ | CRUD banner storefront |
| Khiếu nại/tranh chấp | Buyer-seller dispute, admin can thiệp |
| Thống kê toàn sàn | GMV, doanh thu, top seller, top product |
| Đối soát tổng | Xem/duyệt kỳ payout trước khi chi trả seller |
| Quản lý thuộc tính | Xem usage toàn sàn, chuẩn hóa và gắn danh mục, gộp trùng, ẩn vi phạm; thuộc tính `PENDING` của seller khác không được gợi ý chéo trước khi chuẩn hóa |

## 7. Checklist tính năng marketplace và mapping phase

| Tính năng | Phase | Ghi chú |
|---|---|---|
| Đăng ký/đăng nhập buyer, seller, platform admin | Phase 1 | Buyer/admin đang có; thêm SELLER/JWT sellerId |
| Đăng ký & duyệt shop | Phase 1 | Thêm `seller-service` + FE onboarding/admin approval |
| Trang chủ storefront nhiều shop | Phase 4 | Cần seller info trong catalog/search trước |
| Trang riêng từng shop | Phase 4 | Route `/shop/:sellerSlug` |
| Tìm kiếm/filter sản phẩm toàn sàn | Phase 3 | Mở rộng Elasticsearch payload |
| Giỏ hàng multi-seller | Phase 2 | Group theo shop |
| Checkout tách theo shop, thanh toán một lần | Phase 2 | Tạo order cha/sub-order |
| Split-order thành sub-order theo seller | Phase 2 | Cốt lõi `order-service` |
| Quản lý đơn hàng riêng theo seller | Phase 3 | Seller Admin order |
| Voucher 2 tầng | Phase 3 | Seller voucher + platform voucher |
| Đánh giá sản phẩm & shop sau nhận hàng | Phase 5 | Review domain |
| Theo dõi shop | Phase 5 | Follow shop |
| Ví & đối soát seller | Phase 3 | `payout-service` |
| Thống kê seller + toàn sàn | Phase 4 | Reuse `thong-ke`, bổ sung seller/platform scope |
| Duyệt/khóa seller bởi Platform Admin | Phase 1 | Seller status lifecycle |
| Thông báo order status/seller approval | Phase 5 | Reuse `notification-service` |
| Chat buyer-seller | Phase 6 optional | Không làm phase đầu |
| Flash sale toàn sàn | Phase 6 optional | Không làm phase đầu |
| Seller tự tạo thuộc tính động | Phase 2 | Riêng shop khi `PENDING`, tối đa 50/product, hỗ trợ 4 kiểu dữ liệu |
| Buyer filter/chi tiết thuộc tính động | Phase 4 | Filter theo category và nested Elasticsearch |
| Admin hậu kiểm thuộc tính | Phase 4 | Chuẩn hóa/gộp/ẩn và reindex product ảnh hưởng |

## 8. Những phần phải bỏ

Marketplace chuẩn không có POS/offline invoice. Các phần sau phải được loại khỏi roadmap code:

| Khu vực | Thành phần cần bỏ |
|---|---|
| Backend `order-service` | `BanHangController` và service/model chỉ phục vụ `/api/v1/admin/ban-hang/**` |
| Backend `order-service` | Các API hóa đơn offline/POS nếu không còn phục vụ order online/sub-order |
| FE Admin | `FE/src/pages/admin/banhang/BanHang.vue`, route/menu bán hàng tại quầy |
| FE Admin | Các màn hóa đơn offline không thể map sang marketplace order/sub-order |

Không xóa ngay trong phiên tài liệu hóa. Việc xóa sẽ nằm ở Phase 2 khi refactor order domain để tránh phá flow online đang dùng.
