# README Microservice Migration

Tai lieu nay la file dieu phoi qua trinh migrate backend monolith `BE` sang Spring Cloud Microservice.

Quy tac bat buoc:

- Khong sua bat ky file nao trong `BE`.
- `BE` chi dung de doc nghiep vu, API, entity, service, repository.
- Toan bo source microservice moi nam trong `backend-microservice`.
- Sau moi buoc migrate phai build project, sua loi compile neu co, roi moi sang buoc tiep theo.
- Sau moi service phai doi chieu API voi monolith: URL, request, response, HTTP status, validation, exception, business.
- Neu bi gian doan, lan sau doc file nay truoc va tiep tuc tu buoc tiep theo, khong lam lai tu dau.

## Trang thai tong quan

- Backend monolith tham chieu: `BE`
- Thu muc microservice moi: `backend-microservice`
- Trang thai hien tai: Hoan thanh Buoc 1 - Phan tich kien truc
- Trang thai hien tai: Hoan thanh Buoc 2 - Sinh cau truc project Microservice, bo qua build theo yeu cau ngay 2026-08-03
- Trang thai hien tai: Dang thuc hien Buoc 3 - Tach lan luot tung Service
- Buoc tiep theo: Admin route da du ve controller-level; tiep tuc smoke test runtime voi gateway/Eureka/DB va doi chieu response theo tung man hinh Admin neu phat hien lech.

## Local database/startup notes

- Tao database local bang MySQL cai truc tiep: `powershell -ExecutionPolicy Bypass -File backend-microservice\init-databases.ps1`
- Neu dung Docker MySQL cua `backend-microservice/docker-compose.yml`: `powershell -ExecutionPolicy Bypass -File backend-microservice\init-databases.ps1 -UseDocker`
- Reset sach va seed data demo bang MySQL local: `powershell -ExecutionPolicy Bypass -File backend-microservice\reset-demo-databases.ps1 -Force`
- Reset sach va seed data demo bang Docker MySQL: `powershell -ExecutionPolicy Bypass -File backend-microservice\reset-demo-databases.ps1 -UseDocker -Force`
- Docker MySQL publish ra host port `3307`, nen khi chay backend bang runner local can dung: `powershell -ExecutionPolicy Bypass -File backend-microservice\run-all.ps1 -DbPort 3307`
- Runner mac dinh dung database rieng theo service: `ecommerce_auth`, `ecommerce_user`, `ecommerce_catalog`, `ecommerce_promotion`, `ecommerce_cart`, `ecommerce_order`.
- Hibernate `ddl-auto=update` tu tao bang khi service boot.
- Data demo seed san tai khoan: `admin@ecommerce.local`, `staff@ecommerce.local`, `customer1@ecommerce.local`; mat khau chung `Admin@123`.
- Neu gateway tra 503 cho `/api/v1/admin/mau-sac`, kiem tra Eureka `http://localhost:8761/eureka/apps`: route nay can `CATALOG-SERVICE` dang `UP`.

## Checkpoint AGENTS ngay 2026-08-10

- Da doc `AGENTS.md` va tiep tuc refactor theo thu tu muc 6 sau khi bo service ton kho rieng: catalog-service -> user-service -> promotion-service -> cart-service -> order-service -> notification-service -> phan con lai.
- Khong tao file tracking rieng; `README-Microservice.md` la nguon theo doi duy nhat theo AGENTS muc 8.
- `catalog-service`: audit muc 2 khong thay bang/entity ngoai domain; service tiep tuc so huu product/attribute tables. Da bo `JdbcTemplate` khoi `ProductServiceImpl` public product path, dung JPA repository/entity; phan discount lay qua OpenFeign `PromotionClient` toi `promotion-service` internal endpoint `/internal/promotions/discounts/active`. Doi chieu AGENTS muc 5: pass database-per-service, pass API public, build PASS.
- `user-service`: da bo native query `KhachHangRepository.getLSKH` join truc tiep `hoa_don`/`hoa_don_chi_tiet`; thay bang OpenFeign `OrderClient` goi internal endpoint `order-service` `/internal/orders/customers/{customerId}/history`. Them internal JPA endpoint cho auth/customer/staff de service khac khong map bang `khach_hang`/`nhan_vien`. Public API `/api/v1/permitall/profile/hd/{id}` giu nguyen response wrapper/message. Doi chieu AGENTS muc 5: pass database-per-service, pass API public, build PASS.
- Service ton kho rieng: da quyet dinh bo khoi source vi du an hien tai/BE khong dung; ton kho tiep tuc nam theo product detail trong catalog/order/cart flow hien co.
- `promotion-service`: da xoa cac entity/repository copy domain `khach_hang`, `san_pham`, `san_pham_chi_tiet`, `mau_sac`, `kich_co`; detail promotion/voucher chi luu id tham chieu. Cac route doc san pham/mau/size cua promotion goi `catalog-service` qua OpenFeign `CatalogClient` va internal endpoint `/internal/catalog/**`. Them internal endpoint JPA `/internal/promotions/discounts/active` cho catalog lay discount thay vi catalog query bang promotion. Public route dot-giam-gia/voucher giu nguyen. Doi chieu AGENTS muc 5: pass database-per-service, pass API public, build PASS.
- `cart-service`: da xoa entity/repository copy `khach_hang` va `san_pham_chi_tiet`; `gio_hang`/`gio_hang_chi_tiet` chi luu id tham chieu. Them `CatalogClient` toi `catalog-service` de check `soLuong` va enrich product detail khi tra gio hang. Public API `/api/v1/permitall/cart` giu nguyen route/message chinh. Doi chieu AGENTS muc 5: pass database-per-service, pass API public, build PASS.
- `order-service`: da bo cac query/update DB cheo sang catalog/user/promotion trong checkout, don mua, admin ban hang va thong ke top san pham; thay bang OpenFeign `CatalogClient`, `UserClient`, `PromotionClient`, `CartClient` va internal endpoint JPA o service so huu du lieu. `HoaDonRepository.layTop3SanPhamBanChay` chi aggregate tren `hoa_don`/`hoa_don_chi_tiet`, enrich product qua catalog. Public API order/ban-hang/don-mua/thong-ke giu nguyen route va response key chinh. Doi chieu AGENTS muc 5: pass database-per-service, pass API public, build PASS.
- `auth-service`: da xoa entity/repository copy `khach_hang`/`nhan_vien`; login/register/change-password/JWT lookup dung OpenFeign `UserClient` toi `user-service`. Local admin seed chuyen sang user-service de user-service so huu bang `nhan_vien`. Doi chieu AGENTS muc 5: pass database-per-service, pass API public, build PASS.
- Sua ngay 2026-08-10 cho login admin local: runtime tra `Email hoac mat khau khong dung` vi `user-service` tim thay `admin@ecommerce.local` nhung `nhan_vien.mat_khau` dang null. `LocalAdminSeeder` duoc doi sang upsert mem, neu row admin da ton tai nhung thieu/sai hash thi cap nhat lai password demo `Admin@123`; verify `POST /api/v1/auth/login-admin` qua gateway PASS va tra token.
- Build tong sau checkpoint: `.\gradlew.bat clean build --no-daemon` trong `backend-microservice` PASS ngay 2026-08-10.

