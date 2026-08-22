# 00-audit-hien-trang.md

## 1. Phạm vi audit

Phiên này đọc lại source hiện tại trước khi thiết kế chuyển đổi marketplace. Các nguồn đã đối chiếu:

| Nhóm | File/thư mục đã đọc |
|---|---|
| Tổng quan backend | `backend-microservice/settings.gradle`, `backend-microservice/build.gradle`, `backend-microservice/README.md` |
| Cấu hình chạy | `RUN_PROJECT_LOCAL.md`, `backend-microservice/docker-compose.yml`, các `application.yml` của service |
| API backend | Toàn bộ `*Controller.java` trong các service, loại trừ `build/` |
| Data model | Toàn bộ entity có `@Entity`/`@Table`, loại trừ `build/` |
| Service dependency | Toàn bộ `*Client.java` OpenFeign, Kafka listener/config |
| Frontend | `FE/package.json`, `.env*`, `FE/src/routes/router.ts`, `FE/src/constants`, `FE/src/services`, cấu trúc `FE/src/pages` |
| Search/CDC | `backend-microservice/search-pipeline/README.md` và connector files |

Ghi chú trạng thái repo: `docs/PROGRESS.md` chưa tồn tại tại đầu phiên. Worktree hiện có sẵn thay đổi ngoài phạm vi tài liệu này: `Agents.md` và `README-Microservice.md` đang bị delete, `Prompt chuyen doi marketplace.md` đang modified/staged. Không chỉnh các thay đổi đó.

## 2. Kiến trúc hiện tại

Hệ thống hiện tại là e-commerce bán giày một cửa hàng, đang chạy theo microservice:

| Module | Vai trò hiện tại | Port local | Database |
|---|---|---:|---|
| `discovery-server` | Eureka registry | 8761 | Không |
| `api-gateway` | Gateway public, route path tới service, filter ADMIN JWT cho `/api/v1/admin/**` | 8080 | Không |
| `common-lib` | DTO response/exception/common util | N/A | Không |
| `auth-service` | Login/register/change password, JWT/OAuth2, gọi user-service để lấy user/staff | 8081 | Có datasource `ecommerce_auth`, chưa thấy entity |
| `user-service` | Khách hàng, nhân viên, profile | 8082 | `ecommerce_user` |
| `catalog-service` | Sản phẩm, biến thể, thuộc tính, tồn kho hiện tại trên `san_pham_chi_tiet.so_luong`, outbox search | 8083 | `ecommerce_catalog` |
| `promotion-service` | Voucher và đợt giảm giá | 8085 | `ecommerce_promotion` |
| `order-service` | Hóa đơn, checkout online, POS/bán hàng tại quầy, VNPay, thống kê | 8086 | `ecommerce_order` |
| `cart-service` | Giỏ hàng | 8087 | `ecommerce_cart` |
| `notification-service` | REST gửi email và Kafka consumer email | 8088 | Không |

Stack chính:

| Nhóm | Công nghệ |
|---|---|
| Backend | Java 17, Spring Boot 3.4.4, Spring Cloud 2024.0.1, Gradle multi-module |
| Service-to-service | OpenFeign qua Eureka |
| Database | MySQL 8.4, database riêng theo service |
| ORM | Spring Data JPA; riêng `order-service` vẫn có `JdbcTemplate` ở checkout/admin/order |
| Search | Elasticsearch, Kafka Connect, Debezium Outbox từ `catalog-service.outbox` |
| Messaging | Kafka; `notification-service` consume topic email |
| FE | Vue 3, TypeScript, Vite, Vue Router, Pinia, Axios, Ant Design Vue, Bootstrap |
| Monitoring/logging | Actuator, Prometheus, Grafana, Filebeat, Logstash, Kibana |

## 3. Route/API hiện tại

### 3.1 Gateway routes

| Gateway path | Target |
|---|---|
| `/api/v1/auth/**`, `/oauth2/**` | `auth-service` |
| `/api/v1/admin/khach-hang/**`, `/api/v1/admin/nhan-vien/**`, `/api/v1/permitall/profile/**` | `user-service` |
| `/api/v1/admin/mau-sac/**`, `/size/**`, `/thuong-hieu/**`, `/xuat-xu/**`, `/san-pham/**`, `/san-pham-chi-tiet/**`, `/chat-lieu/**`, `/danh-muc/**`, `/loai-de/**`, `/api/v1/permitall/san-pham/**`, `/api/v1/permitall/san-pham-chi-tiet/**`, `/api/v1/permitall/thuong-hieu/**`, `/api/catalog/user/products/**` | `catalog-service` |
| `/api/v1/admin/dot-giam-gia/**`, `/api/v1/admin/voucher/**` | `promotion-service` |
| `/api/v1/admin/ban-hang/**`, `/api/v1/admin/hoa-don/**`, `/api/v1/admin/thong-ke/**`, `/api/v1/permitall/don-mua/**`, `/api/orders/**` | `order-service` |
| `/api/v1/permitall/cart/**` | `cart-service` |
| `/api/v1/notifications/**` | `notification-service` |

