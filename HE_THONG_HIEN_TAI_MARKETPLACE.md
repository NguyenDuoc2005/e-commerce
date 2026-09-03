# HỆ THỐNG HIỆN TẠI – MARKETPLACE E-COMMERCE

> Tài liệu mô tả trạng thái **as-is** của repository tại ngày **03/09/2026**. Nội dung được đối chiếu lại từ source FE, controller/service/entity backend, gateway, cấu hình chạy, SQL migration/seed và hai lượt full E2E liên tiếp trên database reset sạch. Source code là nguồn sự thật cao hơn snapshot runtime và dữ liệu demo.
>
> Đây không phải tài liệu backlog hoặc thiết kế mong muốn. Các điểm chưa hoàn chỉnh được gom riêng tại mục **Giới hạn và rủi ro hiện tại**.

## 1. Kết luận nhanh

Hệ thống hiện tại là marketplace nhiều nhà bán với ba không gian sử dụng:

- **Buyer storefront**: xem sản phẩm/shop, giỏ hàng, checkout, đơn mua, review, follow, chat, khiếu nại và báo cáo.
- **Seller Center**: dashboard, sản phẩm/SKU, đơn theo shop, voucher, khuyến mại, Flash Sale, review, chat, tranh chấp và đối soát.
- **Platform Admin**: vận hành người dùng/nhân sự, seller, taxonomy/thuộc tính, campaign/Flash Sale/voucher sàn, banner, báo cáo, tranh chấp, thống kê và payout.

Kiến trúc backend là Spring Boot microservice qua Eureka và Spring Cloud Gateway. Mỗi domain có database MySQL riêng. Quan hệ giữa các service dùng ID chuỗi UUID và gọi REST/Feign; không có foreign key vật lý xuyên database.

Các đặc điểm marketplace đã có thật trong source:

- Một tài khoản buyer có thể đăng ký một hồ sơ shop đang hoạt động/chờ duyệt.
- Seller được gắn vào JWT sau khi shop ở trạng thái `APPROVED`.
- `sellerId` hiện đồng thời là định danh seller và shop trong toàn hệ thống.
- Sản phẩm thuộc seller, có category, ảnh, thuộc tính động, tối đa hai trục biến thể và nhiều SKU.
- Giỏ hàng và checkout nhận nhiều seller.
- Một lần checkout tạo một `orders`, sau đó tách thành nhiều `order_seller` và `order_item`.
- Mỗi seller xử lý sub-order của mình; khi hoàn thành sẽ tạo khoản phải trả và tính hoa hồng.
- Có voucher sàn/voucher shop, campaign thường, Flash Sale toàn sàn có quy trình seller đăng ký và admin duyệt.
- Có review sản phẩm/shop, follow shop, chat buyer–seller, dispute theo sub-order và report nội dung.
- Checkout đã bắt buộc buyer JWT, lấy `customerId` từ gateway, tự tính lại giá/voucher/phí ship ở server, phân bổ tiền theo seller và có bù trừ đồng bộ khi side effect xuyên service lỗi.
- Gateway đã tắt discovery locator; protected/internal endpoint ở downstream yêu cầu credential do gateway hoặc service nội bộ cấp. Seller route còn live-check trạng thái shop nên token cũ bị chặn ngay sau khi suspend.
- Refresh token, review–catalog internal contract, root-order aggregation và các moderation action đã có implementation thực tế và đã được chạy E2E.

## 2. Kiến trúc tổng thể

```mermaid
flowchart LR
    FE[Vue 3 SPA<br/>localhost:6688] --> GW[API Gateway<br/>localhost:8080]
    GW --> AUTH[auth-service]
    GW --> USER[user-service]
    GW --> CATALOG[catalog-service]
    GW --> PROMO[promotion-service]
    GW --> CART[cart-service]
    GW --> ORDER[order-service]
    GW --> SELLER[seller-service]
    GW --> PAYOUT[payout-service]
    GW --> NOTIFY[notification-service]

    AUTH -. Feign + internal credential .-> USER
    AUTH -. Feign + internal credential .-> SELLER
    CART -. Feign + internal credential .-> CATALOG
    CART -. Feign + internal credential .-> SELLER
    PROMO -. Feign + internal credential .-> CATALOG
    USER -. Feign + internal credential .-> ORDER
    ORDER -. Feign + internal credential .-> USER
    ORDER -. Feign + internal credential .-> CATALOG
    ORDER -. Feign + internal credential .-> PROMO
    ORDER -. Feign + internal credential .-> CART
    ORDER -. Feign + internal credential .-> SELLER
    ORDER -. Feign + internal credential .-> PAYOUT
    ORDER -. Feign + internal credential .-> NOTIFY
    SELLER -. Feign + internal credential .-> USER
    SELLER -. Feign + internal credential .-> CATALOG
    SELLER -. Feign + internal credential .-> ORDER
    SELLER -. Feign + internal credential .-> NOTIFY
    PAYOUT -. Feign + internal credential .-> SELLER
    PAYOUT -. Feign + internal credential .-> USER
    PAYOUT -. Feign + internal credential .-> NOTIFY

    CATALOG --> MYSQL[(MySQL 8.4)]
    USER --> MYSQL
    PROMO --> MYSQL
    CART --> MYSQL
    ORDER --> MYSQL
    SELLER --> MYSQL
    PAYOUT --> MYSQL

    CATALOG --> OUTBOX[(catalog outbox)]
    OUTBOX -. CDC khi được triển khai .-> KAFKA[Kafka]
    KAFKA -. sink .-> ES[Elasticsearch]
    KAFKA -. email event .-> NOTIFY
```

### 2.1. Công nghệ chính

- Frontend: Vue 3, TypeScript, Vite 5, Vue Router, Pinia, Axios, Ant Design Vue.
- Backend: Java 17, Spring Boot 3.4.4, Spring Cloud 2024.0.1, Gradle multi-module.
- Service discovery: Netflix Eureka.
- Gateway: Spring Cloud Gateway WebFlux.
- Giao tiếp đồng bộ nội bộ: OpenFeign qua tên service Eureka.
- Database: MySQL 8.4, tách schema theo domain.
- Thanh toán: VNPay sandbox ở `order-service`.
- Vận chuyển: FE gọi trực tiếp GHN để lấy địa chỉ/ước tính phí; `order-service` không tin phí từ FE mà dùng `checkout.shipping-fee`, mặc định 30.000đ.
- Messaging/search infrastructure: Kafka KRaft, Debezium/Kafka Connect và Elasticsearch.
- Quan sát hệ thống: Spring Actuator, Micrometer/Prometheus; Docker Compose có cấu hình Prometheus, Grafana, Filebeat, Logstash, Kibana, node-exporter và cAdvisor.

### 2.2. Service và cổng mặc định