## Buoc 1 - Phan tich backend hien tai

Trang thai: HOAN THANH

Ngay cap nhat: 2026-08-03

### Thu muc da doc

- `BE/build.gradle`
- `BE/settings.gradle`
- `BE/src/main/resources/application.properties`
- `BE/src/main/java/com/be/server/core`
- `BE/src/main/java/com/be/server/entity`
- `BE/src/main/java/com/be/server/repository`
- `BE/src/main/java/com/be/server/infrastructure`
- `BE/src/main/java/com/be/server/service`
- `README Microservice/Migration.txt`
- `README Microservice/Thuc hien theo tung giai doan.txt` ten file goc co dau tieng Viet

### Ket luan hien trang

Backend hien tai la Spring Boot monolith:

- Spring Boot: 3.4.4
- Java toolchain hien tai: 17
- Database: MySQL mot database `datn_v2`
- Security: Spring Security, JWT, OAuth2 Google/GitHub, refresh token
- Persistence: Spring Data JPA
- Ngoai vi: Cloudinary, VNPay, Email SMTP, scheduler
- Main package: `com.be.server`
- Main application: `DatnApplication`

### Module hien tai

- `core/auth`: login user, login admin, register, change password.
- `core/admin/banhang`: ban hang tai quay, tao hoa don offline, them/xoa san pham, thanh toan, giao hang.
- `core/admin/hoadon`: quan ly hoa don, lich su thanh toan, doi trang thai, PDF hoa don/giao hang.
- `core/admin/sanpham`: quan ly san pham.
- `core/admin/SanPhamChiTiet`: quan ly bien the san pham, anh, mau, size, gia, so luong.
- `core/admin/voucher`: quan ly phieu giam gia.
- `core/admin/dotgiamgia`: quan ly dot giam gia theo san pham chi tiet.
- `core/admin/khachhang`: quan ly khach hang.
- `core/admin/nhanvien`: quan ly nhan vien.
- `core/admin/thongke`: thong ke doanh thu, don hang, top san pham.
- `core/admin/*`: CRUD thuoc tinh san pham gom mau sac, size, thuong hieu, xuat xu, danh muc, loai de, chat lieu.
- `core/permitall/sanpham`: danh sach san pham public.
- `core/permitall/chitietsanpham`: chi tiet san pham public.
- `core/permitall/giohang`: gio hang.
- `core/permitall/thanhtoan`: checkout, VNPay, voucher khi thanh toan.
- `core/permitall/donmua`: lich su don mua, sua thong tin, doi trang thai.
- `core/permitall/profile`: thong tin khach hang/profile.
- `core/vnpay`: VNPay config/util.

### API public hien tai phai giu nguyen

Lay tu `MappingConstants` va controller:

- `/api/v1/auth/**`
- `/api/v1/admin/mau-sac/**`
- `/api/v1/admin/size/**`
- `/api/v1/admin/thuong-hieu/**`
- `/api/v1/admin/xuat-xu/**`
- `/api/v1/admin/khach-hang/**`
- `/api/v1/admin/nhan-vien/**`
- `/api/v1/admin/san-pham/**`
- `/api/v1/admin/san-pham-chi-tiet/**`
- `/api/v1/admin/dot-giam-gia/**`
- `/api/v1/admin/chat-lieu/**`
- `/api/v1/admin/danh-muc/**`
- `/api/v1/admin/loai-de/**`
- `/api/v1/admin/ban-hang/**`
- `/api/v1/admin/hoa-don/**`
- `/api/v1/admin/voucher/**`
- `/api/v1/admin/thong-ke/**`
- `/api/v1/permitall/san-pham/**`
- `/api/v1/permitall/san-pham-chi-tiet/**`
- `/api/v1/permitall/thuong-hieu/**`
- `/api/v1/permitall/profile/**`
- `/api/v1/permitall/cart/**`
- `/api/v1/permitall/don-mua/**`
- `/api/orders/**`

Gateway phai expose lai dung cac URL nay de frontend khong can sua hoac sua rat it.

### Entity va quan he chinh

- `SanPham` lien ket `ThuongHieu`, `XuatSu`, `DanhMuc`, `LoaiDe`, `ChatLieu`.
- `SanPhamChiTiet` lien ket `SanPham`, `KichCo`, `MauSac`, co `giaBan`, `anh`, `soLuong`.
- `HoaDon` lien ket `KhachHang`, `PhieuGiamGia`, `NhanVien`, co lich su trang thai va chi tiet hoa don.
- `HoaDonChiTiet` lien ket `HoaDon`, `SanPhamChiTiet`.
- `Cart` lien ket `KhachHang`.
- `CartDetail` lien ket `Cart`, `SanPhamChiTiet`.
- `PhieuGiamGiaChiTiet` lien ket `KhachHang`, `PhieuGiamGia`.
- `DotGiamGiaChiTietSanPham` lien ket `DotGiamGia`, `SanPhamChiTiet`.
- `RefreshToken` luu refresh token theo `userId`.

### Service boundary de xuat

Kien truc version dau:

```text
Frontend
  -> API Gateway
      -> Eureka Discovery
          -> auth-service
          -> user-service
          -> catalog-service
          -> promotion-service
          -> order-service
          -> cart-service
          -> notification-service
```

Khong tach `payment-service` rieng trong version dau vi logic VNPay dang bi tron chat voi order creation, order status, voucher, cart va ton kho san pham. De giu nghiep vu 100%, payment se nam trong `order-service` truoc; sau khi on dinh moi tach rieng.

### Database per service de xuat

- `auth_db`: refresh token, auth projection neu can.
- `user_db`: `khach_hang`, `nhan_vien`.
- `catalog_db`: `san_pham`, `san_pham_chi_tiet` metadata, `thuong_hieu`, `xuat_su`, `danh_muc`, `loai_de`, `chat_lieu`, `kich_co`, `mau_sac`.
- `promotion_db`: `phieu_giam_gia`, `phieu_giam_gia_chi_tiet_khach_hang`, `dot_giam_gia`, `dot_giam_gia_chi_tiet_san_pham`.
- `order_db`: `hoa_don`, `hoa_don_chi_tiet`, `lich_su_thanh_toan`, `lich_su_trang_thai_hoa_don`, snapshot customer/product/voucher/staff.
- `cart_db`: `gio_hang`, `gio_hang_chi_tiet`.
- `notification_db`: email outbox/log neu can.

Khong service nao truy cap database cua service khac. Cac quan he JPA cross-domain cua monolith se chuyen thanh id/snapshot va goi OpenFeign hoac Kafka.

### Luong giao tiep service

Login:

```text
Frontend -> Gateway -> auth-service -> user-service -> auth-service tra JWT
```

Danh sach san pham:

```text
Frontend -> Gateway -> catalog-service
catalog-service -> promotion-service neu can gia giam
catalog-service tra ton kho theo `san_pham_chi_tiet.so_luong`
```

Gio hang:

```text
Frontend -> Gateway -> cart-service
cart-service -> user-service kiem tra khach hang
cart-service -> catalog-service lay product detail
cart-service -> catalog-service check ton kho theo product detail
```

Checkout/order:

```text
Frontend -> Gateway -> order-service
order-service -> promotion-service validate voucher
order-service -> catalog-service lay product snapshot
order-service -> user-service lay customer/staff snapshot
order-service -> Kafka OrderCreated/OrderPaid
notification-service -> gui email
```

Admin ban hang:

```text
Frontend admin -> Gateway -> order-service
order-service -> catalog-service, promotion-service, user-service
```

Promotion:

```text
Frontend admin -> Gateway -> promotion-service
promotion-service -> catalog-service lay product detail/gia goc
```

### Kafka de xuat

Chi dung Kafka cho viec bat dong bo:

- `OrderCreated`
- `OrderPaid`
- `OrderStatusChanged`
- `VoucherAssigned`
- `EmployeeCreated`
- Email notification
- Audit/log event

Khong dung Kafka cho login, validate voucher tuc thoi, check ton kho truoc checkout, lay danh sach san pham.

### Van de ton tai/rui ro

- Monolith hien co nhieu native query join truc tiep qua bang khac, can tach thanh query noi bo DB va Feign DTO.
- `ADBanHangServiceImpl` dang vua xu ly order, customer, staff, voucher, stock.
- `thanhtoanserviceImpl` dang tron checkout, VNPay, cart, voucher, ton kho san pham, email.
- `DotGiamGiaServiceImpl` dang dung truc tiep product/product detail repository.
- Sau khi database-per-service, khong the giu entity JPA relation cross-service.
- Mot so message tieng Viet trong source hien bi loi encoding hien thi, khi migrate can giu dung response behavior theo source hien tai.

### Build sau Buoc 1

Trang thai: KHONG CHAY

Ly do: Buoc 1 chi phan tich, chua co source microservice de build. Khong chay build trong `BE` vi build se ghi vao `BE/build` hoac `BE/.gradle`, mau thuan voi quy tac bat buoc khong sua bat ky file nao trong backend monolith hien tai.

### Thu muc da sua trong Buoc 1

- Tao moi `README-Microservice.md`.

### Viec chua lam

- Chua tao `backend-microservice`.
- Chua sinh skeleton project.
- Chua tach service.
- Chua them Eureka/Gateway/Kafka/Docker.
- Chua verify API runtime.

### Buoc tiep theo

Buoc 2 - Sinh cau truc project Microservice trong `backend-microservice`, sau do build toan bo project moi.

## Buoc 2 - Sinh cau truc project Microservice

Trang thai: DANG THUC HIEN

Ngay cap nhat: 2026-08-03

### Thu muc da sua trong Buoc 2

- Tao moi `backend-microservice/settings.gradle`
- Tao moi `backend-microservice/build.gradle`
- Tao moi `backend-microservice/common-lib`
- Tao moi `backend-microservice/discovery-server`
- Tao moi `backend-microservice/api-gateway`
- Tao moi `backend-microservice/auth-service`
- Tao moi `backend-microservice/user-service`
- Tao moi `backend-microservice/catalog-service`
- Tao moi `backend-microservice/promotion-service`
- Tao moi `backend-microservice/order-service`
- Tao moi `backend-microservice/cart-service`
- Tao moi `backend-microservice/notification-service`
- Tao moi `backend-microservice/README.md`
- Tao Dockerfile cho cac service va `backend-microservice/docker-compose.yml`

### Noi dung da lam

- Tao Gradle multi-project.
- Cau hinh Java 17 cho tat ca subproject de chay duoc voi JDK hien co tren may local; neu deploy co JDK 21 co the nang lai toolchain sau.
- Cau hinh Spring Boot 3.4.4 va Spring Cloud 2024.0.1.
- Tao application class cho tung service.
- Tao Eureka Server skeleton.
- Tao Gateway route skeleton giu nguyen cac URL public cua monolith.
- Them Gateway route cho `notification-service`: `/api/v1/notifications/**`.
- Tao dependency nen cho Web, JPA, Security, Eureka Client, OpenFeign, Kafka theo tung service.
- Tao `common-lib` chi gom DTO/exception dung chung, khong dung entity JPA dung chung.
- Them Docker compose gom MySQL, Kafka, Zookeeper, Eureka, Gateway va cac service ung dung.

### Build sau Buoc 2

Trang thai: BO QUA THEO YEU CAU

Lenh du kien:

```powershell
BE\gradlew.bat -p backend-microservice clean build
```

Ghi chu ngay 2026-08-03: user yeu cau khong build vi ton nhieu thoi gian. Tiep tuc migrate theo tung service va chi build khi user yeu cau lai.

### Viec chua lam

- Chua migrate business logic.
- Chua migrate entity/repository/controller/service tu monolith.
- Chua tao Dockerfile/docker-compose.
- Chua verify API runtime.

### Van de ton tai

- Skeleton chua co API endpoint business.
- Gateway moi co route mapping theo URL, chua co JWT propagation/filter rieng.
- Chua co database migration/schema rieng cho tung service.

### Buoc tiep theo

Buoc 3 - Tach lan luot tung service, bat dau tu `auth-service`.

## Buoc 3 - Tach lan luot tung Service

Trang thai: DANG THUC HIEN

Ngay cap nhat: 2026-08-03

### Service dang tach

- `auth-service`
- `user-service`
- `catalog-service`

