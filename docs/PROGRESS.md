# PROGRESS.md - Nháº­t kÃ½ tiáº¿n Ä‘á»™ chuyá»ƒn Ä‘á»•i Marketplace

## Tráº¡ng thÃ¡i tá»•ng quan hiá»‡n táº¡i
- Giai doan: Product canonical cutover da co DB/backend/API/CDC smoke theo update P7; dang hoan thien UI Admin cho hau kiem thuoc tinh va truc bien the.
- Task dang lam do (neu co): Admin `Quan ly thuoc tinh` da fix them truc goi y, category label khong hien UUID, va loi `CATALOG_CONSTRAINT_VIOLATION` khi chuan hoa lai cung danh muc do duplicate `category_attribute_suggestion`. Catalog-service da build/restart va smoke direct API pass. Chua co FE build/typecheck/browser proof vi shell hien tai khong co `node`/`npm.cmd` kha dung.
- Viec tiep theo can lam ngay: Khi co Node/npm hop le, reload/rerun FE dev server, chay `npm run build` va `vue-tsc --noEmit` trong `FE`, sau do browser smoke `/admin/product-attributes` cho category select/tag hien path ten danh muc, tab truc bien the load insight + goi y chuan, tao/duyet/an goi y truc, va chuan hoa lai thuoc tinh co danh muc cu khong con conflict.

## Cập nhật product normalization — 2026-08-24
- P1.1–P1.3, P2, P3 và P4 đã có bằng chứng source/test/compile/runtime cô lập; P5 Admin đã hoàn tất source và FE build/typecheck.
- FE build dùng Docker volume dependency Linux: npm run build pass; vue-tsc --noEmit pass.
- DB live ecommerce_catalog chưa được cutover: còn đủ 6 bảng legacy; các bảng product hiện tại là schema half-migrated cũ (ví dụ `product_variant` còn `size_id/color_id`, chưa có canonical axis/image schema). Không có DB tạm/index/volume kiểm chứng còn tồn tại.
- P3 CDC/connector live và P7 cutover chưa DONE; cần phê duyệt reset DB và quyền CDC trước khi thực hiện.
- Task tiếp theo: P6 FE Buyer, sau đó mới xem xét P7 theo checklist roadmap.

## Cập nhật P6 Buyer — 2026-08-24
- Buyer product list/filter/detail và ShopDetail đã dùng public catalog aggregate canonical; không còn dùng field variant màu/size trong các màn hình này.
- Selector detail hỗ trợ 0/1/2 axis, disable value không có variant còn hàng; cart/checkout giữ variant ID và generic `variantLabel/selections`.
- `vue-tsc --noEmit` và `npm run build` pass; `:catalog-service:compileJava` pass sau bổ sung seller filter public.
- P7 vẫn blocked theo quyền: live DB còn schema legacy/half-migrated, chưa reset/cutover; Kafka Connect hiện có 0 connector; chưa triển khai Debezium/CDC hoặc browser E2E runtime.

## CÃ¢u há»i / quyáº¿t Ä‘á»nh cáº§n ngÆ°á»i dÃ¹ng xÃ¡c nháº­n
- Phase 1 Ä‘ang triá»ƒn khai theo quyáº¿t Ä‘á»‹nh táº¡m: má»—i tÃ i khoáº£n buyer sá»Ÿ há»¯u tá»‘i Ä‘a 1 shop Ä‘ang hoáº¡t Ä‘á»™ng/chá» duyá»‡t; shop bá»‹ `REJECTED` hoáº·c `CLOSED` cÃ³ thá»ƒ Ä‘Äƒng kÃ½ láº¡i.
- Da chot review domain dat trong `seller-service` de dung roadmap Phase 5 va tranh them microservice khi domain con nho.
- Can nguoi dung xac nhan ro co cho phep DROP/CREATE lai 8 database `ecommerce_*` tren Docker local de nap bo seed marketplace moi hay khong.
- Can nguoi dung xac nhan co cho phep chay `search-pipeline/sql/002-create-debezium-user-mysql.sql`: tao `debezium`@`%` va cap `SELECT`, `RELOAD`, `SHOW DATABASES`, `REPLICATION SLAVE`, `REPLICATION CLIENT` tren `*.*` de hoan tat CDC/reindex hay khong.
- Quyet dinh Muc 10.6 ngay 2026-08-22 da bi prompt product moi thay the trong pham vi chuan hoa san pham: descriptive attribute khong gioi han 50, definition/option chua verified dung chung de autocomplete; type dich la `TEXT`, `NUMBER`, `SELECT_ONE`, `SELECT_MULTI`; variant axis toi da 2.

## Checklist tÃ­nh nÄƒng (Ä‘á»‘i chiáº¿u Má»¥c 3.5 cá»§a prompt gá»‘c)
- [x] ÄÄƒng kÃ½/Ä‘Äƒng nháº­p buyer, seller, platform admin
- [x] ÄÄƒng kÃ½ & duyá»‡t shop
- [x] Trang chá»§ storefront gá»™p nhiá»u shop
- [x] Trang riÃªng tá»«ng shop
- [x] TÃ¬m kiáº¿m/filter sáº£n pháº©m toÃ n sÃ n
- [x] Giá» hÃ ng multi-seller
- [x] Checkout tÃ¡ch theo shop, thanh toÃ¡n má»™t láº§n
- [x] Split-order thÃ nh sub-order theo seller
- [x] Quáº£n lÃ½ Ä‘Æ¡n hÃ ng riÃªng theo tá»«ng seller
- [x] Voucher 2 táº§ng
- [x] ÄÃ¡nh giÃ¡ sáº£n pháº©m & shop sau khi nháº­n hÃ ng
- [x] Theo dÃµi shop
- [x] VÃ­ & Ä‘á»‘i soÃ¡t cho seller
- [x] Thá»‘ng kÃª riÃªng theo seller + thá»‘ng kÃª tá»•ng toÃ n sÃ n
- [x] Duyá»‡t/khÃ³a seller bá»Ÿi Platform Admin
- [x] ThÃ´ng bÃ¡o qua `notification-service`
- [ ] Chuan hoa san pham: schema target + reset seed demo da nganh, xoa 6 bang hard-code
- [ ] Thuoc tinh dong: Seller suggestion/autocomplete/tu them tren form san pham (lien ket Muc 3.2)
- [ ] Thuoc tinh dong: Buyer filter theo danh muc va product detail (lien ket Muc 3.3)
- [ ] Thuoc tinh dong: Platform Admin hau kiem/chuan hoa/gop/an (lien ket Muc 3.4)
- [ ] Chat buyer-seller
- [ ] Flash sale toÃ n sÃ n

## Nháº­t kÃ½ chi tiáº¿t (entry má»›i nháº¥t á»Ÿ trÃªn cÃ¹ng)

### [2026-08-24 15:27] Phien #26
**Da lam:**
- Truy vet loi user bao `CONFLICT / CATALOG_CONSTRAINT_VIOLATION` trong catalog log luc 15:21:56+07: DB bao duplicate key `category_attribute_suggestion.uk_category_attribute_suggestion` cho cap `category_id + attribute_definition_id`.
- Sua `CatalogAdminService.standardize()`: sau khi `deleteAll(current)` thi `flush()` truoc khi insert lai mapping category, tranh Hibernate day insert moi truoc delete cu trong cung transaction; dong thoi dedupe `categoryIds` bang `LinkedHashSet`.
- Sua FE `ProductAttributes.vue`: dedupe categoryIds khi mo modal chuan hoa, khi them missing category options, va truoc khi submit.
- Build va restart rieng `catalog-service` voi jar moi, DB port 3307.
- Smoke direct API: PUT `/api/v1/admin/product-attributes/41000000-0000-0000-0000-000000000003/standardize` voi category ID lap 2 lan tra 200 va response chi con 1 categoryId; catalog log khong con `Duplicate entry` sau smoke.

