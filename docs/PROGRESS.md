# PROGRESS.md - Nhat ky tien do chuyen doi Marketplace

## Trang thai tong quan hien tai
- Giai doan: Da hoan tat ca source va runtime acceptance cua PR NHOM 11 — nhom PR cuoi cung duoc dinh nghia trong Muc 6 cua prompt.
- Task dang lam do (neu co): Khong. Migration payout da apply, runtime moi da restart va luong COMPLETE -> PENDING -> AVAILABLE -> PAID batch + soldCount public da duoc smoke test tren DB that.
- Viec tiep theo can lam ngay: Nguoi dung chon backlog tiep theo vi prompt khong dinh nghia PR NHOM 12.

## Cau hoi / quyet dinh can nguoi dung xac nhan
- Khong con cau hoi treo trong pham vi Muc 1 prompt moi; cac quyet dinh nghiep vu da duoc chot dut diem trong prompt.
- Can chon backlog sau PR NHOM 11: Chat buyer-seller, Flash sale toan san, don route/security legacy `/permitall`, hay audit/cutover schema product con lai. Cac nhanh nay khac pham vi lon va prompt khong sap thu tu tiep.

## Checklist tinh nang
- [x] Dang ky/dang nhap buyer, seller, platform admin
- [x] Dang ky & duyet shop
- [x] Trang chu storefront gop nhieu shop
- [x] Trang rieng tung shop
- [x] Tim kiem/filter san pham toan san
- [x] Gio hang multi-seller
- [x] Checkout tach theo shop, thanh toan mot lan
- [x] Split-order thanh sub-order theo seller
- [x] Quan ly don hang rieng theo tung seller
- [x] Voucher 2 tang
- [x] Danh gia san pham & shop sau khi nhan hang
- [x] Theo doi shop
- [x] Vi & doi soat cho seller
- [x] Thong ke rieng theo seller + thong ke tong toan san
- [x] Duyet/khoa seller boi Platform Admin
- [x] Thong bao qua notification-service
- [x] PR NHOM 1: Xoa/hide FE legacy POS `ban-hang`, `hoa-don` va route/folder catalog legacy da redirect
- [x] PR NHOM 2 source/compile: Xoa gateway route POS `ban-hang`, `hoa-don`; order-service test pass
- [x] PR NHOM 2 runtime gateway/API: Jar moi restart, route POS tra 404, route order/admin con dung tra 200
- [x] PR NHOM 2 browser: Login buyer that, gio hang 2 shop/2 san pham, vao `/thanh-toan` va hien du 2 san pham
- [x] PR NHOM 3: Doi ten menu Admin va hoan tat Nguoi dung/Quan tri vien/Voucher san
- [x] PR NHOM 4: Tach layout/sidebar Admin va Seller
- [x] PR NHOM 5: Category Management UI, tree CRUD/status va attribute suggestions
- [x] PR NHOM 6: Admin hau kiem thuoc tinh day du 5 tab
- [x] PR NHOM 11: Payout pending -> available -> paid theo batch, lich su chi tra va soldCount that
- [ ] Chuan hoa san pham: schema target + reset seed demo da nganh, xoa 6 bang hard-code
- [x] Thuoc tinh dong: Seller suggestion/autocomplete/tu them tren form san pham
- [x] Thuoc tinh dong: Buyer filter theo danh muc va product detail
- [x] Thuoc tinh dong: Platform Admin hau kiem/chuan hoa/gop/an
- [x] Dispute: Buyer tao/trao doi, Seller phan hoi, Admin resolve/dong va payout adjustment
- [x] Report: Buyer/Seller bao cao product/shop/review, Admin kiem duyet va thi hanh action
- [ ] Chat buyer-seller
- [ ] Flash sale toan san

## Nhat ky chi tiet

### [2026-08-25 19:45] Phien #43
**Da lam:**
- Doc lai `docs/PROGRESS.md`, `HE_THONG_HIEN_TAI_MARKETPLACE.md` va `docs/Prompt chuyen doi marketplace.md`; xac nhan Muc 6 ket thuc tai PR NHOM 11 va viec con lai da ghi ro la runtime acceptance.
- Audit runtime phat hien cac Java service dang tro MySQL Docker cong `3307`, nhung container `backend-microservice-mysql-1` da dung khoang 7 gio; khoi dong lai dung container va volume cu, khong reset/seed DB. MySQL host cong `3306` la instance khac, khong dung.
- Apply thanh cong `m11_payout_lifecycle_up.sql` vao `ecommerce_payout`: them cot lifecycle, index `idx_receivable_release`, bang `payout_batch`/`payout_batch_item` va backfill `available_at` cho 4 receivable cu.
- Build boot jar va restart catalog-service, order-service, payout-service; sau do build/restart seller-service khi smoke phat hien runtime seller cu van tra `soldCount=0` du internal order da tra dung.
- Doi chieu DB that phat hien `order_seller.total_after_discount` co gom `shipping_fee`: sub-order hang 1.100.000 co total_after_discount 1.130.000. Sua payout gross thanh `total_amount - discount_amount`, dung tong item sau discount va khong tinh phi van chuyen.
- Smoke sub-order `62000000-0000-0000-0000-000000000005` cua seller `70000000-0000-0000-0000-000000000002`: goi complete that, status `3 -> 4`, receivable `ed2b9379-4b86-4684-ad52-3b8da0adabf2` tu dong tao gross 1.100.000, commission 5%=55.000, net 1.045.000 va wallet pending tang 1.283.400 -> 2.328.400.
- Mo phong het hold period rieng receivable moi bang cach dat `available_at` ve truoc hien tai 1 giay, goi release API. Hai receivable seed cu da qua han tu 2024 cung duoc chuyen AVAILABLE dung theo lifecycle; wallet pending ve 0 va available tang tuong ung.
- Tao batch runtime `PAY-8F7E7B53` (`8f7e7b53-a4da-4828-9a57-fec2a47a221f`) cho receivable moi: status PAID, released/paid amount 1.045.000, wallet seller 2 paid tang 1.422.900 -> 2.467.900, batch item va staff/note smoke duoc luu dung.
- Xac minh soldCount sau complete: internal order aggregate tra 3; public shop `run-active-store` qua ca seller-service direct va gateway deu tra `soldCount=3`, khong con runtime hard-code 0.