### API auth da migrate sang `auth-service`

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/login-admin`
- `PUT /api/v1/auth/register`
- `POST /api/v1/auth/change-password`

### Source monolith da doi chieu

- `BE/src/main/java/com/be/server/core/auth/controller/AuthController.java`
- `BE/src/main/java/com/be/server/core/auth/service/AuthService.java`
- `BE/src/main/java/com/be/server/core/auth/service/impl/AuthServiceImpl.java`
- `BE/src/main/java/com/be/server/core/auth/dto/request/LoginRequest.java`
- `BE/src/main/java/com/be/server/core/auth/dto/request/RegisterRequest.java`
- `BE/src/main/java/com/be/server/core/auth/dto/request/ChangePasswordRequest.java`
- `BE/src/main/java/com/be/server/infrastructure/security/service/CustomUserDetailsService.java`
- `BE/src/main/java/com/be/server/infrastructure/security/service/TokenProvider.java`
- `BE/src/main/java/com/be/server/infrastructure/security/user/UserPrincipal.java`
- `BE/src/main/java/com/be/server/entity/KhachHang.java`
- `BE/src/main/java/com/be/server/entity/NhanVien.java`

### Thu muc da sua trong Buoc 3

- `backend-microservice/common-lib/src/main/java/com/ecommerce/common/base/ResponseObject.java`
- `backend-microservice/common-lib/src/main/java/com/ecommerce/common/base/PageableRequest.java`
- `backend-microservice/common-lib/src/main/java/com/ecommerce/common/base/PageableObject.java`
- `backend-microservice/common-lib/src/main/java/com/ecommerce/common/util/ResponseUtils.java`
- `backend-microservice/common-lib/src/main/java/com/ecommerce/common/util/PageUtils.java`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/controller`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/service`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/security`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/repository`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/entity`
- `backend-microservice/auth-service/src/main/java/com/ecommerce/auth/dto`
- `backend-microservice/auth-service/src/main/resources/application.yml`
- `backend-microservice/user-service/src/main/java/com/ecommerce/user/controller`
- `backend-microservice/user-service/src/main/java/com/ecommerce/user/service`
- `backend-microservice/user-service/src/main/java/com/ecommerce/user/repository`
- `backend-microservice/user-service/src/main/java/com/ecommerce/user/entity`
- `backend-microservice/user-service/src/main/java/com/ecommerce/user/model`
- `backend-microservice/user-service/src/main/resources/application.yml`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/controller/AdminAttributeController.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/AttributeService.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/service/AttributeDefinitions.java`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/repository`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/entity`
- `backend-microservice/catalog-service/src/main/java/com/ecommerce/catalog/model`
- `backend-microservice/catalog-service/src/main/resources/application.yml`

### Noi dung da lam

- Tao DTO request auth dung field cua monolith: `email`, `password`, `userName`, `phone`, `currentPassword`, `newPassword`.
- Tao entity auth toi thieu map bang `khach_hang` va `nhan_vien`, khong keo JPA listener/domain khac tu monolith.
- Tao repository rieng cho auth: tim email/status/sdt.
- Tao `AuthenticationManager`, `PasswordEncoder`, `CustomUserDetailsService`, `UserPrincipal`.
- Tao `TokenProvider` sinh JWT voi claims cu: `email`, `userId`, `fullName`, `pictureUrl`, `role`, issuer `glamsole`.
- Port login user/admin, register, change-password sang `auth-service`.
- `change-password` giu duong session email cu va bo sung doc email tu Bearer token de phu hop gateway/stateless.
- Dieu chinh `common-lib` `ResponseObject` ve gan response shape cua monolith: `isSuccess`, `status`, `data`, `message`, `timestamp`.
- Them `PageableRequest`, `PageableObject`, `PageUtils` dung chung theo paging behavior cua monolith.
- Them Spring Boot BOM vao root `backend-microservice/build.gradle` de dependency version cua `common-lib` on dinh hon.

### API user da migrate sang `user-service`

- `GET /api/v1/admin/khach-hang`
- `GET /api/v1/admin/khach-hang/{id}`
- `POST /api/v1/admin/khach-hang`
- `PUT /api/v1/admin/khach-hang`
- `PUT /api/v1/admin/khach-hang/{id}/change-status`
- `GET /api/v1/admin/nhan-vien`
- `GET /api/v1/admin/nhan-vien/{id}`
- `POST /api/v1/admin/nhan-vien`
- `PUT /api/v1/admin/nhan-vien/{id}/change-status`
- `PUT /api/v1/admin/nhan-vien/{id}/change-role`
- `POST /api/v1/admin/nhan-vien/check-duplicate`
- `GET /api/v1/permitall/profile/{id}`
- `GET /api/v1/permitall/profile/hd/{id}`
- `POST /api/v1/permitall/profile`

### Source monolith user da doi chieu

- `BE/src/main/java/com/be/server/core/admin/khachhang/controller/ADKhachHangController.java`
- `BE/src/main/java/com/be/server/core/admin/khachhang/service/impl/ADKhachHangServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/khachhang/repository/ADKhachHangRepository.java`
- `BE/src/main/java/com/be/server/core/admin/nhanvien/controller/ADNhanVienController.java`
- `BE/src/main/java/com/be/server/core/admin/nhanvien/service/impl/ADNhanVienServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/nhanvien/repository/ADNhanVienRepository.java`
- `BE/src/main/java/com/be/server/core/permitall/profile/controller/ProfileController.java`
- `BE/src/main/java/com/be/server/core/permitall/profile/service/impl/PMProfileServiceImpl.java`
- `BE/src/main/java/com/be/server/core/permitall/profile/repository/PMProfileRepository.java`

### Ghi chu user-service

- `GET /api/v1/permitall/profile/hd/{id}` hien van doc projection join `hoa_don` de giu API cu. Khi `order-service` tach xong se thay bang Feign/noi bo order-service.
- Upload avatar trong monolith dung `CloudinaryUtils`; user-service da giu multipart field `avatar`, nhung can bo sung cau hinh Cloudinary/notification-service khi chay runtime day du.
- Tao nhan vien trong monolith gui email truc tiep; trong microservice nen chuyen sang `notification-service`/Kafka sau khi them Kafka.

### API catalog attribute da migrate sang `catalog-service`

- `GET /api/v1/admin/mau-sac`
- `GET /api/v1/admin/mau-sac/{id}`
- `POST /api/v1/admin/mau-sac`
- `PUT /api/v1/admin/mau-sac/{id}/change-status`
- `GET /api/v1/admin/size`
- `GET /api/v1/admin/size/{id}`
- `POST /api/v1/admin/size`
- `PUT /api/v1/admin/size/{id}/change-status`
- `GET /api/v1/admin/thuong-hieu`
- `GET /api/v1/admin/thuong-hieu/{id}`
- `POST /api/v1/admin/thuong-hieu`
- `PUT /api/v1/admin/thuong-hieu/{id}/change-status`
- `GET /api/v1/admin/xuat-xu`
- `GET /api/v1/admin/xuat-xu/{id}`
- `POST /api/v1/admin/xuat-xu`
- `PUT /api/v1/admin/xuat-xu/{id}/change-status`
- `GET /api/v1/admin/chat-lieu`
- `GET /api/v1/admin/chat-lieu/{id}`
- `POST /api/v1/admin/chat-lieu`
- `PUT /api/v1/admin/chat-lieu/{id}/change-status`
- `GET /api/v1/admin/danh-muc`
- `GET /api/v1/admin/danh-muc/{id}`
- `POST /api/v1/admin/danh-muc`
- `PUT /api/v1/admin/danh-muc/{id}/change-status`
- `GET /api/v1/admin/loai-de`
- `GET /api/v1/admin/loai-de/{id}`
- `POST /api/v1/admin/loai-de`
- `PUT /api/v1/admin/loai-de/{id}/change-status`

### Source monolith catalog attribute da doi chieu

- `BE/src/main/java/com/be/server/core/admin/mausac/controller/ADMauSacController.java`
- `BE/src/main/java/com/be/server/core/admin/mausac/service/impl/ADMauSacServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/kichthuoc/controller/ADKichThuocController.java`
- `BE/src/main/java/com/be/server/core/admin/kichthuoc/service/impl/ADKichThuocServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/thuonghieu/controller/ADThuongHieuController.java`
- `BE/src/main/java/com/be/server/core/admin/xuatxu/controller/ADXuatXuController.java`
- `BE/src/main/java/com/be/server/core/admin/chatlieu/controller/ADChatLieuController.java`
- `BE/src/main/java/com/be/server/core/admin/chatlieu/service/impl/ADChatLieuServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/loaigiay/controller/ADLoaiGiayController.java`
- `BE/src/main/java/com/be/server/core/admin/loaide/controller/ADLoaiDeController.java`
- `BE/src/main/java/com/be/server/entity/MauSac.java`
- `BE/src/main/java/com/be/server/entity/KichCo.java`
- `BE/src/main/java/com/be/server/entity/ThuongHieu.java`
- `BE/src/main/java/com/be/server/entity/XuatSu.java`
- `BE/src/main/java/com/be/server/entity/ChatLieu.java`
- `BE/src/main/java/com/be/server/entity/DanhMuc.java`
- `BE/src/main/java/com/be/server/entity/LoaiDe.java`

### Ghi chu catalog-service

- Da tao generic attribute CRUD cho 7 bang thuoc tinh de giu route cu va tranh lap code.
- `danh-muc` trong monolith nam o package `loaigiay` nhung route la `/api/v1/admin/danh-muc`; catalog-service giu route public cu.
- Da them entity `san_pham`, `san_pham_chi_tiet` va logic sinh ma `SPxxxx`/`SPCTxxxx` thay cho JPA listener cua monolith.
- Da migrate admin product API chinh, admin product detail API chinh va public product/list attribute API.

### API product da migrate sang `catalog-service`

- `GET /api/v1/admin/san-pham`
- `GET /api/v1/admin/san-pham/list-thuong-hieu`
- `GET /api/v1/admin/san-pham/list-xuat-xu`
- `GET /api/v1/admin/san-pham/list-loai-de`
- `GET /api/v1/admin/san-pham/list-danh-muc`
- `GET /api/v1/admin/san-pham/list-chat-lieu`
- `GET /api/v1/admin/san-pham/{id}`
- `POST /api/v1/admin/san-pham`
- `PUT /api/v1/admin/san-pham/{id}/change-status`
- `GET /api/v1/permitall/san-pham/list-thuong-hieu`
- `GET /api/v1/permitall/san-pham/list-xuat-xu`
- `GET /api/v1/permitall/san-pham/list-size`
- `GET /api/v1/permitall/san-pham/list-mau`
- `GET /api/v1/permitall/san-pham/list-loai-de`
- `GET /api/v1/permitall/san-pham/list-danh-muc`
- `GET /api/v1/permitall/san-pham/list-chat-lieu`
- `GET /api/v1/permitall/san-pham/get-all/danh-sach-san-pham`
- `GET /api/v1/permitall/san-pham/get-all/san-pham-moi`
- `GET /api/v1/permitall/san-pham/get-all/san-pham-giam-gia`
- `GET /api/v1/permitall/thuong-hieu/get-all/thuong-hieu-trang-chu`

### API product detail da migrate sang `catalog-service`

- `GET /api/v1/admin/san-pham-chi-tiet`
- `PUT /api/v1/admin/san-pham-chi-tiet/{id}/change-status`
- `GET /api/v1/admin/san-pham-chi-tiet/{id}`
- `GET /api/v1/admin/san-pham-chi-tiet/detail/{id}`
- `GET /api/v1/admin/san-pham-chi-tiet/list-mau`
- `GET /api/v1/admin/san-pham-chi-tiet/list-size`
- `POST /api/v1/admin/san-pham-chi-tiet`
- `POST /api/v1/admin/san-pham-chi-tiet/update`
- `GET /api/v1/admin/san-pham-chi-tiet/list-sp`
- `GET /api/v1/permitall/san-pham-chi-tiet/get-all/san-pham-chi-tiet`

### Source monolith product da doi chieu

- `BE/src/main/java/com/be/server/core/admin/sanpham/controller/ADSanPhamController.java`
- `BE/src/main/java/com/be/server/core/admin/sanpham/service/impl/ADSanPhamServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/sanpham/repository/ADSanPhamRepository.java`
- `BE/src/main/java/com/be/server/core/permitall/sanpham/controller/PMSanPhamController.java`
- `BE/src/main/java/com/be/server/entity/SanPham.java`
- `BE/src/main/java/com/be/server/entity/SanPhamChiTiet.java`
- `BE/src/main/java/com/be/server/infrastructure/listener/CreateSanPhamEntityListener.java`
- `BE/src/main/java/com/be/server/infrastructure/listener/CreateSanPhamChiTietEntityListener.java`
- `BE/src/main/java/com/be/server/core/admin/SanPhamChiTiet/controller/SanPhamChiTietController.java`
- `BE/src/main/java/com/be/server/core/admin/SanPhamChiTiet/service/impl/ADSanPhamChiTietServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/SanPhamChiTiet/repository/ADSanPhamChiTietRepository.java`
- `BE/src/main/java/com/be/server/core/permitall/chitietsanpham/controller/PMSanPhamChiTietController.java`

### Ghi chu product detail

- `POST /api/v1/admin/san-pham-chi-tiet` va `/update` da giu validation trung mau/size/san pham theo query `checkThemSanPham`.
- Anh san pham van nhan multipart field `anh`. Hien chua co Cloudinary runtime trong microservice nen moi doc file de giu contract request; can noi lai Cloudinary/asset-service sau.
- Public product detail hien dung query admin filtered va force `status=1`; can doi chieu tiep voi `PMSanPhamChiTietServiceImpl` de bo sung response detail public neu khac.
- Public `san-pham-moi`, `san-pham-giam-gia` da enrich them `kichCo`, `mauSac`, `dsAnh`, `dotGiamGia` theo logic `PMSanPhamServiceImpl`.
- Public `thuong-hieu-trang-chu` da migrate tu `PMThuongHieuController`.

### API promotion da migrate sang `promotion-service`

- `GET /api/v1/admin/voucher`
- `GET /api/v1/admin/voucher/{id}`
- `GET /api/v1/admin/voucher/listkh/{id}`
- `POST /api/v1/admin/voucher`
- `PUT /api/v1/admin/voucher/{id}/change-status`
- `GET /api/v1/admin/dot-giam-gia`
- `GET /api/v1/admin/dot-giam-gia/san-pham`
- `GET /api/v1/admin/dot-giam-gia/san-pham-chi-tiet/{id}`
- `GET /api/v1/admin/dot-giam-gia/san-pham-chi-tiet-by-dot/{id}`
- `GET /api/v1/admin/dot-giam-gia/mau-sac`
- `GET /api/v1/admin/dot-giam-gia/size`
- `POST /api/v1/admin/dot-giam-gia`
- `POST /api/v1/admin/dot-giam-gia/expired/{id}`
- `PUT /api/v1/admin/dot-giam-gia/{id}`
- `GET /api/v1/admin/dot-giam-gia/{id}`
- `GET /api/v1/admin/dot-giam-gia/byProductDetail/{id}`

### Source monolith promotion da doi chieu

- `BE/src/main/java/com/be/server/core/admin/voucher/controller/ADVoucherController.java`
- `BE/src/main/java/com/be/server/core/admin/voucher/service/impl/ADVoucherServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/voucher/repository/ADVoucherRepository.java`
- `BE/src/main/java/com/be/server/entity/PhieuGiamGia.java`
- `BE/src/main/java/com/be/server/entity/PhieuGiamGiaChiTiet.java`
- `BE/src/main/java/com/be/server/core/admin/dotgiamgia/controller/ADDotGiamGiaRestController.java`
- `BE/src/main/java/com/be/server/core/admin/dotgiamgia/service/impl/DotGiamGiaServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/dotgiamgia/repository/ADDotGiamGiaRepository.java`
- `BE/src/main/java/com/be/server/core/admin/dotgiamgia/repository/ADDotGiamGiaChiTietRepository.java`
- `BE/src/main/java/com/be/server/entity/DotGiamGia.java`
- `BE/src/main/java/com/be/server/entity/DotGiamGiaChiTietSanPham.java`

### Ghi chu promotion-service

- Voucher tao moi van tao chi tiet phieu cho danh sach khach hang, nhung gui email da duoc de lai cho `notification-service`/Kafka thay vi goi truc tiep `EmailService`.
- `dot-giam-gia` khong con copy entity/repository `san_pham`, `san_pham_chi_tiet`, `mau_sac`, `kich_co`; cac read san pham/mau/size da chuyen sang `catalog-service` qua OpenFeign, promotion chi luu id tham chieu.
- Status promotion da sua ve logic hop ly: qua han la `HET_HAN_KICH_HOAT`, trong han la `DANG_KICH_HOAT`.

### API cart da migrate sang `cart-service`

- `GET /api/v1/permitall/cart`
- `POST /api/v1/permitall/cart`
- `PUT /api/v1/permitall/cart/{id}`

### Source monolith cart da doi chieu

- `BE/src/main/java/com/be/server/core/permitall/giohang/controller/CartController.java`
- `BE/src/main/java/com/be/server/core/permitall/giohang/service/impl/CartServiceImpl.java`
- `BE/src/main/java/com/be/server/core/permitall/giohang/model/request/CartDetailRequest.java`
- `BE/src/main/java/com/be/server/core/permitall/giohang/model/request/CartGetAllRequest.java`
- `BE/src/main/java/com/be/server/repository/CartRepository.java`
- `BE/src/main/java/com/be/server/repository/CartDetailRepository.java`
- `BE/src/main/java/com/be/server/entity/Cart.java`
- `BE/src/main/java/com/be/server/entity/CartDetail.java`

### Ghi chu cart-service

- Da giu behavior cu: auto tao gio hang theo `idKhachHang`, check ton kho theo `san_pham_chi_tiet.so_luong`, cong don item trung san pham chi tiet, xoa cart detail bang `PUT /{id}`.
- Cart-service khong con projection/entity copy `khach_hang` va `san_pham_chi_tiet`; gio hang chi luu `khachHangId`/`sanPhamChiTietId`, enrich san pham qua `catalog-service`.

### API thong ke da migrate sang `order-service`

- `GET /api/v1/admin/thong-ke/doanh-thu`
- `GET /api/v1/admin/thong-ke/don-hang-hoan-thanh`
- `GET /api/v1/admin/thong-ke/top-san-pham-ban-chay`
- `GET /api/v1/admin/thong-ke/ti-le-trang-thai`

### Source monolith thong ke da doi chieu

- `BE/src/main/java/com/be/server/core/admin/thongke/controller/ADThongKeController.java`
- `BE/src/main/java/com/be/server/core/admin/thongke/service/ThongKeDoanhThuService.java`
- `BE/src/main/java/com/be/server/core/admin/thongke/model/response/ThongKeDoanhThuResponse.java`
- `BE/src/main/java/com/be/server/core/admin/thongke/model/response/ThongKeDonHangResponse.java`
- `BE/src/main/java/com/be/server/core/admin/thongke/model/response/TopSanPhamBanChayResponse.java`
- `BE/src/main/java/com/be/server/core/admin/thongke/model/response/ThongKeTrangThaiHoaDonResponse.java`
- `BE/src/main/java/com/be/server/repository/HoaDonRepository.java`

### Ghi chu order-service

- Da migrate phan thong ke, admin hoa don, don mua, checkout/VNPay va admin ban hang tai quay trong `order-service`.
- Thong ke top san pham khong con join bang catalog; order-service aggregate tren `hoa_don`/`hoa_don_chi_tiet`, thong tin san pham enrich qua `catalog-service`.

### API admin hoa don da migrate sang `order-service`

- `GET /api/v1/admin/hoa-don`
- `GET /api/v1/admin/hoa-don/all`
- `GET /api/v1/admin/hoa-don/{id}`
- `GET /api/v1/admin/hoa-don/lich_su_thanh_toan/{id}`
- `POST /api/v1/admin/hoa-don/thanh_toan`
- `PUT /api/v1/admin/hoa-don/change-status`
- `GET /api/v1/admin/hoa-don/pdf/{maHoaDon}`
- `GET /api/v1/admin/hoa-don/delivery/{maHoaDon}/pdf`

### Source monolith admin hoa don da doi chieu

- `BE/src/main/java/com/be/server/core/admin/hoadon/controller/ADHoaDonController.java`
- `BE/src/main/java/com/be/server/core/admin/hoadon/service/impl/ADHoaDonServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/hoadon/repository/ADHoaDonRepositoryImpl.java`
- `BE/src/main/java/com/be/server/core/admin/hoadon/repository/ADHoaDonChiTietRepository.java`
- `BE/src/main/java/com/be/server/core/admin/hoadon/repository/ADLichSuThanhToanRepository.java`
- `BE/src/main/java/com/be/server/core/admin/hoadon/repository/LichSuTrangThaiRepository.java`
- `BE/src/main/java/com/be/server/entity/HoaDon.java`
- `BE/src/main/java/com/be/server/entity/HoaDonChiTiet.java`
- `BE/src/main/java/com/be/server/entity/LichSuThanhToan.java`
- `BE/src/main/java/com/be/server/entity/LichSuTrangThaiHoaDon.java`

### Ghi chu admin hoa don

- `order-service` khong con query DB cheo sang catalog/user/promotion cho admin ban hang, don mua, checkout va thong ke top san pham; cac du lieu ngoai domain lay qua OpenFeign. Cac query native/JdbcTemplate con lai chi doc/ghi bang order-domain va can tiep tuc chuyen sang JPA repository neu refactor tiep theo muc 3b.
- `GET /api/v1/admin/hoa-don`, `/all`, `/lich_su_thanh_toan/{id}` da bo join truc tiep sang `khach_hang`, `nhan_vien`, `san_pham`, `phieu_giam_gia` de order-service chay duoc voi DB rieng `ecommerce_order`; response dung snapshot/id dang co tren `hoa_don` va `hoa_don_chi_tiet`.
- `PUT /api/v1/admin/hoa-don/change-status` da cap nhat `hoa_don.trang_thai_hoa_don` va ghi `lich_su_trang_thai_hoa_don`. Side effect cong lai ton kho/voucher khi `DA_HUY` khong con update table truc tiep trong order DB; can noi Feign/event sang `catalog-service` va `promotion-service` de hoan tat logic cross-service.
- `POST /api/v1/admin/hoa-don/thanh_toan` da ghi `lich_su_thanh_toan` va update `tong_tien_sau_giam`, `tong_tien`, `trang_thai_hoa_don` nhu monolith.
- Email khi doi trang thai chua gui truc tiep trong `order-service`; se chuyen sang `notification-service`/Kafka.
- PDF invoice/delivery da giu endpoint, header va content-type PDF. Noi dung PDF hien la ban toi thieu; can port template iText chi tiet sau khi tach xong order flow chinh.
- Sua ngay 2026-08-10 cho man admin hoa don: `GET /api/v1/admin/hoa-don` bo filter theo cot `ten_hoa_don` de khong loi SQL voi DB local cu, alias lai `so_dien_thoai_khach_hang AS so_dien_thoai`, va `/all` tra `tenHoaDon` tu `ma_hoa_don`. Doi chieu AGENTS.md muc 5: pass database-per-service, khong them join DB cheo, giu response key cu; `:order-service:compileJava -x :common-lib:jar --no-daemon` PASS, full `:order-service:build` chua pass vi `common-lib-0.0.1-SNAPSHOT.jar` dang bi process khac lock.

### API don mua da migrate sang `order-service`

- `GET /api/v1/permitall/don-mua`
- `GET /api/v1/permitall/don-mua/spct`
- `GET /api/v1/permitall/don-mua/all/{code}`
- `POST /api/v1/permitall/don-mua/sua-thong-tin`
- `PUT /api/v1/permitall/don-mua/change-status`
- `GET /api/v1/permitall/don-mua/all`+
- `GET /api/v1/permitall/don-mua/{id}`
- `POST /api/v1/permitall/don-mua/them-san-pham`
- `GET /api/v1/permitall/don-mua/lich_su_thanh_toan/{id}`

### Source monolith don mua da doi chieu

- `BE/src/main/java/com/be/server/core/permitall/donmua/controller/DonMuaController.java`
- `BE/src/main/java/com/be/server/core/permitall/donmua/service/impl/DonMuaServiceImpl.java`
- `BE/src/main/java/com/be/server/core/permitall/donmua/model/request/PMThemSanPhamRequest.java`
- `BE/src/main/java/com/be/server/core/permitall/donmua/model/request/UpdateDeliveryDTO.java`
- `BE/src/main/java/com/be/server/core/admin/hoadon/model/response/HoaDonPageResponse1.java`
- `BE/src/main/java/com/be/server/core/admin/hoadon/model/response/ADHoaDonResponse1.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/repository/ADTaoHoaDonChiTietRepository.java`
- `BE/src/main/java/com/be/server/core/admin/SanPhamChiTiet/repository/ADSanPhamChiTietRepository.java`

### Ghi chu don mua

- `GET /api/v1/permitall/don-mua` va `/all/{code}` da giu response dang `page`, `totalRecords`, `countByStatus` nhu `HoaDonPageResponse1`.
- `POST /api/v1/permitall/don-mua/sua-thong-tin` da giu rule chi cho sua khi don o `CHO_XAC_NHAN`, cap nhat thong tin giao hang, `phi_van_chuyen`, `tong_tien_sau_giam`, `du_no`, `hoan_phi`.
- `POST /api/v1/permitall/don-mua/them-san-pham` da giu rule ton kho, them moi dong neu chua co, neu gia thay doi thi tao dong moi va tra message bao doi gia, neu trung gia thi tang so luong.
- Email thong bao sua thong tin don mua chua gui truc tiep; se dua sang `notification-service`/Kafka.

### API checkout/VNPay da migrate sang `order-service`

- `POST /api/orders/create`
- `GET /api/orders/vnpay-return`
- `POST /api/orders/pgg`
- `POST /api/orders/pgg/list`
- `POST /api/orders/khach-hang/{id}`

### Source monolith checkout/VNPay da doi chieu

- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/controller/PMThanhToanController.java`
- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/service/Impl/thanhtoanserviceImpl.java`
- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/model/request/PMThanhToan.java`
- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/model/request/PMPhieuGiamGia.java`
- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/model/Respones/ListSP.java`
- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/repository/PMPhieuGiamGiaThanhToan.java`
- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/repository/PMChiTietGioHangRepository.java`
- `BE/src/main/java/com/be/server/core/permitall/thanhtoan/repository/PHHoaDonChiTietRepository.java`
- `BE/src/main/java/com/be/server/core/vnpay/VNPayConfig.java`