### 3.2 Public/buyer APIs

| Service | Endpoint chính | Ghi nhận |
|---|---|---|
| `auth-service` | `POST /api/v1/auth/login`, `POST /login-admin`, `PUT /register`, `POST /change-password` | Chưa thấy `POST /api/v1/auth/refresh` trong controller, dù FE interceptor đang gọi |
| `catalog-service` | `/api/v1/permitall/san-pham/**`, `/api/v1/permitall/san-pham-chi-tiet/**`, `/api/catalog/user/products/search` | Product public chưa có `sellerId`, shop, rating |
| `cart-service` | `GET/POST /api/v1/permitall/cart`, `PUT /{id}` | Giỏ hàng phẳng theo user/product-detail, chưa nhóm shop |
| `order-service` | `POST /api/orders/create`, `GET /api/orders/vnpay-return`, `/api/orders/pgg`, `/api/v1/permitall/don-mua/**` | Checkout hiện tạo một hóa đơn, chưa split sub-order theo seller |
| `notification-service` | `POST /api/v1/notifications/email` | Có API gửi email trực tiếp |

### 3.3 Admin/POS APIs

| Service | Endpoint chính | Ghi nhận |
|---|---|---|
| `catalog-service` | `/api/v1/admin/san-pham/**`, `/san-pham-chi-tiet/**`, thuộc tính sản phẩm | Admin toàn sàn hiện quản lý toàn bộ sản phẩm, chưa phân quyền seller |
| `promotion-service` | `/api/v1/admin/voucher/**`, `/api/v1/admin/dot-giam-gia/**` | Voucher/đợt giảm giá chưa có seller scope |
| `order-service` | `/api/v1/admin/ban-hang/**`, `/api/v1/admin/hoa-don/**`, `/api/v1/admin/thong-ke/**` | Chứa POS/bán hàng tại quầy và hóa đơn offline, cần bỏ hẳn khi chuyển marketplace |
| `user-service` | `/api/v1/admin/khach-hang/**`, `/api/v1/admin/nhan-vien/**` | Chưa có seller/shop approval |

## 4. Bảng dữ liệu hiện tại

| Service | Entity | Table | Nhận xét marketplace |
|---|---|---|---|
| `user-service` | `KhachHang` | `khach_hang` | Buyer account hiện có; cần liên kết seller/shop sau khi được duyệt |
| `user-service` | `NhanVien` | `nhan_vien` | Platform admin/staff hiện có; chưa có role `SELLER` |
| `catalog-service` | `SanPham` | `san_pham` | Thiếu `seller_id`, shop snapshot/filter |
| `catalog-service` | `SanPhamChiTiet` | `san_pham_chi_tiet` | Thiếu `seller_id` hoặc suy ra từ `san_pham`; tồn kho nằm ở đây |
| `catalog-service` | `MauSac`, `KichCo`, `DanhMuc`, `ThuongHieu`, `LoaiDe`, `ChatLieu`, `XuatSu` | thuộc tính sản phẩm | `danh_muc` nên là danh mục chung toàn sàn; thuộc tính còn lại có thể giữ platform-managed |
| `catalog-service` | `OutboxEvent` | `outbox` | Dùng cho search sync; payload cần bổ sung seller/shop/rating khi marketplace |
| `promotion-service` | `PhieuGiamGia` | `phieu_giam_gia` | Thiếu `seller_id nullable`, scope voucher sàn/shop |
| `promotion-service` | `PhieuGiamGiaChiTiet` | `phieu_giam_gia_chi_tiet_khach_hang` | Liên kết customer bằng ID tham chiếu |
| `promotion-service` | `DotGiamGia`, `DotGiamGiaChiTietSanPham` | `dot_giam_gia`, `dot_giam_gia_chi_tiet_san_pham` | Thiếu seller scope; chi tiết đang tham chiếu product-detail ID |
| `cart-service` | `Cart`, `CartDetail` | `gio_hang`, `gio_hang_chi_tiet` | Thiếu seller grouping; `CartDetail` chỉ có `sanPhamChiTietId`, quantity, price |
| `order-service` | `HoaDon` | `hoa_don` | Hiện vừa online vừa POS/offline; chưa có order cha/con theo seller |
| `order-service` | `HoaDonChiTiet` | `hoa_don_chi_tiet` | Chi tiết hóa đơn chưa gắn seller/sub-order |
| `order-service` | `LichSuThanhToan` | `lich_su_thanh_toan` | Có thể tái sử dụng cho payment history |
| `order-service` | `LichSuTrangThaiHoaDon` | `lich_su_trang_thai_hoa_don` | Cần chuyển/nhân rộng sang lịch sử trạng thái sub-order |

