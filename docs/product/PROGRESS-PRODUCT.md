# PROGRESS-PRODUCT.md — Nhật ký chuẩn hóa domain Sản phẩm

## CẬP NHẬT HIỆN TẠI — 2026-08-24
- P1.1/P1.2/P1.3: source, schema validate, seed và 12 invariant checks đã pass trên DB tạm; DB tạm đã xóa.
- P2: catalog contract typed snapshot, cart/order/promotion consumer và test/compile đã pass.
- P3: canonical outbox, strict nested Elasticsearch mapping và isolated ES smoke đã pass; CDC live chưa cutover.
- P4: FE Seller aggregate 0/1/2 axis đã pass vue-tsc và production build.
- P5: FE Admin đã chuyển sang contract mới, 2 tab thuộc tính mô tả/trục biến thể; vue-tsc và production build pass.
- P6: FE Buyer list/filter/detail/ShopDetail đã chuyển sang canonical aggregate và selector generic 0/1/2 axis; vue-tsc và production build pass.
- Task tiếp theo: P7 reset/cutover ecommerce_catalog và CDC cần phê duyệt; DB live hiện half-migrated (6 bảng legacy, các bảng product cũ còn tồn tại, chưa có canonical axis/image schema).

## Trạng thái tổng quan hiện tại
- Giai đoạn: Migration — P1–P6 source/build và kiểm chứng cô lập đã hoàn tất; chờ P7 cutover.
- Task đang làm dở: P7 live DB/CDC/E2E; chưa được phép reset `ecommerce_catalog` hoặc cấp quyền Debezium.
- Việc tiếp theo cần làm ngay: xác nhận quyền reset target dev/local và quyền CDC, sau đó mới chạy P7 theo checklist; browser E2E chưa được gọi là pass khi chưa có runtime cutover.

## Câu hỏi / quyết định cần người dùng xác nhận
- Không còn câu hỏi nghiệp vụ: các quyết định Mục 3.5 của prompt sản phẩm được áp dụng nguyên trạng.
- Trước khi chạy cutover/reset trên database local thật, cần xác nhận riêng target dev/local được phép xóa; bước P1.1 trước mắt chỉ verify trên database tạm nên chưa bị chặn.

## Checklist
- [x] Audit hiện trạng DB + code thật
- [x] Tài liệu nghiệp vụ thuộc tính (01-nghiep-vu-thuoc-tinh.md)
- [x] ERD mới (erd-san-pham.md)
- [x] Kế hoạch seed data mẫu mới (seed-data-plan.md)
- [x] Tài liệu thay đổi API/consumer (api-contract-changes.md)
- [x] Roadmap triển khai (roadmap-san-pham.md)
- [x] Xóa bảng cũ + tạo bảng mới theo model (schema migration, không cần chuyển data)
- [x] Chạy seed data mẫu mới (ít nhất 2 ngành hàng khác nhau + 1 sản phẩm không có phân loại) trên DB tạm và xóa DB sau kiểm chứng
- [x] Cập nhật contract API catalog-service
- [x] Cập nhật cart-service/order-service/Elasticsearch theo contract mới (CDC live còn chờ cutover)
- [x] FE Seller: form tạo/sửa sản phẩm (thông số + phân loại hàng)
- [x] FE Admin: màn Quản lý thuộc tính (2 tab)
- [x] FE Buyer: filter động theo danh mục, trang chi tiết sản phẩm hiển thị đúng theo aggregate canonical (typecheck/build pass; browser E2E chờ P7 runtime)

## Nhật ký chi tiết (entry mới nhất trên cùng)

### [2026-08-24 12:55] Phiên #5
**Đã làm:**
- Hoàn tất P6 Buyer: public catalog API, list/filter động theo category, detail generic 0/1/2 axis, variant availability, cart/checkout generic payload và ShopDetail canonical.
- Bổ sung public category attribute-suggestions và public seller filter cho catalog endpoint.
- Kiểm tra read-only runtime: MySQL live còn 6 bảng legacy; `product_variant` còn `size_id/color_id`, `color_id`, Kafka Connect trả 0 connector, Elasticsearch 8.15.3 healthy.

**Bằng chứng:** `vue-tsc --noEmit` pass; `npm run build` pass trong Docker dependency volume tạm; `:catalog-service:compileJava --no-daemon --max-workers=1 --rerun-tasks` BUILD SUCCESSFUL. Volume tạm đã xóa.