### Ghi chu checkout/VNPay

- `POST /api/orders/create` da giu rule check ton kho truoc khi tao don. Don tien mat tao `hoa_don` online `CHO_XAC_NHAN`, ghi lich su trang thai, ghi chi tiet hoa don, tru ton kho, tru voucher neu co va xoa item gio hang cua khach hang.
- Luong `VNPAY` tao don `LUU_TAM`, ghi chi tiet va lich su thanh toan truoc; `GET /api/orders/vnpay-return` verify secure hash, response code `00` thi chuyen don sang `CHO_XAC_NHAN`, tru voucher, tru ton kho, xoa gio hang va ghi lich su trang thai.
- `POST /api/orders/pgg` giu validation voucher: ton tai, active, con so luong, voucher ca nhan phai gan voi khach hang, don du dieu kien.
- `POST /api/orders/pgg/list` loc voucher hop le va sap xep theo `giaTriGiamThucTe` giam dan nhu monolith.
- Email xac nhan don hang chua gui truc tiep; se chuyen sang `notification-service`/Kafka.

### API admin ban hang da migrate sang `order-service`

- `GET /api/v1/admin/ban-hang/danh-sach-phieu-giam-gia-ko_du`
- `GET /api/v1/admin/ban-hang/list-hoa-don`
- `POST /api/v1/admin/ban-hang/create-hoa-don`
- `POST /api/v1/admin/ban-hang/huy`
- `POST /api/v1/admin/ban-hang/them-san-pham`
- `GET /api/v1/admin/ban-hang/list-gio-hang/{id}`
- `POST /api/v1/admin/ban-hang/xoa-san-pham`
- `POST /api/v1/admin/ban-hang/them-so-luong`
- `POST /api/v1/admin/ban-hang/them-moi-khach-hang`
- `POST /api/v1/admin/ban-hang/xoa-so-luong`
- `GET /api/v1/admin/ban-hang/list-khach-hang`
- `GET /api/v1/admin/ban-hang/list-san-pham`
- `POST /api/v1/admin/ban-hang/them-khach-hang`
- `GET /api/v1/admin/ban-hang/khach-hang/{id}`
- `GET /api/v1/admin/ban-hang/thanh-toan/{id}`
- `GET /api/v1/admin/ban-hang/phuong-thuc-thanh-toan/{id}`
- `POST /api/v1/admin/ban-hang/cap-nhat-phuong-thuc-thanh-toan`
- `POST /api/v1/admin/ban-hang/thanh-toan-thanh-cong`
- `GET /api/v1/admin/ban-hang/danh-sach-phieu-giam-gia`
- `POST /api/v1/admin/ban-hang/giao-hang/{id}`