## 5. Dependency hiện tại giữa service

| Caller | Callee | Giao thức | Mục đích |
|---|---|---|---|
| `auth-service` | `user-service` | OpenFeign `/internal/users` | Lookup/tạo customer, lookup staff, đổi mật khẩu |
| `user-service` | `order-service` | OpenFeign `/internal/orders` | Lấy lịch sử đơn theo customer |
| `catalog-service` | `promotion-service` | OpenFeign `/internal/promotions` | Lấy discount active cho sản phẩm |
| `promotion-service` | `catalog-service` | OpenFeign `/internal/catalog` | Lấy product/product-detail/color/size |
| `cart-service` | `catalog-service` | OpenFeign `/internal/catalog` | Lấy product-detail khi hiển thị/thêm giỏ |
| `order-service` | `catalog-service` | OpenFeign | Lấy product-detail, search product-detail, trừ tồn kho |
| `order-service` | `promotion-service` | OpenFeign | Voucher by code, voucher assigned/applicable, decrement voucher |
| `order-service` | `user-service` | OpenFeign | Lấy/tạo customer |
| `order-service` | `cart-service` | OpenFeign | Xóa item sau checkout |
| `notification-service` | Kafka/SMTP | Kafka listener + SMTP | Consume `email-notification`, gửi email |

## 6. Frontend hiện tại

| Khu vực | Route/component | Trạng thái |
|---|---|---|
| Storefront user | `trang-chu`, `san-pham`, `san-pham-chi-tiet/:idsp`, `gio-hang`, `thanh-toan`, `don-mua`, profile | Một cửa hàng, chưa có trang shop riêng, seller filter, review/follow |
| Admin | `/admin/*`, layout `FE/src/layout/Admin.vue` | Quản trị sản phẩm, thuộc tính, khách hàng, nhân viên, voucher, đợt giảm giá, hóa đơn, thống kê, POS |
| Auth | `/login`, `/register`, `/admin/login` | Pinia auth store + localStorage token |
| API client | `FE/src/services/request.ts`, `FE/src/constants/url.ts` | Base URL `VITE_BASE_URL_SERVER`, mặc định gateway 8080 |

Điểm cần sửa khi marketplace:

| Mục | Hiện tại | Cần chuyển |
|---|---|---|
| Trang chủ | Product của một shop | Product nhiều seller, shop nổi bật, banner platform |
| Product card | Không hiển thị shop | Hiển thị tên shop, link `/shop/:sellerSlug`, rating |
| Product detail | Không có shop block/review | Shop block, review sản phẩm/shop |
| Cart | Phẳng | Nhóm theo shop, chọn theo shop/item |
| Checkout | Một hóa đơn | Hiển thị nhiều shop, thanh toán một lần, tạo order cha và sub-order |
| Order history | Danh sách hóa đơn phẳng | Nhóm order cha, bên trong sub-order theo shop |
| Admin | Chỉ platform admin/POS | Tách Platform Admin và Seller Admin, bỏ POS |

## 7. Khoảng cách so với marketplace

| Domain marketplace | Trạng thái source hiện tại | Việc cần làm |
|---|---|---|
| Seller/shop onboarding | Chưa có service/entity/API/FE | Thêm `seller-service`, bảng seller, approval flow |
| Role SELLER/JWT sellerId | Chưa có | Mở rộng user/auth/gateway |
| Seller Admin | Chưa có | Tái sử dụng `Admin.vue`, thêm route seller và filter server-side theo JWT sellerId |
| Multi-seller catalog | Chưa có `seller_id` | Thêm seller ownership vào product/product-detail/search payload |
| Multi-seller cart | Chưa nhóm shop | Bổ sung seller info trong cart response, nhóm FE |
| Split-order/sub-order | Chưa có | Refactor `order-service` từ `hoa_don` đơn lẻ sang order cha/sub-order |
| Voucher sàn/shop | Chưa có scope | Thêm `seller_id nullable`, rule voucher 2 tầng |
| Payout/wallet | Chưa có | Thêm `payout-service` và bảng đối soát |
| Review/follow | Chưa có | Thêm bảng/API/FE |
| POS/offline invoice | Đang có nhiều controller/service/UI | Xóa khỏi roadmap marketplace |

## 8. Kết luận audit

Code hiện tại có thể tái sử dụng lớn ở các phần: auth cơ bản, user/customer, catalog/product CRUD, product-detail stock, voucher/discount core, cart core, checkout/VNPay, notification email, search outbox, gateway/discovery/monitoring. Phần phải thay đổi kiến trúc nhiều nhất là `order-service` vì hiện vừa chứa online order vừa chứa POS/offline invoice, trong khi marketplace cần order cha/sub-order theo seller và bỏ POS. Phần phải thêm mới là `seller-service`, `payout-service`, seller-facing UI, shop page, review/follow, commission/payout domain.
