# AGENTS.md — Quy tắc kiến trúc bắt buộc khi migrate Monolith → Microservice

> File này PHẢI được đọc và tuân thủ TRƯỚC KHI viết/sửa bất kỳ code nào trong repo `e-commerce`.
> Nếu code hiện tại vi phạm các quy tắc dưới đây (VD: 2 service khác nhau có bảng trùng tên/trùng cấu trúc), đó là BUG kiến trúc cần refactor, không phải "đã xong việc".

## 1. Bối cảnh

Dự án đang migrate từ 1 monolith sang microservices, gồm các service: `api-gateway`, `auth-service`, `cart-service`, `catalog-service`, `common-lib`, `discovery-server`, `notification-service`, `order-service`, `promotion-service`, `user-service`.

Mục tiêu migrate là giữ **logic nghiệp vụ** (luồng xử lý, hợp đồng API/DTO trả cho FE) — KHÔNG phải giữ nguyên cách thao tác dữ liệu của monolith (join bảng trực tiếp, share 1 DB).

## 2. Nguyên tắc "Database per Service" — KHÔNG được vi phạm

- Mỗi service có **1 database riêng**, và chỉ được tạo bảng cho dữ liệu **thuộc domain của chính nó (bounded context)**.
- **CẤM tuyệt đối**: 2 service khác nhau có bảng cùng tên / cùng cấu trúc lưu cùng 1 loại thực thể gốc (VD: `san_pham`, `mau_sac`, `kich_co` chỉ được tồn tại ở `catalog-service`).
- Nếu phát hiện service B đang có bảng vốn thuộc về service A (ví dụ `promotion-service` có bảng `san_pham`, `kich_co`, `mau_sac`) → đây là lỗi, phải xoá bảng đó khỏi service B và thay bằng cơ chế gọi qua service A (mục 3).
- Mỗi service chỉ được lưu:
    1. Dữ liệu nghiệp vụ gốc của chính nó.
    2. **ID tham chiếu** (product_id, sku_id, user_id, order_id...) tới thực thể ở service khác — không lưu nguyên bảng.
    3. (Tuỳ chọn) Bản **snapshot tối thiểu, bất biến tại thời điểm sự kiện xảy ra** nếu nghiệp vụ bắt buộc (VD: lưu `product_name`, `price_at_order_time` trong `order_detail` để hoá đơn không đổi theo thời gian) — đây không phải "copy bảng", mà là dữ liệu lịch sử gắn với chính domain của order.

### Bảng phân domain tham khảo (dựa theo tên service hiện có)
| Service | Sở hữu bảng nào | KHÔNG được có bảng nào |
|---|---|---|
| catalog-service | san_pham, san_pham_chi_tiet, mau_sac, kich_co, danh_muc, thuong_hieu, loai_de, chat_lieu, xuat_su; ton kho hien tai nam tren san_pham_chi_tiet.so_luong theo BE | khach_hang, don_hang, khuyến mãi... |
| promotion-service | dot_giam_gia, dot_giam_gia_chi_tiet_san_pham (chỉ lưu product_id/sku_id tham chiếu), phieu_giam_gia, phieu_giam_gia_chi_tiet_khach_hang (chỉ lưu customer_id) | san_pham, san_pham_chi_tiet, mau_sac, kich_co, khach_hang (full info) |
| user-service | khach_hang, tài khoản, địa chỉ | san_pham, đơn hàng |
| order-service | đơn hàng, order_detail (snapshot giá/tên tại thời điểm mua) | san_pham gốc, khuyến mãi gốc |

## 3. Cách service này lấy dữ liệu của service khác — BẮT BUỘC chọn 1 trong 2

### a) OpenFeign (đồng bộ)
Dùng khi **cần dữ liệu real-time ngay lúc xử lý request**, và có thể chấp nhận chờ phản hồi.
- VD: `promotion-service` cần biết giá gốc của sản phẩm để tính % giảm khi user xem trang khuyến mãi → gọi `catalog-service` qua Feign client.
- Luôn có fallback (Resilience4j/Circuit breaker) khi service kia down.
- Không gọi Feign lồng nhau quá 1-2 tầng để tránh cascading failure.

### b) Kafka (bất đồng bộ, event-driven)
Dùng khi **không cần real-time**, cần đồng bộ trạng thái, hoặc để tránh coupling cứng lúc runtime.
- VD: `catalog-service` publish event `ProductPriceChanged`; `promotion-service`/`order-service` subscribe để cập nhật cache nội bộ hoặc snapshot của mình.
- VD: `order-service` publish `OrderCreated`; `notification-service` subscribe để gửi email. Ton kho hien tai khong tach service rieng; cac luong can ton kho goi/ghi qua domain san pham hien co.
- Đây là cách chuẩn để tránh phải gọi Feign đồng bộ ở luồng quan trọng (checkout, thanh toán).

### KHÔNG BAO GIỜ được:
- Insert/update trực tiếp vào DB của service khác.
- Join SQL xuyên schema/DB giữa 2 service.
- Copy nguyên cấu trúc bảng của service khác sang service mình "cho tiện".