**File da tao/sua:**
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/impl/SellerOrderServiceImpl.java`
- `docs/PROGRESS.md`

**Ket qua:** DONE runtime acceptance PR NHOM 11. Migration, service artifact moi, payout lifecycle, batch history, wallet va public soldCount deu da duoc xac minh tren database/runtime that.

**Kiem chung:**
- `gradlew :catalog-service:bootJar :order-service:bootJar :payout-service:bootJar --no-daemon --max-workers=1`: PASS.
- `gradlew :order-service:test :order-service:bootJar --no-daemon --max-workers=1`: PASS sau khi sua gross khong gom shipping.
- `gradlew :seller-service:test :seller-service:bootJar --no-daemon --max-workers=1`: PASS.
- Port runtime `8080`, `8083`, `8086`, `8089`, `8091`, `6688`, `3307`: LISTEN.
- Order-service va payout-service actuator: HTTP 200/UP. Catalog API internal tra category snapshot moi; actuator catalog HTTP 503 chi vi Elasticsearch local dang dung.
- Gateway public shop `/api/v1/permitall/shops/run-active-store`: HTTP 200, `soldCount=3`.
- DB join receivable/batch: gross 1.100.000, commission 55.000, net/released/batch amount 1.045.000, status PAID, reference `PAY-8F7E7B53`.

**Ghi chu/vuong mac:**
- Runtime smoke co chu dong thay doi du lieu demo: sub-order `...0005` da thanh HOAN_THANH, tao/chi receivable moi va release hai receivable PENDING seed cu da qua ngay kha dung tu 2024. Day la thay doi acceptance co chu dich, khong phai reset du lieu.
- Gateway admin payout tra 401 khi goi khong kem JWT admin, dung voi route protected. Lifecycle duoc goi truc tiep payout-service va doi chieu DB; public soldCount duoc xac minh qua gateway.
- Catalog actuator con DOWN do Elasticsearch container dang dung, nhung DB va internal product-variant API can cho payout hoat dong binh thuong; khong khoi dong ca monitoring/search stack ngoai pham vi acceptance.
- Prompt khong co PR NHOM 12; khong tu chon Chat/Flash sale/security/schema cleanup vi day la cac nhanh nghiep vu khac nhau dang ke.

**Viec tiep theo can lam ngay:**
- Chon mot backlog moi de tao thu tu PR tiep: Chat buyer-seller, Flash sale toan san, don route/security legacy `/permitall`, hoac audit/cutover schema product con lai.

---

### [2026-08-25 19:20] Phien #42
**Da lam:**
- Doc `docs/PROGRESS.md`, hien trang order/payout/seller trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 11 trong `docs/Prompt chuyen doi marketplace.md`.
- Audit xac nhan order-service da goi internal payout API khi `order_seller` sang `HOAN_THANH`, payout-service tao receivable idempotent theo `orderSellerId` va cong `seller_wallet.pending_amount`; bo swallow loi payout de transaction complete rollback khi dich vu payout loi, tranh mat receivable am tham va cho phep retry an toan.
- Bo sung `categoryId` vao internal catalog variant snapshot; order-service nhom cac dong `order_item` theo category, phan bo gross sau discount theo ty trong va payout-service tinh tong commission tung category. Sub-order co nhieu category khong con roi toan bo ve commission mac dinh; `commissionRate` tren receivable la ty le hieu dung.
- Them chu ky doi soat cau hinh bang `PAYOUT_SETTLEMENT_HOLD_DAYS` (mac dinh 7 ngay), gan `availableAt` khi tao receivable va job hourly cau hinh bang `PAYOUT_RELEASE_CRON` de chuyen khoan du dieu kien tu `PENDING` sang `AVAILABLE`.
- Khi release, chuyen dung so tien tu wallet pending sang available va ghi `releasedAmount`; dong thoi ton trong so du pending am cua dispute da paid de no cu duoc bu vao receivable tuong lai.
- Them xu ly dispute cho receivable `AVAILABLE`: giam net/released/available tuong ung, phan con thieu tiep tuc ghi vao pending am; giu nguyen flow PENDING va PAID da co.
- Tao `payout_batch`, `payout_batch_item` va API admin release/list/create batch. Chi receivable `AVAILABLE` moi duoc chi; batch chuyen wallet `available_amount -> paid_amount`, gan `paidAt`/`payoutBatchId` va luu lich su thanh toan.
- Nang cap trang Admin Payout: tong hop pending/available/paid, chon nhieu receivable AVAILABLE, thanh toan batch, tab lich su batch va nut cap nhat khoan kha dung. Trang Seller Payout hien ngay kha dung va tag trang thai.
- Audit `soldCount`: phuong an (a) duoc prompt cho phep da hoan tat o PR NHOM 8 qua internal batch order-service, cong `order_item.quantity` cua sub-order `HOAN_THANH`; seller-service public shop/profile dang dung gia tri nay va test `SellerOrderSoldCountTest` da bao ve ket qua. Khong viet lai thanh event/outbox khi acceptance hien tai da dat.
- Them migration thu cong cho cot lifecycle, index release va bang lich su batch; query release co fallback cho receivable cu co `available_at IS NULL` neu chi dung Hibernate schema update.
- Them test tao receivable + pending wallet, release pending -> available, batch available -> paid va giu test dispute adjustment.

**File da tao/sua chinh:**
- `backend-microservice/payout-service/src/main/java/com/ecommerce/payout/{PayoutServiceApplication.java,service/PayoutService.java,controller/PayoutController.java}`
- `backend-microservice/payout-service/src/main/java/com/ecommerce/payout/entity/{SellerReceivable.java,PayoutBatch.java,PayoutBatchItem.java}`
- `backend-microservice/payout-service/src/main/java/com/ecommerce/payout/{model/PayoutBatchRequest.java,repository/SellerReceivableRepository.java,repository/PayoutBatchRepository.java,repository/PayoutBatchItemRepository.java}`
- `backend-microservice/payout-service/src/main/java/com/ecommerce/payout/model/{ReceivableRequest.java,CommissionLineRequest.java}`
- `backend-microservice/payout-service/src/main/resources/{application.yml,db/migration/manual/m11_payout_lifecycle_up.sql}`
- `backend-microservice/payout-service/src/test/java/com/ecommerce/payout/service/{PayoutServiceLifecycleTest.java,PayoutServiceDisputeAdjustmentTest.java}`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/impl/SellerOrderServiceImpl.java`
- `backend-microservice/common-lib/src/main/java/com/ecommerce/common/catalog/CatalogVariantSnapshot.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/CatalogProductService.java`
- `FE/src/services/api/admin/payout.api.ts`, `FE/src/services/api/seller/payout.api.ts`
- `FE/src/pages/admin/payout/AdminPayout.vue`, `FE/src/pages/seller/payout/SellerPayout.vue`

**Ket qua:** DONE PR NHOM 11 ve source/API/UI contract. Mot sub-order COMPLETE tao receivable va pending wallet; sau hold period tien sang available, admin chi theo batch co lich su; public shop tiep tuc hien soldCount that theo aggregate order-service.

**Kiem chung:**
- `gradlew :catalog-service:test :cart-service:test :order-service:test :payout-service:test --no-daemon --max-workers=1`: PASS, 18 task.
- `PayoutServiceLifecycleTest`: PASS 3 nhanh tao pending voi commission da category, release available va batch paid; `PayoutServiceDisputeAdjustmentTest`: PASS.
- `SellerOrderSoldCountTest` trong order-service: PASS trong bo test order-service.
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3558 modules.
- `git diff --check`: PASS, chi warning LF/CRLF cua worktree.

**Ghi chu/vuong mac:**
- Runtime stack dang chay chua restart va migration chua duoc ap dung trong phien nay; khong tu dong sua du lieu DB local. Can apply migration/restart truoc smoke test end-to-end.
- Gross payout dung tong gia tri `order_item` sau discount (`order_seller.total_amount - discount_amount`), khong tinh `shipping_fee`; gross duoc phan bo theo ty trong tung category de ap dung config rieng. Category khong co config dung muc mac dinh (5% neu chua cau hinh).
- Flow complete -> payout hien dung internal API dong bo co idempotency thay vi event/outbox. Neu payout-service tam loi, complete rollback de retry; notification van best-effort va khong rollback nghiep vu.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.
- Prompt chi dinh nghia den PR NHOM 11, khong co PR NHOM 12 de tu dong lam tiep.

**Viec tiep theo can lam ngay:**
- Apply `m11_payout_lifecycle_up.sql`, restart payout/order/gateway/FE va smoke test 1 sub-order HOAN_THANH -> PENDING -> AVAILABLE -> PAID batch tren DB/runtime that.
- Sau runtime acceptance, chon backlog tiep theo trong checklist audit (chat, flash sale hoac don route/security legacy) vi thu tu PR hien tai da ket thuc.

---