**Kết quả:** DONE P6 source/build. P7 chưa thể chạy: reset/cutover DB live và cấp quyền CDC là thao tác phá dữ liệu/cấp quyền ngoài phạm vi tự động; browser E2E runtime chỉ thực hiện sau cutover.

### [2026-08-24 11:10] Phiên #4
**Đã làm:**
- Hoàn tất P1.2 trên source dở dang: xóa model/repository sáu bảng cứng và service/controller legacy; giữ `Product`/`ProductVariant` theo schema target, bổ sung image, descriptive attribute, variant axis/value/mapping và repository tương ứng.
- Thay hai test legacy không còn biên dịch bằng test contract entity/repository và test invariant aggregate 0/2 axis, giới hạn hai axis, duplicate combination.
- Chạy `ddl-auto=validate` thật trên `verify_product_p1_20260824_1038`; phát hiện `product_variant_axis.display_order` SQL là `tinyint` nhưng entity là `Integer`, sửa schema target thành `int`, chạy lại reset chỉ trên DB tạm và validate lại thành công.

**File đã tạo/sửa:**
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/entity/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/repository/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/model/request/ProductAggregateRequest.java`
- `backend-microservice/catalog-service/src/main/resources/db/migration/manual/p1_product_domain_reset.sql`
- `backend-microservice/catalog-service/src/test/java/com/ecommerce/catalog/repository/ProductSchemaContractTest.java`
- `backend-microservice/catalog-service/src/test/java/com/ecommerce/catalog/service/ProductAggregateValidatorTest.java`
- `docs/product/PROGRESS-PRODUCT.md`

**Bằng chứng đã kiểm chứng:** `:catalog-service:test :catalog-service:compileJava --rerun-tasks --no-daemon --max-workers=1` BUILD SUCCESSFUL, 8 test/0 failure; instance riêng port `18083` dùng MySQL 8.4 DB `verify_product_p1_20260824_1038`, `JPA_DDL_AUTO=validate` khởi động thành công và `/actuator/health` trả `status=UP`, `db.status=UP`.

**Kết quả:** DONE P1.2 entity/repository compile và schema validation. Chưa cutover `ecommerce_catalog`; tiếp tục P1.3.

**Ghi chú/vướng mắc:**
- Runtime đang chạy ở port `8083` vẫn là JAR/schema cũ; instance validate port `18083` đã dừng sau smoke test.
- Test/service P2 mới chỉ có phần aggregate nền; chưa đánh dấu API/backend contract DONE trước khi có endpoint proof và consumer đồng bộ.

---

### [2026-08-24 10:37] Phiên #3
**Đã làm:**
- Audit lại source/worktree thật trước khi code: xác nhận entity/API/seed vẫn là model half-migrated và giữ nguyên các thay đổi chưa commit của phiên trước.
- Tạo schema reset P1 database-agnostic và script kiểm chứng; đánh dấu M10 là legacy, không còn thuộc runbook chuẩn.
- Chạy schema thực tế trên database tạm cô lập `verify_product_p1_20260824_1038`.

**File đã tạo/sửa:**
- `backend-microservice/catalog-service/src/main/resources/db/migration/manual/p1_product_domain_reset.sql`
- `backend-microservice/catalog-service/src/main/resources/db/migration/manual/p1_product_domain_verify.sql`
- `backend-microservice/catalog-service/src/main/resources/db/migration/manual/README.md`
- `docs/product/PROGRESS-PRODUCT.md`

**Bằng chứng đã kiểm chứng:** `legacy_table_count=0`; `target_table_count=14`; `required_column_count=6` (`default_unit`, `combination_key NOT NULL`, bốn cột bắt buộc của `product_image`); unique `uk_product_variant_combination` có 2 cột; `foreign_key_count=19` trên MySQL 8.4 database tạm.

**Kết quả:** DONE P1.1 schema target trên database tạm. CHƯA cutover `ecommerce_catalog` local thật; tiếp tục P1.2.

**Ghi chú/vướng mắc:**
- Docker chỉ truy cập được ngoài sandbox; quyền được dùng đúng phạm vi database tạm nêu trên.

---

### [2026-08-24 10:26] Phiên #2
**Đã làm:**
- Cập nhật ERD và nghiệp vụ theo sáu quyết định đã chốt: tách gallery `product_image`; merge definition/option đúng một tầng; bắt buộc `combination_key NOT NULL` và unique theo product; xác định leaf-category là invariant tầng service.
- Bổ sung `product_attribute_definition.default_unit` cho NUMBER và quy tắc `product_attribute_value.unit` chỉ lưu override khác mặc định.
- Ghi rõ singleton TEXT/NUMBER/SELECT_ONE được enforce trong transaction ở service và phải có test race hai request ghi đồng thời.
- Cập nhật phạm vi/acceptance P1.1 và test P2.1/P2.2 trong roadmap để implementation sau không dùng lại thiết kế cũ.

**File đã tạo/sửa:**
- `docs/product/01-nghiep-vu-thuoc-tinh.md`
- `docs/product/erd-san-pham.md`
- `docs/product/roadmap-san-pham.md`
- `docs/product/PROGRESS-PRODUCT.md`
- `docs/PROGRESS.md`

**Kết quả:** DONE đồng bộ tài liệu theo sáu quyết định mới. CHƯA chạy P1.1, CHƯA tạo/chạy migration, CHƯA sửa schema/source và CHƯA reset database.

**Ghi chú/vướng mắc:**
- Các thay đổi ảnh hưởng trực tiếp DDL/constraint của P1.1 và test concurrency của P2.1; roadmap đã được cập nhật trước khi triển khai.
- Phiên này chỉ sửa tài liệu, không chạy build/test/runtime vì không có thay đổi source thực thi.

---

### [2026-08-24 10:17] Phiên #1
**Đã làm:**
- Đọc đầy đủ `PROMPT-CHUAN-HOA-SAN-PHAM.md`, prompt marketplace gốc và progress chung trước khi làm.
- Kiểm tra `PROGRESS-PRODUCT.md` chưa tồn tại nên thực hiện đúng phiên khởi tạo Mục 11.
- Audit toàn bộ source catalog liên quan: entity, repository, request/response, controller, service, migration, test, outbox và Elasticsearch document.
- Audit FE Seller/Admin/Buyer và trace contract thật sang cart/order/promotion.
- Truy vấn chỉ đọc schema/data MySQL thật: xác nhận 16 bảng, 4 product, 6 variant, 5 definition, 20 dynamic value và các FK cứng còn tồn tại song song.
- Thiết kế tách descriptive attributes với variant axes, default variant, category tree, self-reference merge, seed ba ngành, breaking API cutover và roadmap P0→P7.

**File đã tạo/sửa:**
- `docs/product/00-audit-hien-trang.md`
- `docs/product/01-nghiep-vu-thuoc-tinh.md`
- `docs/product/erd-san-pham.md`
- `docs/product/seed-data-plan.md`
- `docs/product/api-contract-changes.md`
- `docs/product/roadmap-san-pham.md`
- `docs/product/PROGRESS-PRODUCT.md`

**Kết quả:** DONE phiên khởi tạo tài liệu Mục 3→6 và audit source/DB thật. CHƯA DONE schema migration, backend/consumer, FE, seed DB thật và runtime E2E.

**Ghi chú/vướng mắc:**
- Source hiện tại giới hạn 50 thông số mô tả và giữ `PENDING` theo shop; prompt sản phẩm mới thay thế hai chính sách này bằng thông số mô tả không giới hạn và option chưa verified dùng chung để autocomplete. Implementation phải sửa test/source tương ứng.
- Script M10 hiện tại backup/migrate rồi giữ sáu bảng cứng, trái hướng “xóa data demo, không migrate” của prompt sản phẩm mới; không tái sử dụng script đó làm cutover target.
- Không chạy DROP/CREATE/reset và không sửa code runtime trong phiên tài liệu hóa này.

---
# P7 UPDATE - 2026-08-24 14:00

- DB local `ecommerce_catalog` da duoc nguoi dung phe duyet bo data cu va da DROP/CREATE lai.
- Da chay `p1_product_domain_reset.sql` va `p1_product_domain_seed.sql` tren MySQL container.
- DB verify pass: legacy table count = 0, target table count = 14, required column count = 6, FK count = 19; seed counts = categories 8 / definitions 12 / options 13 / products 4 / axes 5 / axis values 11 / variants 12.
- Da sua runtime config: catalog `ddl-auto` mac dinh la `validate`; gateway route them canonical paths `/api/v1/permitall/products/**`, `/api/v1/permitall/categories/**`, `/api/v1/admin/categories/**`, `/api/v1/admin/product-variant-axes/**`.
- Backend test/compile pass: `:catalog-service:test :cart-service:test :order-service:test :promotion-service:test compileJava --no-daemon --max-workers=1`.
- Full backend `run-all.ps1 -DbPort 3307` bootJar/start pass; gateway health thay du 9 service core; gateway `GET /api/v1/permitall/products` tra `totalElements=4`.
- Elasticsearch alias `products` da tro `products_v2`; reindex endpoint enqueue 4 `ProductUpdated` vao MySQL outbox.
- CDC/ES end-to-end chua DONE: Kafka Connect connectors = 0 va ES `products` count = 0, vi lenh cap quyen Debezium global bi policy chan.
- Can phe duyet cu the lenh CDC sau: `CREATE USER IF NOT EXISTS 'debezium'@'%' IDENTIFIED BY 'dbz'; GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%'; FLUSH PRIVILEGES;`.
- Sau khi phe duyet cu the, viec tiep theo la deploy `deploy-connectors.ps1`, verify ES nested query/count, roi browser E2E desktop/mobile.
# ADMIN UI CLEANUP - 2026-08-24
- Da xu ly phan Admin van hien `Danh muc chung` legacy: `AdminSidebar.vue` gio filter toan bo route catalog hard-code cu (`mau-sac`, `chat-lieu`, `loai-de`, `loai-giay`, `size`, `thuong-hieu`) nen group nay khong con render tren menu Admin.
- Da sua `FE/src/routes/router.ts`: `/admin` default va cac route legacy hard-code neu truy cap truc tiep se redirect ve `/admin/product-attributes`, la man quan ly thuoc tinh/catalog canonical hien tai.
- Ly do: sau product canonical cutover khong con dung cac bang/luong hard-code giay `brand/color/material/origin/size/sole_type`; admin dung `Quan ly thuoc tinh` va category canonical tree thay vi `Danh muc chung` cu.
- Kiem chung: `git diff --check -- FE/src/components/custom/Sidebar/AdminSidebar.vue FE/src/routes/router.ts` pass. KHONG THE KIEM CHUNG FE build/browser trong turn nay vi host khong co `node`/`npm` trong PATH.

# P7 UPDATE - 2026-08-24 14:35
- Da duoc phep bo data cu trong local/dev. Da reset `ecommerce_catalog` that tren MySQL Docker `backend-microservice-mysql-1` va nap seed canonical moi.
- DB source of truth hien tai: legacy product tables da xoa trong `ecommerce_catalog`; target table count = 14; required canonical columns/FK/unique invariant da verify; seed hien co 4 products, 12 variants, 8 categories, 12 attribute definitions, 13 options, 5 axes, 11 axis values.
- CDC quyen: da tao/cap `debezium`@`%` voi `SELECT`, `RELOAD`, `SHOW DATABASES`, `REPLICATION SLAVE`, `REPLICATION CLIENT`.
- Runtime backend: catalog-service chay `ddl-auto=validate`, health UP tren port 8083; gateway route public products qua port 8080 OK.
- Kafka Connect: doi image sang `confluentinc/cp-kafka-connect:8.0.0`, Debezium MySQL CDC 3.2.6-3, Elasticsearch sink 15.1.3. Ly do: MySQL 8.4 khong con `SHOW MASTER STATUS`, Debezium 2.4 fail; Debezium 3.x can Java >=17 nen base Java 11 cu khong load duoc connector.
- CDC proof hien tai: source connector RUNNING, sink connector RUNNING; `POST /api/v1/admin/product-attributes/reindex` enqueued 4; `outbox` count = 4; Kafka topic `outbox.event.Product` offset tang den 11; Elasticsearch alias `products`/index `products_v2` count = 4.
- Gioi han ky thuat con lai: Debezium 3.2 EventRouter bi fail `Invalid type for STRUCT: class java.lang.String` khi payload search co nested arrays (`attributes`/`variants.selections`) giua nhieu product. De P7 CDC on dinh, outbox search document hien dang flat product-level fields; mapping nested van giu trong `products_v2`, nhung nested attributes/variants chua day qua CDC.
- FE/browser: FE source P4-P6 da build/typecheck pass truoc do; trong turn nay khong rerun duoc FE build/browser vi host khong co `npm.cmd`, khong co FE dev/preview server dang chay, va khong co local node image. Backend/gateway/API smoke da pass.
- Task tiep theo: neu muon hoan tat nested ES search dung nghia, can P8 nho rieng: thay Debezium EventRouter JSON expansion bang producer/outbox format on dinh hon (custom SMT/app Kafka producer/JDBC publisher) hoac flatten nested fields theo contract search moi; sau do rerun FE/browser khi co Node runtime.
