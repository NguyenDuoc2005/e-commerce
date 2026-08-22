# 04-luong-nghiep-vu.md

Nghiệp vụ thuộc tính sản phẩm động ở Mục 10 của prompt được gắn trực tiếp vào Mục 3.2 (Seller tạo/sửa sản phẩm), Mục 3.3 (Buyer xem/search/filter) và Mục 3.4 (Platform Admin hậu kiểm); đây không phải một flow độc lập ngoài marketplace.

## 1. Đăng ký và duyệt seller

### 1.1 Luồng buyer đăng ký bán hàng

```mermaid
sequenceDiagram
  participant BuyerFE as Buyer FE
  participant GW as api-gateway
  participant SELLER as seller-service
  participant USER as user-service
  participant NOTI as notification-service

  BuyerFE->>GW: POST /api/v1/sellers/register-shop
  GW->>GW: Verify JWT USER
  GW->>SELLER: Forward with X-Customer-Id
  SELLER->>USER: Feign get customer profile
  USER-->>SELLER: customer snapshot
  SELLER->>SELLER: Validate shopName/slug unique
  SELLER->>SELLER: Create seller status PENDING_APPROVAL
  SELLER-->>BuyerFE: Registration submitted
  SELLER-->>NOTI: Notify platform admin (event or REST)
```

Các bước chi tiết:

| Bước | Xử lý |
|---|---|
| 1 | Buyer đăng nhập bằng tài khoản hiện có |
| 2 | FE mở trang "Đăng ký bán hàng" |
| 3 | Form thu tên shop, slug, mô tả, logo/cover, địa chỉ lấy hàng, phone, định danh, bank, ngành hàng |
| 4 | FE không gửi `sellerId`; backend lấy customer từ JWT |
| 5 | `seller-service` validate unique `shop_name`, `seller_slug` |
| 6 | Tạo `seller` với trạng thái `PENDING_APPROVAL` |
| 7 | Seller thấy màn "Đang chờ duyệt"; chưa truy cập được Seller Admin product/order/voucher |

### 1.2 Platform Admin duyệt/từ chối seller

```mermaid
sequenceDiagram
  participant AdminFE as Platform Admin FE
  participant GW as api-gateway
  participant SELLER as seller-service
  participant AUTH as auth-service
  participant NOTI as notification-service

  AdminFE->>GW: GET /api/v1/admin/sellers/pending
  GW->>GW: Verify ADMIN
  GW->>SELLER: list pending sellers
  SELLER-->>AdminFE: pending seller list
  AdminFE->>GW: POST /api/v1/admin/sellers/{id}/approve
  GW->>SELLER: approve seller
  SELLER->>SELLER: status APPROVED + history
  SELLER-->>AUTH: seller role claim available on next login/refresh
  SELLER-->>NOTI: email seller approved
  SELLER-->>AdminFE: approved
```

Approve:

| Rule | Mô tả |
|---|---|
| Actor | Chỉ Platform Admin |
| Điều kiện | Seller đang `PENDING_APPROVAL` hoặc resubmit sau `REJECTED` |
| Kết quả | `status = APPROVED`, ghi `approvedBy`, `approvedAt`, status history |
| Auth | Lần login/refresh tiếp theo JWT có role `SELLER`, claim `sellerId` |

Reject:

| Rule | Mô tả |
|---|---|
| Actor | Chỉ Platform Admin |
| Điều kiện | Hồ sơ thiếu/sai |
| Kết quả | `status = REJECTED`, lưu `rejectionReason`, status history |
| Seller action | Seller sửa hồ sơ và nộp lại |

Suspend/close:

| Trạng thái | Actor | Điều kiện |
|---|---|---|
| `SUSPENDED` | Platform Admin | Shop vi phạm, tranh chấp, rủi ro |
| `CLOSED` | Seller owner hoặc Platform Admin | Đóng shop theo chính sách; cần xử lý order đang mở trước |

## 2. Login và chuyển vai trò Buyer/Seller/Admin