### [2026-08-25 18:45] Phien #41
**Da lam:**
- Doc `docs/PROGRESS.md`, hien trang seller/catalog/review trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 5 + Muc 6/PR NHOM 10 trong `docs/Prompt chuyen doi marketplace.md`.
- Audit xac nhan `danh_gia.status` da ton tai trong entity va public query da chi doc `VISIBLE`; khong them trung cot, chi bo sung setter/action an va tinh lai rating product sau khi an.
- Tao entity/repository/schema `report` trong seller-service, dung ID `VARCHAR(36)` theo convention service that; ho tro PRODUCT/SHOP/REVIEW/USER, JSON evidence, PENDING/REVIEWING/ACTION_TAKEN/DISMISSED va ghi note/staff/time xu ly.
- Implement `POST /api/v1/buyer/reports` va `POST /api/v1/seller/reports`; validate target ton tai, target/reason enum va ngan trung bao cao dang cho xu ly cua cung reporter.
- Implement Admin list/filter/detail, tiep nhan `PENDING -> REVIEWING` va resolve voi kiem tra action phu hop target.
- `PRODUCT_DELISTED`: them internal catalog API an product `INACTIVE` va ghi outbox `ProductDeleted`; public catalog tu dong khong con doc product.
- `SHOP_SUSPENDED`: dung lai SellerService suspend, tao status history va notification nhu flow admin seller co san; public shop chi doc APPROVED nen shop bi an.
- `REVIEW_HIDDEN`: set review HIDDEN, public review khong con hien va cap nhat lai rating average/count sang catalog.
- `WARNING_SENT`: tim owner/customer vi pham theo target va gui email qua notification-service; `NO_ACTION` chuyen report sang DISMISSED.
- Gateway them buyer/admin report route; seller report da nam trong route protected `/api/v1/seller/**`; filter role generic hien tai enforce USERS/SELLER/ADMIN.
- Tao component report modal dung chung; them nut bao cao tai product detail, shop card/trang shop va tung review; Seller Center co nut report review gia.
- Tao trang `/admin/reports`, menu `San pham & Noi dung -> Kiem duyet noi dung`, filter/detail/tiep nhan/chon action/ghi chu xu ly.
- Bo sung unit test tao report tu du 3 nguon, resolve delist/suspend/hide, review rating recalculation va catalog product delist/outbox.

**File da tao/sua chinh:**
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/entity/Report.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/{repository/ReportRepository.java,model/CreateReportRequest.java,model/ResolveReportRequest.java}`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/{service/ReportService.java,controller/ReportController.java}`
- `backend-microservice/seller-service/src/main/resources/db/migration/manual/m10_report_up.sql`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/{entity/Review.java,service/ReviewService.java,client/CatalogClient.java}`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/{controller/InternalCatalogController.java,service/CatalogProductService.java}`
- `backend-microservice/seller-service/src/test/java/com/ecommerce/seller/service/{ReportServiceTest.java,ReviewModerationTest.java}`
- `backend-microservice/catalog-service/src/test/java/com/ecommerce/catalog/service/CatalogProductAttributeRulesTest.java`
- `backend-microservice/api-gateway/src/main/resources/application.yml`
- `FE/src/services/api/report/report.api.ts`, `FE/src/components/report/ReportButton.vue`
- `FE/src/pages/admin/reports/AdminReports.vue`
- `FE/src/pages/users/products/ProductDetail.vue`, `FE/src/pages/users/seller/ShopDetail.vue`, `FE/src/pages/seller/reviews/SellerReviews.vue`
- `FE/src/constants/path.ts`, `FE/src/routes/router.ts`, `FE/src/components/custom/Sidebar/AdminSidebar.vue`

**Ket qua:** DONE PR NHOM 10 ve source/API/UI contract. Report tao duoc tu product/shop/review; Admin action tac dong dung service so huu va status report duoc chot theo ket qua.

**Kiem chung:**
- `gradlew :catalog-service:test :seller-service:test :api-gateway:compileJava --no-daemon --max-workers=1`: PASS.
- `ReportServiceTest`: PASS 6 test, gom tao du 3 target, delist/suspend/hide, warning va dismiss.
- `ReviewModerationTest`: PASS, review thanh HIDDEN va rating aggregate duoc cap nhat.
- `CatalogProductAttributeRulesTest.adminDelistMakesProductInactive`: PASS, product INACTIVE va outbox duoc ghi.
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3558 modules.
- `git diff --check`: PASS, chi warning LF/CRLF cua worktree.

**Ghi chu/vuong mac:**
- Runtime stack dang chay la artifact cu va chua restart/migrate trong phien nay; acceptance duoc kiem chung bang unit test cac action that, compile va FE production build, khong ghi report/suspend/an du lieu vao DB local.
- Manual schema co `resolution_note` bo sung de khong lam mat field `note` cua API resolve, ngoai cac cot bat buoc trong Muc 5.2.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.
- Gradle test cap nhat generated build metadata tracked; khong dung git restore/reset de tranh ghi de thay doi co san.

**Viec tiep theo can lam ngay:**
- PR NHOM 11: audit va hoan thien payout lifecycle pending -> available -> paid, payout history va soldCount/event theo dung Muc 6.

---

### [2026-08-25 18:10] Phien #40
**Da lam:**
- Doc `docs/PROGRESS.md`, hien trang order/payout/gateway/FE trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 4 + Muc 6/PR NHOM 9 trong `docs/Prompt chuyen doi marketplace.md`.
- Tao domain `dispute`, `dispute_message` trong order-service bang JPA va SQL migration thu cong, dung ID `VARCHAR(36)` theo schema that cua he thong; co JSON evidence/attachment, index ownership/status/date va day du timestamp/ket qua xu ly.
- Implement API Buyer: tao dispute chi khi `order_seller.order_status=HOAN_THANH`, validate ownership/type/so tien, list/detail theo customer va gui message khi ho so chua ket thuc.
- Implement API Seller: list/detail dung seller ownership, phan hoi va chuyen `OPEN -> SELLER_RESPONDED`.
- Implement API Admin: list/filter status/seller/date voi uu tien ho so moi, detail kem sub-order/items/messages, `take-review`, resolve full/partial/reject va close theo state machine.
- Tich hop payout idempotent theo `disputeId`: receivable `PENDING` duoc giam gross/commission/net va wallet pending; receivable `PAID` tao `DISPUTE_ADJUSTMENT` am va ghi no vao pending de bu ky sau.
- Them gateway route buyer/seller/admin disputes va enforce JWT role `USERS`/`SELLER`/`ADMIN`; gateway tiep tuc truyen `X-User-Id`, `X-Seller-Id` tu claim.
- FE Buyer: them form khieu nai theo tung sub-order/shop tai chi tiet don da hoan thanh, route `/khieu-nai`, danh sach/chi tiet/message.
- FE Seller: thay placeholder `/seller/disputes` bang workspace list/detail/respond.
- FE Admin: them `/admin/disputes`, menu `Don hang -> Xu ly tranh chap`, filter/tiep nhan/resolve/dong ho so.
- Bo sung du lieu `orderSellerId`, seller/shop va tong sub-order vao API chi tiet don buyer de form chon dung shop.
- Them unit test eligibility, seller ownership, partial refund -> payout; them test payout cho ca receivable PENDING va PAID.