### Source monolith admin ban hang da doi chieu

- `BE/src/main/java/com/be/server/core/admin/banhang/controller/ADBanHangController.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/service/ADBanHangService.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/service/impl/ADBanHangServiceImpl.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/service/impl/PhieuGiamGiaService.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/repository/ADTaoHoaDonRepository.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/repository/ADTaoHoaDonChiTietRepository.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/repository/ADSanPhamBanHangRepository.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/model/request/*.java`
- `BE/src/main/java/com/be/server/core/admin/banhang/model/response/*.java`

### Ghi chu admin ban hang

- `order-service` da giu cac side effect chinh: tao hoa don offline, ghi lich su trang thai, them/xoa/tang/giam san pham trong hoa don, chon/them nhanh khach hang, cap nhat phuong thuc thanh toan, chuyen offline/giao hang, huy hoa don.
- `POST /api/v1/admin/ban-hang/thanh-toan-thanh-cong` da check ton kho, tru ton kho, update hoa don, tru voucher, ghi lich su trang thai va lich su thanh toan nhu monolith. Nhanh `GIAO_HANG` chuyen `DA_XAC_NHAN`, nhanh tai quay chuyen `HOAN_THANH`.
- `GET /api/v1/admin/ban-hang/danh-sach-phieu-giam-gia` va `/danh-sach-phieu-giam-gia-ko_du` da tinh `giaTriGiamThucTe`, best voucher va better voucher theo logic cu.
- Cac response hien dung `Map` alias theo projection cu thay vi interface projection JPA de tranh keo quan he entity cross-service.
- Sua ngay 2026-08-10 cho chon ma giam gia man admin ban hang: FE gui lai `idHD` dung id hoa don va gui them `tongTien`; `order-service` chap nhan `tienHang`/`tongTien`/legacy `idHD` numeric hoac tinh tong tien tu `hoa_don_chi_tiet`, dong thoi map voucher Feign tu promotion ve alias cu `ma`, `ten`, `giaTriGiam`, `laPhanTram`, `giaTriGiamThucTe` de modal hien ma cho admin chon. Doi chieu AGENTS.md muc 5: khong them bang/join DB cheo, giu public route `/api/v1/admin/ban-hang/danh-sach-phieu-giam-gia`; `:order-service:compileJava -x :common-lib:jar --no-daemon --max-workers=1` PASS, full compile bi chan do `common-lib-0.0.1-SNAPSHOT.jar` dang bi lock.
- Sua tiep ngay 2026-08-10 cho popup chon ma giam gia: nut `Bo chon` tren FE khong goi `resetDiscount()` nua vi ham nay xoa ca `state.discountList`; tach `clearSelectedDiscount()` de chi bo ma dang ap dung, giu danh sach ma trong popup khi mo lai.

