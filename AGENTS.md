# Repository Guidelines

## Phạm vi và cấu trúc

- `FE/` là Vue 3 + TypeScript + Vite; page/layout ở `src/pages`, `src/layout`, component dùng lại ở `src/components`, API client ở `src/services/api`.
- `backend-microservice/` là Gradle multi-module Spring Boot: `api-gateway`, `discovery-server`, `auth-service`, `user-service`, `catalog-service`, `promotion-service`, `cart-service`, `order-service`, `notification-service`, `seller-service`, `payout-service`, và `common-lib`.
- Mỗi service sở hữu database/schema riêng; không đặt JPA entity nghiệp vụ vào `common-lib`. `order-service` còn dùng `JdbcTemplate` ở các luồng checkout/admin/order.
- Backend dùng Java 17, Spring Boot 3.4.4, Spring Cloud 2024.0.1, Eureka và OpenFeign. `catalog-service` dùng Elasticsearch; Kafka dùng cho outbox/order/notification/payout.

## Build, test và chạy local

Chỉ chạy các lệnh dưới đây khi đã có hạ tầng local theo `RUN_PROJECT_LOCAL.md`:

```powershell
cd backend-microservice
.\gradlew.bat clean build --no-daemon
.\gradlew.bat test --no-daemon
.\gradlew.bat :<service>:test --no-daemon
.\gradlew.bat :<service>:bootRun
```

`<service>` là một module backend (ví dụ `:order-service`); `common-lib` không có `bootRun`, dùng `:common-lib:jar` hoặc `:common-lib:test`. Root task `startLocalBackend` build `bootJar` rồi gọi runner; cách chạy cả stack được mô tả trong `RUN_PROJECT_LOCAL.md`.

Các service Spring Boot đều có cùng lệnh test/bootRun theo module: `api-gateway`, `discovery-server`, `auth-service`, `user-service`, `catalog-service`, `promotion-service`, `cart-service`, `order-service`, `notification-service`, `seller-service`, `payout-service`. Test dùng JUnit 5/JUnit Platform qua `spring-boot-starter-test`; source hiện dùng Mockito và test contract, chưa thấy Testcontainers.

Frontend chạy từ `FE/`:

```powershell
npm install
npm run dev
npm run build
npm run build:stage
```

`FE/package.json` không khai báo script test/lint riêng. Backend không có plugin Checkstyle/Spotless/PMD/formatter/Jacoco trong các Gradle script đã đọc; giữ format theo code lân cận (Java 4 spaces, frontend theo `FE/.editorconfig`: 2 spaces, LF, 100 ký tự).

## Quy ước code và kiến trúc cần giữ

- Request/response backend thường nằm trong package `model.request`/`model.response` hoặc `dto.request`; entity, repository, service và controller tách package theo service. Chưa thấy convention `Mapper` dùng chung; không tự đưa mapper/entity sang `common-lib`.
- Giao tiếp service-to-service dùng OpenFeign qua Eureka service name. Feign nội bộ phải gửi `X-Internal-Service-Token` qua `InternalServiceTokenInterceptor`; endpoint `/internal/**` được `TrustedRequestFilter` bảo vệ.
- `api-gateway` kiểm tra JWT `ACCESS`, role `ADMIN`/`SELLER`/`USERS`, seller active; sau đó loại header giả từ client và phát `X-Gateway-Token`, `X-User-Id`, `X-Seller-Id`. Downstream không được tin identity header nếu thiếu gateway credential.
- `catalog-service` dùng `OutboxEvent`/`OutboxEventRepository`; các thay đổi product/variant phải enqueue event tương ứng (`ProductCreated`, `ProductUpdated`, `ProductDeleted`). `order-service` dùng `OrderOutboxService` + `OrderOutboxPublisher`, trạng thái `PENDING/PUBLISHED/FAILED` và publish Kafka.
- Kafka consumer phải giữ idempotency, xử lý retry/DLT theo cấu hình hiện có và không coi việc gửi email/payout là transaction đồng bộ với database. Topic/group phải khớp consumer hiện hữu.
- Database-per-service là boundary: gọi API nội bộ qua Feign, không query database của service khác. Snapshot catalog và các contract `common-lib` là dữ liệu trao đổi được phép.

## Review guidelines

### P0 — chặn merge