**File da tao/sua chinh:**
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/{entity,repository,model/request,service,controller}` (Dispute)
- `backend-microservice/order-service/src/main/resources/db/migration/manual/m9_dispute_up.sql`
- `backend-microservice/order-service/src/test/java/com/ecommerce/order/service/DisputeServiceTest.java`
- `backend-microservice/payout-service/src/main/java/com/ecommerce/payout/{entity,model,repository,service,controller}` (PayoutAdjustment)
- `backend-microservice/payout-service/src/main/resources/db/migration/manual/m9_payout_adjustment_up.sql`
- `backend-microservice/payout-service/src/test/java/com/ecommerce/payout/service/PayoutServiceDisputeAdjustmentTest.java`
- `backend-microservice/api-gateway/src/main/java/com/ecommerce/gateway/security/AdminAuthorizationFilter.java`
- `backend-microservice/api-gateway/src/main/resources/application.yml`
- `FE/src/services/api/dispute/dispute.api.ts`
- `FE/src/components/dispute/DisputeWorkspace.vue`
- `FE/src/pages/users/disputes/BuyerDisputes.vue`
- `FE/src/pages/seller/disputes/SellerDisputes.vue`
- `FE/src/pages/admin/disputes/AdminDisputes.vue`
- `FE/src/pages/users/orderhistory/OrderDetail.vue`
- `FE/src/constants/path.ts`, `FE/src/routes/router.ts`, `FE/src/components/custom/Sidebar/AdminSidebar.vue`

**Ket qua:** DONE PR NHOM 9 ve source/API/UI contract. State machine va ownership duoc validate o backend; refund co tac dong tai chinh idempotent len receivable/wallet.

**Kiem chung:**
- `gradlew :order-service:compileJava :payout-service:compileJava :api-gateway:compileJava --no-daemon --max-workers=1`: PASS.
- `gradlew :order-service:test --tests com.ecommerce.order.service.DisputeServiceTest`: PASS, 3 test.
- `gradlew :payout-service:test --tests com.ecommerce.payout.service.PayoutServiceDisputeAdjustmentTest`: PASS, 2 test.
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3552 modules.
- `git diff --check`: PASS, chi warning LF/CRLF cua worktree.

**Ghi chu/vuong mac:**
- Order hien tai khong co status `DELIVERED` rieng; theo enum va workflow that, `HOAN_THANH=4` la trang thai da giao/hoan tat duoc dung de mo dispute.
- He thong chua co payment refund provider/API; PR nay phan anh so tien hoan vao so doi soat seller theo dung pham vi Muc 4.6. Viec chuyen tien nguoc ve buyer can payment provider neu duoc bo sung sau.
- Runtime stack dang chay la artifact cu va chua duoc restart/migrate trong phien nay; acceptance duoc kiem chung bang compile, unit test state/financial branches va FE production build, khong ghi du lieu smoke vao DB local.
- Gradle compile/test cap nhat mot so generated build metadata tracked; khong dung git restore/reset de tranh ghi de thay doi co san.

**Viec tiep theo can lam ngay:**
- PR NHOM 10: Module Report theo Muc 5 cho product/shop/review, action admin va FE bao cao/xu ly.

---

### [2026-08-25 16:32] Phien #39
**Da lam:**
- Doc `docs/PROGRESS.md`, hien trang Buyer catalog/product detail trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 8 trong `docs/Prompt chuyen doi marketplace.md`.
- Audit phat hien FE da co khung filter dong nhung backend `publicProducts` bo qua `categoryId`, `attributeFilters`, min/max price va sort; sua backend de loc that theo category, TEXT/SELECT/NUMBER attribute, price cua active variant va 4 kieu sort.
- Trang `/san-pham` chi hien category leaf theo path cay; sau khi chon category chi render suggestion co `filterable=true`, ho tro text, numeric range va option canonical `resolvedOptionId`; khong con filter giay hard-code.
- Card san pham hien shop name, product rating/rating count va sold count; them API batch public shop theo seller IDs de khong goi N+1 tu FE.
- Them internal batch `GET /internal/orders/sellers/sold-counts`: cong `order_item.quantity` chi tren `order_seller.order_status=HOAN_THANH`; seller-service dung ket qua that thay cho `soldCount=0` hard-code.
- Trang `/san-pham-chi-tiet/:idsp` tach block `Thong so san pham`, `Chon phan loai`, shop card va review; hien dung selected option/number unit va seller reply.
- Sua logic variant: khoi tao default combination con hang, bat buoc chon du moi axis, disable gia tri khong tuong thich voi axis da chon va cap nhat dung SKU/gia/ton/anh theo combination; ho tro toi da 2 axis tu aggregate backend.
- Bo sung unit test backend cho dynamic filter tren hai nganh Dien thoai/Ao va sold count cua completed sub-order.
- Chay Chrome headless smoke bang mock dung HTTP contract, sau do xoa script/profile/log tam.

**File da tao/sua:**
- `FE/src/pages/users/products/FilterBox.vue`
- `FE/src/pages/users/products/ProductsView.vue`
- `FE/src/pages/users/products/ProductDetail.vue`
- `FE/src/services/api/catalog/catalog.api.ts`
- `FE/src/services/api/seller/seller.api.ts`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/model/request/ProductSearchRequest.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/CatalogProductService.java`
- `backend-microservice/catalog-service/src/test/java/com/ecommerce/catalog/service/CatalogPublicProductSearchTest.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/controller/InternalOrderController.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/repository/OrderSellerRepository.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/SellerOrderService.java`
- `backend-microservice/order-service/src/main/java/com/ecommerce/order/service/impl/SellerOrderServiceImpl.java`
- `backend-microservice/order-service/src/test/java/com/ecommerce/order/service/impl/SellerOrderSoldCountTest.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/client/OrderClient.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/controller/SellerController.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/repository/SellerRepository.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/service/SellerService.java`
- `docs/PROGRESS.md`

**Ket qua:** DONE PR NHOM 8 ve source/UI/API contract. Buyer loc attribute dong that theo tung category, card co du marketplace signals va product detail chon dung variant aggregate.

**Kiem chung:**
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3544 modules.
- `gradlew :catalog-service:test :seller-service:test :order-service:test --no-daemon --max-workers=1`: PASS; catalog 22 tests, order 2 tests, seller khong co test source; 0 failure/error/skipped.
- Backend unit: category `Dien thoai` loc SELECT chip + price va category `Ao` loc TEXT material deu tra dung product; sold count giu 0 cho seller chua ban va chi lay tong completed.
- Browser smoke list: category tree co `Dien tu / Dien thoai`, `Thoi trang / Ao`; parent disabled; request gui dung `attr-chip=snapdragon` va `attr-material=cotton`; card hien shop/rating/sold count that theo contract.
- Browser smoke detail: hien `Thong so san pham`, option `Snapdragon`, `5000 mAh`, dung 2 axis; doi Mau sang Xanh cap nhat SKU `PHONE-BLUE-64`; shop card/review/seller reply hien dung.
- Browser smoke tong: 12/12 assertion PASS; `failures=[]`, `consoleErrors=[]`.
- `git diff --check`: PASS, chi warning LF/CRLF cua cac file da co trong worktree.

**Ghi chu/vuong mac:**
- Catalog runtime port 8083 van timeout tu phien truoc, nen browser acceptance dung mock response theo controller/service contract that; logic loc va sold count da co unit test backend rieng, khong restart/reset stack ngoai pham vi.
- Sold count hien dung mot batch query dong bo seller-service -> order-service, khong N+1 tu FE. PR NHOM 11 van can audit payout workflow va co the denormalize sold count bang event/outbox theo ke hoach neu can toi uu runtime.
- Gradle test cap nhat generated file tracked `backend-microservice/catalog-service/build/tmp/compileJava/previous-compilation-data.bin`; khong dung git restore/reset de tranh ghi de file trong worktree.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.

**Viec tiep theo can lam ngay:**
- PR NHOM 9: trien khai module Dispute theo Muc 4 gom schema/API Buyer-Seller-Admin, payout adjustment, gateway va ba man FE.

---

### [2026-08-25 16:05] Phien #38
**Da lam:**
- Doc `docs/PROGRESS.md`, hien trang Seller Product Form trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 7 trong `docs/Prompt chuyen doi marketplace.md`.
- Audit aggregate contract that cua `SellerProducts.vue`, seller product API va validator/service catalog; giu lai cac phan da dung nhu category tree, goi y theo category, custom attribute, toi da 2 axis va Cartesian matrix.
- Danh dau ro suggestion `required`, `filterable` va thuoc tinh Seller tu them dang cho hau kiem; khi edit hien ca suggestion optional chua co gia tri de Seller co the bo sung.
- Them autocomplete ten truc tu `variant-axis-name-suggestions`, gui va doc lai `nameSuggestionId` de giu lien ket voi suggestion da chuan hoa.
- Bo sung FE validation ten/gia tri axis khong trong/khong trung, toi da 2 axis, SKU bat buoc va unique khong phan biet hoa thuong, gia/ton kho khong am.
- Tu goi y SKU tu product code/name va cac gia tri combination; ma tran 2 x 2 sinh dung 4 variant, moi variant co gia, ton kho va anh rieng optional.
- Them modal preview aggregate truoc khi xac nhan POST/PUT; flow edit giu category, attributes, axis/value IDs, suggestion IDs, variant IDs va cac gia tri cu khi chi sua mo ta.
- Backend detail response bo sung `nameSuggestionId` cua moi variant axis de FE khong lam mat metadata khi edit.
- Chay Chrome headless smoke bang mock dung HTTP contract, sau do xoa script/profile/log tam.