```mermaid
sequenceDiagram
  participant FE
  participant GW as api-gateway
  participant AUTH as auth-service
  participant USER as user-service
  participant SELLER as seller-service

  FE->>GW: POST /api/v1/auth/login
  GW->>AUTH: route auth-service
  AUTH->>USER: Lookup customer by email
  USER-->>AUTH: customer auth data
  AUTH->>SELLER: Lookup approved seller by ownerCustomerId
  SELLER-->>AUTH: sellerId/status if exists
  AUTH-->>FE: JWT roles USER[,SELLER], sellerId
  FE->>FE: Store token + roles in Pinia/localStorage
```

FE behavior:

| User state | UI |
|---|---|
| Buyer only | Storefront + nút "Đăng ký bán hàng" |
| Seller pending | Storefront + màn trạng thái "Đang chờ duyệt" |
| Seller approved | Storefront + nút "Kênh Người Bán" |
| Admin | Platform Admin link `/admin` |

Backend guard:

| Prefix | Rule |
|---|---|
| `/api/v1/admin/**` | Role `ADMIN` |
| `/api/v1/seller/**` | Role `SELLER`, seller status `APPROVED` |
| `/api/v1/permitall/**` | Public/buyer, token optional tùy endpoint |

## 3. Seller Admin - quản lý sản phẩm

### 3.1 CRUD sản phẩm theo seller

```mermaid
sequenceDiagram
  participant SellerFE
  participant GW as api-gateway
  participant CATALOG as catalog-service
  participant SELLER as seller-service
  participant OUTBOX as catalog outbox

  SellerFE->>GW: POST /api/v1/seller/products
  GW->>GW: Verify SELLER and extract sellerId
  GW->>CATALOG: Forward X-Seller-Id
  CATALOG->>SELLER: Verify seller APPROVED/public snapshot
  SELLER-->>CATALOG: sellerName, slug
  CATALOG->>CATALOG: Save san_pham seller_id
  CATALOG->>OUTBOX: ProductUpdated payload with seller fields
  CATALOG-->>SellerFE: product created
```

Rules:

| Rule | Mô tả |
|---|---|
| Seller scope | Seller chỉ thấy product `seller_id = sellerId` |
| Create | `seller_id` lấy từ JWT/header, không từ body |
| Update/delete | Validate ownership trước khi sửa |
| Attributes | Seller chọn danh mục, dùng thuộc tính gợi ý hoặc tự thêm; không hard-code Thương hiệu/Xuất xứ/Chất liệu/Loại đế cho mọi ngành hàng |
| Search | Product public chỉ index nếu product active và seller approved |

### 3.2 Seller tự thêm thuộc tính động khi tạo/sửa sản phẩm

Endpoint trong diagram là contract dự kiến; tên cuối cùng phải bám convention controller hiện hữu khi code.

```mermaid
sequenceDiagram
  participant SellerFE as Seller Product Form
  participant GW as api-gateway
  participant CATALOG as catalog-service
  participant DB as ecommerce_catalog
  participant OUTBOX as catalog outbox

  SellerFE->>GW: GET /api/v1/seller/categories/{categoryId}/attributes
  GW->>CATALOG: X-Seller-Id + categoryId
  CATALOG->>DB: Load category suggestions and visible custom attributes
  DB-->>CATALOG: definitions, type, options, display order
  CATALOG-->>SellerFE: suggested attributes
  SellerFE->>SellerFE: Seller enters suggested values
  SellerFE->>GW: GET /api/v1/seller/attributes/suggest?categoryId=&q=
  GW->>CATALOG: autocomplete normalized name in category
  CATALOG-->>SellerFE: matching existing attributes
  alt Seller chooses an existing attribute
    SellerFE->>SellerFE: Reuse attributeId and enter value
  else No suitable attribute exists
    SellerFE->>SellerFE: Click + Thêm thuộc tính, enter name/type/value
  end
  SellerFE->>GW: POST or PUT /api/v1/seller/products with attributes
  GW->>CATALOG: Verified X-Seller-Id
  CATALOG->>DB: Transaction: validate ownership/category/type, create pending definition if needed, save product values
  CATALOG->>OUTBOX: ProductUpdated with dynamic attributes
  CATALOG-->>SellerFE: Product and saved attributes
```

Rules:

| Rule | Xử lý |
|---|---|
| Chọn danh mục trước | Chỉ tải thuộc tính gợi ý/autocomplete sau khi có `categoryId` |
| Tự thêm | Không yêu cầu Admin duyệt trước khi lưu/đăng product; thuộc tính mới có trạng thái `PENDING` để hậu kiểm |
| Chống trùng | Chuẩn hóa tên và autocomplete theo cùng danh mục trước khi cho tạo mới; backend vẫn kiểm tra race condition |
| Ownership | `creator_seller_id` lấy từ JWT/header, không nhận tin cậy từ body |
| Kiểu và giá trị | Seller được chọn `TEXT`, `NUMBER`, `SINGLE_SELECT`, `MULTI_SELECT`; backend validate typed value và option ownership |
| Số lượng | Tối đa 50 thuộc tính động/product; FE chặn trước và backend luôn kiểm tra lại |
| Phạm vi gợi ý | Thuộc tính `PENDING` chỉ gợi ý/tái sử dụng cho shop tạo; seller khác chỉ thấy sau khi Admin chuyển `STANDARDIZED` và gắn danh mục |
| Tính nguyên tử | Product, definition mới, option mới, actual values và outbox ghi trong cùng transaction |

Khi đổi danh mục sau khi đã nhập thuộc tính:

```mermaid
sequenceDiagram
  participant Seller
  participant Form as Seller Product Form
  participant CATALOG as catalog-service

  Seller->>Form: Select another category
  Form->>Form: Detect entered attribute values
  alt No attribute value entered
    Form->>CATALOG: Load suggestions for new category
  else Existing values may be incompatible
    Form-->>Seller: Confirm category change and attribute reset/review
    alt Seller cancels
      Form->>Form: Keep old category and values
    else Seller confirms
      Form->>CATALOG: Load suggestions for new category
      Form->>Form: Keep only explicitly compatible reused attributes; mark remaining values for review
    end
  end
```

## 4. Seller Admin - quản lý đơn hàng

```mermaid
sequenceDiagram
  participant SellerFE
  participant GW as api-gateway
  participant ORDER as order-service
  participant NOTI as notification-service

  SellerFE->>GW: GET /api/v1/seller/orders?status=CHO_XAC_NHAN
  GW->>GW: Verify SELLER, sellerId
  GW->>ORDER: X-Seller-Id
  ORDER->>ORDER: Query don_hang_seller by seller_id
  ORDER-->>SellerFE: sub-order list
  SellerFE->>GW: PUT /api/v1/seller/orders/{subOrderId}/confirm
  ORDER->>ORDER: Verify seller ownership + status transition
  ORDER->>ORDER: status DA_XAC_NHAN/DANG_DONG_GOI
  ORDER-->>NOTI: notify buyer order status
  ORDER-->>SellerFE: updated sub-order
```

Trạng thái sub-order:

```mermaid
stateDiagram-v2
  [*] --> CHO_XAC_NHAN
  CHO_XAC_NHAN --> DA_XAC_NHAN
  DA_XAC_NHAN --> DANG_DONG_GOI
  DANG_DONG_GOI --> DANG_GIAO
  DANG_GIAO --> HOAN_THANH
  CHO_XAC_NHAN --> DA_HUY
  DA_XAC_NHAN --> DA_HUY
  DANG_GIAO --> TRA_HANG_HOAN_TIEN
```

Seller actions:

| Action | Allowed status | Result |
|---|---|---|
| Xác nhận | `CHO_XAC_NHAN` | `DA_XAC_NHAN` |
| Đóng gói/in phiếu | `DA_XAC_NHAN` | `DANG_DONG_GOI` |
| Bàn giao vận chuyển | `DANG_DONG_GOI` | `DANG_GIAO` |
| Hủy | `CHO_XAC_NHAN`, `DA_XAC_NHAN` | `DA_HUY`, cần hoàn/điều chỉnh payout nếu đã paid |

## 5. Seller voucher và đợt giảm giá

```mermaid
sequenceDiagram
  participant SellerFE
  participant GW as api-gateway
  participant PROMO as promotion-service
  participant CATALOG as catalog-service

  SellerFE->>GW: POST /api/v1/seller/vouchers
  GW->>PROMO: X-Seller-Id
  PROMO->>PROMO: Force seller_id = X-Seller-Id, scope SHOP
  PROMO-->>SellerFE: voucher shop created
  SellerFE->>GW: POST /api/v1/seller/promotions/{id}/products
  PROMO->>CATALOG: Validate product-detail belongs to sellerId
  CATALOG-->>PROMO: valid
  PROMO->>PROMO: Save campaign detail
```

