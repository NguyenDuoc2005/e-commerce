# 05-roadmap.md

## 1. Nguyên tắc triển khai

| Nguyên tắc | Chi tiết |
|---|---|
| Không code trước tài liệu | Bộ docs này là baseline; phiên code sau đọc `docs/PROGRESS.md` và file liên quan |
| Từng phase nhỏ | Mỗi phase có build/smoke test riêng |
| Không phá buyer checkout trước khi có flow thay thế | Giữ endpoint online tới khi split-order chạy |
| Seller isolation trước UI đẹp | Server-side filter/ownership là bắt buộc |
| Bỏ POS có kiểm soát | Xóa route/UI POS khi order marketplace đã thay thế |
| Thuộc tính động là phần mở rộng xuyên phase | Data/Seller flow ở Phase 2, Buyer/Admin search ở Phase 4, audit/regression ở Phase 5; không tạo phase rời |
| Mục 10.6 đã chốt | `PENDING` riêng shop tạo, tối đa 50 thuộc tính động/product, seller chọn đủ `TEXT`, `NUMBER`, `SINGLE_SELECT`, `MULTI_SELECT` |
| Migration phải hoàn tác được | Backup + đối soát trước/sau + script down; chưa drop field giày cũ ở migration đầu |

## 2. Phase 0 - Hoàn tất tài liệu hóa

| Task | Kết quả |
|---|---|
| Audit source hiện tại | `docs/00-audit-hien-trang.md` |
| Mapping nghiệp vụ marketplace | `docs/01-business-mapping.md` |
| Kiến trúc mới | `docs/02-kien-truc-moi.md` |
| ERD/database | `docs/03-erd-database.md` |
| Sequence/business flow | `docs/04-luong-nghiep-vu.md` |
| Roadmap | `docs/05-roadmap.md` |
| Architecture tổng hợp | `docs/ARCHITECTURE.md` |
| Progress log | `docs/PROGRESS.md` |

Định nghĩa xong: đủ file, không còn mục trống/TODO, Mermaid hợp lệ về cú pháp cơ bản.

## 3. Phase 1 - Seller onboarding, role và bảo mật nền

Mục tiêu: có seller-service, đăng ký shop, admin duyệt, JWT có seller role/claim, route guard seller.

| Task | File/service dự kiến | Acceptance |
|---|---|---|
| Tạo `seller-service` Gradle module | `settings.gradle`, `seller-service/**`, Dockerfile, compose | Service start, register Eureka |
| Schema seller | `seller`, `seller_status_history`, `shop_follow` cơ bản | JPA entity/repository đúng database riêng |
| API đăng ký shop | `POST /api/v1/sellers/register-shop` hoặc `/api/v1/permitall/sellers/register-shop` có auth | Lấy customer từ JWT/header, tạo `PENDING_APPROVAL` |
| API admin duyệt/từ chối | `/api/v1/admin/sellers/**` | ADMIN mới duyệt được, ghi history |
| Mở rộng auth/user | `auth-service`, `user-service` | Login buyer approved seller trả role `SELLER`, `sellerId` |
| Gateway seller filter | `api-gateway` | `/api/v1/seller/**` yêu cầu SELLER approved |
| FE onboarding | `FE/src/pages/users/seller-registration`, route mới | Buyer nộp hồ sơ, xem pending/rejected |
| FE admin duyệt seller | `FE/src/pages/admin/sellers` | Admin xem/dueyet/từ chối |

Rủi ro cần xử lý trong Phase 1:

| Rủi ro | Cách xử lý |
|---|---|
| FE đang gọi `/api/v1/auth/refresh` nhưng backend chưa có | Bổ sung endpoint refresh hoặc sửa FE interceptor |
| Role hiện tại chỉ `ADMIN/USERS` | Chuẩn hóa roles array hoặc tương thích role string cũ |

## 4. Phase 2 - Catalog seller ownership, cart multi-seller, split checkout

Mục tiêu: sản phẩm thuộc seller, cart nhóm shop, checkout tạo order cha/sub-order và thanh toán một lần.