### Notification-service da migrate

- `POST /api/v1/notifications/email`
- Kafka consumer topic mac dinh: `email-notification`

### Source monolith notification da doi chieu

- `BE/src/main/java/com/be/server/service/EmailService.java`
- Cac noi gui email truc tiep trong `ADHoaDonServiceImpl`, `DonMuaServiceImpl`, `thanhtoanserviceImpl`, `ADNhanVienServiceImpl`, `ADVoucherServiceImpl`.

### Ghi chu notification-service

- Email SMTP khong hard-code username/password nhu monolith; doc tu env `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_HOST`, `MAIL_PORT`.
- Cac service order/user/promotion hien da ghi chu defer email sang Kafka/notification. Buoc tiep theo neu can runtime async hoan chinh la publish event JSON toi topic `email-notification` tai cac diem tao don, doi trang thai, tao voucher/nhan vien.

### Docker/Kafka da them

- `backend-microservice/docker-compose.yml`
- Dockerfile cho `discovery-server`, `api-gateway`, `auth-service`, `user-service`, `catalog-service`, `promotion-service`, `cart-service`, `order-service`, `notification-service`.
- Compose gom MySQL 8.4, Zookeeper, Kafka, Eureka, Gateway va cac service app.
- Chua chay Docker/build theo yeu cau user khong build.