Rules:

| Scope | Ai tạo | Áp dụng |
|---|---|---|
| Voucher shop | Seller approved | Chỉ sub-order của seller đó |
| Voucher toàn sàn | Platform Admin | Order cha hoặc phân bổ vào sub-order |
| Đợt giảm giá shop | Seller approved | Product-detail thuộc seller |
| Đợt giảm giá platform | Platform Admin | Theo rule toàn sàn |

## 6. Buyer storefront nhiều shop

### 6.1 Trang chủ và search

```mermaid
sequenceDiagram
  participant FE as Buyer FE
  participant GW as api-gateway
  participant CATALOG as catalog-service
  participant SELLER as seller-service
  participant ES as Elasticsearch

  FE->>GW: GET /api/catalog/user/products/search?q=giay&sellerId=&rating=
  GW->>CATALOG: route catalog-service
  CATALOG->>ES: Search products index
  ES-->>CATALOG: product docs with seller fields
  CATALOG-->>FE: product cards + sellerName/sellerSlug/rating
  FE->>GW: GET /api/v1/permitall/shops/featured
  GW->>SELLER: featured shops
  SELLER-->>FE: shop cards
```

Product card phải có:

| Field | Nguồn |
|---|---|
| Product name/image/price | `catalog-service` |
| Seller name/slug/logo | Search payload hoặc seller-service |
| Rating/sold count | Product projection |
| Discount | `promotion-service` hoặc catalog enriched response |

Filter và chi tiết sản phẩm theo danh mục (liên kết Mục 3.3):

```mermaid
sequenceDiagram
  participant FE as Buyer FE
  participant GW as api-gateway
  participant CATALOG as catalog-service
  participant DB as ecommerce_catalog
  participant ES as Elasticsearch

  FE->>GW: GET /api/catalog/user/categories/{categoryId}/filters
  GW->>CATALOG: categoryId
  CATALOG->>DB: Load active filterable category attributes
  CATALOG->>ES: Aggregate nested attribute values by attributeId
  ES-->>CATALOG: options/ranges with counts
  CATALOG-->>FE: Dynamic filter definition and counts
  FE->>GW: GET /api/catalog/user/products/search?categoryId=&attributes=
  CATALOG->>ES: Nested query by attributeId and typed value
  ES-->>CATALOG: matching products
  CATALOG-->>FE: cards and active filters
  FE->>GW: GET product detail
  CATALOG-->>FE: product, variants, all visible dynamic attributes
```

- FE không hiển thị cố định Size/Màu cho mọi danh mục; filter được dựng từ response của danh mục hiện tại.
- Query một filter phải ràng buộc `attributeId` và value trong cùng nested object để không match chéo.
- Trang chi tiết hiển thị cả thuộc tính gợi ý và seller tự thêm nếu thuộc tính/value còn visible.
- Thuộc tính Admin ẩn không còn dùng cho filter/public detail, nhưng dữ liệu nguồn chưa bị xóa vật lý để còn audit/revert.

### 6.2 Trang shop riêng

```mermaid
sequenceDiagram
  participant FE
  participant GW
  participant SELLER as seller-service
  participant CATALOG as catalog-service

  FE->>GW: GET /api/v1/permitall/shops/{sellerSlug}
  GW->>SELLER: get public shop
  SELLER-->>FE: shop profile + stats
  FE->>GW: GET /api/v1/permitall/san-pham?...sellerSlug
  GW->>CATALOG: list products by seller
  CATALOG-->>FE: shop products
  FE->>GW: POST /api/v1/permitall/shops/{sellerId}/follow
  GW->>SELLER: follow shop (buyer token)
```

## 7. Cart multi-seller

```mermaid
sequenceDiagram
  participant FE
  participant GW
  participant CART as cart-service
  participant CATALOG as catalog-service

  FE->>GW: POST /api/v1/permitall/cart
  GW->>CART: add productDetailId
  CART->>CATALOG: get product-detail incl sellerId/shop snapshot
  CATALOG-->>CART: product detail + seller info
  CART->>CART: save cart item with seller snapshot
  CART-->>FE: updated cart
  FE->>GW: GET /api/v1/permitall/cart
  CART-->>FE: groups by shop
```

