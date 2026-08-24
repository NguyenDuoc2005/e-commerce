# Roadmap chuẩn hóa domain Sản phẩm

## 1. Nguyên tắc thực hiện

- Làm tuần tự; không chạy DB cutover khi source các consumer chưa compile với contract mới.
- Mỗi task chỉ DONE khi có bằng chứng source + test/compile phù hợp; runtime là lớp bằng chứng riêng.
- Không giữ compatibility v1/v2 và không giữ alias field/bảng cứng sau cutover.
- Seller ownership luôn lấy từ `X-Seller-Id`.
- Không đánh dấu hoàn tất toàn domain khi cart/order/promotion/search hoặc FE còn dùng Màu/Size/Brand hard-code.
- Reset DB thật là hành động phá hủy. Chỉ chạy sau khi xác định rõ target dev/local và được phép; trước đó có thể verify schema/seed trên database tạm cô lập.

## 2. Phase P0 — Audit và tài liệu hóa

### P0.1 Audit source và DB thật — DONE 2026-08-24

- Đọc entity/migration/controller/service/repository/model catalog.
- Đọc FE Seller/Admin/Buyer.
- Trace cart/order/promotion/outbox/Elasticsearch.
- Query schema và data `ecommerce_catalog` đang chạy.
- Output: `00-audit-hien-trang.md`.

### P0.2 Chốt nghiệp vụ và ERD — DONE 2026-08-24

- Tách descriptive attributes và variant axes.
- Chốt default variant, tối đa hai axis, shared unverified option, merge definition/option một tầng và direct API cutover.
- Output: `01-nghiep-vu-thuoc-tinh.md`, `erd-san-pham.md`.

### P0.3 Seed và contract impact — DONE 2026-08-24

- Thiết kế seed ba ngành và đủ case 0/1/2 axis.
- Liệt kê endpoint/consumer affected và contract đích.
- Output: `seed-data-plan.md`, `api-contract-changes.md`.

## 3. Phase P1 — Schema target và seed mới

### P1.1 Viết schema migration mới

Phạm vi:

- Tạo script up chỉ dành cho dev reset/cutover; không chuyển data M10 cũ.
- Bổ sung cây category.
- Recreate descriptive tables theo ERD, gồm `product_attribute_definition.default_unit` nullable cho NUMBER và merge definition/option đúng một tầng với target chưa merge.
- Tạo `product_image` cho gallery tổng thể, tách khỏi `product_variant.image_url`.
- Tạo `variant_axis_name_suggestion`, axis, axis value, variant mapping.
- Sửa `product` và `product_variant`; bắt buộc `combination_key NOT NULL`, unique `(product_id, combination_key)` và thêm constraints/index.
- Xóa bảng cũ và `product_attribute_value_option` đúng thứ tự FK.
- Vô hiệu hóa/loại script M10 backup/migrate cũ khỏi runbook chuẩn.

Acceptance:

- Chạy pass trên database tạm clone schema/seed, không chạm DB local thật.
- Information schema đúng tên/cột/FK/index; `ddl-auto=validate` pass nếu bật.
- Kiểm chứng `product_image`, `default_unit`, `combination_key NOT NULL`/unique; service/schema contract thể hiện merge chỉ một tầng và target có con trỏ merge `NULL`.
- SQL rerun behavior được xác định rõ (fresh reset hoặc idempotent, không nhập nhằng).

### P1.2 Refactor entity/repository compile theo schema

- Xóa entity/repository sáu bảng cứng và generic `CatalogAttribute` cũ.
- Xóa quan hệ hard-code trong Product/ProductVariant.
- Tạo entity axis/value/mapping/từ điển gợi ý.
- Chuyển tiền sang `BigDecimal`, code/SKU unique.
- Viết repository query không JOIN bảng cứng.

Acceptance: catalog unit/repository test + `:catalog-service:compileJava` pass; scan Java không còn entity/table cũ.

### P1.3 Viết seed mới và verify cô lập

- Seed category tree, definition/option, bốn product và 12 variant theo `seed-data-plan.md`.
- Cập nhật variant ID trong promotion/cart/order seed.
- Tạo outbox bằng canonical builder/fixture shape.

Acceptance: 12 kiểm tra SQL ở seed plan pass trên DB tạm; database tạm được xóa sau verify.

## 4. Phase P2 — Backend aggregate và consumer

### P2.1 Descriptive attribute service mới

- DTO type mới `SELECT_ONE/SELECT_MULTI`.
- Tạo definition/option chưa verified dùng ngay.
- Áp dụng merge definition/option một tầng; target bắt buộc chưa merge và tầng đọc chỉ dùng mapping trực tiếp.
- Value một bảng, validate đúng cột/type.
- NUMBER dùng `default_unit`; `product_attribute_value.unit` chỉ lưu override khác mặc định.
- Bỏ giới hạn 50 descriptive attributes.
- Category suggestion/required/filterable/display order.

Test bắt buộc: TEXT, NUMBER/default unit/unit override, one/multi select, option mới shared, merge một tầng/target đã merge bị từ chối, hidden, type mismatch, category required; có test race hai request đồng thời để TEXT/NUMBER/SELECT_ONE không tạo hơn một value cho cùng `(product, definition)`.

### P2.2 Variant aggregate service

- Validate 0/1/2 axis.
- Sinh/validate tổ hợp, canonical combination key.
- Mọi variant có `combination_key` khác null; unique `(product_id, combination_key)` là constraint bắt buộc.
- Default variant invariant.
- Atomic create/update aggregate product.
- Ownership, SKU, price, stock, status.