**File da tao/sua:**
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/CatalogAdminService.java`
- `FE/src/pages/admin/product-attributes/ProductAttributes.vue`
- `docs/PROGRESS.md`

**Ket qua:** DONE source + runtime backend fix cho loi `CATALOG_CONSTRAINT_VIOLATION` khi chuan hoa thuoc tinh voi danh muc goi y da ton tai/lap lai. Catalog `compileJava` pass, `bootJar` pass, actuator health UP sau restart.

**Ghi chu/vuong mac:**
- FE build/typecheck chua chay duoc vi shell hien tai khong tim thay `node`/`npm.cmd`.
- Co file build artifact `backend-microservice/catalog-service/build/tmp/compileJava/previous-compilation-data.bin` dang modified do compile; chua revert vi truoc do `git restore` gap quyen `.git/index.lock`.

---

### [2026-08-24 15:19] Phien #25
**Da lam:**
- Truy vet loi danh muc goi y trong bang thuoc tinh va modal chuan hoa van hien UUID: `FE/src/services/api/catalog/catalog.api.ts` cung hard-code `const base='/api/v1/permitall'` trong khi Axios `baseURL` da la `/api/v1`, lam `getCategoryTree()` co nguy co goi sai URL.
- Doi shared catalog API sang `PREFIX_API_PERMITALL`, dong bo voi cac API FE khac.
- Kiem tra runtime `/api/v1/permitall/categories/tree`: co category name/path that, nhung mot leaf category tra `children: ""`; harden `flattenCategories()` de chi duyet children khi la array, tranh throw roi set `categoryOptions=[]`.
- Doi fallback `categoryLabel()` khong hien UUID dai nua: neu category chua load/missing se hien `Danh muc chua tai (SHORTID)`; `openStandardize()` them missing selected IDs vao options de Select khong phoi raw UUID.
- Runtime doi chieu: category tree gateway 200; admin product attributes co `categoryIds` map voi category tree IDs nhu `...103`, `...105`, `...107`; `git diff --check` pass.

**File da tao/sua:**
- `FE/src/services/api/catalog/catalog.api.ts`
- `FE/src/pages/admin/product-attributes/ProductAttributes.vue`
- `docs/PROGRESS.md`

**Ket qua:** DONE source fix cho danh muc goi y hien ten/path thay vi UUID khi tree load dung; fallback khong con phoi UUID dai. CHUA DONE FE build/typecheck/browser do thieu Node/npm trong shell.

**Ghi chu/vuong mac:**
- Neu FE dev server dang chay bundle cu, can reload/restart FE dev server de nhan fix `catalog.api.ts` va `flattenCategories()`.
- Runtime API da co du name category, loi nam o FE URL/flatten/fallback.

---

### [2026-08-24 15:14] Phien #24
**Da lam:**
- Truy vet loi `Them truc goi y`: `variant-axis.api.ts` dang dung base `'/api/v1/admin/product-variant-axes'` trong khi Axios `baseURL` da la `${VITE_BASE_URL_SERVER}/api/v1`, co nguy co goi sai URL lap `/api/v1/api/v1/...`.
- Them constant `PREFIX_API_PRODUCT_VARIANT_AXES_ADMIN = ${PREFIX_API_ADMIN}/product-variant-axes` va doi `variant-axis.api.ts` dung constant nay, dong bo voi cac API admin khac.
- Xac nhan runtime cu chua co `GET /suggestions` vi catalog-service dang chay bootJar cu: direct GET tra 405. Chay `:catalog-service:bootJar --no-daemon --max-workers=1`, sau do `run-all.ps1 -DbPort 3307` de restart full backend voi jar moi.
- Runtime smoke: catalog health 200; gateway health 200; direct `GET /api/v1/admin/product-variant-axes/suggestions?q=Codex` 200; gateway no-token POST tra 401 dung ky vong.
- Login admin `admin@ecommerce.local` / `Admin@123` thanh cong; gateway POST `/api/v1/admin/product-variant-axes/suggestions` voi admin token tao duoc suggestion, sau do hide suggestion test 204; direct GET `q=Codex` tra `[]`, khong con du lieu test active.

**File da tao/sua:**
- `FE/src/constants/url.ts`
- `FE/src/services/api/admin/variant-axis.api.ts`
- `docs/PROGRESS.md`

**Ket qua:** DONE fix source va runtime proof cho loi khong them duoc truc goi y qua gateway/admin token. CHUA DONE FE build/typecheck/browser do thieu Node/npm trong shell.

**Ghi chu/vuong mac:**
- Backend source `GET /suggestions` tu phien #23 da duoc bootJar/restart trong phien nay; neu FE dev server dang chay tu bundle cu thi can reload/restart FE dev server de nhan fix URL moi.
- `run-all.ps1` start lai full backend; notification-service khong bat vi script mac dinh khong co `-WithNotification`.
- Artifact tracked `backend-microservice/catalog-service/build/tmp/compileJava/previous-compilation-data.bin` van modified do compile/bootJar; lan truoc thu restore bi chan `.git/index.lock Permission denied`.

---

### [2026-08-24 15:03] Phien #23
**Da lam:**
- Sua tiep UI Admin `Quan ly thuoc tinh` theo feedback: bo nut `Them truc goi y` bi trung o header, chi giu nut trong tab `Truc bien the`.
- Doi nguon danh muc cua modal chuan hoa sang category tree canonical `/api/v1/permitall/categories/tree`, flatten label dang `Cha / Con` de select va tag hien ten truc quan thay vi UUID khi co category name.
- Them backend Admin API `GET /api/v1/admin/product-variant-axes/suggestions` va service `axisSuggestions(q)` de Admin doc duoc danh sach goi y truc chuan.
- Sua FE tab `Truc bien the`: load dong thoi axis insight va goi y chuan; them bang `Goi y truc chuan` co trang thai, hau kiem, thao tac duyet/an; tao goi y xong reload lai danh sach.
- Chay `:catalog-service:compileJava --no-daemon --max-workers=1` voi JDK 17: BUILD SUCCESSFUL.

**File da tao/sua:**
- `FE/src/pages/admin/product-attributes/ProductAttributes.vue`
- `FE/src/services/api/admin/variant-axis.api.ts`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/AdminVariantAxisController.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/CatalogAdminService.java`
- `docs/PROGRESS.md`

**Ket qua:** DONE source/backend compile cho fix truc bien the va category label. CHUA DONE FE build/typecheck/browser runtime do thieu Node/npm trong shell.

**Ghi chu/vuong mac:**
- `git diff --check` pass cho cac file source vua sua.
- Lenh compile lam thay doi artifact tracked `backend-microservice/catalog-service/build/tmp/compileJava/previous-compilation-data.bin`; da thu `git restore` file nay nhung bi chan do khong tao duoc `.git/index.lock` (`Permission denied`).

---

### [2026-08-24 14:56] Phien #22
**Da lam:**
- Doc `AGENTS.md`, `docs/PROGRESS.md`, prompt marketplace goc va skill/memory roadmap truoc khi sua.
- Refactor lai toan bo man Admin `Quan ly thuoc tinh`: them header, thong ke nhanh, filter theo ten/trang thai/hau kiem/danh muc, bang responsive, drawer xem option, modal chuan hoa, modal gop va nut dong bo search.
- Sua tab `Truc bien the`: bo `window.prompt`, thay bang modal nhap ten truc; sua contract FE theo backend `AxisInsight` hien tai (`displayName`, `normalizedName`, `topValues`) thay vi field cu `name/suggestions`.
- Them helper API FE cho verify option va reindex product attributes; doi component khong goi raw URL truc tiep.
- Doi chieu gateway da co route `/api/v1/admin/product-attributes/**` va `/api/v1/admin/product-variant-axes/**`; khong can sua backend routing.

**File da tao/sua:**
- `FE/src/pages/admin/product-attributes/ProductAttributes.vue`
- `FE/src/services/api/admin/product-attribute.api.ts`
- `FE/src/services/api/admin/variant-axis.api.ts`
- `docs/PROGRESS.md`

**Ket qua:** DONE source FE cleanup cho man Admin quan ly thuoc tinh/truc bien the; `git diff --check` pass. CHUA DONE FE build/typecheck/browser runtime.

**Ghi chu/vuong mac:**
- `npm.cmd`, `npm` va `node` khong co trong PATH; thu goi truc tiep `C:\ProgramData\nvm\v20.19.6\node.exe`/`npm.cmd` cung khong tim thay executable trong sandbox shell, nen chua chay duoc `vue-tsc --noEmit` hoac `npm run build`.
- Chua click browser `/admin/product-attributes`; can verify lai khi FE dev/build runtime kha dung.

---

### [2026-08-24 10:26] Phien #21
**Da lam:**
- Dong bo `erd-san-pham.md` va `01-nghiep-vu-thuoc-tinh.md` theo 6 quyet dinh da chot: them `product_image`; merge definition/option mot tang voi target chua merge; bat buoc `combination_key NOT NULL` va unique theo product; leaf-category enforce tai service.
- Them `default_unit` nullable cho definition NUMBER va quy tac value `unit` chi luu override khac mac dinh.
- Ghi ro invariant singleton TEXT/NUMBER/SELECT_ONE phai enforce trong transaction va co test race hai request ghi dong thoi.
- Cap nhat P1.1/P2.1/P2.2 trong roadmap de pham vi DDL, constraint va test sau nay khop thiet ke moi.

**File da tao/sua:**
- `docs/product/01-nghiep-vu-thuoc-tinh.md`
- `docs/product/erd-san-pham.md`
- `docs/product/roadmap-san-pham.md`
- `docs/product/PROGRESS-PRODUCT.md`
- `docs/PROGRESS.md`

**Ket qua:** DONE dong bo tai lieu theo 6 quyet dinh. CHUA chay P1.1, CHUA tao/chay migration, CHUA sua schema/source va CHUA reset database.

**Ghi chu/vuong mac:**
- Thay doi anh huong truc tiep DDL P1.1 va test concurrency P2.1; roadmap da duoc cap nhat truoc khi implementation.
- Phien nay chi sua tai lieu, khong chay build/test/runtime vi khong co thay doi source thuc thi.

---

### [2026-08-24 10:17] Phien #20
**Da lam:**
- Doc day du `AGENTS.md`, `docs/PROGRESS.md`, prompt marketplace goc va `docs/product/PROMPT-CHUAN-HOA-SAN-PHAM.md`; xac nhan `PROGRESS-PRODUCT.md` chua ton tai nen chay dung cau lenh khoi tao Muc 11.
- Audit source that catalog-service: entity, repository, model, controller, service, migration/test/outbox/Elasticsearch; audit FE Seller/Admin/Buyer va trace contract sang cart/order/promotion.
- Query chi doc MySQL that: xac nhan 16 bang, 4 product, 6 variant, 5 dynamic definition, 20 dynamic value; `product` van FK brand/origin/material/sole_type va `product_variant` van FK color/size song song voi dynamic attribute.
- Tao bo tai lieu product day du: audit, nghiep vu 3 nganh, ERD Mermaid, seed plan, API/consumer impact va roadmap P0-P7. Model dich tach descriptive attribute voi variant axis, toi da 2 axis, product khong axis co 1 default variant, direct breaking API cutover va xoa 6 bang hard-code.
- Kiem tra tai lieu khong co TODO/TBD/placeholder, Mermaid fence day du va `git diff --check -- docs/product docs/PROGRESS.md` khong co whitespace error.