| Task | Service | Acceptance |
|---|---|---|
| Backfill/default seller cho dữ liệu cũ | `seller-service`, `catalog-service` | Product cũ có seller default approved |
| Thêm `seller_id` vào product/detail | `catalog-service` | Seller CRUD chỉ thao tác product của mình |
| Seller Product APIs | `catalog-service` `/api/v1/seller/products/**` | Không nhận sellerId từ client |
| Cart item seller snapshot | `cart-service` | `GET cart` trả grouped by shop |
| FE cart grouped UI | `FE/src/pages/users/cart` | Chọn shop/item, subtotal theo shop |
| Order parent/sub-order schema | `order-service` | Có `don_hang`, `don_hang_seller`, `don_hang_chi_tiet` |
| Checkout split by seller | `order-service` | Một payment, nhiều sub-order |
| VNPay return update split order | `order-service` | Payment success chuyển sub-order chờ xác nhận |
| Xóa/ẩn POS | `order-service`, `FE/src/pages/admin/banhang`, router/menu | Không còn luồng bán hàng tại quầy trong UI marketplace |
| Chốt 3 policy thuộc tính động | Docs/decision record | DONE 2026-08-22: riêng shop khi `PENDING`, tối đa 50/product, hỗ trợ 4 kiểu dữ liệu |
| Schema thuộc tính động | `catalog-service`, `ecommerce_catalog` | Có definition, dropdown option, category suggestion M:N, product actual value và constraint theo `docs/03-erd-database.md` |
| Migration dữ liệu giày có thể revert | `catalog-service` migration scripts + seed | Backup và map đủ Thương hiệu/Xuất xứ/Chất liệu/Loại đế của production/demo/test; count/checksum pass; script down phục hồi được |
| Seller attribute API/autocomplete | `catalog-service` `/api/v1/seller/**` | Gợi ý theo category, tìm tên gần trùng, tạo tự do không tiền kiểm, ownership lấy server-side |
| Seller Product form động | `FE/src/pages/seller/products` | Chọn category trước, thuộc tính gợi ý, `+ Thêm thuộc tính`, validate type/limit theo policy và confirm khi đổi category |

## 5. Phase 3 - Seller order, voucher 2 tầng, payout nền

Mục tiêu: seller xử lý order riêng, voucher shop/platform rõ scope, payout-service có dữ liệu đối soát.

| Task | Service | Acceptance |
|---|---|---|
| Seller order APIs | `order-service` `/api/v1/seller/orders/**` | Seller chỉ thấy sub-order của mình |
| Seller order FE | `FE/src/pages/seller/orders` | Xác nhận/đóng gói/giao/hủy |
| Voucher scope | `promotion-service` | `seller_id null` platform, `seller_id not null` shop |
| Seller voucher FE/API | `promotion-service`, FE seller | Seller tạo voucher shop |
| Shop promotion FE/API | `promotion-service` | Campaign chỉ chọn product seller sở hữu |
| Tạo `payout-service` | new module | Wallet/settlement entity + Eureka |
| Commission config | `payout-service` admin API | Admin cấu hình % theo category |
| Generate pending receivable | `order-service` -> `payout-service` | Sub-order hoàn thành tạo số tiền chờ đối soát |

## 6. Phase 4 - Storefront marketplace hoàn chỉnh và thống kê

Mục tiêu: buyer thấy marketplace nhiều shop, search/filter tốt, shop page, seller/platform dashboard.

| Task | Service/FE | Acceptance |
|---|---|---|
| Search payload seller fields | `catalog-service`, outbox, ES mapping | Search trả sellerName/slug/rating/sold |
| Filter by shop/rating/sold sort | `catalog-service` + FE products | User filter/sort được |
| Trang shop riêng | `seller-service`, `catalog-service`, FE `/shop/:slug` | Logo/cover/stats/product list |
| Trang chủ nhiều shop | FE home + APIs | Gợi ý, shop nổi bật, banner |
| Platform banner | `seller-service` hoặc config module | Admin CRUD banner, buyer xem |
| Seller dashboard | `order-service`, `catalog-service`, FE seller | Revenue/order/low stock |
| Platform statistics | `order-service`, `payout-service`, FE admin | GMV, top seller/product |
| Dynamic attribute search document | `catalog-service`, outbox, Elasticsearch | Nested key-value typed mapping; reindex đúng sau create/update/merge/hide |
| Dynamic category filters | `catalog-service`, FE products | Filter đổi theo category, không hard-code Size/Màu; query không match chéo attribute |
| Dynamic product details | `catalog-service`, FE product detail | Hiển thị đủ thuộc tính gợi ý và custom còn visible |
| Admin quản lý thuộc tính | `catalog-service`, FE admin | Xem usage, chuẩn hóa/gắn category, merge duplicate, soft-hide vi phạm; không chặn seller tạo product |