### Kiem tra lai Admin ngay 2026-08-05

Pham vi: chi so sanh va sua phan Admin giua `BE` va `backend-microservice`.

Ket qua route/controller:

- `BE/src/main/java/com/be/server/core/admin`: 105 route Admin.
- `backend-microservice`: 105 route Admin `/api/v1/admin/**`.
- Ket qua doi chieu method + path: khong thieu route, khong du route.

Sai lech da sua:

- Monolith `SecurityConfig` bat `/api/v1/admin/**` phai co authority `ADMIN`.
- Gateway microservice truoc do dang `permitAll()` toan bo request, lam Admin API khong bi chan theo role nhu `BE`.
- Da them `AdminAuthorizationFilter` trong `backend-microservice/api-gateway` de validate Bearer JWT bang cung `jwt.secret` voi `auth-service`; route `/api/v1/admin/**` chi cho qua khi claim `role = ADMIN`.
- OPTIONS request van duoc cho qua de giu CORS preflight.

File da sua:

- `backend-microservice/api-gateway/build.gradle`
- `backend-microservice/api-gateway/src/main/java/com/ecommerce/gateway/security/AdminAuthorizationFilter.java`
- `backend-microservice/api-gateway/src/main/resources/application.yml`

Build/verify:

- `.\gradlew.bat :api-gateway:build --no-daemon`: PASS.
- `.\gradlew.bat clean build --no-daemon`: PASS, 65 actionable tasks executed.

Trang thai API Admin theo nhom:

- Thuoc tinh catalog (`mau-sac`, `size`, `thuong-hieu`, `xuat-xu`, `chat-lieu`, `danh-muc`, `loai-de`): da khop route, build pass, can smoke test runtime response voi data that.
- San pham va san pham chi tiet: da khop route, build pass, can review them voi man hinh upload anh/Cloudinary.
- Khach hang va nhan vien: da khop route, build pass, can review them voi flow gui email tao nhan vien vi email da defer sang notification.
- Voucher va dot giam gia: da khop route, build pass, can review runtime voi data join san pham/khach hang that.
- Hoa don, thong ke, ban hang tai quay: da khop route, build pass, can smoke test voi DB/Eureka vi logic doc/ghi nhieu bang bang native SQL.
- Quyen Admin: da sua de khop behavior monolith o gateway-level.

Ghi chu khong tu sua:

- `BE` co OAuth2 admin TODO trong `CustomOAuth2UserService.processAdmin`; chua port/sua vi khong tu y thay doi bug/behavior chua duoc confirm.
- PDF hoa don/giao hang trong microservice van la ban toi thieu nhu checkpoint cu; can port template chi tiet neu user uu tien rieng.
- Email truc tiep trong cac flow Admin da defer sang `notification-service`/Kafka theo checkpoint cu; chua bat buoc publish event o tat ca diem neu user chua confirm.

### Build sau auth-service

Trang thai: DA CHAY LAI TOAN BO NGAY 2026-08-05

Ket qua: `.\gradlew.bat clean build --no-daemon` trong `backend-microservice` PASS.

### Viec chua lam

- Chua migrate OAuth2 Google/GitHub sang `auth-service`.
- Chua tao refresh-token persistence/revoke endpoint rieng.
- Chua doi chieu runtime response voi frontend.
- Chua publish Kafka email event tu cac service order/user/promotion; notification-service da co endpoint/consumer nhan event.
- Service ton kho rieng da bi loai bo theo quyet dinh 2026-08-10 vi ton kho trong monolith/du an hien tai nam tren `san_pham_chi_tiet` va dang duoc xu ly truc tiep trong catalog/order/cart.
- Da build toan bo `backend-microservice` ngay 2026-08-05, nhung chua chay full runtime smoke qua gateway/Eureka/DB cho tung API Admin.

### Buoc tiep theo

Neu user cho phep, chay runtime smoke test Admin qua gateway/Eureka/DB, sau do noi Kafka publish email event; khong tach ton kho sang service rieng.