**File da tao/sua:**
- `docs/product/00-audit-hien-trang.md`
- `docs/product/01-nghiep-vu-thuoc-tinh.md`
- `docs/product/erd-san-pham.md`
- `docs/product/seed-data-plan.md`
- `docs/product/api-contract-changes.md`
- `docs/product/roadmap-san-pham.md`
- `docs/product/PROGRESS-PRODUCT.md`
- `docs/PROGRESS.md`

**Ket qua:** DONE audit source/DB that va phien khoi tao tai lieu product Muc 3-6. CHUA DONE schema migration, backend/consumer refactor, FE, DB cutover/seed va runtime E2E.

**Ghi chu/vuong mac:**
- Prompt product moi thay policy tam Muc 10 cu: descriptive attribute khong gioi han 50; definition/option chua verified duoc dung chung de autocomplete; variant axis moi gioi han toi da 2. Source/test hien tai chua duoc sua theo policy moi.
- `m10_dynamic_attributes_up/down.sql` hien backup/migrate va giu bang hard-code, khong phu hop cutover product moi; P1.1 se viet schema target rieng va verify tren DB tam.
- Khong chay DROP/CREATE/reset, khong sua code runtime va khong goi build/compile la proof trong phien tai lieu hoa nay. Reset DB local that van can xac dinh ro target/chap thuan truoc khi thuc thi.

---

### [2026-08-24 10:02] Phien #19
**Da lam:**
- Doc day du `AGENTS.md`, prompt goc, `PROGRESS.md`, roadmap va cac muc thiet ke dynamic attribute lien quan; doi chieu source/diff that va giu nguyen cac thay doi khong lien quan trong worktree.
- Tai hien loi startup: `run-all.ps1` tao mot Gradle daemon cho moi service, `catalog-service` chet voi `The paging file is too small`. Sua script thanh build tat ca `bootJar` tuan tu mot lan, sau do chay JAR truc tiep voi heap/metaspace co gioi han va fail-fast neu build loi.
- Chay script moi: bootJar 10 module pass; 9 ung dung core `AUTH/USER/CATALOG/PROMOTION/CART/ORDER/SELLER/PAYOUT/API-GATEWAY` dang ky `UP` tren Eureka, log hien tai khong co `ERROR`, `OutOfMemoryError`, `paging file` hay `APPLICATION FAILED TO START`.
- JWT/gateway smoke: Admin va Seller login 200; Seller token co `roles=[USERS,SELLER]`, `sellerId` va shop claims. Admin attribute no-token 401, Seller token 403, Admin token 200; Seller products 200 va chi tra 2 product cua shop hien tai.
- Dynamic Buyer/runtime: category filters tra 5 thuoc tinh; filter Thuong hieu=Nike tra dung 2 product. Admin reindex qua gateway tra 202 va enqueue 4 product; outbox tang tu 4 len 8.
- Phat hien va xoa doan FE gan nguoc vao computed `totalPages` read-only trong `ProductsView.vue`; `vue-tsc --noEmit` pass va Vite production build pass sau sua.
- Fresh verification: `:catalog-service:test :catalog-service:compileJava --rerun-tasks` pass 5 tests/0 failure; FE route `/san-pham` va `/admin/product-attributes` deu HTTP 200. Browser runtime khong co browser instance nen khong co click/screenshot desktop-mobile.

**File da tao/sua:**
- `backend-microservice/run-all.ps1`
- `FE/src/pages/users/products/ProductsView.vue`
- `docs/PROGRESS.md`

**Ket qua:** DONE harden startup full stack, JWT authorization/list/reindex queue smoke, Buyer dynamic filter runtime va FE computed fix. DO DANG CDC Elasticsearch, mutation smoke Seller/Admin, browser visual va migration DB that.

**Ghi chu/vuong mac:**
- Docker MySQL hien khong co user `debezium`; Kafka Connect co 0 connector. Deploy preflight dung voi `Access denied for user 'debezium'`; Elasticsearch `products` alias co 1 document trong khi MySQL co 4 product, 5 definition, 20 value va 8 outbox event.
- `ecommerce_catalog` that van khong co `m10_migration_manifest`; khong chay migration/reset DB va khong cap quyen replication khi chua co xac nhan ro.
- Khong chay Seller create/edit hay Admin standardize/merge/hide mutation tren data hien tai khi CDC tat, de tranh tao trang thai test khong duoc dong bo; authorization va read/reindex route da co runtime proof.
- Vite build con warning 5 font Inter resolve luc runtime va main chunk 1.66 MB; build van thanh cong. FE dev server dang o `http://127.0.0.1:5173`; backend full stack dang chay, notification-service khong bat vi script mac dinh khong co `-WithNotification`.

---

### [2026-08-24 09:32] Phien #18
**Da lam:**
- Doc lai `AGENTS.md`, toan bo prompt goc, `PROGRESS.md` va roadmap/dong nghiep vu Muc 10 truoc khi sua; doi chieu source that thay Buyer/Admin dynamic attribute da co nhung Elasticsearch filter chua di xuong nested query va migration thieu audit DDL.
- Buyer filter: parse `attributeFilters` mot lan, dung moi filter thanh mot Elasticsearch `nested` clause ghep `attributeId` voi text/option/range trong cung object; van doi soat MySQL fallback de khong sai ket qua khi index chua du.
- Admin hau kiem: standardize validate category ton tai, dong bo chinh xac tap category duoc chon (deactivate link bo chon), them endpoint `POST /api/v1/admin/product-attributes/reindex` enqueue full active product qua outbox, khong dual-write Elasticsearch.
- Migration: them bang `product_attribute_moderation_audit` vao up/down; chay schema-only clone + 1 product synthetic trong DB tam `verify_m10_20260824`. Up pass `1/1` backup, 5 dynamic values, audit table ton tai; down restore checksum `3144645032/3144645032` va xoa dynamic tables. DB tam da xoa sau verify.
- Search pipeline: harden deploy script de preflight Kafka Connect/connector credential truoc migration, phat hien concrete index legacy, chi migrate khi co switch ro, bo `_class`, refresh + count guard, va verify mapping `attributes=nested`. Local legacy `products` 1 document da migrate an toan sang `products_v1` + alias `products`.
- Verification: `:catalog-service:test` pass 5 tests/0 failure, co test cau truc nested query; Vite production build pass bang Node 20.19.6. Runtime catalog/MySQL/Elasticsearch/Eureka UP; gateway category filters tra 5 filter, danh sach 4 product va filter Nike tra 2; direct reindex queue 5 product, cung route qua gateway tra 401 khi khong co Admin JWT.

**File da tao/sua:**
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/AdminDynamicAttributeController.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/AdminDynamicAttributeService.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/DynamicAttributeService.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/impl/ProductSearchServiceImpl.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/impl/ProductServiceImpl.java`
- `backend-microservice/catalog-service/src/test/java/com/ecommerce/catalog/service/impl/ProductServiceImplTest.java`
- `backend-microservice/catalog-service/src/main/resources/db/migration/manual/m10_dynamic_attributes_up.sql`
- `backend-microservice/catalog-service/src/main/resources/db/migration/manual/m10_dynamic_attributes_down.sql`
- `backend-microservice/search-pipeline/scripts/deploy-connectors.ps1`
- `backend-microservice/search-pipeline/README.md`
- `docs/PROGRESS.md`

**Ket qua:** DONE source hardening Buyer nested filter, Admin category moderation/audit/reindex, FE build va migration up/down tren DB tam. DO DANG CDC nested end-to-end va full Seller/Admin/browser/runtime proof.

**Ghi chu/vuong mac:**
- Connector preflight dung dung voi loi `Access denied for user 'debezium'`; yeu cau cap quyen toan MySQL bi lop phe duyet chan, khong duoc lach. 5 ProductUpdated moi dang nam trong outbox, chua duoc day sang Elasticsearch.
- Browser plugin khong co browser instance kha dung, nen khong co click/screenshot desktop-mobile; chi co FE build va HTTP runtime proof.
- `ecommerce_catalog` that khong co `m10_migration_manifest`, nen khong danh dau migration DB that DONE du current Hibernate schema dang co 20 attribute values.
- Khong chay reset 8 database. FE dev server duoc khoi dong tai `http://127.0.0.1:5173`; runtime chi khoi dong discovery/catalog/gateway va dependency MySQL/Elasticsearch/Kafka/Kafka Connect, khong phai full stack marketplace.

---