## 7. Phase 5 - Review, follow, notification hoàn chỉnh

Mục tiêu: có review sau mua, follow shop, notification nghiệp vụ.

| Task | Service/FE | Acceptance |
|---|---|---|
| Review product/shop schema/API | `seller-service` hoặc review module | Buyer chỉ review sub-order hoàn thành |
| Seller reply review | Seller FE/API | Seller chỉ reply review shop mình |
| Rating aggregate | `catalog-service`, `seller-service` | Product/shop rating cập nhật |
| Follow shop | `seller-service`, FE | Follow/unfollow, count hiển thị |
| Notification events | `notification-service`, producers | Seller approved/order status/settlement paid email |
| Order history grouped UI | FE `don-mua` | Order cha + sub-order + review action |
| Attribute moderation audit | `catalog-service` | Lưu admin actor/reason/source-target/affected counts cho normalize/merge/hide |
| Dynamic attribute regression | backend + FE tests | Seller isolation, migration up/down, ES nested filters, cart/order compatibility và desktop/mobile flow đều pass |

## 8. Phase 6 optional - Chat và flash sale

| Task | Ghi chú |
|---|---|
| Chat buyer-seller | Có thể dùng WebSocket/STOMP sau khi domain chính ổn |
| Flash sale toàn sàn | Platform Admin tổ chức, cần inventory locking kỹ hơn |

## 9. Checklist prompt 3.5 - mỗi tính năng đúng một phase

| Tính năng | Phase |
|---|---|
| Đăng ký/đăng nhập buyer, seller, platform admin | Phase 1 |
| Đăng ký & duyệt shop | Phase 1 |
| Trang chủ storefront gộp nhiều shop | Phase 4 |
| Trang riêng từng shop | Phase 4 |
| Tìm kiếm/filter sản phẩm toàn sàn | Phase 4 |
| Giỏ hàng multi-seller | Phase 2 |
| Checkout tách theo shop, thanh toán một lần | Phase 2 |
| Split-order thành sub-order theo seller | Phase 2 |
| Quản lý đơn hàng riêng theo từng seller | Phase 3 |
| Voucher 2 tầng | Phase 3 |
| Đánh giá sản phẩm & shop sau khi nhận hàng | Phase 5 |
| Theo dõi shop | Phase 5 |
| Ví & đối soát cho seller | Phase 3 |
| Thống kê riêng theo seller + thống kê tổng toàn sàn | Phase 4 |
| Duyệt/khóa seller bởi Platform Admin | Phase 1 |
| Thông báo qua `notification-service` | Phase 5 |
| Chat buyer-seller | Phase 6 optional |
| Flash sale toàn sàn | Phase 6 optional |
| Schema + migration thuộc tính động | Phase 2 |
| Seller tự thêm/autocomplete thuộc tính khi tạo sản phẩm | Phase 2 |
| Buyer filter/chi tiết theo thuộc tính động | Phase 4 |
| Platform Admin hậu kiểm/chuẩn hóa/gộp thuộc tính | Phase 4 |
| Audit và regression thuộc tính động | Phase 5 |

## 10. Lệnh verify đề xuất sau từng phase code

```powershell
cd "C:\My Project\e-commerce\backend-microservice"
.\gradlew.bat clean build --no-daemon
```

```powershell
cd "C:\My Project\e-commerce\FE"
npm run build
```

Runtime smoke sau khi service chạy:

| Flow | Smoke test |
|---|---|
| Seller onboarding | Buyer register shop -> admin approve -> login token has sellerId |
| Seller product | Seller create product -> seller list sees it -> other seller cannot update |
| Cart/checkout | Add products from 2 shops -> cart grouped -> checkout creates 1 parent + 2 sub-orders |
| Voucher | Shop voucher applies only own shop; platform voucher applies order parent |
| Payout | Completed sub-order creates pending receivable |
| Seller dynamic attribute | Chọn category -> dùng suggestion -> tạo custom attribute -> lưu product -> mở lại không mất value |
| Category change | Nhập attribute -> đổi category -> có confirm -> cancel giữ dữ liệu, confirm tải suggestion mới |
| Admin moderation | Normalize/merge/hide -> MySQL values nhất quán -> affected products được reindex |
| Buyer dynamic filter | Hai category có bộ filter khác nhau -> filter nested trả đúng product -> detail hiện đủ attributes |
| Migration | Backup -> up -> đối soát field giày/demo/test -> down -> đối soát phục hồi |