## 3b. Bắt buộc dùng Spring Data JPA — hạn chế JdbcTemplate/native SQL

Hiện code đang dùng `JdbcTemplate`/native SQL rất nhiều (VD: `order-service` cho admin hoá đơn, thống kê). Đây KHÔNG phải cách làm chuẩn về lâu dài, dù lý do ban đầu (tránh kéo JPA relation cross-service) là đúng — cách đúng là dùng JPA nhưng entity CHỈ map bảng thuộc domain của chính service đó, không phải bỏ JPA để né vấn đề.

- **Mặc định BẮT BUỘC dùng Spring Data JPA** (`@Entity` + `JpaRepository`) cho toàn bộ thao tác CRUD trên dữ liệu thuộc domain của service.
- Entity JPA của 1 service **chỉ được có quan hệ (`@OneToMany`, `@ManyToOne`...) với entity khác trong CÙNG service/CÙNG database**. Không map quan hệ JPA sang bảng ở service khác — đây là lý do JDBC bị dùng để né trước đây; giờ giải quyết đúng gốc bằng cách tách entity theo domain (mục 2), không phải bỏ JPA.
- Dữ liệu cần từ service khác → lấy qua DTO trả về từ OpenFeign/Kafka (mục 3), gán vào response ở tầng service/mapper, KHÔNG lấy bằng native SQL join hay JdbcTemplate query thẳng vào bảng ngoài domain.
- **Chỉ được dùng `JdbcTemplate`/native SQL trong các trường hợp ngoại lệ sau, và phải nêu rõ lý do trong code comment + README:**
    - Câu query thống kê/aggregate phức tạp (SUM, GROUP BY nhiều tầng) MÀ chỉ trên bảng thuộc domain của chính service đó — vẫn nên ưu tiên `@Query` (JPQL/native trong Spring Data Repository) thay vì tự mở `JdbcTemplate` rời rạc.
    - Không dùng JdbcTemplate như một cách "lách" để join sang bảng của service khác — nếu thấy native SQL có tên bảng không thuộc service hiện tại (VD: order-service query thẳng `san_pham`, `khach_hang`, `phieu_giam_gia`) → đây LÀ vi phạm mục 2/3, phải refactor thay bằng Feign, không phải giữ nguyên vì "đang chạy được".
- Khi refactor 1 chỗ đang dùng JdbcTemplate: việc cần làm KHÔNG chỉ là đổi cú pháp JdbcTemplate → JPA trên cùng bảng, mà phải kiểm tra luôn bảng đó có thuộc domain của service không; nếu không thuộc, phải bỏ hẳn, chuyển sang gọi Feign lấy dữ liệu, rồi mới map field cần thiết bằng JPA/DTO nội bộ.

## 4. Quy trình migrate 1 chức năng từ monolith

1. Đọc logic cũ trong monolith → xác định luồng nghiệp vụ + input/output API.
2. Xác định chức năng đó thuộc bounded context/service nào.
3. Xác định trong luồng đó có bước nào cần dữ liệu từ domain khác không.
4. Với mỗi bước cần dữ liệu ngoài domain:
    - Cần ngay lúc request → OpenFeign.
    - Có thể async / chỉ cần đồng bộ trạng thái → Kafka.
5. Thiết kế lại bảng cho service đích: chỉ giữ field thuộc domain đó + field ID tham chiếu.
6. Viết API/DTO giữ nguyên hợp đồng cũ nếu FE đang dùng, transform ở tầng service/mapper.
7. Nếu phát hiện bảng bị trùng với service khác trong quá trình migrate → dừng lại, refactor tách bảng trước khi migrate tiếp, không migrate tiếp trên nền sai.

## 5. Checklist tự kiểm tra trước khi coi 1 phần migrate là "xong"

- [ ] Bảng mới tạo có tên/cấu trúc trùng với bảng ở service khác không? Nếu có → SAI.
- [ ] Có insert/update field không thuộc domain của service này không? Nếu có → SAI.
- [ ] Có foreign key/join thẳng sang DB khác không? Nếu có → SAI, phải thay bằng Feign/Kafka.
- [ ] Có dùng JdbcTemplate/native SQL để đọc bảng KHÔNG thuộc domain service này không? Nếu có → SAI theo mục 3b, phải thay bằng Feign.
- [ ] Thao tác CRUD trên bảng thuộc domain service này có dùng Spring Data JPA (Entity/Repository) thay vì JdbcTemplate không, trừ trường hợp ngoại lệ đã nêu ở mục 3b?
- [ ] API/DTO trả cho FE có giữ đúng field/format như trước khi sửa không? Nếu FE cần đổi, phải nói rõ ra, không được tự ý đổi ngầm.
- [ ] Logic nghiệp vụ (input/output, quy tắc tính toán) có giữ đúng như monolith không?
- [ ] Đã có test/build pass (`.\gradlew.bat clean build --no-daemon`) chưa?