Test bắt buộc: default variant, one axis, two axes, omitted combination, duplicate, missing mapping, more than two axes, concurrent duplicate SKU/combination.

### P2.3 Seller và Admin API

- Thay Seller product/variant CRUD cũ bằng aggregate API.
- Category tree và autocomplete.
- Admin two-tab endpoints, moderation audit và reindex queue.
- Xóa Admin hard-coded attribute controllers/routes.

Acceptance: MockMvc/contract tests; no-token/Buyer token/Admin/Seller role matrix qua gateway ở runtime sau cutover.

### P2.4 Public và internal API

- Public summary/detail/filter canonical.
- Internal typed variant snapshot, bulk lookup, stock adjust.
- Xóa internal colors/sizes và hard-coded search params.

Acceptance: JSON schema/contract tests cho product 0/1/2 axis.

### P2.5 Cập nhật cart/order/promotion

- DTO typed thay Map.
- Cart dùng server price/stock và variant label generic.
- Order snapshot generic, stock flow không đổi invariant variant ID.
- Promotion chọn variant generic, bỏ color/size APIs.

Acceptance:

- `:cart-service:test`, `:order-service:test`, `:promotion-service:test`.
- Compile toàn backend bằng JDK 17, `--no-daemon --max-workers=1`.
- Security/ownership test không cho seller thao tác variant shop khác.

## 5. Phase P3 — Search/Outbox

### P3.1 Document và mapping mới

- Bỏ `brandId/brand` top-level.
- Index `attributes` nested và `variants` nested với selections cùng variant.
- Cập nhật outbox builder, seed payload, sink mapping.
- Sửa zero-hit/fallback semantics.

### P3.2 Reindex và CDC

- Tạo versioned concrete index mới.
- Deploy connector/preflight credential.
- Bulk reindex, verify mapping strict/count/sample query rồi switch alias.

Acceptance:

- Query descriptive multi-filter đúng.
- Query hai axis không cross-match giữa hai variant.
- Category filter và price filter dùng variant active.
- Outbox create/update/hide/merge làm document thay đổi end-to-end.

Nếu Debezium credential vẫn thiếu, ghi rõ source/compile pass nhưng CDC runtime blocked; không gọi Phase P3 DONE.

## 6. Phase P4 — FE Seller

### P4.1 Form aggregate

- Category tree chọn trước.
- Descriptive suggestion/autocomplete/free create, không giới hạn 50.
- Khối Phân loại riêng, 0/1/2 axis tùy tên.
- Sinh ma trận, bỏ tổ hợp, bulk price/stock, ảnh/SKU mỗi variant.
- Cảnh báo đổi category và diff khi sửa axis.

### P4.2 Danh sách/quản lý

- Tồn kho/giá tổng hợp từ variants.
- Variant label render theo selections, không `tenMau/kichThuoc`.
- Low-stock và status dùng canonical variant API.

Acceptance: `vue-tsc --noEmit`, Vite build, browser desktop/mobile tạo/sửa một product mỗi case 0/1/2 axis.

## 7. Phase P5 — FE Platform Admin

- Màn Quản lý thuộc tính có hai tab.
- Tab descriptive: verify/standardize/category/merge option+definition/hide/audit.
- Tab axes: insights, suggestion verify/merge/hide.
- Xóa route/menu/page/API CRUD Brand/Origin/Material/Sole/Color/Size cũ.

Acceptance: typecheck/build; browser smoke từng mutation và kiểm tra Seller autocomplete/Buyer filter thay đổi sau reindex.

## 8. Phase P6 — FE Buyer

- Product list type canonical; bỏ alias field cứng.
- Filter descriptive + axis tự đổi theo category và chỉ hiện value đang dùng.
- Detail render thông số, selector 0/1/2 axis và disable tổ hợp không tồn tại/hết hàng.
- Cart/checkout/history render `variantLabel/selections` generic.
- Navbar/home/shop/promotion UI bỏ phụ thuộc Thương hiệu/Size hard-coded toàn cục.

Acceptance:

- Typecheck/build.
- Browser desktop/mobile cho giày, điện thoại, serum default variant.
- Add cart, checkout, order history giữ đúng variant selection.

## 9. Phase P7 — Cutover DB dev và E2E

Chỉ bắt đầu khi P1–P6 source/build pass.

1. Dừng app/connector ghi catalog.
2. Xác nhận target DB dev/local và quyền reset.
3. Chạy reset/schema/seed mới.
4. Start MySQL, Elasticsearch, Kafka/Connect và service liên quan.
5. Reindex/switch alias.
6. Gateway smoke Seller/Admin/Buyer/cart/order/promotion.
7. Browser E2E desktop/mobile.
8. Scan log, DB invariant, ES document count.

Definition of Done cuối:

- Không còn sáu bảng cũ trong DB và không còn entity/repository/endpoint/type FE tương ứng.
- Không còn hard-code Màu/Size trong logic variant; chỉ còn text minh họa/test fixture hoặc dữ liệu seed.
- Product ba ngành dùng cùng aggregate API.
- 0/1/2 axis hoạt động; default variant invariant đúng.
- Cart/order/promotion/search chạy trên variant ID và selection generic.
- Backend test/compile, FE typecheck/build, DB seed, CDC/ES và gateway/browser runtime đều có bằng chứng hiện tại.

## 10. Task tiếp theo

Task tiếp theo là **P7: Cutover DB dev và E2E**. Chỉ chạy reset/cutover `ecommerce_catalog` và CDC sau khi có phê duyệt rõ ràng.