| Thành phần | Cổng | Database | Trách nhiệm hiện tại |
|---|---:|---|---|
| `api-gateway` | 8080 | Không | Route public API, xác thực JWT theo prefix, live-check seller, inject marketplace context và gateway credential |
| `auth-service` | 8081 | `ecommerce_auth` nhưng hiện không có bảng nghiệp vụ | Login/register/change password/refresh, phát JWT; dữ liệu tài khoản lấy từ `user-service` |
| `user-service` | 8082 | `ecommerce_user` | Customer, staff, profile và internal auth lookup |
| `catalog-service` | 8083 | `ecommerce_catalog` | Category, product aggregate, SKU, stock, thuộc tính động, search public và outbox |
| `promotion-service` | 8085 | `ecommerce_promotion` | Voucher, campaign seller/admin và Flash Sale toàn sàn |
| `order-service` | 8086 | `ecommerce_order` | Checkout authoritative, VNPay, đơn buyer, sub-order seller, thống kê, compensation và dispute |
| `cart-service` | 8087 | `ecommerce_cart` | Giỏ hàng buyer và snapshot shop trên cart item |
| `notification-service` | 8088 | Không | Gửi email qua REST hoặc Kafka consumer |
| `seller-service` | 8089 | `ecommerce_seller` | Shop/seller, duyệt seller, banner, follow, review, chat và report |
| `payout-service` | 8091 | `ecommerce_payout` | Hoa hồng, receivable, ví seller, điều chỉnh tranh chấp và batch payout |
| `discovery-server` | 8761 | Không | Eureka registry |
| FE Vite | 6688 | Không | SPA cho Buyer, Seller và Admin |

Ghi chú cổng dữ liệu local:

- MySQL Docker: host `3307` → container `3306`.
- Kafka: `9092`.
- Elasticsearch: `9200`.
- Kafka Connect nếu chạy: `8084`.
- Kibana nếu chạy: `5601`.

## 3. Vai trò, JWT và phân quyền

### 3.1. Vai trò thực tế

| Vai trò | Claim | Ý nghĩa |
|---|---|---|
| Buyer | `USERS` | Customer đã đăng nhập |
| Seller | `USERS` + `SELLER` | Customer có shop `APPROVED` tại thời điểm token được phát |
| Platform Admin | `ADMIN` | Staff đăng nhập qua luồng admin |

Seller không có tài khoản đăng nhập riêng. Buyer và seller dùng cùng record `customer`; quyền seller được enrich động khi login.

### 3.2. Nội dung JWT

Access token tồn tại 2 giờ; refresh token tồn tại 7 ngày. Cả hai đều là JWT stateless và có claim `tokenType` tương ứng `ACCESS` hoặc `REFRESH`. Các claim chính:

- `email`, `userId`, `fullName`, `pictureUrl`.
- `role`: vai trò chính, buyer là `USERS`, admin là `ADMIN`.
- `roles`: danh sách vai trò; buyer có shop approved sẽ có `USERS`, `SELLER`.
- `tokenType`: gateway chỉ chấp nhận `ACCESS` cho protected API; endpoint refresh chỉ chấp nhận `REFRESH`.
- Khi là seller: `sellerId`, `sellerStatus`, `sellerSlug`, `shopName`.

`auth-service` không lưu user riêng. Nó gọi:

- `user-service` để lấy customer/staff và password hash.
- `seller-service /internal/sellers/approved/by-owner` để enrich seller role.

Nếu `seller-service` tạm lỗi, buyer vẫn login được nhưng token không có role `SELLER`.

`POST /api/v1/auth/refresh` kiểm tra chữ ký, hạn token và `tokenType`, đọc lại customer/staff đang active, enrich lại seller context rồi trả một cặp access/refresh mới. Vì token chưa được lưu server-side, refresh token cũ chưa bị revoke sau khi đổi token và vẫn dùng được tới khi hết hạn.

### 3.3. Gateway authorization

Gateway áp quy tắc theo prefix:

- `/api/v1/admin/**` cần role `ADMIN`.
- `/api/v1/seller/**` cần role `SELLER` và claim `sellerId`.
- `/api/v1/buyer/**` cần role `USERS`.
- `/api/orders/**` cần role `USERS`, ngoại trừ callback public `GET /api/orders/vnpay-return`.
- `POST /api/v1/notifications/email` cần role `ADMIN` khi gọi qua gateway.
- `OPTIONS` được bỏ qua để phục vụ CORS.

Sau khi xác thực, gateway inject:

- `X-User-Id` từ claim `userId`.
- `X-Seller-Id` từ claim `sellerId`.
- `X-Gateway-Token` là credential sinh ngẫu nhiên mỗi lần `run-all.ps1` khởi động stack.

Gateway xóa các header marketplace/credential do client tự gửi trước khi inject giá trị tin cậy. Với seller route, gateway gọi `seller-service` bằng internal credential để kiểm tra shop vẫn `APPROVED`; shop bị suspend làm token cũ bị từ chối `403` ngay, còn seller-service không khả dụng làm request trả `503`.

Các downstream service vẫn để Spring authorization ở mức `permitAll`, nhưng `TrustedRequestFilter` đứng trước controller để bắt buộc:

- protected admin/seller/buyer/checkout path phải có gateway credential;
- `/internal/**` phải có `X-Internal-Service-Token` do Feign interceptor gắn;
- notification email chấp nhận gateway credential hoặc internal credential.

Gateway discovery locator hiện `enabled: false`, nên không có route động dạng `/{service-id}/internal/**`.

FE cũng có route guard theo role và kiểm tra thời hạn token, nhưng gateway/backend mới là lớp cần quyết định quyền cuối cùng.

Hai ngoại lệ contract cần hiểu đúng:

- `/api/v1/sellers/register-shop` và `/api/v1/sellers/my-shop` không nằm dưới buyer prefix. `seller-service` tự parse và xác minh chữ ký/hạn Bearer JWT để lấy customer ID; resolver hiện chưa kiểm tra `tokenType` hoặc role `USERS`.
- `/api/orders/**` vẫn là prefix legacy, nhưng đã được gateway và downstream xếp vào protected buyer path; `CheckoutController` luôn ghi đè customer trong body bằng `X-User-Id`.

## 4. Mô hình seller và shop

Hệ thống chưa tách `seller` và `shop` thành hai aggregate. Record `seller` chính là hồ sơ nhà bán và hồ sơ shop.

- `seller.id` được dùng như `sellerId` và cũng là shop ID.
- `owner_customer_id` liên kết logic tới `ecommerce_user.customer.id`.
- JWT chỉ chứa một `sellerId`.
- Catalog, cart, order, voucher, review, chat và payout đều scope theo một `sellerId`.

Quy tắc đăng ký hiện tại:

- Một owner không được tạo hồ sơ mới nếu hồ sơ mới nhất khác `REJECTED` và `CLOSED`.
- `shop_name` và `seller_slug` unique trong DB.
- Đăng ký mới tạo trạng thái `PENDING_APPROVAL` và ghi `seller_status_history`.
- Owner có hồ sơ `REJECTED` hoặc `CLOSED` có thể tạo hồ sơ mới; DB không có unique constraint trên `owner_customer_id`, quy tắc nằm ở service.

Trạng thái seller:

```text
DRAFT
PENDING_APPROVAL
APPROVED
REJECTED
SUSPENDED
CLOSED
```

Transition được API hiện tại sử dụng:

```mermaid
stateDiagram-v2
    [*] --> PENDING_APPROVAL: register shop
    PENDING_APPROVAL --> APPROVED: admin approve
    PENDING_APPROVAL --> REJECTED: admin reject
    REJECTED --> APPROVED: admin approve
    APPROVED --> SUSPENDED: admin suspend
    SUSPENDED --> APPROVED: admin reopen
```

`DRAFT` và `CLOSED` có trong enum nhưng chưa có API nghiệp vụ tạo/chuyển tương ứng. API `suspend` và `reopen` hiện cũng không giới hạn chặt trạng thái nguồn như sơ đồ lý tưởng.

## 5. Database hiện tại

### 5.1. Nguyên tắc dữ liệu