Chỉ được đánh dấu 1 service là "DONE"/"Hoàn thành" trong `README-Microservice.md` khi TẤT CẢ các dòng trên đều pass.

## 6. Thứ tự migrate toàn dự án (theo chiều phụ thuộc dữ liệu)

Không migrate ngẫu nhiên theo tên file/thư mục. Bắt buộc theo thứ tự này, vì service đứng sau thường gọi Feign/Kafka tới service đứng trước — sửa sai thứ tự sẽ phải làm lại:

1. `catalog-service` — nguồn dữ liệu sản phẩm gốc, nhiều service khác phụ thuộc vào nó.
2. `user-service` — nguồn dữ liệu khách hàng gốc.
3. `promotion-service` — phụ thuộc catalog + user.
4. `cart-service` — phụ thuộc catalog + user.
5. `order-service` — phụ thuộc catalog, user, promotion.
6. `notification-service` — phụ thuộc order/user (chủ yếu consume Kafka event).
7. `auth-service`, `api-gateway`, `discovery-server`, `common-lib` — hạ tầng, xử lý sau cùng hoặc song song vì ít phụ thuộc dữ liệu domain.

## 7. Quy trình bắt buộc cho MỖI service: AUDIT trước, FIX sau (không gộp 1 bước)

### Bước A — AUDIT (chỉ đọc, KHÔNG sửa code/DB)
Khi được yêu cầu audit 1 service, chỉ output ra danh sách lỗi theo đúng 3 mục:
1. Bảng nào đang bị trùng/thừa domain so với mục 2.
2. Chỗ code nào đang insert/update/join thẳng sang DB của service khác.
3. Endpoint nào đang lệch hợp đồng API cũ so với monolith.

Không viết code, không sửa file ở bước này. Kết thúc bước audit bằng cách ghi kết quả vào `MIGRATION_PROGRESS.md` ở mục "Audit findings" của service đó.

### Bước B — FIX (thực hiện theo đúng danh sách đã audit)
Chỉ sửa đúng những gì đã liệt kê ở bước Audit của chính service đó. Nếu phát hiện thêm lỗi mới ngoài danh sách trong lúc fix → dừng lại, bổ sung vào Audit findings trước, không tự ý mở rộng phạm vi sửa.

Sau khi fix xong: chạy build, tự chấm checklist mục 5, ghi kết quả pass/fail từng dòng vào `MIGRATION_PROGRESS.md`, và chỉ chuyển sang service tiếp theo trong thứ tự mục 6 khi service hiện tại đã pass hết checklist.

### Không được làm gộp nhiều service trong 1 lượt chạy
Mỗi lượt chạy (audit hoặc fix) chỉ xử lý ĐÚNG 1 service được chỉ định. Nếu người dùng yêu cầu "làm hết dự án", tự động chia thành các lượt tuần tự theo thứ tự mục 6, xử lý xong 1 service (cả audit + fix + cập nhật progress) rồi mới báo cáo và hỏi có tiếp tục service kế tiếp không.

## 8. File theo dõi tiến độ: `README-Microservice.md` (KHÔNG tạo file tracking mới)

Repo đã có sẵn `README-Microservice.md` ở root — đây là file điều phối/tracking chính thức DUY NHẤT. Không được tạo thêm `MIGRATION_PROGRESS.md` hay bất kỳ file trạng thái song song nào khác, để tránh 2 nguồn sự thật lệch nhau.

Quy tắc bắt buộc với file này:
- Trước khi làm bất kỳ việc gì, đọc toàn bộ `README-Microservice.md` trước để biết: đang ở Bước mấy, service nào đang xử lý, những gì đã note là "chưa làm"/"cần review thêm"/"projection tạm" — coi các dòng này như audit findings có sẵn, không audit lại từ đầu.
- Đặc biệt chú ý các dòng dạng "hiện có projection tối thiểu cho X, sau khi Feign nối service ổn định nên chuyển sang service Y" — đây CHÍNH LÀ vi phạm mục 2 (Database per Service) đã được note trước, ưu tiên xử lý các dòng này theo đúng bước B (Fix) của mục 7.
- Sau mỗi service/bước xử lý xong: cập nhật lại đúng theo văn phong/cấu trúc hiện có của file (mục "Trạng thái", "Thư mục đã sửa", "Ghi chú", "Việc chưa làm", "Bước tiếp theo"), không đổi cấu trúc file, không viết thêm file mới.
- Bổ sung thêm 1 dòng đối chiếu nhanh với checklist mục 5 của AGENTS.md vào phần "Ghi chú" của mỗi service khi fix xong, để biết đã pass hết chưa (VD: "Đối chiếu AGENTS.md mục 5: pass 4/5, còn thiếu — chưa gọi Feign sang catalog-service").
- Nếu 1 phần trước đó ghi "hoàn thành" nhưng phát hiện lại vi phạm mục 2/3 khi audit sau này → cập nhật lại trạng thái phần đó rõ ràng trong README, không được im lặng bỏ qua hoặc coi như đã xong.