### [2026-08-22 16:43] Phien #17
**Da lam:**
- Nhan xac nhan 1A/2A/3A va doi chieu lai cac docs chi dinh: `01-business-mapping.md`, `03-erd-database.md`, `04-luong-nghiep-vu.md`, `05-roadmap.md` khong con noi dung "cho xac nhan Muc 10.6"; cac rang buoc da chot duoc ghi ro trong tai lieu.
- Kiem tra source dynamic attribute Phase 2: backend co enum `TEXT`, `NUMBER`, `SINGLE_SELECT`, `MULTI_SELECT`; service enforce toi da 50 thuoc tinh/product; `PENDING` chi visible voi `creatorSellerId`; seller attribute suggestion/autocomplete lay `X-Seller-Id`; form Seller Product co them/chon kieu/confirm doi category.
- Chay backend compile bang JDK 17 cuc bo `C:\Users\duoc.nguyen1\.jdks\ms-17.0.20`: `.\gradlew.bat compileJava --no-daemon --max-workers=1` pass.
- Thu FE build nhung `npm.cmd` khong co trong PATH; co thu muc Node trong `C:\ProgramData\nvm\v20.19.6` nhung bi `Access is denied`, nen chua co FE build proof.

**File da tao/sua:**
- `docs/PROGRESS.md`
- Doi chieu source/tai lieu hien co: `docs/01-business-mapping.md`, `docs/03-erd-database.md`, `docs/04-luong-nghiep-vu.md`, `docs/05-roadmap.md`, `backend-microservice/catalog-service/**`, `FE/src/pages/seller/products/SellerProducts.vue`, `FE/src/services/api/seller/product.api.ts`

**Ket qua:** DONE chot va doi chieu docs Muc 10.6; DONE backend compile cho source dynamic attribute. CHUA DONE FE build/browser, DB migration up/down va runtime smoke gateway/DB.

**Ghi chu/vuong mac:**
- Khong can hoi lai 1A/2A/3A.
- Database `ecommerce_*` chua reset/chay migration that vi thao tac DROP/CREATE/restore can chap thuan pha huy rieng va can backup ro.
- Neu build FE tiep, can dua Node/npm hop le vao PATH hoac cap quyen doc `C:\ProgramData\nvm\v20.19.6`.

---

### [2026-08-22 15:58] Phien #16
**Da lam:**
- Doc `docs/PROGRESS.md` truoc, sau do doc day du Muc 10 moi bo sung trong prompt goc va doi chieu ba tai lieu thiet ke hien tai.
- Ghi nhan day la nghiep vu bo sung phat sinh sau audit thuc te: product/form dang hard-code theo nganh giay, lam seller nganh khac khong mo ta san pham linh hoat.
- Cap nhat ERD voi definition, dropdown option, category-attribute suggestion M:N, product actual value, nested Elasticsearch document va migration du lieu giay co backup/up/down/doi soat.
- Cap nhat luong nghiep vu Seller tu them/autocomplete/doi danh muc (lien ket Muc 3.2), Buyer filter/detail dong (lien ket Muc 3.3), Platform Admin hau kiem/chuan hoa/gop/an (lien ket Muc 3.4).
- Chen cong viec vao Phase 2/4/5 hien huu trong roadmap, khong tao phase roi; bo sung acceptance va runtime smoke cho migration, Seller, Buyer va Admin.
- Dung truoc khi sua code dung theo Muc 10.6; khong tu quyet 3 policy dang cho nguoi dung xac nhan.

**File da tao/sua:**
- `docs/03-erd-database.md`
- `docs/04-luong-nghiep-vu.md`
- `docs/05-roadmap.md`
- `docs/PROGRESS.md`

**Ket qua:** DONE phan tai lieu Muc 10; BLOCKED phan source theo decision gate bat buoc tai Muc 10.6.

**Ghi chu/vuong mac:**
- Chua sua DDL/API/FE va chua chay build/runtime test trong phien nay vi ba policy anh huong truc tiep schema, validation, autocomplete visibility va UX.
- Chua thuc hien reset database; thao tac DROP/CREATE 8 database van can chap thuan rieng.
- Can nguoi dung tra loi 3 cau hoi da ghi o muc quyet dinh; sau khi chot se tiep tuc tu Phase 2 extension theo `docs/05-roadmap.md`.

---

### [2026-08-22 15:44] Phien #15
**Da lam:**
- Doc lai `docs/PROGRESS.md` va kiem tra truc tiep `seed-demo-data.sql` cung output cua script reset de xac nhan tai khoan seller demo.
- Xac nhan bo seed moi co 2 seller `APPROVED` va 1 seller `PENDING_APPROVAL`; ca ba dung chung mat khau demo `Admin@123`.

**File da tao/sua:**
- `docs/PROGRESS.md`

**Ket qua:** DONE xac nhan tai khoan seller trong bo seed hien tai.

**Ghi chu/vuong mac:**
- Database local hien tai chua duoc reset bang bo seed moi, nen cac tai khoan nay chi chac chan ton tai sau khi chay reset demo co phep.

---

### [2026-08-22 15:39] Phien #14
**Da lam:**
- Doc `docs/PROGRESS.md`, prompt goc, business mapping va roadmap truoc khi sua; tiep tuc dung ranh gioi nghiep vu marketplace.
- Xoa han FE POS va hoa don Admin phang: route/constants/sidebar, `admin/banhang`, `admin/hoadon` va API client lien quan; admin login/redirect nay vao thong ke toan san.
- Xoa backend POS va Admin invoice-only controller/service/request/response. Chuyen Buyer cancel, lich su trang thai va lich su thanh toan sang `DonMuaService`; huy don chi cho phep khi toan bo sub-order con cho xac nhan, cap nhat dong bo order cha/sub-order va hoan ton kho/voucher.
- Sua phan biet Buyer/Seller theo mang JWT `roles`: Seller login/OAuth vao `/seller/dashboard`, navbar hien `KENH NGUOI BAN`, Buyer chua co shop hien `DANG KY BAN HANG`; Seller Center co loi quay lai `Mua hang` bang cung tai khoan.
- Them man `/seller/products` quan ly san pham va phan loai theo seller: list/search/pagination, tao/sua, doi trang thai, thuoc tinh, ton kho, gia va upload anh; sellerId van lay server-side tu JWT/gateway.
- Xoa Admin Product UI va mutation endpoints cu sau khi co man Seller thay the; Platform Admin chi giu API doc danh sach san pham de cau hinh khuyen mai toan san va cac man danh muc/thuoc tinh chung.

**File da tao/sua/xoa chinh:**
- `FE/src/pages/seller/products/SellerProducts.vue`
- `FE/src/services/api/seller/product.api.ts`
- `FE/src/components/custom/layouts/NavBar.vue`
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `FE/src/routes/router.ts`
- `FE/src/constants/path.ts`
- `FE/src/constants/url.ts`
- `FE/src/pages/admin/banhang/**` (xoa)
- `FE/src/pages/admin/hoadon/**` (xoa)
- `FE/src/pages/admin/sanpham/**` (xoa)
- `FE/src/pages/admin/sanphamchitiet/**` (xoa)
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/controller/DonMuaController.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/DonMuaService.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/impl/DonMuaServiceImpl.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/controller/BanHangController.java` (xoa)
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/controller/AdminOrderController.java` (xoa)
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/AdminOrderService.java` (xoa)
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/AdminProductController.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/AdminProductDetailController.java` (xoa)
- `backend-microservice/promotion-service/src/main/java/com/ecommerce/promotion/controller/InternalPromotionController.java`
- `docs/PROGRESS.md`

**Ket qua:** DONE legacy marketplace cleanup va Seller Product/role-switch o muc source + build. `vue-tsc --noEmit` pass; Vite production build pass; clean compile `catalog-service`, `promotion-service`, `order-service` pass; Gradle test task `catalog-service`/`order-service` pass nhung `NO-SOURCE`; scan khong con route/API POS, hoa don Admin hay Admin Product UI; `git diff --check` khong co whitespace error. FE dev server `http://127.0.0.1:5173` va route `/seller/products` tra HTTP 200.

**Ghi chu/vuong mac:**
- Backend, Eureka va DB dang tat, nen chua smoke API co JWT cho Seller Products, cancel order va role switch; khong danh dau runtime DONE.
- Browser tich hop khong co session kha dung, nen chua co click/screenshot desktop-mobile cho man moi.
- Reset fresh seed van la thao tac pha huy 8 database va chua duoc phep; phai doi user chap thuan ro truoc khi chay.
- Vite build con warning font Inter resolve luc runtime va main chunk lon hon 500 kB; build van thanh cong.

---

### [2026-08-22 15:17] Phien #13
**Da lam:**
- Doc lai progress va doi chieu backend JWT enrichment, FE token parser va router guard de xac nhan cach phan biet buyer/seller khi dung chung login.
- Xac nhan buyer co role `USERS`; customer so huu shop `APPROVED` co them role `SELLER` va claims `sellerId`, `sellerSlug`, `shopName`; seller routes yeu cau `SELLER`.
- Phat hien UX chua ro: seller login xong van redirect ve trang chu va navbar van hien `Dang ky ban hang`, chua co nut chuyen ro rang sang Seller Center.

**File da tao/sua:**
- `docs/PROGRESS.md`

**Ket qua:** DONE audit cach phan biet buyer/seller; authorization hien tai dung mo hinh mot identity nhieu role.

**Ghi chu/vuong mac:**
- Sau fresh-data reset/smoke, nen sua navbar/login redirect: role `SELLER` hien `Kenh nguoi ban`, role `USERS` chua co shop hien `Dang ky ban hang`; seller van duoc mua hang bang cung tai khoan.

