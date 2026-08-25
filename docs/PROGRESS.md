# PROGRESS.md - Nhat ky tien do chuyen doi Marketplace

## Trang thai tong quan hien tai
- Giai doan: Rollout theo prompt marketplace moi, da hoan tat PR NHOM 3 va san sang sang PR NHOM 4.
- Task dang lam do (neu co): Khong. PR NHOM 3 da PASS source audit, backend compile/test, FE build/typecheck, API runtime va browser smoke Admin.
- Viec tiep theo can lam ngay: Bat dau PR NHOM 4 theo dung thu tu: tao `PlatformAdminLayout.vue`, `SellerCenterLayout.vue`, tach `AdminSidebar.vue`/`SellerSidebar.vue`, cap nhat router va browser smoke role/layout.

## Cau hoi / quyet dinh can nguoi dung xac nhan
- Khong con cau hoi treo trong pham vi Muc 1 prompt moi; cac quyet dinh nghiep vu da duoc chot dut diem trong prompt.

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
- [ ] PR NHOM 4: Tach layout/sidebar Admin va Seller
- [ ] Chuan hoa san pham: schema target + reset seed demo da nganh, xoa 6 bang hard-code
- [ ] Thuoc tinh dong: Seller suggestion/autocomplete/tu them tren form san pham
- [ ] Thuoc tinh dong: Buyer filter theo danh muc va product detail
- [ ] Thuoc tinh dong: Platform Admin hau kiem/chuan hoa/gop/an
- [ ] Chat buyer-seller
- [ ] Flash sale toan san

## Nhat ky chi tiet

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