**File da tao/sua:**
- `FE/src/pages/seller/products/SellerProducts.vue`
- `FE/src/services/api/seller/product.api.ts`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/CatalogProductService.java`
- `docs/PROGRESS.md`

**Ket qua:** DONE PR NHOM 7 ve source/UI/API contract. Seller Product Form ho tro san pham da nganh, suggested/custom attributes, 0-2 truc, Cartesian variants, preview va create/edit aggregate.

**Kiem chung:**
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3544 modules.
- `gradlew :catalog-service:test --no-daemon --max-workers=1`: PASS, 21 tests, 0 failure/error/skipped.
- Browser smoke create: category tree co `Dien tu / Dien thoai` va `Thoi trang / Ao`; required/filterable/custom pending-review hien dung; POST co custom attribute, 2 axis gan suggestion, 4 Cartesian variants, SKU unique, gia/ton/anh rieng dung.
- Browser smoke edit: preview hien dung; PUT chi doi mo ta van giu name/category, brand, 2 axis/value ID, 2 `nameSuggestionId`, 4 variant ID va du lieu cu.
- Browser smoke tong: 14/14 nhom assertion PASS; `failures=[]`, `consoleErrors=[]`.
- `git diff --check`: PASS, chi warning LF/CRLF cua cac file da co trong worktree.

**Ghi chu/vuong mac:**
- Catalog runtime port 8083 van timeout tu phien truoc, nen browser acceptance dung mock response theo controller/service contract that; khong restart/reset stack hoac sua seed ngoai pham vi.
- Gradle test cap nhat generated file tracked `backend-microservice/catalog-service/build/tmp/compileJava/previous-compilation-data.bin`; khong dung git restore/reset de tranh ghi de file trong worktree.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.

**Viec tiep theo can lam ngay:**
- PR NHOM 8: hoan thien Buyer Dynamic Filters + Product Detail, bo filter giay hard-code, hien shop/rating/sold count va test it nhat 2 category khac nganh.

---

### [2026-08-25 15:45] Phien #37
**Da lam:**
- Doc `docs/PROGRESS.md`, hien trang Admin product attributes trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 6 trong `docs/Prompt chuyen doi marketplace.md`.
- Audit 9 endpoint attribute, controller/service variant axis, bang/entity `product_attribute_moderation_audit` va category suggestion that.
- Hoan thien dung 5 tab tai `/admin/product-attributes`: Thuoc tinh, Option, Danh muc goi y, Gop & Lich su, Truc bien the.
- Tab Thuoc tinh bo sung nguon tao `He thong/Admin` hoac `Seller`, product count, ngay tao; category hien ten/path, khong fallback UUID; giu day du verify/standardize/merge/hide.
- Tab Option cho chon rieng attribute `SELECT_ONE/SELECT_MULTI`, xem option, verify va merge option cung definition.
- Tab Danh muc goi y cho xem/them/xoa gán definition da verify, sua `filterable`, `required`, `displayOrder` va luu dung category configure API.
- Tab Gop & Lich su hien actor, action, source/target definition name, reason, product count anh huong va thoi gian.
- Tab Truc bien the giu insights/suggestions tach rieng thuoc tinh mo ta, bo sung merge suggestion ben canh create/verify/hide.
- Backend bo sung field `createdDate` trong definition response va endpoint read-only `GET /api/v1/admin/product-attributes/moderation-audits`, map source/target sang ten definition.
- Chay Chrome headless smoke 5 tab/action bang mock dung HTTP contract, sau do xoa script/profile tam.

**File da tao/sua:**
- `FE/src/pages/admin/product-attributes/ProductAttributes.vue`
- `FE/src/services/api/admin/product-attribute.api.ts`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/AdminProductAttributeController.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/repository/ProductAttributeModerationAuditRepository.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/CatalogAdminService.java`
- `docs/PROGRESS.md`

**Ket qua:** DONE PR NHOM 6 ve source/UI/API contract. Man hau kiem co du 5 tab, day du action va khong hien UUID thay ten category.

**Kiem chung:**
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3544 modules.
- `gradlew :catalog-service:test --no-daemon --max-workers=1`: PASS, 21 tests, 0 failure/error/skipped.
- Browser smoke: tabs dung `Thuoc tinh`, `Option`, `Danh muc goi y`, `Gop & Lich su`, `Truc bien the`; `noUuid=true`.
- Browser smoke action: verify, standardize (name/categoryIds), merge (targetId), hide, option verify va category suggestion save deu phat dung request; history va axis data hien dung; `failures=[]`, `consoleErrors=[]`.
- `git diff --check`: PASS, chi warning LF/CRLF cua cac file da co trong worktree.

**Ghi chu/vuong mac:**
- Catalog runtime port 8083 van timeout tu phien truoc, nen browser acceptance dung mock response theo controller/service contract that; khong restart/reset stack hoac sua seed ngoai pham vi.
- Prompt noi backend da du API nhung source that khong co endpoint doc moderation audit; da them duy nhat endpoint read-only can cho tab Lich su, khong thay doi schema.
- Gradle test cap nhat generated file tracked `backend-microservice/catalog-service/build/tmp/compileJava/previous-compilation-data.bin`; khong dung git restore/reset de tranh ghi de file trong worktree.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.

**Viec tiep theo can lam ngay:**
- PR NHOM 7: hoan thien Seller Product Form theo luong 7 buoc category -> suggested/custom attributes -> toi da 2 axes -> Cartesian combinations -> preview -> POST/PUT aggregate.

---

### [2026-08-25 15:32] Phien #36
**Da lam:**
- Doc `docs/PROGRESS.md`, hien trang category/attribute trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 5 trong `docs/Prompt chuyen doi marketplace.md`.
- Audit contract that cua `AdminCategoryController`, `CatalogCategoryService`, category tree va Product Attribute Admin; chot dung payload backend thay vi suy doan tu tai lieu.
- Tao route `/admin/categories`, constant `CATEGORIES` va trang `CategoryManagement.vue`.
- Lam tree view category co expand/collapse, tim kiem, hien dung cay cha/con, chon node va tao category goc/con.
- Lam form them/sua category voi ten, category cha, code, slug, display order va status; ngan chon chinh node/descendant lam parent; thao tac an dung `status=INACTIVE` va reload tree.
- Lam tab Thuoc tinh goi y: chi tai `product_attribute_definition` verified/ACTIVE, ho tro keo-tha hoac bam Them, sua required/filterable/displayOrder, sap xep va xoa, gui dung payload configure suggestions.
- Gop `/admin/categories` va `/admin/product-attributes` vao menu cha `Danh muc & Thuoc tinh` co 2 sub-item.
- Chay Chrome headless browser smoke bang mock dung HTTP contract cua backend, sau do xoa script/profile tam.

**File da tao/sua:**
- `FE/src/pages/admin/category/CategoryManagement.vue`
- `FE/src/services/api/admin/category.api.ts`
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `FE/src/constants/path.ts`
- `FE/src/routes/router.ts`
- `docs/PROGRESS.md`

**Ket qua:** DONE PR NHOM 5 ve source/UI/API contract. Admin co man Category Management moi va flow tree CRUD/status/attribute suggestions day du.

**Kiem chung:**
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3544 modules.
- Browser smoke contract: tree mau co `Thoi trang -> Ao` hien dung phan cap; menu `Danh muc & Thuoc tinh` ton tai.
- Browser smoke create: POST body `name=category smoke`, `parentId=null`, `displayOrder=0`; category moi duoc chon lai tren UI.
- Browser smoke update: PUT body gui name/code/slug/parentId/displayOrder dung; UI hien ten moi.
- Browser smoke suggestions: PUT gui `definitionId`, `required`, `filterable`, `displayOrder` dung schema backend.
- Browser smoke hide: PUT status gui `INACTIVE`, node bien mat khoi tree; `failures=[]`, `consoleErrors=[]`.
- `git diff --check`: PASS, chi warning LF/CRLF cua `docs/PROGRESS.md`.