- Mỗi service sở hữu schema riêng.
- ID nghiệp vụ chủ yếu là UUID chuỗi 36 ký tự.
- Không có foreign key xuyên schema; ví dụ `product.seller_id`, `orders.customer_id`, `order_seller.seller_id` chỉ là logical reference.
- Foreign key vật lý chỉ tồn tại bên trong một số schema như catalog, cart, promotion và dispute message.
- Nhiều bảng legacy dùng `status` ordinal (`tinyint`) và timestamp epoch milliseconds (`bigint`). Các domain marketplace mới như seller, dispute, report và payout dùng status chuỗi cùng `datetime/Instant`.
- `catalog-service` dùng `ddl-auto=validate`; schema catalog phải được tạo/migrate trước.
- Các service JPA còn lại mặc định dùng `ddl-auto=update` trong local config.

### 5.2. Inventory schema và bảng

| Schema | Bảng hiện có | Ý nghĩa |
|---|---|---|
| `ecommerce_auth` | Không có bảng nghiệp vụ | Auth stateless, đọc user qua `user-service` |
| `ecommerce_user` | `customer`, `staff` | Tài khoản buyer/seller owner và nhân sự platform |
| `ecommerce_seller` | `seller`, `seller_status_history`, `shop_follow`, `danh_gia`, `chat_conversation`, `chat_message`, `platform_banner`, `report` | Shop, social/review/chat và moderation |
| `ecommerce_catalog` | `category`, `product`, `product_image`, `product_variant`, `product_variant_axis`, `product_variant_axis_value`, `product_variant_axis_value_mapping`, `product_attribute_definition`, `product_attribute_option`, `product_attribute_value`, `category_attribute_suggestion`, `product_attribute_moderation_audit`, `variant_axis_name_suggestion`, `outbox` | Product domain canonical và search outbox |
| `ecommerce_cart` | `cart`, `cart_detail` | Giỏ hàng buyer |
| `ecommerce_order` | `orders`, `order_seller`, `order_item`, `order_status_history`, `payment_history`, `dispute`, `dispute_message` | Đơn gốc, sub-order, thanh toán và tranh chấp |
| `ecommerce_promotion` | `voucher`, `voucher_customer`, `promotion_campaign`, `promotion_campaign_product` | Voucher, campaign, Flash Sale registration |
| `ecommerce_payout` | `commission_config`, `seller_wallet`, `seller_receivable`, `payout_adjustment`, `payout_batch`, `payout_batch_item` | Hoa hồng và vòng đời đối soát |

### 5.3. Các bảng cốt lõi

#### User

- `customer`: thông tin cá nhân, email, số điện thoại, địa chỉ, password BCrypt, trạng thái.
- `staff`: thông tin nhân sự, password BCrypt, `role`, `role_type`, trạng thái.

#### Seller

- `seller`: owner, tên/slug shop, mô tả, logo/cover, địa chỉ lấy hàng, liên hệ, định danh, ngân hàng, category chính, trạng thái duyệt.
- `seller_status_history`: from/to status, actor, role, lý do, thời điểm.
- `shop_follow`: unique theo `seller_id + customer_id`.
- `danh_gia`: review gắn customer, seller, product, product variant (`product_detail_id`) và `order_seller`.
- `chat_conversation`: một conversation theo buyer/shop, lưu snapshot tên, unread count và last message.
- `chat_message`: sender `BUYER`/`SELLER`, nội dung, read time.
- `platform_banner`: banner theo position, sort order, thời gian hiệu lực.
- `report`: reporter, target `PRODUCT/SHOP/REVIEW/USER`, reason, evidence, action và resolution.

#### Catalog

- `category`: cây category self-reference qua `parent_id`.
- `product`: thuộc một seller và một leaf category, có rating aggregate.
- `product_image`: danh sách ảnh theo display order.
- `product_variant`: SKU, `combination_key`, giá bán, tồn kho, ảnh và default flag.
- `product_variant_axis` và `product_variant_axis_value`: tối đa hai trục như màu/size/dung tích.
- `product_variant_axis_value_mapping`: ánh xạ SKU với giá trị từng trục.
- `product_attribute_definition`: định nghĩa thuộc tính động `TEXT/NUMBER/SELECT_ONE/SELECT_MULTI`, trạng thái verify/merge.
- `product_attribute_option`: option chuẩn hoặc seller tạo, có verify/merge.
- `product_attribute_value`: giá trị mô tả/filter của sản phẩm.
- `category_attribute_suggestion`: gợi ý thuộc tính theo category, có `required_value` và `filterable`.
- `product_attribute_moderation_audit`: audit chuẩn hóa/merge/hide thuộc tính.
- `variant_axis_name_suggestion`: danh mục tên trục biến thể được gợi ý/kiểm duyệt.
- `outbox`: event search `ProductCreated`, `ProductUpdated`, `ProductDeleted`.

#### Cart và order

- `cart`: một giỏ theo `customer_id`.
- `cart_detail`: variant, quantity, price và snapshot `seller_id/shop_name/seller_slug`.
- `orders`: đơn gốc của buyer, thông tin nhận hàng, tổng tiền, voucher, payment method và root status.
- `order_seller`: sub-order của từng shop, snapshot shop, tổng/ship/discount và status riêng.
- `order_item`: variant, quantity, sale price, seller và sub-order.
- `order_status_history`: lịch sử status cấp đơn gốc.
- `payment_history`: số tiền, transaction code/type và order.
- `dispute`: khiếu nại gắn trực tiếp `order_seller`.
- `dispute_message`: trao đổi buyer/seller/admin trong tranh chấp.

#### Promotion và payout

- `voucher.seller_id = null`: voucher sàn do admin quản lý.
- `voucher.seller_id = sellerId`: voucher shop do seller quản lý.
- `voucher_customer`: voucher chỉ định customer.
- `promotion_campaign`: campaign thường hoặc `FLASH_SALE`; campaign seller có `seller_id`, Flash Sale toàn sàn có `seller_id = null`.
- `promotion_campaign_product`: variant tham gia campaign; với Flash Sale có trạng thái đăng ký và audit duyệt.
- `commission_config`: hoa hồng mặc định (`category_id = null`) hoặc theo category.
- `seller_receivable`: khoản phải trả duy nhất theo `order_seller_id`.
- `seller_wallet`: `pending_amount`, `available_amount`, `paid_amount` theo seller.
- `payout_batch`/`payout_batch_item`: một đợt chi trả nhiều receivable.
- `payout_adjustment`: điều chỉnh âm do quyết định hoàn tiền tranh chấp.

### 5.4. Quan hệ dữ liệu logic