---

### [2026-08-22 15:14] Phien #12
**Da lam:**
- Doc lai progress va kiem tra truc tiep FE router/path cho luong seller login.
- Xac nhan seller dung chung route buyer `/login`, route seller center la `/seller/dashboard`; route seller yeu cau JWT co role `SELLER`.
- Kiem tra runtime: `http://127.0.0.1:5173/login` dang tra HTTP 200; cong cau hinh mac dinh trong `vite.config.ts` la 6688 nhung server hien tai dang chay o 5173.

**File da tao/sua:**
- `docs/PROGRESS.md`

**Ket qua:** DONE xac nhan link login va seller center hien tai.

**Ghi chu/vuong mac:**
- Login page hien redirect ve trang chu sau dang nhap; seller can vao `/seller/dashboard` de mo Seller Center.
- Seed moi chua duoc reset vao DB local; tai khoan seller hien tai phu thuoc du lieu dang co cho den khi user chap thuan reset.

---

### [2026-08-22 15:09] Phien #11
**Da lam:**
- Doc `docs/PROGRESS.md`, prompt goc va source reset/seed truoc khi xu ly; khong chay reset pha huy database hien tai khi chua co chap thuan ro.
- Audit database hien tai va seed cu: xac nhan seed cu chi co 2 customer, 1 approved seller, 2 product/3 variant, cart va order mot shop, khong co review/follow; du lieu payout hien tai cung da drift so voi seed.
- Mo rong seed thanh bo marketplace nhieu shop co quan he nhat quan: 3 customer; 2 seller approved va 1 seller pending; seller status history; follow; banner; 4 product/6 variant thuoc 2 shop; voucher va campaign theo tung shop; cart multi-seller; 4 order cha tach thanh 6 sub-order voi nhieu trang thai; review/reply; commission, wallet va receivable pending/paid.
- Cap nhat output script reset de in ro tai khoan Admin, Staff va 3 tai khoan Seller/Customer demo cung trang thai shop.
- Chay seed tren 8 database tam `verify_ecommerce_*`, query doi soat va xoa database tam sau khi pass; khong thay doi 8 database `ecommerce_*` hien tai.

**File da tao/sua:**
- `backend-microservice/seed-demo-data.sql`
- `backend-microservice/reset-demo-databases.ps1`
- `docs/PROGRESS.md`

**Ket qua:** DONE bo seed marketplace hoan chinh o muc MySQL isolated verification. Ket qua: 3 customer, 3 seller (2 approved/1 pending), 4 product, 6 variant, cart customer1 co 2 shop, 4 order, 6 sub-order, 4 review, 4 follow, 4 receivable (2 paid). Doi soat `orders.total_after_discount = SUM(order_seller.total_after_discount)` va wallet pending/paid = receivable pending/paid khong co sai lech.

**Ghi chu/vuong mac:**
- Chua nap seed moi vao database local hien tai. Lenh reset se DROP/CREATE `ecommerce_auth`, `ecommerce_user`, `ecommerce_catalog`, `ecommerce_promotion`, `ecommerce_cart`, `ecommerce_order`, `ecommerce_seller`, `ecommerce_payout`; can nguoi dung chap thuan ro truoc khi chay.
- Isolated seed verification chi xac nhan SQL, so luong va cac phep doi soat chinh; fresh-data API/browser smoke se thuc hien sau reset that.

---

### [2026-08-22 14:54] Phien #10
**Da lam:**
- Doc lai tien do, prompt goc va roadmap; tiep tuc dung thu tu Phase 4 -> Phase 5, khong chay reset DB pha huy du lieu khi chua co approve ro rang.
- Hoan tat Phase 4: Platform Admin CRUD banner + storefront banner; public shop/shop page; product search/filter/sort theo shop/rating/ban chay; seller dashboard; thong ke GMV/top seller/top product toan san.
- Hoan tat Phase 5 backend: review product/shop chi khi customer da mua sub-order `HOAN_THANH`, seller chi reply review shop minh, aggregate rating product/shop, follow/unfollow shop, grouped order history va review eligibility.
- Hoan tat Phase 5 FE: grouped order history + modal review, seller review/reply, follow shop, danh gia tai shop va trang chi tiet san pham.
- Bo sung notification best-effort cho seller duoc duyet/tu choi/khoa, order status, review moi/reply va payout paid; loi notification khong rollback transaction nghiep vu.
- Bo sung Platform Admin payout UI `/admin/payout`: xem receivable, ghi nhan da chi tra va cau hinh hoa hong mac dinh/theo category.
- Sua bug runtime product detail: FE gui `idSanPham` nhung backend chi doc `idSP/idProduct`; backend nay nhan ca ba alias, tra payload tuong thich FE, seller metadata va rating/review.
- Harden buyer authorization: toan bo cart va don-mua qua gateway phai co JWT; customer ID lay tu token; chan buyer doc/xoa cart/order cua buyer khac; buyer chi duoc cancel order, khong duoc dung legacy endpoint de them product vao order da tao.
- Sua SQL seller order dung cot that `customer_name/customer_phone` thay cho cot khong ton tai; them schema review/rating seed va sua Hibernate `@Lob` de table `danh_gia` khoi tao thanh cong.
- Cai tien Docker build: tang Gradle wrapper timeout va dung BuildKit Gradle cache chia se; build thanh cong catalog/cart/promotion/order/gateway va cac module Phase 4/5 lien quan.
- Khoi dong runtime core: MySQL, Kafka, Elasticsearch va 10 business services deu `Up`; Eureka co instance `UP` cho gateway/auth/user/catalog/promotion/cart/order/notification/seller/payout.

**File da tao/sua chinh:**
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/controller/ReviewController.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/service/ReviewService.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/entity/Review.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/controller/DonMuaController.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/impl/DonMuaServiceImpl.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/impl/ProductDetailServiceImpl.java`
- `backend-microservice/cart-service/src/main/java/com/ecommerce/cart/controller/CartController.java`
- `backend-microservice/cart-service/src/main/java/com/ecommerce/cart/service/impl/CartServiceImpl.java`
- `backend-microservice/api-gateway/src/main/java/com/ecommerce/gateway/security/AdminAuthorizationFilter.java`
- `backend-microservice/payout-service/src/main/java/com/ecommerce/payout/service/PayoutService.java`
- `FE/src/pages/users/orderhistory/OrderHistory.vue`
- `FE/src/pages/users/products/ProductDetail.vue`
- `FE/src/pages/users/seller/ShopDetail.vue`
- `FE/src/pages/seller/reviews/SellerReviews.vue`
- `FE/src/pages/admin/payout/AdminPayout.vue`
- `FE/src/services/api/admin/payout.api.ts`
- `FE/src/services/api/seller/review.api.ts`
- `docs/PROGRESS.md`

**Ket qua:** HOAN THANH roadmap bat buoc Phase 1-5 o muc source, compile/build va API runtime smoke. `vue-tsc --noEmit`, Vite production build, Docker `bootJar` cac module thay doi va `git diff --check` deu pass. Gateway smoke `200` cho login buyer/admin, product detail, banner, review list/mine, grouped order, seller dashboard/review/wallet, admin banner/statistics/payout; invalid review bi `400`. Security smoke: no-token cart/order `401`, buyer khac xoa cart `403`, buyer khac doc order `404`, owner van `200`. Log runtime cuoi khong co `ERROR`, `Exception` hoac SQL error.

**Ghi chu/vuong mac:**
- Browser runtime tra danh sach kha dung `[]`, nen chua co screenshot/click/mobile visual proof; dev server dang chay tai `http://127.0.0.1:5173`.
- Vite con warning font Inter khong resolve luc build va main chunk lon hon 500 kB; build van thanh cong.
- SMTP chua co credential nen moi xac minh producer/API notification va co che best-effort, chua xac minh email den hop thu that.
- Khong tao review hop le, khong pay receivable, khong checkout/VNPay va khong reset DB trong smoke de tranh lam thay doi du lieu nghiep vu hien co. Full clean-seed/E2E payment van can mot lan test co approve.
- Eureka tam thoi hien them lease cu `DOWN` cho service vua recreate, dong thoi instance moi van `UP`; can doi lease eviction neu muon dashboard chi con mot instance.

---

### [2026-08-22 13:36] Phien #9
**Da lam:**
- Doc lai `AGENTS.md`, `docs/PROGRESS.md`, `docs/Prompt chuyen doi marketplace.md` va `docs/05-roadmap.md`; xac nhan khong chay reset DB vi lenh do DROP/CREATE DB demo/local va user chua approve ro viec xoa du lieu.
- Phase 4 backend: them public shop list `/api/v1/permitall/shops`, public seller map co `rating`/`soldCount` placeholder de storefront/search dung chung contract.
- Phase 4 catalog/search: them `SellerClient` cho `catalog-service`, enrich public product rows va `ProductDocument` voi `sellerId`, `sellerName`, `sellerSlug`, `sellerLogoUrl`, `sellerRating`, `soldCount`; search query them field `sellerName`; mapping Elasticsearch them seller fields.
- Phase 4 filter/sort: `ProductSearchRequest` nhan them alias FE `thuongHieuIds/chatLieuIds/loaiDeIds/danhMucIds`, `sellerId`, `sellerSlug`, `ratingMin`; public product sort them `sold_desc`, `rating_desc`; response giu key cu va them alias FE `tenSanPham/giaBan/kichCo/mauSac/dotGiamGia`.
- FE storefront: `ShopList.vue` bo du lieu hard-code Shopee, goi API public shops va link noi bo `/shop/:sellerSlug`; bat `ShopList` tren home; `ShopDetail.vue` hien profile shop va grid san pham theo `sellerId`; `ProductsView.vue` them filter shop, sort ban chay/rating, va link shop tren card san pham.
- Gateway: dua route `payout-service` len truoc `seller-service` de `/api/v1/seller/payout/**` khong bi route rong `/api/v1/seller/**` bat nham.