- Không bỏ qua `TrustedRequestFilter`, không cho client tự quyết `X-User-Id`/`X-Seller-Id`, và không mở `/internal/**` hoặc route admin/seller/buyer mà thiếu credential/role; kiểm tra `AdminAuthorizationFilter`, `SecurityConfig` và `common-lib/security`.
- Không để refresh token đi qua marketplace route: gateway phải yêu cầu JWT `tokenType=ACCESS` và role đúng; seller route phải kiểm tra `sellerId` và seller active.
- Không dùng `double`/`float` cho amount, commission, discount, shipping, refund hoặc payout. Các luồng `CheckoutServiceImpl`, `PromotionServiceImpl`, `FlashSaleService`, `PayoutService` phải giữ precision/rounding rõ ràng và tổng tiền server-authoritative.
- Checkout/order không được tin tổng tiền, giá, stock, voucher status từ request/client; phải lấy snapshot catalog, validate voucher ở promotion và chạy compensation/idempotency hiện có (`CheckoutIdempotencyCoordinator`, `OrderCheckoutSagaExecutor`).
- Mọi chuyển trạng thái order/dispute/payout phải tôn trọng `OrderStatusConstant`, `OrderSagaStepStatus` và lifecycle hiện có; không cho transition ngược, double release/refund hoặc xử lý lại Kafka event gây ghi nhận tiền lần hai.
- Thay đổi dữ liệu nghiệp vụ và event phải atomic theo boundary hiện có: order event phải append `OrderOutboxEvent` trước publisher; không gọi Kafka như thay thế cho database transaction.
- Không log/commit JWT secret, service token, password, VNPay/SMTP credential hoặc dữ liệu thanh toán; không serialize password hash và không đưa secret vào FE.

### P1 — quan trọng

- Khi sửa product/variant/status trong `CatalogProductService`, phải cập nhật `OutboxEvent` payload/key để Debezium/Elasticsearch nhận đúng upsert hoặc tombstone delete.
- Kafka consumer (`OrderEventsConsumer`, `EmailEventConsumer`) phải có test cho duplicate, retry/failure và payload không hợp lệ; producer phải giữ topic, key và schema payload tương thích consumer.
- Endpoint có ID customer/seller/order phải lấy owner từ trusted identity header/JWT và kiểm tra authorization trong service; không dùng ID request làm quyền truy cập.
- API mới phải dùng request/response DTO hiện hữu, validation và exception response của service; không trả JPA entity trực tiếp nếu có nguy cơ lộ field nội bộ.
- Thay đổi order/promotion/payout phải bổ sung hoặc cập nhật unit/contract test cho boundary tiền, status, voucher quantity, stock, idempotency và compensation; test đặt trong module tương ứng.
- Không cross-service import entity/repository hoặc hard-code database khác; cập nhật Feign contract và endpoint `/internal/**` cùng lúc khi thay đổi API.

### Bỏ qua

- Không góp ý đổi naming/encoding của API legacy nếu không nằm trong phạm vi task và không tạo lỗi security/data.
- Không yêu cầu thêm lint/formatter/Testcontainers khi repo chưa cấu hình; chỉ yêu cầu format nhất quán với file lân cận.
- Không review generated/build output, log, `.idea` hoặc thay đổi UI không liên quan đến contract backend.

## Không được đụng vào

Không sửa, format, commit hoặc đưa vào patch các mục sau: `.env`, `logs/`, `build/`, `.gradle/`, `.idea/`, `.tmp/`, `hs_err_pid*.log`, `replay_pid*.log`.

## Commit/PR và tài liệu nguồn

- Dùng subject ngắn theo Conventional Commit (`feat(order): ...`, `fix: ...`). PR cần nêu service/behavior bị ảnh hưởng, API/schema/config thay đổi, lệnh validation và screenshot nếu có thay đổi frontend.
- Ưu tiên đối chiếu source theo thứ tự gateway authorization, controller/service, entity/repository/schema, FE client, rồi script/config. Tài liệu chính để chạy/hiểu hệ thống là `ARCHITECTURE.md`, `HE_THONG_HIEN_TAI_MARKETPLACE.md`, `RUN_PROJECT_LOCAL.md`, `backend-microservice/settings.gradle` và các `build.gradle`.

## Cần xác nhận

- Repo chưa có plugin lint/format hoặc CI quality gate trong các file đã đọc; cần xác nhận ngoài repo nếu CI áp dụng quy tắc khác.
- Chưa thấy `Mapper`/MapStruct convention hoặc Testcontainers; cần xác nhận trước khi biến chúng thành yêu cầu bắt buộc.
- Các default secret, token local, port và trạng thái migration trong tài liệu chạy local cần được chủ repo xác nhận trước khi dùng cho môi trường production.