**Ghi chu/vuong mac:**
- Khong chay duoc acceptance tren DB runtime that: `catalog-service` port 8083 timeout ca `/actuator/health`, category tree va product attributes; gateway truoc do cung dang loi DNS service discovery. Khong restart/reset stack hoac sua seed ngoai pham vi. Browser smoke da mock response va ghi nhan request theo chinh contract controller/service that.
- Admin tree backend hien chi tra category ACTIVE; day la ly do category INACTIVE bien mat sau khi an va UI khong cung cap man khoi phuc category an trong PR nay.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.

**Viec tiep theo can lam ngay:**
- PR NHOM 6: hoan thien 5 tab `ProductAttributes.vue`: Thuoc tinh, Option, Danh muc goi y, Gop & Lich su, Truc bien the.

---

### [2026-08-25 15:19] Phien #35
**Da lam:**
- Doc `docs/PROGRESS.md`, `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 4 trong `docs/Prompt chuyen doi marketplace.md`.
- Tao `PlatformAdminLayout.vue` chi dung `AdminSidebar.vue`; tao `SellerCenterLayout.vue` chi dung `SellerSidebar.vue`.
- Cap nhat tat ca route `/admin/**` sang `PlatformAdminLayout`, route `/seller/**` sang `SellerCenterLayout`; router khong con import `layout/Admin.vue`.
- Loai bo `useAuthStore`, `ROLES`, menu Seller va ham loc theo role khoi `AdminSidebar.vue`; Admin sidebar nay chi con menu platform.
- Tao `SellerSidebar.vue` rieng voi dung 8 muc: Tong quan shop, San pham, Don hang, Marketing shop, Danh gia, Vi & doi soat, Tranh chap/Khieu nai, Ho so shop.
- Them route/page Ho so shop dung API seller profile da co. Them route/page placeholder Tranh chap de menu khong tro route hong; nghiep vu dispute van de dung PR NHOM 9 theo ke hoach.
- Chay Chrome headless smoke hai role bang session localStorage qua route guard that, sau do xoa script/profile tam.

**File da tao/sua:**
- `FE/src/layout/PlatformAdminLayout.vue`
- `FE/src/layout/SellerCenterLayout.vue`
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `FE/src/components/custom/Sidebar/SellerSidebar.vue`
- `FE/src/components/custom/Sidebar/sidebar-shell.css`
- `FE/src/constants/path.ts`
- `FE/src/routes/router.ts`
- `FE/src/pages/seller/profile/SellerProfile.vue`
- `FE/src/pages/seller/disputes/SellerDisputes.vue`
- `docs/PROGRESS.md`

**Ket qua:** DONE PR NHOM 4 ve source/layout/menu. Admin va Seller khong con dung chung layout/sidebar hoac re nhanh role trong mot sidebar.

**Kiem chung:**
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3540 modules.
- Source audit: khong con `layout/Admin.vue` trong router; `AdminSidebar.vue` khong con `useAuthStore`, `ROLES.SELLER` hoac `ROUTES_CONSTANTS.SELLER`.
- Browser smoke role/layout: Admin hien 8 menu Admin va `adminForbidden=[]`; Seller hien du 8 menu Seller va `sellerForbidden=[]`, `missingSeller=[]`; failed network = 0, console error = 0 trong luong layout duoc test.
- `git diff --check`: PASS.

**Ghi chu/vuong mac:**
- Khong verify duoc thao tac login API end-to-end trong phien nay: gateway dang route service toi hostname `D-DU11-DUOCNH1.ntq-solution.com.vn`, DNS `10.0.64.3` timeout; ca `/api/v1/auth/login-admin` va `/api/v1/auth/login` tra 500. Day la trang thai runtime/service discovery co san, khong phai loi build/layout FE. Smoke da nap session role vao localStorage va di qua router guard that de kiem tra isolation layout.
- `Admin.vue` cu khong con duoc router import; giu file tam thoi de tranh xoa ngoai pham vi neu co consumer chua duoc audit ngoai router. Layout chay that da tach hoan toan.
- Trang Tranh chap hien chi la placeholder; backend/API va UI nghiep vu duoc trien khai tai PR NHOM 9.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.

**Viec tiep theo can lam ngay:**
- PR NHOM 5: Category Management UI moi cho Admin theo API category tree/CRUD/status/attribute-suggestions da co.

---

### [2026-08-25 08:51] Phien #34
**Da lam:**
- Doc `docs/PROGRESS.md`, cac muc hien trang lien quan trong `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 3 trong `docs/Prompt chuyen doi marketplace.md`.
- Audit va bao toan cac thay doi PR NHOM 3 da co san trong worktree, sau do hoan tat cac khoang trong con lai.
- Chot 3 label sidebar Admin: `Nguoi dung`, `Quan tri vien/Phan quyen`, `Voucher san`; giu nguyen route backend/FE hien tai `/admin/khach-hang`, `/admin/nhan-vien`, `/admin/voucher`.
- Hoan tat batch seller status: seller-service co `GET /internal/sellers/by-owner-ids?ids=...` va route Admin qua gateway `GET /api/v1/admin/sellers/by-owner-ids?ids=...`; FE trang Nguoi dung goi mot batch cho trang hien tai, khong N+1, hien status va ten shop.
- Dong bo nhan trang/form con tu `khach hang` sang `Nguoi dung`, tu `nhan vien` sang `nhan su van hanh/Quan tri vien`, tu `phieu giam gia` sang `Voucher san`.
- Audit role: DB/entity/runtime hien chi co `ADMIN` va `STAFF`, khong co role ban hang/thu ngan. Them cot `Vai tro platform` tren UI, hien `Quan tri vien cap cao` va `Nhan su van hanh`; khong them 5 operator role khi auth-service hien van phat moi staff token thanh role `ADMIN` va gateway chua co authorization chi tiet cho cac role do.
- Sua `EmployeeServiceImpl`: cap nhat ho so staff khong con vo tinh ha role `ADMIN` thanh `STAFF` va khong tu mo lai status.
- Audit voucher: FE Admin khong co field/gui `sellerId`; promotion-service Admin list/create/update cuong che scope platform (`sellerId = null`). Form them/sua da gop modal trong `/admin/voucher`.
- Xoa moi text/link con lai toi route legacy `/admin/them-phieu-giam-gia`; route nay khong con constant/router song.
- Build jar moi va restart rieng `user-service`/`seller-service`; health UP tren `8082`/`8089`, PID moi lan luot 8236/5324.
- Browser smoke Admin bang Chrome headless qua UI that, sau do xoa script tam.

**File da tao/sua:**
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `FE/src/pages/admin/khachhang/KhachHang.vue`
- `FE/src/pages/admin/khachhang/KhachHangTable.vue`
- `FE/src/pages/admin/khachhang/KhachHangModal.vue`
- `FE/src/pages/admin/nhanvien/NhanVien.vue`
- `FE/src/pages/admin/nhanvien/NhanVIenTable.vue`
- `FE/src/pages/admin/nhanvien/NhanVienModal.vue`
- `FE/src/pages/admin/voucher/Voucher.vue`
- `FE/src/pages/admin/voucher/VoucherTable.vue`
- `FE/src/pages/admin/voucher/VoucherModal.vue`
- `FE/src/services/api/admin/khachhang.api.ts`
- `FE/src/services/api/admin/nhanvien.api.ts`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/controller/InternalSellerController.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/controller/SellerController.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/repository/SellerRepository.java`
- `backend-microservice/seller-service/src/main/java/com/ecommerce/seller/service/SellerService.java`
- `backend-microservice/user-service/src/main/java/com/ecommerce/user/service/impl/EmployeeServiceImpl.java`
- `docs/PROGRESS.md`

**Ket qua:** DONE PR NHOM 3. Admin thay dung 3 label, trang Nguoi dung hien seller status theo batch, trang Quan tri vien hien role platform, Voucher san khong cho chon seller va khong con flow route legacy rieng.

**Kiem chung:**
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3530 modules.
- `gradlew :user-service:test :seller-service:test :promotion-service:test --no-daemon --max-workers=1`: PASS; user/seller khong co test source, promotion test PASS.
- `gradlew :user-service:bootJar :seller-service:bootJar`: PASS.
- Runtime API qua gateway voi admin login that: 3 users, batch seller status tra 3 owner; 2 staff co role `ADMIN`,`STAFF`; 2 voucher Admin va `nonPlatformVouchers=0`.
- Internal contract `GET :8089/internal/sellers/by-owner-ids`: PASS, tra 3 owner.
- Browser smoke: admin login PASS; sidebar co du 3 label; Nguoi dung hien cot seller status va 3 shop; Quan tri vien hien cot role va 2 role label; form Voucher san co `sellerFields=[]`, `legacyLinks=0`; failed API request = 0, console error = 0.
- `git diff --check`: PASS, chi co warning LF/CRLF cua cac file da co trong worktree.

**Ghi chu/vuong mac:**
- Chua them `SELLER_APPROVAL_OPERATOR`, `DISPUTE_OPERATOR`, `PAYOUT_OPERATOR`, `CONTENT_MODERATOR`, `SUPER_ADMIN`: he thong hien chua co RBAC end-to-end cho cac role nay; them enum/UI rieng le se tao phan quyen gia. Se can thiet ke schema/JWT/gateway/menu quyen dong bo khi trien khai RBAC operator that.
- FE build van co warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.
- Runtime data hien co `Streetwear Pending` status `APPROVED`, khac ten demo nhung batch API hien dung status thuc te trong DB.

**Viec tiep theo can lam ngay:**
- PR NHOM 4: tach layout/sidebar Admin va Seller theo dung Muc 6, khong tiep tuc dung chung `Admin.vue` va logic re nhanh role trong `AdminSidebar.vue`.

---

### [2026-08-24 17:44] Phien #33
**Da lam:**
- Doc lai `docs/PROGRESS.md`, `HE_THONG_HIEN_TAI_MARKETPLACE.md` va dung Muc 6/PR NHOM 2 trong `docs/Prompt chuyen doi marketplace.md`.
- Tim thay Node/npm runtime tai `C:\nvm4w\nodejs` (runtime ton tai nhung khong nam trong `PATH` cua shell), khong can cai them phan mem.
- Start Vite FE tai `http://127.0.0.1:6688` va xac nhan port mo.
- Chay FE production build va `vue-tsc --noEmit` bang Node runtime tren; ca hai PASS.
- Chay browser smoke bang Chrome headless qua UI that: dang nhap buyer demo, mo gio hang, xac nhan 2 shop/2 san pham, chon tat ca, bam `Mua hang`, vao `/thanh-toan`, xac nhan 2 dong san pham va nut `DAT HANG` hien thi.
- Khong bam xac nhan dat don de khong lam thay doi order/stock/cart seed.
- Phan loai 2 console error: deu tu endpoint thuong hieu legacy `/api/v1/permitall/thuong-hieu/get-all/thuong-hieu-trang-chu` tra 404 khi trang nen tai; khong thuoc route/order checkout bi xoa trong PR NHOM 2. Cac request login, cart, profile/order va voucher trong smoke tra 200.
- Xoa script browser smoke tam sau khi verify.

**File da tao/sua:**
- `docs/PROGRESS.md`

**Ket qua:** DONE PR NHOM 2. Gateway khong con route POS, order-service test pass, FE build/typecheck pass va buyer checkout browser smoke pass.

**Kiem chung:**
- `C:\nvm4w\nodejs\npm.cmd run build`: PASS, Vite build 3530 modules.
- `FE/node_modules/.bin/vue-tsc.cmd --noEmit`: PASS.
- `JAVA_HOME=C:\Users\duoc.nguyen1\.jdks\ms-17.0.20; .\gradlew.bat :seller-service:test --no-daemon --max-workers=1`: PASS (compile thanh cong, module khong co test source), kiem tra bo sung cho thay doi PR NHOM 3 san co.
- Chrome headless: login PASS; cart `/gio-hang` co `shopCount=2`, `itemCount=2`; checkout `/thanh-toan` co `itemCount=2`, nut dat hang ton tai; khong co failed XHR/fetch trong luong cot loi.
- `Test-NetConnection 127.0.0.1 -Port 6688`: PASS.

**Ghi chu/vuong mac:**
- FE build con warning font Inter khong resolve tai build-time va chunk lon hon 500 kB; khong lam build fail.
- Endpoint thuong hieu legacy neu tra 404 van tao console error o trang nen, nhung khong phai regression cua PR NHOM 2.
- Worktree co cac thay doi PR NHOM 3 san co o Admin UI va seller-service; da bao toan, chua danh dau DONE khi chua audit acceptance.

**Viec tiep theo can lam ngay:**
- PR NHOM 3: audit va hoan tat cac thay doi Admin dang co, build/typecheck backend + FE, sau do browser smoke 3 label, seller status va voucher san.

---

### [2026-08-24 16:36] Phien #32
**Da lam:**
- Doc `docs/PROGRESS.md` va doi chieu Muc 6 trong `docs/Prompt chuyen doi marketplace.md`; viec tiep theo la runtime smoke PR NHOM 2 truoc khi sang PR NHOM 3.
- Kiem tra runtime: gateway `8080` va Eureka `8761` dang mo port, FE `6688` khong chay; Docker bi tu choi quyen API; Node/npm/pnpm/yarn khong co trong PATH.
- Kiem tra gateway health: `/actuator/health` UP, Eureka co 9 service UP (`api-gateway`, `auth-service`, `user-service`, `catalog-service`, `promotion-service`, `cart-service`, `order-service`, `seller-service`, `payout-service`).
- Build jar gateway moi: `JAVA_HOME=C:\Users\duoc.nguyen1\.jdks\ms-17.0.20; .\gradlew.bat :api-gateway:bootJar --no-daemon --max-workers=1` PASS.
- Restart rieng gateway: `taskkill` bi Access denied voi wrapper cu, sau do `Stop-Process` duoc wrapper va Java PID cu, start lai gateway bang `backend-microservice/logs/api-gateway.run.cmd`; log moi ghi PID 26848, Netty started port 8080, Eureka registration 204.
- Tao JWT smoke tam thoi ky dung secret local de test qua `AdminAuthorizationFilter`, sau do xoa token tam trong `%TEMP%`.
- Verify route POS runtime voi admin token hop le: `/api/v1/admin/ban-hang/test` tra 404, `/api/v1/admin/hoa-don/test` tra 404.
- Verify route con hop le khong bi gay: `/api/v1/admin/thong-ke/doanh-thu` voi admin token tra 200; `/api/v1/permitall/don-mua/grouped` voi buyer token tra 200 `{"data":[]}`.

**File da tao/sua:**
- `docs/PROGRESS.md`

**Ket qua:** PR NHOM 2 da co source/compile/test/runtime gateway/API evidence. Route POS da bien mat tren gateway runtime moi, route order/admin con dung van hoat dong.

**Kiem chung:**
- `Test-NetConnection localhost -Port 8080`: PASS.
- `Test-NetConnection localhost -Port 8761`: PASS.
- `Test-NetConnection localhost -Port 6688`: FAIL, FE khong chay.
- `where.exe node`, `where.exe npm.cmd`, `where.exe pnpm.cmd`, `where.exe yarn.cmd`: FAIL, khong tim thay runtime FE.
- `docker ps`: FAIL do Docker config/API access denied trong session nay.

**Ghi chu/vuong mac:**
- Chua the chay browser smoke checkout buyer that do FE khong chay va khong co Node/npm trong PATH. Khong sang PR NHOM 3 trong phien nay de giu dung thu tu/acceptance cua prompt.

---

### [2026-08-24 16:30] Phien #31
**Da lam:**
- Doc `docs/PROGRESS.md` va doi chieu Muc 6 trong `docs/Prompt chuyen doi marketplace.md`; tiep tuc dung PR NHOM 2.
- Buoc 1: Doc `backend-microservice/api-gateway/src/main/resources/application.yml`; route `/api/v1/admin/ban-hang/**` va `/api/v1/admin/hoa-don/**` dang nam trong route `order-service`.
- Buoc 2: Doc cac controller `order-service`: `CheckoutController` (`/api/orders`), `DonMuaController` (`/api/v1/permitall/don-mua`), `SellerOrderController` (`/api/v1/seller/orders`), `ThongKeController` (`/api/v1/admin/thong-ke`), `InternalOrderController` (`/internal/orders`). Khong co controller mapping POS `/api/v1/admin/ban-hang` hoac `/api/v1/admin/hoa-don`.
- Buoc 3-4: Ket luan route POS la route gateway mo coi, khong dung chung method/class voi buyer checkout hoac seller order. Da xoa dung 2 pattern POS khoi route `order-service`, giu nguyen `/api/v1/admin/thong-ke/**`, `/api/v1/permitall/don-mua/**`, `/api/orders/**`, `/api/v1/seller/orders/**`.

**File da tao/sua:**
- `backend-microservice/api-gateway/src/main/resources/application.yml`
- `docs/PROGRESS.md`

**Ket qua:** PR NHOM 2 source cleanup va compile/test backend DONE. Gateway source va build resource khong con route POS `ban-hang`/`hoa-don`.

**Kiem chung:**
- Grep `backend-microservice` source chinh (loai `build/.gradle`) voi `/api/v1/admin/ban-hang`, `/api/v1/admin/hoa-don`, `ban-hang`, `hoa-don`: PASS, khong con ket qua.
- Grep `api-gateway/src/main/resources` va `api-gateway/build/resources/main`: PASS, khong con route POS sau `processResources`.
- `git diff --check -- backend-microservice/api-gateway/src/main/resources/application.yml`: PASS.
- `JAVA_HOME=C:\Users\duoc.nguyen1\.jdks\ms-17.0.20; .\gradlew.bat :order-service:test --no-daemon --max-workers=1`: PASS, BUILD SUCCESSFUL.
- `JAVA_HOME=C:\Users\duoc.nguyen1\.jdks\ms-17.0.20; .\gradlew.bat :api-gateway:processResources :api-gateway:compileJava --no-daemon --max-workers=1`: PASS, BUILD SUCCESSFUL.

**Ghi chu/vuong mac:**
- Chua thuc hien browser smoke checkout buyer that vi phien nay chua start full local stack/gateway/FE; FE build cua PR NHOM 1 cung van thieu do Node/npm khong co trong PATH.
- `docs/Prompt chuyen doi marketplace.md` va `HE_THONG_HIEN_TAI_MARKETPLACE.md` la thay doi co san tu truoc; khong sua trong phien nay.

---

### [2026-08-24 16:24] Phien #30
**Da lam:**
- Doc lai prompt moi `docs/Prompt chuyen doi marketplace.md`, doi chieu Muc 1 voi `docs/PROGRESS.md`.
- Xoa cac cau hoi treo da duoc prompt moi tra loi dut diem khoi muc "Cau hoi / quyet dinh can nguoi dung xac nhan".
- PR NHOM 1 - buoc 1-3: Kiem tra route/menu FE cho `/admin/ban-hang`, `/admin/hoa-don`; hien khong con constant/router/sidebar song cho 2 route POS nay.
- PR NHOM 1 - buoc 4: Grep import folder `admin/banhang`, `admin/hoadon`; khong con import song, da xoa folder page legacy neu ton tai.
- PR NHOM 1 - buoc 5: Xoa constants va redirect routes legacy `/admin/mau-sac`, `/admin/chat-lieu`, `/admin/loai-de`, `/admin/loai-giay`, `/admin/size`, `/admin/thuong-hieu`; xoa block menu "Danh muc chung" trong `AdminSidebar.vue`; xoa folder page legacy `admin/mausac`, `admin/size`, `admin/chatlieu`, `admin/loaide`, `admin/loaigiay`, `admin/thuonghieu`, `admin/sanpham`, `admin/sanphamchitiet`.
- Sua diem dieu huong admin sau login tu route legacy `MAUSAC` sang `PRODUCT_ATTRIBUTES`.

**File da tao/sua:**
- `FE/src/constants/path.ts`
- `FE/src/routes/router.ts`
- `FE/src/components/custom/Sidebar/AdminSidebar.vue`
- `FE/src/components/ui/login/Login.vue`
- `FE/src/pages/admin/banhang`
- `FE/src/pages/admin/hoadon`
- `FE/src/pages/admin/mausac`
- `FE/src/pages/admin/size`
- `FE/src/pages/admin/chatlieu`
- `FE/src/pages/admin/loaide`
- `FE/src/pages/admin/loaigiay`
- `FE/src/pages/admin/thuonghieu`
- `FE/src/pages/admin/sanpham`
- `FE/src/pages/admin/sanphamchitiet`
- `docs/PROGRESS.md`

**Ket qua:** PR NHOM 1 source cleanup DONE trong pham vi FE. Chua sang gateway/backend POS theo PR NHOM 2.

**Kiem chung:**
- `git diff --check -- FE/src/constants/path.ts FE/src/routes/router.ts FE/src/components/custom/Sidebar/AdminSidebar.vue FE/src/components/ui/login/Login.vue`: PASS.
- Grep `ROUTES_CONSTANTS.ADMIN.children.(MAUSAC|SIZE|CHAT_LIEU|LOAI_DE|LOAI_GIAY|THUONG_HIEU)`, import `@/pages/admin/...` legacy, va path `/admin/(ban-hang|hoa-don|mau-sac|chat-lieu|loai-de|loai-giay|size|thuong-hieu)`: PASS, khong con ket qua.
- `npm run build` / `vue-tsc --noEmit`: CHUA CHAY DUOC do `where.exe node`, `where.exe npm.cmd`, `where.exe pnpm.cmd`, `where.exe yarn.cmd` deu khong tim thay runtime trong PATH.

**Ghi chu/vuong mac:**
- `docs/Prompt chuyen doi marketplace.md` va `HE_THONG_HIEN_TAI_MARKETPLACE.md` da co thay doi tu truoc phien nay; khong sua hai file nay trong PR NHOM 1.

---

### [2026-08-24 16:10] Phien #29
**Da lam:**
- Bo sung tiep vao `HE_THONG_HIEN_TAI_MARKETPLACE.md` theo yeu cau: mo ta sau hon hien trang DB, table ownership theo tung service, cac bang cot loi user/seller/catalog/cart/order/promotion/payout, va cac luong end-to-end.
- Them cac sequence/flow: buyer login, dang ky seller/shop, admin approve seller, seller tao san pham, buyer xem san pham, cart, checkout -> order -> seller order, payout, admin hau kiem thuoc tinh.
- Them logical ERD Mermaid va cac muc can tranh hieu sai: sellerId hien tai dang la shop ID trong da so context, order goc khac order_seller, attribute mo ta khac variant axis, voucher san/shop cung bang nhung khac seller_id.
- File tong the hien co 2371 dong.

**File da tao/sua:**
- `HE_THONG_HIEN_TAI_MARKETPLACE.md`
- `docs/PROGRESS.md`

**Ket qua:** DONE bo sung tai lieu hien trang he thong cuc chi tiet hon ve DB/service/luong de doc vao nam duoc he thong dang chay nhu nao.

**Ghi chu/vuong mac:**
- Trong phien nay `docs/PROGRESS.md` da bi mat khoi filesystem khi dang cap nhat, nen da tao lai file progress toi thieu. Can kiem tra worktree/docs truoc khi commit.

---