Response shape đề xuất:

```json
{
  "shops": [
    {
      "sellerId": "SELLER001",
      "sellerName": "Shop A",
      "sellerSlug": "shop-a",
      "items": [
        { "cartDetailId": "1", "productDetailId": "SPCT1", "quantity": 2, "price": 100000 }
      ],
      "subtotal": 200000
    }
  ],
  "totalQuantity": 2,
  "totalAmount": 200000
}
```

## 8. Checkout split-order và thanh toán một lần

```mermaid
sequenceDiagram
  participant FE
  participant GW
  participant ORDER as order-service
  participant CATALOG as catalog-service
  participant PROMO as promotion-service
  participant CART as cart-service
  participant VNPAY as VNPay
  participant PAYOUT as payout-service

  FE->>GW: POST /api/orders/create selectedItems + vouchers
  GW->>ORDER: buyer token
  ORDER->>CATALOG: get product details + sellerId + stock
  ORDER->>PROMO: validate shop vouchers per seller
  ORDER->>PROMO: validate platform voucher
  ORDER->>ORDER: group items by seller
  ORDER->>ORDER: create don_hang parent
  ORDER->>ORDER: create don_hang_seller per seller
  ORDER->>ORDER: create don_hang_chi_tiet snapshots
  ORDER->>CATALOG: adjust stock per item
  ORDER->>CART: delete checked-out items
  alt VNPAY
    ORDER-->>FE: paymentUrl
    FE->>VNPAY: redirect payment
    VNPAY->>GW: GET /api/orders/vnpay-return
    GW->>ORDER: payment callback
    ORDER->>ORDER: mark parent paid, sub-orders CHO_XAC_NHAN
    ORDER->>PAYOUT: create pending receivable per seller
  else COD
    ORDER->>ORDER: parent/sub-order COD pending
  end
```

Voucher allocation:

| Voucher | Apply level | Rule |
|---|---|---|
| Shop voucher | Sub-order | Chỉ tính vào subtotal của seller đó |
| Platform voucher | Order cha | Phân bổ theo tỉ lệ subtotal từng sub-order để tính payout đúng |

## 9. Lịch sử đơn mua và review

```mermaid
sequenceDiagram
  participant BuyerFE
  participant GW
  participant ORDER as order-service
  participant REVIEW as seller-service/review
  participant CATALOG as catalog-service

  BuyerFE->>GW: GET /api/v1/permitall/don-mua
  GW->>ORDER: buyer token
  ORDER-->>BuyerFE: parent orders with sub-orders
  BuyerFE->>GW: POST /api/v1/permitall/reviews
  GW->>REVIEW: review product/shop
  REVIEW->>ORDER: verify sub-order completed and belongs to buyer
  ORDER-->>REVIEW: valid
  REVIEW->>REVIEW: save danh_gia
  REVIEW-->>CATALOG: update product rating aggregate
```

Review rule:

| Rule | Mô tả |
|---|---|
| Chỉ review sau hoàn thành | `don_hang_seller.status = HOAN_THANH` |
| Không review sản phẩm chưa mua | Verify buyer/sub-order/item |
| Một item một review | Unique theo buyer + sub-order + product-detail |
| Seller reply | Seller chỉ reply review thuộc shop mình |

## 10. Payout/đối soát

```mermaid
sequenceDiagram
  participant ORDER as order-service
  participant PAYOUT as payout-service
  participant SELLER as seller-service
  participant AdminFE as Platform Admin
  participant SellerFE as Seller Admin

  ORDER->>PAYOUT: Sub-order completed, seller receivable
  PAYOUT->>PAYOUT: Add pending wallet amount
  PAYOUT->>PAYOUT: Generate settlement period
  SellerFE->>PAYOUT: GET /api/v1/seller/settlements
  PAYOUT-->>SellerFE: settlement history
  AdminFE->>PAYOUT: POST /api/v1/admin/settlements/{id}/approve
  PAYOUT->>SELLER: get bank snapshot
  SELLER-->>PAYOUT: bank info
  PAYOUT->>PAYOUT: create payout transaction
  PAYOUT-->>SellerFE: status PAID after payment success
```

Payout amount:

```text
seller_receivable = sub_order_subtotal
                  + shipping_fee_charged_to_buyer_if_passed_to_seller
                  - shop_discount
                  - platform_commission
                  - refund/penalty
```