```mermaid
erDiagram
    CUSTOMER ||--o{ SELLER : owns_history
    SELLER ||--o{ PRODUCT : sells
    SELLER ||--o{ SHOP_FOLLOW : followed
    CUSTOMER ||--o{ SHOP_FOLLOW : follows
    SELLER ||--o{ REVIEW : receives
    CUSTOMER ||--o{ REVIEW : writes
    SELLER ||--o{ CHAT_CONVERSATION : participates
    CUSTOMER ||--o{ CHAT_CONVERSATION : participates

    CATEGORY ||--o{ CATEGORY : parent_of
    CATEGORY ||--o{ PRODUCT : classifies
    PRODUCT ||--o{ PRODUCT_IMAGE : has
    PRODUCT ||--o{ PRODUCT_VARIANT : has
    PRODUCT ||--o{ PRODUCT_ATTRIBUTE_VALUE : describes
    PRODUCT ||--o{ PRODUCT_VARIANT_AXIS : varies_by
    PRODUCT_VARIANT_AXIS ||--o{ PRODUCT_VARIANT_AXIS_VALUE : contains
    PRODUCT_VARIANT ||--o{ PRODUCT_VARIANT_AXIS_VALUE_MAPPING : maps

    CUSTOMER ||--o| CART : owns
    CART ||--o{ CART_DETAIL : contains
    PRODUCT_VARIANT ||--o{ CART_DETAIL : selected

    CUSTOMER ||--o{ ORDERS : places
    ORDERS ||--o{ ORDER_SELLER : splits_into
    SELLER ||--o{ ORDER_SELLER : fulfills
    ORDER_SELLER ||--o{ ORDER_ITEM : contains
    PRODUCT_VARIANT ||--o{ ORDER_ITEM : snapshots
    ORDER_SELLER ||--o{ DISPUTE : disputed_by

    SELLER ||--o{ VOUCHER : owns_optional
    PROMOTION_CAMPAIGN ||--o{ PROMOTION_CAMPAIGN_PRODUCT : contains
    PRODUCT_VARIANT ||--o{ PROMOTION_CAMPAIGN_PRODUCT : joins

    SELLER ||--o| SELLER_WALLET : has
    ORDER_SELLER ||--o| SELLER_RECEIVABLE : settles
    SELLER_RECEIVABLE ||--o| PAYOUT_BATCH_ITEM : paid_as
    PAYOUT_BATCH ||--o{ PAYOUT_BATCH_ITEM : contains
```

Mermaid trên là logical ERD. Chỉ các quan hệ nội bộ cùng schema mới có thể là FK vật lý; các đường xuyên service dựa trên UUID và API contract.

## 6. Luồng nghiệp vụ hiện tại

### 6.1. Đăng ký và đăng nhập

```mermaid
sequenceDiagram
    actor U as User
    participant FE
    participant GW
    participant A as auth-service
    participant US as user-service
    participant SS as seller-service

    U->>FE: Register/Login
    FE->>GW: /api/v1/auth/*
    GW->>A: forward
    A->>US: lookup/create customer hoặc lookup staff
    alt customer login
        A->>SS: approved seller by owner
        SS-->>A: seller hoặc empty
    end
    A-->>FE: access token + refresh token
    FE->>FE: lưu token và decoded user trong localStorage
```

- Register tạo `customer` với password BCrypt và status active.
- Login buyer chỉ tìm customer active.
- Login admin chỉ tìm staff active.
- Buyer có seller approved nhận thêm role và seller context.
- Refresh đọc lại trạng thái tài khoản/seller, phân biệt đúng loại token và rotate cả cặp token.
- FE có hai màn login: `/login` và `/admin/login`.

### 6.2. Đăng ký và duyệt shop

1. Buyer đã đăng nhập vào `/dang-ky-ban-hang`.
2. FE gửi hồ sơ shop: tên, slug, mô tả, ảnh, địa chỉ lấy hàng, liên hệ, định danh, ngân hàng và category chính.
3. `seller-service` lấy owner từ Bearer JWT, kiểm tra hồ sơ mới nhất và unique name/slug.
4. Tạo `seller=PENDING_APPROVAL` và một dòng history.
5. Admin xem `/admin/seller-approval`, approve hoặc reject.
6. Khi approve, seller thành `APPROVED`; lần login buyer tiếp theo token mới có role `SELLER`.
7. Email thông báo được gọi best-effort; lỗi notification không rollback quyết định seller.

### 6.3. Seller tạo sản phẩm

1. Gateway xác thực role `SELLER`, inject `X-Seller-Id`.
2. Seller chọn leaf category.
3. FE lấy cây category, gợi ý thuộc tính và gợi ý tên trục biến thể.
4. Seller gửi một product aggregate gồm basic info, ảnh, attributes, variant axes và variants.
5. Catalog validate category leaf, display order, loại giá trị, tối đa hai axes, SKU/combination.
6. Trong transaction MySQL, service lưu product aggregate và ghi outbox event.
7. Seller chỉ list/detail/update/change status sản phẩm có `seller_id` đúng context.

Thuộc tính mô tả và trục biến thể là hai khái niệm khác nhau:

- Thuộc tính mô tả dùng cho thông số/filter, không sinh SKU.
- Trục biến thể tạo tổ hợp SKU, giá, tồn kho và lựa chọn mua.

### 6.4. Tìm kiếm và xem sản phẩm

- Public product list/detail chỉ trả product `ACTIVE`.
- Filter hiện có: keyword, seller, category, giá, attribute filters và sort.
- Product detail trả category, ảnh, thuộc tính, axes và variants.
- Shop public chỉ trả seller `APPROVED`, rating/follower và sold count lấy từ order completed.
- Giá/discount campaign được quản lý trong promotion service; public product read path hiện không tự ghép giá khuyến mại từ Elasticsearch.

Read path hiện tại của `GET /api/v1/permitall/products` là MySQL/JPA rồi filter/sort trong `catalog-service`; API này chưa truy vấn Elasticsearch.

### 6.5. Giỏ hàng

Buyer đăng nhập:

1. Gateway lấy `X-User-Id` từ JWT.
2. `cart-service` tìm hoặc tạo `cart` theo customer.
3. Khi thêm variant, service gọi catalog lấy giá/tồn kho/seller.
4. Service snapshot seller/shop vào `cart_detail`.
5. Mỗi lần đọc giỏ, service lấy lại variant hiện tại và cập nhật `cart_detail.price` nếu giá catalog đã đổi; giá do client gửi không được dùng làm nguồn tính tiền.
6. Response gồm `items` và `shopGroups` theo seller.

Guest storefront:

- FE lưu giỏ tạm trong localStorage.
- Route `/gio-hang` public ở FE, nhưng API `/api/v1/buyer/cart/**` chỉ dùng cho buyer có token.
- Checkout FE hiện yêu cầu buyer route, dù backend `/api/orders/**` vẫn chấp nhận contract customer/guest từ request.

### 6.6. Checkout multi-seller và VNPay

```mermaid
sequenceDiagram
    actor B as Buyer
    participant FE
    participant O as order-service
    participant C as catalog-service
    participant P as promotion-service
    participant S as seller-service
    participant CART as cart-service
    participant V as VNPay

    B->>FE: xác nhận checkout
    FE->>O: POST /api/orders/create + Bearer token
    O->>O: lấy customer từ X-User-Id, bỏ customer/tổng tiền client
    O->>C: lấy giá, seller, category và kiểm tra stock
    O->>P: xác minh voucher, customer, seller scope
    O->>S: lấy snapshot shop
    O->>O: tính subtotal + ship cấu hình - discount
    O->>O: tạo orders trong transaction local
    O->>O: group item theo seller
    O->>O: tạo order_seller + order_item, phân bổ ship/discount
    alt COD
        O->>P: giảm quantity voucher
        O->>C: trừ tồn kho
        O->>CART: xóa item đã checkout
        O-->>FE: order hoặc rollback + bù trừ khi lỗi
    else VNPay
        O-->>FE: paymentUrl
        FE->>V: redirect thanh toán
        V->>O: GET /api/orders/vnpay-return
        O->>O: verify HMAC, response, amount và trạng thái
        O->>O: chuyển LUU_TAM -> CHO_XAC_NHAN idempotent
        O->>P: giảm voucher
        O->>C: trừ tồn kho
        O->>CART: xóa item
    end
```

Cấu trúc kết quả:

```text
orders (1 checkout của buyer)
└── order_seller (1 cho mỗi seller)
    └── order_item (variant thuộc seller đó)
```

Status enum order lưu theo ordinal:

| Ordinal | Status |
|---:|---|
| 0 | `CHO_XAC_NHAN` |
| 1 | `DA_XAC_NHAN` |
| 2 | `CHO_GIAO` |
| 3 | `DANG_GIAO` |
| 4 | `HOAN_THANH` |
| 5 | `DA_HUY` |
| 6 | `LUU_TAM` |

Quy tắc tính tiền hiện tại:

- Client vẫn gửi các field legacy `tongTien/phiShip/giamGia/tongCong` để giữ contract, nhưng backend ghi đè toàn bộ.
- Subtotal lấy từ `CatalogVariantSnapshot.salePrice × quantity`; `customerId` luôn lấy từ header gateway.
- Phí ship lấy từ `checkout.shipping-fee` của backend, mặc định 30.000đ. Kết quả GHN do FE tính chưa đi vào nguồn giá server.
- Voucher sàn được phân bổ theo tỷ trọng subtotal; voucher shop chỉ giảm cho đúng `order_seller` của shop đó. Điều kiện tối thiểu, quantity, customer assignment và lịch sử dùng đều được kiểm tra.
- Phí ship được phân bổ theo tỷ trọng subtotal xuống từng `order_seller`; mỗi sub-order lưu đủ `total_amount`, `shipping_fee`, `discount_amount`, `total_after_discount`.

COD chỉ commit order khi giảm voucher, trừ stock và xóa đúng variant khỏi cart đều hoàn tất. Nếu call xuyên service lỗi, order transaction rollback và code cố gắng hoàn stock/voucher đã thay đổi. VNPay chỉ chạy các side effect này sau callback HMAC hợp lệ; callback lặp cho order đã `CHO_XAC_NHAN` trả thành công mà không trừ lần hai. Callback sai HMAC/response/amount không chuyển trạng thái và không trừ stock/voucher/cart.

Buyer chỉ được hủy khi root order và toàn bộ sub-order còn `CHO_XAC_NHAN`. Luồng hủy hoàn lại stock, tăng lại quantity voucher, chuyển root/sub-order sang `DA_HUY` và ghi history.

### 6.7. Seller xử lý đơn và tạo payout

Seller workflow được enforce theo `order_seller`:

```mermaid
stateDiagram-v2
    CHO_XAC_NHAN --> DA_XAC_NHAN: confirm
    DA_XAC_NHAN --> CHO_GIAO: ready-to-ship
    CHO_GIAO --> DANG_GIAO: shipping
    DANG_GIAO --> HOAN_THANH: complete
    CHO_XAC_NHAN --> DA_HUY: cancel
    DA_XAC_NHAN --> DA_HUY: cancel
    CHO_GIAO --> DA_HUY: cancel
    DANG_GIAO --> DA_HUY: cancel
```

Khi một sub-order chuyển `HOAN_THANH`:

1. Order service tính gross từ item total trừ discount sub-order.
2. Item được phân bổ theo category để payout service tính rate từng dòng.
3. Payout service áp commission config theo category; không có config thì dùng rate default, mặc định code là 5%.
4. Tạo `seller_receivable=PENDING` và cộng net vào `seller_wallet.pending_amount`.
5. Sau hold period mặc định 7 ngày, scheduler chuyển khoản đủ điều kiện sang `AVAILABLE` và dời tiền từ pending sang available.
6. Admin tạo payout batch; receivable thành `PAID`, ví giảm available và tăng paid.
7. Email thanh toán cho seller được gửi best-effort.

Sau mỗi transition của seller, `order-service` aggregate lại root status từ toàn bộ `order_seller` và ghi `order_status_history` nếu root thay đổi. Khi seller hủy sub-order, stock của riêng sub-order được hoàn; voucher chỉ được hoàn khi không còn sibling sub-order chưa hủy. Các side effect hủy cũng có bù trừ ngược nếu bước sau thất bại.

### 6.8. Voucher, promotion và Flash Sale

Voucher:

- Admin quản lý voucher sàn (`seller_id=null`).
- Seller quản lý voucher shop của chính mình (`seller_id=sellerId`).
- Voucher hỗ trợ quantity, thời gian, điều kiện đơn, mức giảm, trần giảm và danh sách customer được chỉ định.

Campaign thường:

- Admin có campaign sàn.
- Seller có promotion scope shop và chỉ được chọn variant thuộc seller hiện tại.
- `promotion_campaign_product` lưu giá trước/sau giảm.

Flash Sale toàn sàn:

1. Admin tạo campaign với cửa sổ đăng ký và thời gian chạy.
2. Seller xem campaign còn hiệu lực và variant active của shop.
3. Seller đăng ký variant với giá flash thấp hơn giá đang bán.
4. Registration ở `PENDING`.
5. Admin `APPROVE` hoặc `REJECT`; khi approve service kiểm tra lại ownership và giá.
6. Seller có thể withdraw; registration approved không được rút sau khi Flash Sale bắt đầu.
7. Public API chỉ đưa sản phẩm approved/đang sử dụng vào campaign response.

Registration status: `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`.

Campaign time status: `CHUA_KICH_HOAT`, `DANG_KICH_HOAT`, `HET_HAN_KICH_HOAT`.

### 6.9. Follow, review và chat

Follow:

- Buyer có thể kiểm tra trạng thái, follow và unfollow shop approved.
- Unique theo customer/shop.

Review:

- Chỉ được review variant đã mua trong sub-order `HOAN_THANH`.
- Một customer chỉ review một variant trong một sub-order một lần.
- Lưu cả `product_rating` và `shop_rating`.
- Seller có thể reply review của shop mình.
- Public có thể lấy review theo product hoặc seller.
- `seller-service` lấy variant qua `GET /internal/catalog/product-details/{variantId}` và đồng bộ rating qua `POST /internal/catalog/products/{productId}/rating`; catalog cập nhật aggregate rating và ghi `ProductUpdated` vào outbox.

Chat:

- Một conversation cho mỗi cặp customer/seller.
- Buyer chỉ chat với shop approved.
- Buyer và seller đều có list conversation, list/send message và mark read.
- Chat hiện là REST polling/state trong DB; không có WebSocket controller ở backend hiện tại dù FE có dependency STOMP/SockJS.

### 6.10. Dispute và report

Dispute gắn với `order_seller`, không gắn trực tiếp toàn bộ root order.

```mermaid
stateDiagram-v2
    [*] --> OPEN: buyer tạo trên sub-order hoàn thành
    OPEN --> SELLER_RESPONDED: seller phản hồi
    OPEN --> UNDER_ADMIN_REVIEW: admin tiếp nhận
    SELLER_RESPONDED --> UNDER_ADMIN_REVIEW: admin tiếp nhận
    UNDER_ADMIN_REVIEW --> RESOLVED_REFUND_BUYER
    UNDER_ADMIN_REVIEW --> RESOLVED_PARTIAL_REFUND
    UNDER_ADMIN_REVIEW --> RESOLVED_REJECT_BUYER
    RESOLVED_REFUND_BUYER --> CLOSED
    RESOLVED_PARTIAL_REFUND --> CLOSED
    RESOLVED_REJECT_BUYER --> CLOSED
```

Nếu quyết định hoàn toàn phần/một phần, order service gọi payout service tạo `payout_adjustment`:

- Receivable `PENDING`: giảm gross/net và pending wallet.
- Receivable `AVAILABLE`: giảm available/released tương ứng.
- Receivable `PAID`: ghi âm vào pending để bù bằng receivable tương lai.

Report là luồng moderation riêng cho target `PRODUCT`, `SHOP`, `REVIEW`, `USER`. Admin review rồi resolve bằng action `PRODUCT_DELISTED`, `SHOP_SUSPENDED`, `REVIEW_HIDDEN`, `WARNING_SENT` hoặc `NO_ACTION`. Ba action moderation đầu thực sự gọi catalog/seller/review domain để delist sản phẩm, suspend shop hoặc ẩn review; `WARNING_SENT` gửi email và `NO_ACTION` đóng report ở trạng thái `DISMISSED`.

## 7. API contract hiện tại

Tất cả public traffic thông thường đi qua `http://localhost:8080`.

### 7.1. Auth và user

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/login-admin`
- `PUT /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/change-password`
- Admin customer: `/api/v1/admin/khach-hang/**`
- Admin staff: `/api/v1/admin/nhan-vien/**`
- Profile legacy/public: `/api/v1/permitall/profile/**`

### 7.2. Catalog

Public:

- `GET /api/v1/permitall/products`
- `GET /api/v1/permitall/products/{id}`
- `GET /api/v1/permitall/categories/tree`
- `GET /api/v1/permitall/categories/{categoryId}/attribute-suggestions`

Seller:

- `GET /api/v1/seller/products`
- `GET /api/v1/seller/products/{id}`
- `POST /api/v1/seller/products`
- `PUT /api/v1/seller/products/{id}`
- `PUT /api/v1/seller/products/{id}/status`
- Category/suggestion/low-stock endpoints nằm dưới cùng prefix.

Admin:

- Category: `/api/v1/admin/categories/**`
- Product attributes/options/audit/reindex: `/api/v1/admin/product-attributes/**`
- Variant axis insights/suggestions: `/api/v1/admin/product-variant-axes/**`

### 7.3. Seller/shop domain

- Shop registration/profile owner: `/api/v1/sellers/**`
- Public shop: `/api/v1/permitall/shops/**`
- Buyer follow: `/api/v1/buyer/shops/{sellerId}/follow`
- Admin seller approval: `/api/v1/admin/sellers/**`
- Seller profile: `GET /api/v1/seller/profile`
- Banner public/admin: `/api/v1/permitall/banners`, `/api/v1/admin/banners/**`
- Review: `/api/v1/buyer/reviews/**`, `/api/v1/seller/reviews/**`, `/api/v1/permitall/reviews`
- Chat: `/api/v1/buyer/chat/**`, `/api/v1/seller/chat/**`
- Report: `/api/v1/buyer/reports`, `/api/v1/seller/reports`, `/api/v1/admin/reports/**`

### 7.4. Cart, order và dispute

- Buyer cart: `/api/v1/buyer/cart/**`
- Checkout/VNPay/voucher lookup legacy: `/api/orders/**` (buyer JWT, trừ public VNPay return)
- Buyer order: `/api/v1/buyer/orders/**`
- Seller order/dashboard/status: `/api/v1/seller/orders/**`
- Admin statistics: `/api/v1/admin/thong-ke/**`
- Buyer dispute: `/api/v1/buyer/disputes/**`
- Seller dispute: `/api/v1/seller/disputes/**`
- Admin dispute: `/api/v1/admin/disputes/**`

### 7.5. Promotion, payout và notification

- Admin voucher: `/api/v1/admin/voucher/**`
- Seller voucher: `/api/v1/seller/vouchers/**`
- Admin standard campaign: `/api/v1/admin/campaigns/**`
- Seller promotion: `/api/v1/seller/promotions/**`
- Admin Flash Sale: `/api/v1/admin/flash-sales/**`
- Seller Flash Sale: `/api/v1/seller/flash-sales/**`
- Public Flash Sale: `/api/v1/permitall/flash-sales/**`
- Admin payout: `/api/v1/admin/payout/**`
- Seller payout: `/api/v1/seller/payout/**`
- Email REST: `POST /api/v1/notifications/email` (admin qua gateway hoặc internal service credential)

### 7.6. Internal service API

Các prefix internal không nằm trong danh sách route tĩnh công khai của gateway:

- `/internal/users/**`
- `/internal/sellers/**`
- `/internal/catalog/**`
- `/internal/promotions/**`
- `/internal/carts/**`
- `/internal/orders/**`
- `/internal/payout/**`

Chúng phục vụ Feign giữa service. Mỗi Feign client được `InternalServiceTokenInterceptor` gắn `X-Internal-Service-Token`; `TrustedRequestFilter` tại downstream từ chối internal request thiếu/sai credential. Gateway discovery locator đã tắt, nên dạng `/{service-id}/internal/**` trả `404` thay vì tạo route động.

## 8. Frontend hiện tại

### 8.1. Buyer routes

Public:

- `/trang-chu`
- `/san-pham`
- `/san-pham-chi-tiet/:idsp`
- `/shop/:sellerSlug`
- `/flash-sale`
- `/gio-hang`
- `/thanh-toan-thanh-cong`
- `/lien-he`, `/gioi-thieu`
- `/login`, `/register`

Yêu cầu role `USERS`:

- `/dang-ky-ban-hang`
- `/don-mua`
- `/don-mua-detail/:maHoaDon/:id`
- `/khieu-nai`
- `/tin-nhan`
- `/tra-cuu`
- `/thanh-toan`
- `/thong-tin-ca-nhan`

### 8.2. Seller routes

Tất cả yêu cầu role `SELLER`:

- `/seller/dashboard`
- `/seller/orders`
- `/seller/products`
- `/seller/vouchers`
- `/seller/flash-sales`
- `/seller/payout`
- `/seller/reviews`
- `/seller/profile`
- `/seller/disputes`
- `/seller/chat`

### 8.3. Admin routes

Tất cả yêu cầu role `ADMIN`:

- `/admin/thong-ke`
- `/admin/seller-approval`
- `/admin/categories`
- `/admin/product-attributes`
- `/admin/campaigns`, `/admin/campaigns/new`, `/admin/campaigns/:id/edit`
- `/admin/flash-sales`
- `/admin/voucher`
- `/admin/banners`
- `/admin/payout`
- `/admin/reports`
- `/admin/disputes`
- `/admin/khach-hang`, `/admin/them-khach-hang`
- `/admin/nhan-vien`, `/admin/them-nhan-vien`
- `/admin/login`

FE dùng `VITE_BASE_URL_SERVER=http://localhost:8080`, lưu access token/refresh token/user info trong localStorage và gắn Bearer token qua Axios interceptor. Khi gặp `401` ngoài trang login, interceptor gọi refresh, lưu lại cả access/refresh token cùng user claims rồi retry request một lần. `.env.stage` hiện cũng trỏ backend về `8080` và client về `6688`.

## 9. Search, Kafka và observability

### 9.1. Product outbox pipeline

Catalog ghi event vào `ecommerce_catalog.outbox` trong cùng transaction với thay đổi product/stock:

```text
catalog transaction
  -> outbox
  -> MySQL ROW binlog
  -> Debezium Outbox Event Router
  -> Kafka topic outbox.event.Product
  -> Elasticsearch sink
  -> products alias / products_v2
```

Payload canonical chứa product/category, attributes nested và variants nested. `ProductDeleted` dùng payload null/tombstone để xóa document.

Trạng thái trong hai lượt E2E sạch ngày 03/09/2026:

- Kafka `9092`, Elasticsearch `9200` và Kafka Connect REST `8084` đều sẵn sàng; source/sink connector ở trạng thái `RUNNING`.
- Elasticsearch single-node báo `yellow`, phù hợp với cấu hình replica của môi trường một node.
- E2E đã chủ động dừng Kafka Connect rồi gọi public product API thành công, sau đó bật lại connector. Public catalog API vẫn đọc MySQL và không phụ thuộc Elasticsearch/Kafka Connect.
- Đây là hạ tầng phục vụ index/search pipeline, không phải read path của storefront hiện tại.

### 9.2. Email

`notification-service` hỗ trợ:

- REST `POST /api/v1/notifications/email`; admin gọi qua gateway, service nghiệp vụ gọi bằng internal credential.
- Kafka consumer topic mặc định `email-notification`.

Các luồng seller/order/payout trong source hiện gọi notification đồng bộ bằng Feign REST và đều bắt lỗi để email không rollback transaction nghiệp vụ.

### 9.3. Metrics và logs

- Tất cả Spring service expose `/actuator/health`, `/actuator/info`, `/actuator/prometheus`, `/actuator/metrics`.
- Docker Compose có Prometheus, Grafana, Filebeat, Logstash, Kibana, node-exporter và cAdvisor, nhưng không phải tất cả container đều đang chạy ở snapshot hiện tại.

## 10. Runtime và E2E gần nhất – 03/09/2026

Stack đã được dựng từ database reset sạch bằng `reset-and-run-demo.ps1`, build lại 11 module rồi chạy toàn bộ flow qua gateway. Kết quả:

- 11/11 actuator báo `UP`: gateway, Eureka và 9 domain service.
- Eureka có đủ 10 application: gateway, auth, user, catalog, promotion, cart, order, notification, seller và payout.
- FE `6688` trả HTTP 200; Kafka, Elasticsearch và Kafka Connect sẵn sàng trong lượt chạy.
- Toàn bộ nhóm flow 3.1–3.11 trong `AGENTS.md` đã chạy qua request thật và kiểm tra state DB: auth/refresh/degraded login, seller lifecycle, catalog/search/outbox, cart, COD/VNPay/compensation, order/payout, promotion/Flash Sale, social, dispute/report và admin/banner.
- Hai lượt độc lập `CLEAN1` và `CLEAN2` sau reset sạch đều kết thúc `result=CLEAN`; health/Eureka đầu và cuối lượt đều đạt.
- Security smoke: direct protected/internal service trả `401`, discovery route qua gateway trả `404`, anonymous notification email trả `401`.
- Failure smoke: tắt cart-service ở cuối checkout làm request lỗi có chủ đích nhưng stock, voucher và số order không đổi sau rollback/compensation; tắt seller-service vẫn cho buyer login với duy nhất role `USERS`; tắt Kafka Connect không làm public product read path hỏng.

Snapshot DB cuối lượt `CLEAN2`, chỉ là bằng chứng kiểm thử chứ không phải invariant dữ liệu:

- 15 `orders`, 19 `order_seller`.
- 8 `seller_receivable`, 2 `payout_adjustment`.
- 42 event catalog `outbox`, 7 `dispute`, 5 `report`.
- `mismatched_completed_roots=0`.

Trạng thái port/process sau một phiên có thể thay đổi vì script E2E có chủ động stop/start service. Vì vậy các con số trên mô tả lần full-run gần nhất, không thay cho kiểm tra actuator khi bắt đầu phiên phát triển mới.

## 11. Giới hạn và rủi ro hiện tại

Đây là trạng thái sau khi kiểm chứng lại 10 rủi ro của snapshot cũ. “Đã fix” nghĩa là có implementation trong source và đã có assertion E2E tương ứng, không có nghĩa hệ thống đã đạt mức production ở mọi khía cạnh.

### 11.1. Trạng thái 10 rủi ro đã biết

| # | Rủi ro cũ | Trạng thái hiện tại | Bằng chứng chính |
|---:|---|---|---|
| 1 | Checkout tin tổng tiền client/voucher sai seller | **Đã fix** | `prepareAuthoritativeTotals`, `requireApplicableVoucher`, phân bổ ship/discount theo `order_seller`; E2E gửi số giả vẫn ghi tổng server |
| 2 | Không có saga/compensation | **Đã bổ sung durable log + reconciliation cho COD** | `order_saga_step` lưu từng bước, retry tối đa 3 lần/request và job 15 phút; failure smoke giữ nguyên stock/voucher/order count. Chưa phải distributed saga cho VNPay và chưa có idempotency key tổng quát |
| 3 | Root/sub-order lệch status | **Đã fix** | `aggregateRootStatus` chạy sau seller transition; CLEAN2 có `mismatched_completed_roots=0` |
| 4 | Thiếu auth refresh | **Đã fix** | `POST /api/v1/auth/refresh`, kiểm tra `tokenType`, reload account/seller và trả cặp token mới; FE retry một lần |
| 5 | Lệch review–catalog internal contract | **Đã fix** | Đủ hai endpoint product-detail/rating; rating update ghi outbox `ProductUpdated` |
| 6 | Downstream/internal chỉ dựa network boundary | **Đã fix cho boundary local hiện tại** | `TrustedRequestFilter`, Feign internal credential, gateway strip header, discovery locator off; direct smoke `401`, discovery smoke `404` |
| 7 | Suspend seller nhưng token cũ còn dùng được | **Đã fix cho seller route** | Gateway live-check seller `APPROVED` trên mỗi `/api/v1/seller/**`; token cũ sau suspend trả `403` |
| 8 | Notification email public | **Đã fix** | Gateway yêu cầu `ADMIN`; downstream chỉ nhận gateway/internal credential; anonymous/direct smoke `401` |
| 9 | Search pipeline không phải read path chính | **Vẫn đúng theo thiết kế hiện tại** | Public products đọc MySQL; E2E tắt Kafka Connect vẫn đọc được. Đây là giới hạn scale, không phải lỗi availability của storefront |
| 10 | Contract/UX legacy và `.env.stage` sai | **Fix một phần** | `.env.stage` đã đúng `8080/6688`; các prefix/field/profile/cart/OAuth legacy bên dưới vẫn còn |

### 11.2. Distributed consistency và idempotency còn giới hạn

- COD hiện có `checkout_idempotency_key` bắt buộc ở header `Idempotency-Key`; request trùng `COMPLETED` trả lại order cũ, request trùng `IN_PROGRESS` trả `409`, và request `FAILED` có thể chạy lại. Cơ chế này chỉ áp dụng COD, không áp dụng VNPay.
- Compensation COD đã có log durable, retry 3 lần với backoff `1s/3s/5s` trong request và reconciliation job mỗi 15 phút; sau 5 lượt job (tối đa 15 attempt tích lũy) vẫn giữ `COMPENSATION_FAILED` để retry tay qua internal endpoint.
- Compensation vẫn là các REST call đồng bộ. Nếu stock side effect thất bại giữa một batch nhiều variant, schema hiện chỉ log ở mức step (chưa có child record theo từng variant), nên cần reconciliation/đối soát kỹ trước production.
- COD vẫn chưa có idempotency key ở các contract khác ngoài `POST /api/orders/create`, và chưa có distributed transaction.
- VNPay callback đã idempotent khi order ở `CHO_XAC_NHAN`, nhưng order `LUU_TAM` bị bỏ dở/sai callback chưa có scheduler expire/cleanup.
- Tồn kho chỉ được kiểm tra khi tạo URL VNPay và chỉ trừ sau callback thành công; chưa có stock reservation trong thời gian người mua thanh toán.
- Quy tắc “voucher đã dùng” hiện chỉ đếm order `HOAN_THANH`; cùng customer vẫn có thể tạo nhiều order chưa hoàn thành bằng một voucher nếu quantity tổng còn đủ.

### 11.3. Shipping và money model còn đơn giản

- Backend đã authoritative nhưng phí ship hiện là một mức cấu hình `checkout.shipping-fee` mặc định 30.000đ cho toàn checkout, chưa gọi hãng vận chuyển theo từng shop/địa chỉ/cân nặng.
- FE vẫn gọi GHN trực tiếp và đang chứa GHN token/shop ID trong source. Kết quả phí GHN chỉ mang tính hiển thị vì backend ghi đè bằng mức cấu hình.
- Checkout chỉ hỗ trợ một voucher cho toàn root order. Việc phân bổ dùng `double` và tỷ trọng, chưa có money type/rounding policy thống nhất cho tổng nhiều seller.

### 11.4. Security còn việc phải làm trước production

- Gateway/internal credential là shared bearer secret qua header, chưa phải mTLS hoặc workload identity. `run-all.ps1` sinh credential ngẫu nhiên, nhưng file `.run.cmd` local vẫn chứa credential của phiên đang chạy.
- JWT, MySQL, VNPay và GHN vẫn có default/credential development trong config hoặc FE source; phải chuyển sang secret manager/environment và rotate trước triển khai thật.
- Access/refresh token là JWT stateless, chưa có server-side session/revocation. Refresh “rotation” trả token mới nhưng token cũ chưa bị vô hiệu ngay; buyer/admin bị khóa cũng chưa được live-check trên mọi request như seller.
- FE lưu access/refresh token trong localStorage, nên tác động của XSS cao hơn mô hình cookie `HttpOnly`.
- Mọi staff active đăng nhập qua `/login-admin` hiện được phát role `ADMIN`; chưa có RBAC chi tiết theo `role_type` cho từng nhóm vận hành.
- Hai route plural `/api/v1/sellers/register-shop` và `/api/v1/sellers/my-shop` tự parse JWT ngoài gateway protected prefix nhưng chưa kiểm tra `tokenType=ACCESS` hay role `USERS`; refresh token hoặc token admin có chữ ký hợp lệ vẫn có thể đi tới logic owner.
- `/api/v1/permitall/profile/**` còn cho đọc lịch sử và `POST` tạo/cập nhật customer bằng ID từ request mà không có buyer authorization. Đây là public-write/IDOR legacy cần ưu tiên tách sang `/api/v1/buyer/profile` và lấy owner từ JWT.
- Các endpoint legacy `/api/orders/khach-hang/{id}` và voucher lookup nhận customer ID từ path/form. Gateway đã yêu cầu buyer JWT, nhưng controller chưa ràng buộc ID đó với `X-User-Id`, nên buyer đã đăng nhập có thể hỏi dữ liệu của ID khác.
- Production vẫn phải chỉ expose gateway và cô lập port downstream dù direct protected/internal request hiện đã bị filter.
- Entity `Customer` và `Staff` đã chặn serialize password hash, nhưng cần tiếp tục kiểm soát DTO/response khi thêm endpoint mới.

### 11.5. Search và hiệu năng

- `GET /api/v1/permitall/products` tải danh sách product active từ MySQL rồi filter attribute/price, sort và phân trang trong application memory; nhiều bước còn query variants/attributes theo từng product. Cách này đúng chức năng nhưng không phù hợp catalog lớn.
- Elasticsearch/outbox pipeline đã chạy được, nhưng chưa được dùng làm storefront read model và chưa có cơ chế fallback/read-switch chính thức.
- Chat là REST polling/state trong MySQL, chưa có WebSocket/realtime delivery.

### 11.6. Contract/UX legacy còn tồn tại

- Xóa cart item dùng `PUT /api/v1/buyer/cart/{id}` thay vì `DELETE`.
- Checkout dùng prefix `/api/orders` và giữ alias field cũ (`product/sanPham/items`, `Customer/KhachHang`), dù quyền buyer và customer authority của bước tạo đơn đã được vá.
- Profile user vẫn nằm dưới `/api/v1/permitall/profile/**`, nhận ID qua URL/body và có cả public write; đây không chỉ là naming legacy mà còn là lỗ hổng authorization còn mở.
- `ecommerce_auth` vẫn có datasource/schema rỗng dù auth stateless và không có entity nghiệp vụ.
- Seller profile hiện chỉ có API GET, chưa có API update hồ sơ shop.
- Gateway/FE vẫn khai báo route OAuth2, nhưng auth-service chưa có success handler/controller hoàn chỉnh cho redirect flow.
- `.env.stage` đã được sửa đúng; đây không còn là rủi ro hiện tại.

## 12. Cách chạy và quản lý dữ liệu local

Chạy stack khuyến nghị với MySQL Docker:

```powershell
powershell -ExecutionPolicy Bypass -File backend-microservice\run-all.ps1 -DbPort 3307 -WithNotification
```

`run-all.ps1` build lại boot jar, sinh mới gateway/internal credential cho phiên chạy, khởi động Eureka trước rồi các domain service và gateway. MySQL Docker map host `3307` vào container `3306`; nếu bỏ `-DbPort 3307`, script dùng mặc định `3306`.

Dừng backend:

```powershell
powershell -ExecutionPolicy Bypass -File backend-microservice\stop-all.ps1
```

Chạy FE:

```powershell
cd FE
npm run dev -- --host 127.0.0.1 --port 6688
```

Script sau **DROP và tạo lại toàn bộ 8 database, mất dữ liệu không thể hoàn tác**:

```powershell
powershell -ExecutionPolicy Bypass -File backend-microservice\reset-and-run-demo.ps1
```

Bộ E2E hiện có trong `backend-microservice/e2e-*-flow.ps1`. Orchestrator `e2e-full-regression.ps1` nhận label `CLEAN1` hoặc `CLEAN2`, kiểm tra health/Eureka đầu-cuối, chạy các flow phụ thuộc theo thứ tự và chủ động stop/start một số service để kiểm tra degraded mode/compensation. Chỉ chạy full clean sau khi đã chấp nhận việc reset dữ liệu.

Catalog là ngoại lệ migration quan trọng:

- `catalog-service` chạy `ddl-auto=validate`.
- Schema canonical được dựng từ `p1_product_domain_reset.sql` trong quy trình reset demo.
- Seed catalog canonical dùng `p1_product_domain_seed.sql`.
- Các script audit/preflight/verify nằm trong `catalog-service/src/main/resources/db/migration/manual`.

## 13. Nguồn sự thật khi cập nhật tài liệu này

Khi source thay đổi, cập nhật tài liệu theo thứ tự ưu tiên:

1. Gateway route và authorization filter.
2. Controller mapping và service implementation đang compile.
3. Entity/repository cùng schema live `information_schema`.
4. FE router, API client và màn hình đang được route tới.
5. Script `run-all.ps1`, Docker Compose và application config.
6. Runtime actuator/public smoke test.
7. SQL migration/seed để giải thích dữ liệu demo, không dùng seed thay cho contract source.

Không dùng folder/build artifact cũ, log cũ hoặc tài liệu progress làm nguồn duy nhất nếu chúng mâu thuẫn với source/runtime hiện tại.