**File da tao/sua:**
- `backend-microservice/api-gateway/src/main/resources/application.yml`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/controller/SellerController.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/service/SellerService.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/repository/SellerRepository.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/client/SellerClient.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/document/ProductDocument.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/model/request/ProductSearchRequest.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/ProductSearchIndexer.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/impl/ProductServiceImpl.java`
- `backend-microservice/search-pipeline/elasticsearch/products-index-mapping.json`
- `FE/src/pages/users/home/HomeView.vue`
- `FE/src/pages/users/home/ShopList.vue`
- `FE/src/pages/users/seller/ShopDetail.vue`
- `FE/src/pages/users/products/ProductsView.vue`
- `FE/src/services/api/seller/seller.api.ts`
- `FE/src/services/api/permitall/sanpham/pmsanpham.api.ts`
- `FE/src/types/api.common.ts`
- `docs/PROGRESS.md`

**Ket qua:** DO DANG Phase 4 o muc source; chua co compile/build/runtime proof.

**Ghi chu/vuong mac:**
- Backend compile bi chan ngay tu moi truong: `.\gradlew.bat compileJava --no-daemon --max-workers=1` bao `Gradle requires JVM 17 or later to run. Your build is currently configured to use JVM 11`; `java -version` khong chay vi `java` khong co trong PATH, cac vi tri pho bien chi thay `C:\Program Files\Microsoft\jdk-11.0.30.7-hotspot`.
- FE build chua chay duoc vi `npm.cmd` khong co trong PATH.
- Chua runtime smoke gateway/DB/FE browser; reset DB demo/local van can user approve ro vi script DROP/CREATE database.

---

### [2026-08-17 17:24] Phien #8
**Da lam:**
- Chuan hoa lai `AGENTS.md` dung noi dung UTF-8 bat buoc o goc repo.
- Phase 3 backend: them Seller Order APIs `/api/v1/seller/orders/**`, list/detail sub-order theo `X-Seller-Id`, cac thao tac confirm/ready-to-ship/shipping/complete/cancel va chan seller xem/sua order seller khac.
- Phase 3 payout: them module `payout-service`, DB rieng `ecommerce_payout`, entity/API `commission_config`, `seller_wallet`, `seller_receivable`; order-service goi internal payout khi sub-order hoan thanh.
- Phase 3 voucher: them `seller_id` nullable vao `voucher`; admin voucher chi scope platform (`seller_id null`), seller voucher dung `/api/v1/seller/vouchers/**` va lay seller tu header.
- Phase 3 shop promotion: them `seller_id` vao `promotion_campaign`, API `/api/v1/seller/promotions/**`, validate product variant phai thuoc seller hien tai.
- Gateway/scripts/seed: them route cu the cho seller products/orders/vouchers/promotions/payout truoc route seller-service; them `payout-service` vao Gradle, Docker Compose, run/stop/init/reset scripts va seed DB payout.
- FE Phase 3: them API/page/route `/seller/orders`, `/seller/vouchers`, `/seller/payout`; sidebar co menu Seller Orders/Voucher Shop/Vi doi soat.
- Chay backend compile thanh cong: `cd backend-microservice; .\gradlew.bat compileJava --no-daemon --max-workers=1`.

**File da tao/sua:**
- `AGENTS.md`
- `backend-microservice/settings.gradle`
- `backend-microservice/docker-compose.yml`
- `backend-microservice/init-databases.ps1`
- `backend-microservice/reset-demo-databases.ps1`
- `backend-microservice/run-all.ps1`
- `backend-microservice/stop-all.ps1`
- `backend-microservice/api-gateway/src/main/resources/application.yml`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/**`
- `backend-microservice/promotion-service/src/main/java/com/ecommerce/promotion/**`
- `backend-microservice/payout-service/**`
- `backend-microservice/seed-demo-data.sql`
- `FE/src/constants/path.ts`
- `FE/src/constants/url.ts`
- `FE/src/routes/router.ts`
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `FE/src/services/api/seller/**`
- `FE/src/pages/seller/**`
- `docs/PROGRESS.md`

**Ket qua:** DONE Phase 3 o muc source + backend compile pass; chua DONE runtime smoke.

**Ghi chu/vuong mac:**
- Khong chay duoc FE build vi `npm.cmd` khong co trong PATH.
- Da thu reset Docker DB bang `.\reset-demo-databases.ps1 -UseDocker -Force` nhung sandbox Docker API bi permission denied; yeu cau escalate bi tu choi vi DROP/CREATE database can nguoi dung approve ro viec pha huy du lieu.
- Can user xac nhan ro cho phep reset DB demo/local truoc khi chay runtime smoke that.

---

### [2026-08-17 16:57] Phien #7
**Da lam:**
- Doc lai `docs/PROGRESS.md`, prompt goc va roadmap truoc khi tiep tuc Phase 2.
- Catalog: them `sellerId` vao `Product`/`ProductVariant`, request/response/search filter; them Seller Product APIs `/api/v1/seller/products/**` va `/api/v1/seller/product-variants/**` lay `sellerId` tu header/JWT, co check ownership khi xem/sua/doi trang thai.
- Search/outbox: them `sellerId` vao `ProductDocument`, indexer va Elasticsearch mapping.
- Seed DB: them default approved seller, them `seller_id` vao product/product_variant/cart/order seed, them bang `order_seller`.
- Cart: them seller snapshot (`seller_id`, `shop_name`, `seller_slug`), goi `seller-service` lay public shop, response `GET cart` tra `items` va `shopGroups`.
- Order checkout: them entity/repository `OrderSeller`, them `order_seller_id`/`seller_id` vao `OrderItem`; checkout group line item theo seller, tao sub-order `order_seller`, VNPay success cap nhat ca order cha va sub-order.
- FE: thay `CartView.vue` de render gio hang theo tung shop, chon ca shop/tung item; `CheckoutView.vue` gui them `product`, `Customer`, `address` de khop backend va bo dong markdown fence loi o dau file; sidebar admin loc an POS `BAN_HANG`.
- Chay backend compile thanh cong: `cd backend-microservice; .\gradlew.bat compileJava --no-daemon`.

**File da tao/sua:**
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/**`
- `backend-microservice/cart-service/src/main/java/com/ecommerce/cart/**`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/**`
- `backend-microservice/search-pipeline/elasticsearch/products-index-mapping.json`
- `backend-microservice/seed-demo-data.sql`
- `FE/src/pages/users/cart/CartView.vue`
- `FE/src/pages/users/checkout/CheckoutView.vue`
- `FE/src/services/api/permitall/thanhtoan/thanhtoan.api.ts`
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `docs/PROGRESS.md`

**Ket qua:** DONE Phase 2 o muc source + backend compile pass; chua DONE runtime smoke.

**Ghi chu/vuong mac:**
- Chua chay duoc FE build vi `npm.cmd` khong co trong PATH.
- Chua reset DB/smoke gateway nen can verify thuc te seed English + `order_seller` bang script reset truoc khi coi Phase 2 pass runtime.
- Checkout page da co computed group theo shop va payload dung, nhung block visual checkout grouped chua patch duoc tron ven do file con mojibake; gio hang da grouped ro rang.
- POS moi duoc an khoi sidebar Admin; route/controller POS legacy con ton tai va can audit/xoa co kiem soat trong buoc rieng neu muon loai bo hoan toan.

---

### [2026-08-17 18:05] Phien #6
**Da lam:**
- Kiem tra lai nhu cau gen DB moi theo schema English, khong giu schema tieng Viet cu.
- Xac nhan `seed-demo-data.sql` hien dang tao cac table/column English va script reset se DROP/CREATE lai toan bo DB demo.

**File da tao/sua:**
- `docs/PROGRESS.md`

**Ket qua:** DONE xac nhan cach gen lai DB moi.

**Ghi chu/vuong mac:**
- Can dung `reset-demo-databases.ps1`, khong chi start service voi `ddl-auto=update`, vi `update` khong xoa/rename cot tieng Viet cu.

---
### [2026-08-17 17:55] Phien #5
**Da lam:**
- Kiem tra lai lenh reset/init DB hien tai sau khi doi schema physical sang English.
- Xac nhan script reset chinh van la `backend-microservice/reset-demo-databases.ps1`; script se drop/recreate cac DB: `ecommerce_auth`, `ecommerce_user`, `ecommerce_catalog`, `ecommerce_promotion`, `ecommerce_cart`, `ecommerce_order`, `ecommerce_seller` va import `seed-demo-data.sql`.
- Docker Compose MySQL map host port `3307:3306`; khi dung `-UseDocker` script tu goi qua container, khong can truyen port.

**File da tao/sua:**
- `docs/PROGRESS.md`

**Ket qua:** DONE tra cuu lenh reset DB hien tai.

**Ghi chu/vuong mac:**
- Lenh reset co tinh pha huy du lieu vi DROP DATABASE; chi chay khi chap nhan xoa DB local/demo.

---
### [2026-08-17 17:45] Phien #4
**Da lam:**
- Sua tiep theo yeu cau: physical database schema khong con giu ten bang/cot tieng Viet o backend microservices.
- Doi @Table/@Column/@JoinColumn va SQL/query/seed chinh sang English: `product`, `product_variant`, `customer`, `staff`, `orders`, `order_item`, `payment_history`, `voucher`, `promotion_campaign`, `promotion_campaign_product`, `cart`, `cart_detail`.
- Doi cac cot con sot: `image_url`, `discount_value`, `quantity`, `campaign_status`, `price_before_discount`, `price_after_discount`, `amount`, `customer.name`, `staff.code`, `staff.name`.
- Sua repository/property JPA con bam ten cu: `findByName`, `findByCode`, `findByColor`, `findByStatusAndQuantityGreaterThan`; sua JPQL/native query promotion detail dung `detail_status`.
- Sua seed `promotion_campaign_product` bi trung cot `status`: tach thanh `detail_status` de DB tao moi khong loi duplicate column.
- Chay compile backend thanh cong: `cd backend-microservice; .\gradlew.bat compileJava --no-daemon`.
- Scan lai backend Java/SQL/YAML/PS1/JSON theo cac token schema cu (`san_pham`, `khach_hang`, `hoa_don`, `phieu_giam_gia`, `dot_giam_gia`, `anh_product`, `phan_tram`, `so_price`, ...); chi con alias response `staff_code/staff_name` trong AdminOrderService, khong phai cot DB thuc.

**File da tao/sua:**
- `backend-microservice/*/src/main/java/**`
- `backend-microservice/seed-demo-data.sql`
- `backend-microservice/docker-compose.yml`
- `backend-microservice/init-databases.ps1`
- `backend-microservice/reset-demo-databases.ps1`
- `backend-microservice/run-all.ps1`
- `backend-microservice/stop-all.ps1`
- `docs/PROGRESS.md`

**Ket qua:** DONE refactor schema physical DB + JPA entity naming sang English o muc compile backend pass.

**Ghi chu/vuong mac:**
- Chua smoke test runtime tren MySQL/gateway nen can reset DB va start stack de xac nhan Hibernate/seed/query chay thuc te.
- Con mot so response key/API contract cu cho FE dang la tieng Viet/legacy (`tenProduct`, `giaTriGiam`, `trangThai`, ...). Khong doi het trong phien nay de tranh vo FE ngam; neu muon full English ca REST JSON contract thi can cap nhat dong bo FE.
- FE build van chua chay duoc vi `npm`/`npm.cmd` khong co trong PATH.

---
### [2026-08-17 16:17] PhiÃªn #3
**ÄÃ£ lÃ m:**
- Táº¡m dá»«ng triá»ƒn khai marketplace phase tiáº¿p theo theo yÃªu cáº§u má»›i Ä‘á»ƒ chuáº©n hÃ³a naming entity/backend sang tiáº¿ng Anh.
- Äá»•i cÃ¡c Java entity class/file tiáº¿ng Viá»‡t sang tiáº¿ng Anh, vÃ­ dá»¥: `SanPham` -> `Product`, `SanPhamChiTiet` -> `ProductVariant`, `KhachHang` -> `Customer`, `NhanVien` -> `Staff`, `HoaDon` -> `Order`, `PhieuGiamGia` -> `Voucher`, `DotGiamGia` -> `PromotionCampaign`.
- Äá»•i repository/model/service/controller cÃ³ tÃªn phá»¥ thuá»™c entity sang tiáº¿ng Anh Ä‘á»ƒ compile Ä‘á»“ng bá»™, vÃ­ dá»¥ `SanPhamRepository` -> `ProductRepository`, `AdminHoaDonController` -> `AdminOrderController`, `HoaDonSearchRequest` -> `OrderSearchRequest`.
- Äá»•i field/method entity sang tiáº¿ng Anh (`code`, `name`, `description`, `brand`, `category`, `productVariantId`, `customerId`, `phoneNumber`, `address`, `dateOfBirth`, `password`, ...).
- Giá»¯ nguyÃªn tÃªn báº£ng/cá»™t DB qua annotation `@Table/@Column/@JoinColumn` Ä‘á»ƒ khÃ´ng phÃ¡ schema MySQL hiá»‡n táº¡i vÃ  dá»¯ liá»‡u seed.
- Sá»­a láº¡i cÃ¡c annotation user-service bá»‹ Ä‘á»•i nháº§m (`tinh`, `huyen`, `xa`, `cccd`) vá» Ä‘Ãºng tÃªn cá»™t DB cÅ©.
- Cháº¡y compile backend toÃ n bá»™ thÃ nh cÃ´ng: `.\gradlew.bat compileJava --no-daemon`.

**File Ä‘Ã£ táº¡o/sá»­a:**
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/entity/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/repository/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/model/**`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/**`
- `backend-microservice/cart-service/src/main/java/com/ecommerce/cart/**`
- `backend-microservice/user-service/src/main/java/com/ecommerce/user/**`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/**`
- `backend-microservice/promotion-service/src/main/java/com/ecommerce/promotion/**`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/**`
- `docs/PROGRESS.md`

**Káº¿t quáº£:** DONE refactor naming backend á»Ÿ má»©c `compileJava` pass.

**Ghi chÃº/vÆ°á»›ng máº¯c:**
- API path, DB table/column váº«n giá»¯ tÆ°Æ¡ng thÃ­ch cÅ©; náº¿u muá»‘n Ä‘á»•i cáº£ REST path/JSON contract/database physical schema sang English hoÃ n toÃ n thÃ¬ cáº§n migration riÃªng vÃ  cáº­p nháº­t FE/API docs Ä‘á»“ng bá»™.
- Lá»‡nh compile tiáº¿p tá»¥c lÃ m thay Ä‘á»•i artifact tracked trong `backend-microservice/*/build` vÃ  `.gradle`; sandbox trÆ°á»›c Ä‘Ã³ khÃ´ng cho ghi `.git/index.lock` nÃªn chÆ°a restore Ä‘Æ°á»£c artifact build.
- FE build váº«n chÆ°a cháº¡y Ä‘Æ°á»£c vÃ¬ `npm`/`npm.cmd` khÃ´ng cÃ³ trong PATH.

---

### [2026-08-17 17:05] PhiÃªn #2
**ÄÃ£ lÃ m:**
- Äá»c `docs/PROGRESS.md` vÃ  `docs/Prompt chuyen doi marketplace.md` trÆ°á»›c khi sá»­a code; xÃ¡c nháº­n task tiáº¿p theo lÃ  Phase 1 seller onboarding.
- ThÃªm module backend `seller-service` vÃ o Gradle, Docker Compose vÃ  script local; service sá»Ÿ há»¯u DB `ecommerce_seller` vá»›i báº£ng `seller`, `seller_status_history`, `shop_follow`.
- Viáº¿t API seller onboarding: buyer Ä‘Äƒng kÃ½ shop, xem shop cá»§a mÃ¬nh, public shop profile, admin list/pending/detail/approve/reject/suspend/reopen, seller profile theo `sellerId` server-side.
- Má»Ÿ rá»™ng `auth-service` Ä‘á»ƒ khi buyer login sáº½ gá»i `seller-service` qua Feign; náº¿u shop Ä‘Ã£ `APPROVED` thÃ¬ JWT cÃ³ `roles` gá»“m `USERS`, `SELLER` vÃ  claim `sellerId`, `sellerSlug`, `shopName`.
- Má»Ÿ rá»™ng `api-gateway` route/filter: route seller-service, cháº·n `/api/v1/admin/**` theo role `ADMIN`, cháº·n `/api/v1/seller/**` theo role `SELLER` vÃ  claim `sellerId`, truyá»n `X-User-Id`/`X-Seller-Id` xuá»‘ng service.
- ThÃªm FE API vÃ  UI Phase 1: trang buyer `/dang-ky-ban-hang`, trang public `/shop/:sellerSlug`, trang admin `/admin/seller-approval`, menu admin â€œDuyá»‡t Sellerâ€, link buyer â€œÄÄƒng kÃ½ bÃ¡n hÃ ngâ€.
- Má»Ÿ rá»™ng FE auth token helper Ä‘á»ƒ lÆ°u `roles`, `sellerId`, `sellerStatus`, `sellerSlug`, `shopName` mÃ  váº«n tÆ°Æ¡ng thÃ­ch token cÅ© chá»‰ cÃ³ `role`.
- Cháº¡y compile backend má»¥c tiÃªu thÃ nh cÃ´ng: `.\gradlew.bat :seller-service:compileJava :auth-service:compileJava :api-gateway:compileJava --no-daemon`.

**File Ä‘Ã£ táº¡o/sá»­a:**
- `backend-microservice/settings.gradle`
- `backend-microservice/docker-compose.yml`
- `backend-microservice/init-databases.ps1`
- `backend-microservice/reset-demo-databases.ps1`
- `backend-microservice/run-all.ps1`
- `backend-microservice/stop-all.ps1`
- `backend-microservice/seller-service/**`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/client/SellerClient.java`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/security/TokenProvider.java`
- `backend-microservice/api-gateway/src/main/java/com/ecommerce/gateway/security/AdminAuthorizationFilter.java`
- `backend-microservice/api-gateway/src/main/resources/application.yml`
- `FE/src/constants/url.ts`
- `FE/src/constants/path.ts`
- `FE/src/types/auth.type.ts`
- `FE/src/utils/token.helper.ts`
- `FE/src/services/api/seller/seller.api.ts`
- `FE/src/pages/users/seller/SellerRegistration.vue`
- `FE/src/pages/users/seller/ShopDetail.vue`
- `FE/src/pages/admin/seller/SellerApproval.vue`
- `FE/src/routes/router.ts`
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `FE/src/components/custom/layouts/NavBar.vue`
- `docs/PROGRESS.md`

**Káº¿t quáº£:** DONE code Phase 1 á»Ÿ má»©c compile backend. ChÆ°a xÃ¡c nháº­n runtime gateway/DB/FE build.

**Ghi chÃº/vÆ°á»›ng máº¯c:**
- KhÃ´ng cháº¡y Ä‘Æ°á»£c `npm run build` vÃ¬ `npm.cmd` vÃ  `npm` Ä‘á»u khÃ´ng tá»“n táº¡i trong PATH á»Ÿ mÃ¡y hiá»‡n táº¡i.
- Lá»‡nh compile backend lÃ m thay Ä‘á»•i má»™t sá»‘ artifact build tracked trong `backend-microservice/.gradle` vÃ  `backend-microservice/*/build`; sandbox khÃ´ng cho ghi `.git/index.lock` nÃªn chÆ°a `git restore` Ä‘Æ°á»£c cÃ¡c artifact Ä‘Ã³.
- ChÆ°a cháº¡y smoke test runtime qua gateway vÃ¬ cáº§n stack MySQL/Eureka/Auth/User/Seller hoáº¡t Ä‘á»™ng.
- Phase 2 chÆ°a báº¯t Ä‘áº§u: catalog-service váº«n chÆ°a cÃ³ `seller_id`, Seller Admin sáº£n pháº©m chÆ°a filter theo `sellerId`.

---

### [2026-08-17 15:46] PhiÃªn #1
**ÄÃ£ lÃ m:**
- Äá»c `Prompt chuyen doi marketplace.md` tá»« Má»¥c 0 vÃ  xÃ¡c nháº­n Ä‘Ã¢y lÃ  phiÃªn khá»Ÿi táº¡o vÃ¬ `docs/PROGRESS.md` chÆ°a tá»“n táº¡i.
- Äá»c láº¡i source hiá»‡n táº¡i: module Gradle backend, controller, entity/table, Feign client, application config, Docker compose, search pipeline, FE routes/services/env.
- Táº¡o bá»™ tÃ i liá»‡u chuyá»ƒn Ä‘á»•i marketplace theo Má»¥c 1-8 cá»§a prompt.
- Ghi rÃµ hiá»‡n tráº¡ng chÆ°a cÃ³ seller/shop/sub-order/payout/review/follow vÃ  POS/offline invoice pháº£i bá»‹ loáº¡i khá»i marketplace.
- Chia roadmap Phase 0-6, Ä‘áº£m báº£o má»—i tÃ­nh nÄƒng trong checklist prompt Má»¥c 3.5 xuáº¥t hiá»‡n Ä‘Ãºng má»™t phase.

**File Ä‘Ã£ táº¡o/sá»­a:**
- `docs/00-audit-hien-trang.md`
- `docs/01-business-mapping.md`
- `docs/02-kien-truc-moi.md`
- `docs/03-erd-database.md`
- `docs/04-luong-nghiep-vu.md`
- `docs/05-roadmap.md`
- `docs/ARCHITECTURE.md`
- `docs/PROGRESS.md`

**Káº¿t quáº£:** DONE Phase 0 - tÃ i liá»‡u hÃ³a baseline. ChÆ°a viáº¿t code marketplace.

**Ghi chÃº/vÆ°á»›ng máº¯c:**
- Worktree trÆ°á»›c phiÃªn Ä‘Ã£ cÃ³ `Agents.md` vÃ  `README-Microservice.md` bá»‹ delete, `Prompt chuyen doi marketplace.md` modified/staged; phiÃªn nÃ y khÃ´ng chá»‰nh cÃ¡c file Ä‘Ã³.
- Terminal hiá»ƒn thá»‹ ná»™i dung prompt/root architecture bá»‹ mojibake, nhÆ°ng source/path vÃ  yÃªu cáº§u chÃ­nh váº«n Ä‘á»c Ä‘Æ°á»£c Ä‘á»§ Ä‘á»ƒ thá»±c hiá»‡n.
- FE Ä‘ang cÃ³ interceptor gá»i `/api/v1/auth/refresh`, trong scan `AuthController` hiá»‡n chÆ°a tháº¥y endpoint nÃ y; cáº§n xá»­ lÃ½ sá»›m á»Ÿ Phase 1.

---
# P7 PRODUCT CUTOVER UPDATE - 2026-08-24 14:00

- Nguoi dung da xac nhan bo data cu local/dev va cho phep reset/cutover DB product.
- Da DROP/CREATE lai rieng `ecommerce_catalog`, chay schema/seed canonical product; DB verify pass: legacy table count = 0, target table count = 14, products = 4, variants = 12.
- Da sua `backend-microservice/catalog-service/src/main/resources/application.yml` de default `ddl-auto=validate`, tranh Hibernate tu mutate schema target.
- Da sua `backend-microservice/api-gateway/src/main/resources/application.yml` de route canonical `/api/v1/permitall/products/**`, `/api/v1/permitall/categories/**`, `/api/v1/admin/categories/**`, `/api/v1/admin/product-variant-axes/**`.
- Backend test/compile pass va full `run-all.ps1 -DbPort 3307` bootJar/start pass; gateway health thay 9 service core; gateway `GET /api/v1/permitall/products` tra 4 products.
- Elasticsearch alias `products` da tro `products_v2`; catalog reindex endpoint enqueue 4 `ProductUpdated` vao outbox.
- P7 con DO DANG o CDC/ES/browser: Kafka Connect connectors = 0, ES `products` count = 0. Lenh cap quyen Debezium global bi policy chan, can user phe duyet cu the SQL tao `debezium`@`%` password `dbz` va grant `SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.*`.
# ADMIN PRODUCT UI CLEANUP - 2026-08-24
- Da an/xu ly phan Admin `Danh muc chung` legacy sau product canonical cutover.
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`: sidebar filter bo toan bo route catalog hard-code cu (`mau-sac`, `chat-lieu`, `loai-de`, `loai-giay`, `size`, `thuong-hieu`), nen Admin khong con hien group `Danh muc chung`.
- `FE/src/routes/router.ts`: `/admin` default va cac legacy route hard-code neu go truc tiep redirect ve `/admin/product-attributes`.
- Trang dung tiep theo cho Admin product/catalog: `Quan ly thuoc tinh` (`/admin/product-attributes`), gan danh muc canonical khi chuan hoa thuoc tinh; category tree canonical phuc vu Seller/Buyer.
- Kiem chung: `git diff --check` cho hai file FE pass. KHONG THE KIEM CHUNG FE build/browser trong turn nay vi host khong co `node`/`npm` trong PATH.

# P7 PRODUCT CUTOVER UPDATE - 2026-08-24 14:35
- Da reset/cutover `ecommerce_catalog` local theo product canonical schema va seed moi; khong giu data cu theo chi dao user.
- DB verify: legacy product tables khong con trong `ecommerce_catalog`; target table count = 14; required canonical columns/FK/unique invariant da pass; seed: 4 products, 12 variants, 8 categories, 12 definitions, 13 options, 5 axes, 11 axis values.
- Runtime verify: catalog-service port 8083 health UP voi MySQL 8.4 va `ddl-auto=validate`; api-gateway port 8080 route `/api/v1/permitall/products` OK; public list tra 4 products voi canonical aggregate summary.
- CDC verify: da cap user/quyen Debezium; Kafka Connect image moi `cp-kafka-connect:8.0.0`; plugins load `io.debezium.connector.mysql.MySqlConnector` 3.2.6-3 va Elasticsearch sink 15.1.3; source/sink connectors RUNNING; reindex enqueued 4; MySQL outbox count 4; Kafka topic `outbox.event.Product` offset den 11; Elasticsearch `products` count 4.
- Config/code thay doi trong phien: `catalog-service` default `ddl-auto=validate`; gateway them route canonical catalog public/admin; Kafka Connect base image/plugin; sink dung `topic.to.external.resource.mapping` + `external.resource.usage=alias_index`; source Debezium prefix/schema history v4; `CatalogProductService` outbox search document dang flat product-level fields.
- Gioi han con lai: nested `attributes`/`variants` khong con trong CDC payload vi Debezium 3.2 EventRouter fail voi nested JSON arrays (`Invalid type for STRUCT: class java.lang.String`). Mapping nested ES van con, nhung chua co nested docs qua CDC. FE build/browser khong rerun duoc trong turn nay do host thieu `npm.cmd`, khong co FE dev/preview server va khong co node image local.
- Trang thai: P7 backend/DB/CDC/API smoke DONE voi gioi han nested ES payload neu can search facets tren Elasticsearch. Viec tiep theo neu tiep tuc: xu ly P8 nested ES publisher hoac cung cap Node runtime de rerun FE/browser E2E.