## 11. Platform Admin flows

### 11.1 Banner

```mermaid
sequenceDiagram
  participant AdminFE
  participant GW
  participant SELLER as seller-service
  participant BuyerFE

  AdminFE->>GW: POST /api/v1/admin/banners
  GW->>SELLER: save platform banner
  BuyerFE->>GW: GET /api/v1/permitall/banners?position=HOME_TOP
  GW->>SELLER: active banners
  SELLER-->>BuyerFE: banners
```

### 11.2 Khiếu nại/tranh chấp

Phase sau, nhưng domain cần chuẩn bị:

```mermaid
sequenceDiagram
  participant BuyerFE
  participant SellerFE
  participant AdminFE
  participant ORDER as order-service

  BuyerFE->>ORDER: create dispute for sub-order
  ORDER->>SellerFE: notify seller
  SellerFE->>ORDER: submit evidence/response
  AdminFE->>ORDER: review dispute
  AdminFE->>ORDER: resolve refund/release payout
```

### 11.3 Hậu kiểm, chuẩn hóa và gộp thuộc tính sản phẩm

Màn Platform Admin **Quản lý thuộc tính** bổ sung vào phạm vi Mục 3.4 của prompt. Seller không phải chờ thao tác này để đăng sản phẩm.

```mermaid
sequenceDiagram
  participant AdminFE as Platform Admin FE
  participant GW as api-gateway
  participant CATALOG as catalog-service
  participant DB as ecommerce_catalog
  participant OUTBOX as catalog outbox
  participant ES as Elasticsearch

  AdminFE->>GW: GET /api/v1/admin/product-attributes?status=PENDING
  GW->>GW: Verify ADMIN
  GW->>CATALOG: List definitions, categories, sellers and usage counts
  CATALOG->>DB: Query attribute definitions and usage
  DB-->>AdminFE: Moderation queue
  alt Normalize and attach as default suggestion
    AdminFE->>GW: PUT attribute name/type/category suggestions
    CATALOG->>DB: Transaction: standardize definition and category links
  else Merge duplicate into canonical attribute
    AdminFE->>GW: POST /product-attributes/{sourceId}/merge/{targetId}
    CATALOG->>DB: Transaction: validate compatible types, repoint values/options/category links, mark source MERGED
  else Hide junk or violation
    AdminFE->>GW: PUT attribute status HIDDEN with reason
    CATALOG->>DB: Soft hide definition and public values
  end
  CATALOG->>OUTBOX: Reindex affected product IDs
  OUTBOX->>ES: Upsert product documents
  CATALOG-->>AdminFE: Updated definition and affected counts
```

Merge/normalize rules:

| Rule | Xử lý |
|---|---|
| Hậu kiểm | `PENDING` dùng được ngay trong shop tạo và không được gợi ý chéo shop; Admin xử lý sau |
| Merge type | Chỉ tự động gộp khi type tương thích; đổi `TEXT` sang dropdown/number phải preview lỗi và có mapping rõ |
| Atomicity | Repoint product values, options và category suggestions trong một transaction; source giữ `MERGED` + `merged_into_attribute_id` |
| Duplicate option | Gộp theo `normalized_value`, không tạo hai lựa chọn tương đương trong target |
| Hide/delete | Mặc định soft hide để không mất dữ liệu product; hard delete chỉ khi usage count bằng 0 và đã có audit/backup |
| Search | Mọi normalize/merge/hide phát outbox cho toàn bộ product bị ảnh hưởng và theo dõi reindex failure |
| Audit | Lưu actor, reason, source/target và affected counts trong audit log triển khai ở Phase 5 |

## 12. Thông báo

`notification-service` hiện có REST email và Kafka consumer. Marketplace dùng cho:

| Event | Recipient |
|---|---|
| Seller submitted | Platform Admin |
| Seller approved/rejected/suspended | Seller owner |
| Buyer paid order | Seller(s) |
| Seller confirmed/shipped/completed sub-order | Buyer |
| Review created/replied | Seller/buyer |
| Settlement paid | Seller |

Kafka producer cụ thể từ `order-service` hiện chưa thấy trong source; khi code cần bổ sung producer/event publisher rõ ràng.
