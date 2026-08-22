# PROMPT CHO CODEX: Chuyển đổi dự án E-commerce bán giày sang Marketplace đa gian hàng (Shopee/Tiki-like)

> Copy toàn bộ nội dung dưới đây, đưa cho Codex làm prompt gốc. Từ lần thứ 2 trở đi, chỉ cần nói với Codex: **"Đọc file `docs/PROGRESS.md`, làm tiếp theo đúng kế hoạch, xong thì cập nhật lại file đó."** — không cần dán lại toàn bộ prompt này mỗi lần.

---

## 0. QUY TẮC BẮT BUỘC — ĐỌC TRƯỚC KHI LÀM BẤT CỨ VIỆC GÌ

Đây là dự án được làm **qua nhiều phiên làm việc rời rạc** (mỗi lần chat với bạn là 1 phiên, không nhớ phiên trước). Để duy trì tính liên tục, bạn PHẢI tuân thủ đúng quy trình sau ở **MỌI phiên làm việc**, không có ngoại lệ:

### Đầu phiên (bắt buộc làm trước tiên)
1. Kiểm tra xem file `docs/PROGRESS.md` đã tồn tại chưa.
    - **Nếu CHƯA có** (lần đầu tiên): đây là phiên khởi tạo, làm theo toàn bộ Mục 1–8 bên dưới để sinh ra bộ tài liệu gốc, trong đó có tạo file `docs/PROGRESS.md` theo đúng format ở Mục 9.
    - **Nếu ĐÃ có**: đọc kỹ toàn bộ nội dung file này để biết: đã làm tới đâu, phase nào, task nào đang dang dở, có vướng mắc gì được ghi chú lại không. Đọc thêm các file `docs/0X-*.md` liên quan tới task tiếp theo để nắm lại bối cảnh kỹ thuật/nghiệp vụ trước khi code.
2. Không được tự ý nhảy cóc task, không được làm lại task đã đánh dấu DONE trừ khi PROGRESS.md ghi rõ task đó cần sửa lại.
3. Không được suy đoán trạng thái code hiện tại — luôn đọc lại source thực tế ở những phần liên quan tới task sắp làm, vì code có thể đã đổi khác so với lần ghi log trước.

### Cuối phiên (bắt buộc làm trước khi kết thúc, kể cả khi chưa xong hết task)
1. Cập nhật `docs/PROGRESS.md` theo đúng format ở Mục 9: task nào vừa hoàn thành, file nào đã tạo/sửa, task đang dở ở bước nào cụ thể, task tiếp theo cần làm là gì, có vướng mắc/quyết định cần người dùng xác nhận không.
2. Nếu 1 task bị chặn (thiếu thông tin, cần quyết định nghiệp vụ) — dừng lại, ghi rõ vào PROGRESS.md phần "Câu hỏi cần xác nhận", không tự đoán bừa rồi làm sai hướng.
3. Không bao giờ kết thúc phiên mà không cập nhật PROGRESS.md — đây là điều kiện tiên quyết để phiên sau làm tiếp được chính xác.

---

## 1. BỐI CẢNH DỰ ÁN HIỆN TẠI

Dự án hiện tại là hệ thống e-commerce bán giày, Java 17 + Spring Boot 3.4.4 + Spring Cloud 2024.0.1, kiến trúc microservices gồm: `discovery-server`, `api-gateway`, `common-lib`, `auth-service`, `user-service`, `catalog-service`, `promotion-service`, `order-service`, `cart-service`, `notification-service`. Database MySQL riêng theo service. Có Kafka + Debezium Outbox đồng bộ Elasticsearch cho search sản phẩm. Có Prometheus/Grafana/ELK cho monitoring/logging. Frontend Vue 3 + TypeScript + Pinia + Vue Router + Ant Design Vue.

File `ARCHITECTURE.md` ở gốc repo mô tả chi tiết hiện trạng — đọc file này trước, nhưng phải đối chiếu lại với source code thật vì tài liệu có thể lỗi thời.

**Nguyên tắc xuyên suốt: tận dụng tối đa code cũ.** Không viết lại từ đầu những phần còn dùng được (entity, API, luồng nghiệp vụ). Chỉ sửa/thêm/xóa đúng phần cần thiết cho domain mới.

## 2. MỤC TIÊU CUỐI CÙNG

Chuyển từ "1 cửa hàng giày bán online + tại quầy" thành **nền tảng Marketplace đa gian hàng chuẩn Shopee/Tiki**: nhiều seller độc lập đăng ký, có shop riêng, khách mua chung trên 1 storefront, sàn đứng giữa thu tiền và đối soát. **Bỏ hẳn mọi tính năng bán hàng tại quầy/POS/hóa đơn offline** — vì Shopee/Tiki thật không có tính năng này.

---

## 3. MÔ TẢ NGHIỆP VỤ CHI TIẾT — BẮT BUỘC LÀM ĐÚNG THEO ĐÂY, KHÔNG ĐƯỢC LÀM QUA LOA

Đây là phần quan trọng nhất — mọi tài liệu và code sau này phải bám sát đúng các luồng dưới đây, không được chỉ "thêm cột seller_id" rồi coi là xong.

### 3.1. Luồng đăng ký & quản lý tài khoản Seller (bắt buộc có, chi tiết)

**Ai cũng có thể là buyer trước, muốn bán hàng thì đăng ký nâng cấp thành seller** — giống hệt cơ chế thật của Shopee ("Kênh Người Bán"/Seller Centre):

1. Người dùng đã có tài khoản buyer (hoặc đăng ký mới) → vào trang **"Đăng ký bán hàng" / "Trở thành Người bán"**.
2. Form đăng ký shop thu thập: tên shop (unique, dùng làm slug URL trang shop `/shop/ten-shop`), mô tả shop, logo, ảnh bìa, địa chỉ lấy hàng, số điện thoại liên hệ, thông tin định danh (CMND/CCCD hoặc mã số thuế nếu là hộ kinh doanh — có thể mock ở giai đoạn đầu), thông tin tài khoản ngân hàng nhận tiền đối soát, ngành hàng chính kinh doanh.
3. Sau khi submit, trạng thái shop = `PENDING_APPROVAL`. Seller **chưa đăng được sản phẩm**, chỉ thấy màn hình "Đang chờ duyệt".
4. Platform Admin vào màn **"Duyệt Seller"**, xem danh sách shop đang chờ, xem chi tiết hồ sơ, **Duyệt** (→ `APPROVED`, kích hoạt) hoặc **Từ chối** (→ `REJECTED`, kèm lý do, seller có thể sửa hồ sơ và nộp lại).
5. Sau khi `APPROVED`: tài khoản người dùng đó được gắn thêm role `SELLER` + liên kết `seller_id`, JWT khi login sẽ có thêm claim `sellerId` (hoặc mảng `sellerIds` nếu sau này cho phép 1 người quản nhiều shop). Từ đây user này khi login sẽ thấy thêm mục **"Kênh Người Bán"** để chuyển qua giao diện Seller Admin.
6. Trạng thái shop còn có thể chuyển sang `SUSPENDED` (bị sàn tạm khóa do vi phạm) hoặc `CLOSED` (seller tự đóng shop) — mô tả rõ điều kiện chuyển trạng thái và ai có quyền chuyển.
7. Mô tả rõ: 1 tài khoản đăng nhập (1 JWT) có thể **chuyển đổi qua lại giữa vai trò Buyer và Seller** trên cùng giao diện (giống nút "Kênh người bán" trên Shopee), không phải 2 tài khoản tách biệt.

### 3.2. Giao diện Seller Admin (Kênh Người Bán) — mô tả chi tiết từng màn

Tái sử dụng tối đa layout Admin hiện có (`FE/src/layout/Admin.vue`), nhưng data phải tự động filter theo `sellerId` của người đang đăng nhập, seller không tự nhập/chọn được `sellerId` từ client.

| Màn hình | Tính năng chi tiết |
|---|---|
| Dashboard | Doanh thu hôm nay/tuần/tháng của riêng shop, số đơn chờ xử lý, số đơn đang giao, sản phẩm sắp hết hàng, biểu đồ doanh thu theo thời gian |
| Quản lý sản phẩm | Tái sử dụng toàn bộ CRUD `san-pham`/`san-pham-chi-tiet` hiện có, filter cứng theo `seller_id`, seller chỉ thấy/sửa/xóa sản phẩm của mình |
| Quản lý đơn hàng | Danh sách **sub-order** thuộc shop mình (không thấy đơn của seller khác dù cùng 1 đơn gốc của khách), các trạng thái: Chờ xác nhận → Đã xác nhận/đang đóng gói → Đang giao → Hoàn thành / Đã hủy. Seller thao tác xác nhận, in phiếu giao hàng, cập nhật trạng thái |
| Quản lý voucher shop | Tạo voucher riêng chỉ áp dụng sản phẩm của shop mình (tái sử dụng `promotion-service`, thêm `seller_id`) |
| Đợt giảm giá shop | Tương tự, chỉ áp sản phẩm của shop mình |
| Ví & đối soát | Xem số dư chờ đối soát, lịch sử đã nhận tiền, chi tiết từng kỳ đối soát (doanh thu gộp, hoa hồng sàn trừ, số tiền thực nhận) |
| Hồ sơ shop | Sửa thông tin shop, logo, ảnh bìa, địa chỉ lấy hàng, cấu hình đơn vị vận chuyển |
| Đánh giá & phản hồi | Xem đánh giá sản phẩm/shop từ khách, trả lời đánh giá |
| Thống kê | Sản phẩm bán chạy, tỉ lệ hủy đơn, doanh thu theo danh mục — chỉ trong phạm vi shop mình (tái sử dụng logic `thong-ke` cũ, filter theo seller) |

### 3.3. Giao diện Buyer / Storefront — mô tả chi tiết thay đổi so với hiện tại

Đây là phần **bắt buộc phải mô tả và sửa cụ thể**, không chỉ nói chung chung "trang online":

| Trang | Hiện tại (1 cửa hàng) | Cần sửa thành (Marketplace) |
|---|---|---|
| Trang chủ | Danh sách sản phẩm của 1 shop duy nhất | Gộp sản phẩm từ **nhiều seller**, có section "Gợi ý cho bạn", "Shop nổi bật", banner quảng cáo do Platform Admin cấu hình |
| Trang danh sách sản phẩm/tìm kiếm | Filter theo màu/size/danh mục/giá | Thêm filter theo **shop**, theo **đánh giá sao**, sắp xếp theo "Bán chạy nhất", hiển thị tên shop + link tới trang shop ngay trên card sản phẩm |
| **Trang Shop riêng (MỚI, bắt buộc thêm)** | Không có | Trang `/shop/:sellerSlug` — hiển thị logo, ảnh bìa, tên shop, số sản phẩm, đánh giá trung bình, số lượt theo dõi, danh sách sản phẩm của riêng shop đó, nút "Theo dõi Shop" |
| Trang chi tiết sản phẩm | Không có thông tin shop | Thêm block thông tin shop bán (tên, avatar, đánh giá, nút vào xem shop, nút chat nếu có Phase sau), hiển thị đánh giá sản phẩm kèm ảnh từ người mua |
| Giỏ hàng | 1 danh sách phẳng | **Nhóm sản phẩm theo từng shop** (giống Shopee: mỗi shop là 1 khối riêng trong giỏ, có thể chọn/bỏ chọn từng shop hoặc từng sản phẩm để checkout) |
| Checkout | 1 đơn, 1 tổng tiền | Hiển thị **tách theo từng shop**: mỗi shop 1 khối gồm sản phẩm + phí ship riêng (nếu áp dụng) + voucher riêng shop đó; cuối cùng có voucher toàn sàn áp dụng chung; tổng thanh toán vẫn **1 lần duy nhất** (1 lượt gọi VNPay) cho toàn bộ giỏ dù nhiều shop |
| Lịch sử đơn mua | 1 danh sách đơn phẳng | Nhóm theo **đơn gốc** (mã đơn khách nhìn thấy), bên trong mỗi đơn gốc hiển thị rõ từng **sub-order theo shop** với trạng thái xử lý riêng (vì shop A có thể giao trước, shop B giao sau) |
| Đánh giá sau khi nhận hàng | Không có | Sau khi sub-order chuyển trạng thái Hoàn thành, buyer đánh giá sản phẩm (sao + ảnh + bình luận) và đánh giá shop |
| Trang "Đăng ký bán hàng" (MỚI) | Không có | Form đăng ký seller như mô tả Mục 3.1 |

### 3.4. Giao diện Platform Admin (Admin sàn)

| Màn hình | Tính năng |
|---|---|
| Duyệt Seller | Danh sách chờ duyệt, xem hồ sơ, duyệt/từ chối, khóa/mở shop vi phạm |
| Quản lý danh mục chung | Danh mục toàn sàn để mọi seller gắn sản phẩm vào (tái sử dụng `danh-muc` hiện có, không đổi seller_id vì đây là danh mục chung) |
| Cấu hình hoa hồng | % hoa hồng theo từng ngành hàng/danh mục, áp dụng khi đối soát |
| Voucher toàn sàn | Tạo voucher áp dụng toàn hệ thống, không thuộc seller nào |
| Quản lý banner trang chủ | CRUD banner quảng cáo hiển thị trang chủ storefront |
| Xử lý khiếu nại/tranh chấp | Đơn hàng có khiếu nại giữa buyer-seller, admin can thiệp |
| Thống kê toàn sàn | Doanh thu toàn sàn, top seller, top sản phẩm, GMV theo thời gian |
| Đối soát tổng | Xem/duyệt các kỳ đối soát trước khi hệ thống chi trả cho seller |

### 3.5. Danh sách tính năng chuẩn Marketplace — checklist đối chiếu bắt buộc

Khi thiết kế, đối chiếu để không thiếu các tính năng cốt lõi sau (đánh dấu tính năng nào làm ở Phase nào trong roadmap, không được bỏ sót mà không giải thích):

- [ ] Đăng ký/đăng nhập buyer, seller, platform admin (3 vai trò rõ ràng, 1 hệ thống JWT)
- [ ] Đăng ký & duyệt shop (seller onboarding)
- [ ] Trang chủ storefront gộp nhiều shop
- [ ] Trang riêng từng shop
- [ ] Tìm kiếm/filter sản phẩm toàn sàn (Elasticsearch)
- [ ] Giỏ hàng multi-seller (nhóm theo shop)
- [ ] Checkout tách theo shop, thanh toán 1 lần
- [ ] Split-order thành sub-order theo seller
- [ ] Quản lý đơn hàng riêng theo từng seller
- [ ] Voucher 2 tầng (sàn + seller)
- [ ] Đánh giá sản phẩm & shop sau khi nhận hàng
- [ ] Theo dõi shop (follow shop)
- [ ] Ví & đối soát cho seller
- [ ] Thống kê riêng theo seller + thống kê tổng toàn sàn
- [ ] Duyệt/khóa seller bởi Platform Admin
- [ ] Thông báo (order status, seller được duyệt...) qua `notification-service` hiện có
- [ ] (Phase sau, optional) Chat buyer-seller
- [ ] (Phase sau, optional) Flash sale toàn sàn do Platform Admin tổ chức

---

## 4. THIẾT KẾ KIẾN TRÚC & DỮ LIỆU

### 4.1. Service — giữ / sửa / thêm / bỏ

| Trạng thái | Service | Thay đổi |
|---|---|---|
| BỎ HẲN | Module `ban-hang`, `hoa-don` kiểu tại quầy trong `order-service` | Xóa toàn bộ controller/service/UI liên quan |
| GIỮ + MỞ RỘNG | `catalog-service` | Thêm `seller_id` vào `san_pham`, `san_pham_chi_tiet`; API public thêm filter/sort theo shop |
| GIỮ + MỞ RỘNG | `order-service` | Giữ logic checkout/VNPay, thêm cơ chế split theo seller: 1 đơn gốc (`don_hang`) chứa nhiều `don_hang_chi_tiet_theo_shop` (sub-order), mỗi sub-order có trạng thái riêng |
| GIỮ + MỞ RỘNG | `promotion-service` | Thêm `seller_id` (nullable = voucher sàn) vào voucher/đợt giảm giá |
| GIỮ + MỞ RỘNG | `user-service` | Thêm role `SELLER`, liên kết `seller_id` |
| GIỮ + MỞ RỘNG | `auth-service` | JWT thêm claim `sellerId`/role động, hỗ trợ chuyển vai trò buyer↔seller |
| GIỮ NGUYÊN | `notification-service`, `cart-service` (mở rộng để nhóm theo shop), `discovery-server`, `api-gateway`, `common-lib` | Giữ, `cart-service` cần sửa response trả về nhóm theo `seller_id` |
| THÊM MỚI | `seller-service` | Đăng ký, duyệt, hồ sơ shop, trạng thái, follow shop |
| THÊM MỚI | `payout-service` | Đối soát, ví, hoa hồng, lịch sử chi trả |

### 4.2. Yêu cầu ERD (mô tả trong `docs/03-erd-database.md`)

Bắt buộc có: bảng `seller` đầy đủ field như Mục 3.1; cấu trúc đơn hàng cha/con (order + sub-order theo seller) với trạng thái riêng từng sub-order; bảng `danh_gia` (đánh giá sản phẩm/shop); bảng `theo_doi_shop` (follow); bảng voucher thêm `seller_id` nullable; bảng payout/đối soát theo kỳ.

### 4.3. Multi-tenancy bắt buộc

Mọi API phía Seller Admin phải lấy `sellerId` từ JWT server-side, không nhận từ client. Viết rõ middleware/interceptor kiểm tra ở `api-gateway` hoặc từng service.

---

## 5. OUTPUT BẮT BUỘC — CẤU TRÚC FILE

```
docs/
├── PROGRESS.md                  ← file log tiến độ, đọc/ghi ở MỌI phiên (xem Mục 9)
├── 00-audit-hien-trang.md
├── 01-business-mapping.md       ← toàn bộ nội dung Mục 3 phải được viết chi tiết vào đây
├── 02-kien-truc-moi.md
├── 03-erd-database.md
├── 04-luong-nghiep-vu.md        ← sequence diagram cho từng luồng ở Mục 3.1–3.4
├── 05-roadmap.md                ← chia phase, mỗi tính năng ở checklist Mục 3.5 phải xuất hiện ở đúng 1 phase
└── ARCHITECTURE.md              ← bản tổng hợp mới thay thế file gốc
```

Mỗi file phải chi tiết, không viết chung chung. Với `01-business-mapping.md` và `04-luong-nghiep-vu.md`: **bắt buộc mô tả đủ toàn bộ nội dung Mục 3** (không được tóm tắt sơ sài), vì đây là phần nghiệp vụ cốt lõi để phiên sau code đúng.

---

## 6. QUY TẮC CHUNG

1. Không suy diễn khi chưa đọc code — mọi khẳng định về hiện trạng dựa trên đọc source thật.
2. Không thêm công nghệ mới ngoài stack hiện có trừ khi thật sự cần, phải giải thích lý do.
3. Ngôn ngữ tài liệu: tiếng Việt, giữ quy ước đặt tên bảng tiếng Việt cho phần cũ, đặt tên phù hợp cho phần hoàn toàn mới nhưng nhất quán xuyên suốt.
4. Mọi sơ đồ dùng Mermaid hợp lệ.
5. **Phiên tài liệu hóa (tạo file .md) và phiên viết code là 2 loại phiên khác nhau** — cả hai đều dùng chung cơ chế đọc/ghi `docs/PROGRESS.md` ở Mục 0 và Mục 9. Khi toàn bộ 7 file tài liệu ở Mục 5 đã hoàn thành, các phiên tiếp theo chuyển sang triển khai code theo đúng `05-roadmap.md`, vẫn tuân thủ quy trình đọc/ghi PROGRESS.md y hệt.

---

## 7. CÂU LỆNH BẮT ĐẦU

**Bắt đầu ngay: kiểm tra `docs/PROGRESS.md`, nếu chưa có thì đọc toàn bộ source code hiện tại trước, sau đó thực hiện lần lượt Mục 3 → 4 → 5 để sinh bộ tài liệu ban đầu, cuối cùng tạo `docs/PROGRESS.md` theo format Mục 9.**

---

## 8. ĐỊNH NGHĨA HOÀN THÀNH TỪNG GIAI ĐOẠN

- **Giai đoạn tài liệu hóa hoàn thành khi**: đủ 7 file trong `docs/`, không có mục để trống/TODO, mọi sơ đồ Mermaid hợp lệ, Mục 3 (nghiệp vụ chi tiết) được phản ánh đầy đủ không tóm tắt sơ sài trong `01-business-mapping.md` và `04-luong-nghiep-vu.md`.
- **Giai đoạn code hoàn thành khi**: từng task trong `05-roadmap.md` được đánh dấu DONE trong `PROGRESS.md`, có ghi chú file/service đã tạo/sửa tương ứng.

---

## 9. FORMAT BẮT BUỘC CỦA FILE `docs/PROGRESS.md`

File này là **bộ nhớ duy nhất** giữa các phiên làm việc. Phải luôn giữ cấu trúc sau, thêm entry mới lên **đầu** phần Nhật ký (log mới nhất ở trên cùng để đọc nhanh), KHÔNG xóa log cũ:

```markdown
# PROGRESS.md — Nhật ký tiến độ chuyển đổi Marketplace

## Trạng thái tổng quan hiện tại
- Giai đoạn: [Tài liệu hóa / Phase 1 / Phase 2 / Phase 3]
- Task đang làm dở (nếu có): [mô tả cụ thể, ví dụ "đang viết ERD bảng seller, chưa xong quan hệ với payout"]
- Việc tiếp theo cần làm ngay: [mô tả cụ thể, đủ rõ để phiên sau bắt tay vào luôn không cần hỏi lại]

## Câu hỏi / quyết định cần người dùng xác nhận
- [Liệt kê nếu có, xóa sau khi đã được xác nhận và ghi log lại quyết định]

## Checklist tính năng (đối chiếu Mục 3.5 của prompt gốc)
- [x] Đăng ký/đăng nhập 3 vai trò
- [ ] Đăng ký & duyệt shop
- [ ] ... (liệt kê đầy đủ, cập nhật trạng thái theo tiến độ thật)

## Nhật ký chi tiết (entry mới nhất ở trên cùng)

### [YYYY-MM-DD HH:mm] Phiên #N
**Đã làm:**
- Việc cụ thể 1 (file/service liên quan)
- Việc cụ thể 2

**File đã tạo/sửa:**
- đường dẫn file 1
- đường dẫn file 2

**Kết quả:** [DONE task X / DỞ DANG — dừng ở bước Y]

**Ghi chú/vướng mắc:** [nếu có]

---

### [YYYY-MM-DD HH:mm] Phiên #N-1
...
```

---

## 10. BỔ SUNG NGHIỆP VỤ MỚI: THUỘC TÍNH SẢN PHẨM ĐỘNG (đa ngành hàng, tự thêm tự do)

> Đây là bổ sung phát sinh sau khi phát hiện: sản phẩm hiện tại đang bị hard-code cứng thuộc tính riêng cho ngành giày (Thương hiệu, Xuất xứ, Danh mục, Chất liệu, Loại đế), khiến seller ngành hàng khác không đăng được sản phẩm. Mục này bổ sung vào toàn bộ Mục 3-9 ở trên, KHÔNG thay thế — mọi quy tắc ở Mục 0 (đọc/ghi PROGRESS.md, không suy đoán, không nhảy cóc) vẫn áp dụng y hệt cho phần việc này.

### 10.1. Vấn đề hiện tại
Entity/form sản phẩm đang có các trường cố định trong code, chỉ phù hợp ngành giày: Thương hiệu, Xuất xứ, Chất liệu, Loại đế. Seller thuộc ngành hàng khác (quần áo, mỹ phẩm, đồ gia dụng, phụ kiện...) không có trường phù hợp để mô tả sản phẩm của họ.

### 10.2. Mục tiêu
Chuyển sang mô hình **thuộc tính động, seller được tự do thêm mới** — đúng cơ chế thật của Shopee/Tiki: không phải "chọn từ danh sách có sẵn do Admin định nghĩa trước" mà là "seller tự tạo thuộc tính mới ngay lúc đăng sản phẩm nếu thấy chưa đủ, Admin dọn dẹp/chuẩn hóa sau (hậu kiểm, không phải tiền kiểm)".

### 10.3. Yêu cầu nghiệp vụ chi tiết

**a) Cơ chế thuộc tính động, không giới hạn danh sách định sẵn**
- Mỗi danh mục sản phẩm có sẵn một số thuộc tính **gợi ý** (để seller đỡ phải nghĩ từ đầu, ví dụ danh mục "Giày" gợi ý sẵn "Chất liệu", "Loại đế").
- Seller **được tự thêm thuộc tính mới** ngay trong lúc tạo/sửa sản phẩm nếu các thuộc tính gợi ý sẵn chưa đủ (ví dụ tự thêm "Độ giãn vải", "Số lượng trong hộp").
- Khi seller tự thêm 1 thuộc tính mới, hệ thống lưu lại và gợi ý thuộc tính đó cho các sản phẩm sau trong cùng danh mục, tránh phải gõ lại từ đầu và tránh loạn dữ liệu do trùng ý nhưng đặt tên khác nhau (ví dụ "Chất liệu" và "Chất liệu vải").
- Khi seller gõ tên thuộc tính mới, hệ thống auto-complete gợi ý từ các thuộc tính đã tồn tại (cùng danh mục) để hạn chế trùng lặp.

**b) Vai trò Platform Admin — kiểm soát hậu kiểm**
- Thêm màn **"Quản lý thuộc tính"** trong Admin sàn (bổ sung vào bảng màn hình ở Mục 3.4):
   - Xem toàn bộ thuộc tính đang được seller sử dụng trên toàn sàn.
   - Gộp các thuộc tính trùng ý nhưng khác tên.
   - Chuẩn hóa/gắn thuộc tính vào đúng danh mục để làm gợi ý mặc định cho seller sau.
   - Ẩn/xóa thuộc tính rác, không phù hợp, hoặc vi phạm.
- Đây là kiểm soát **hậu kiểm**: seller tạo tự do trước, Admin dọn dẹp sau — không bắt seller chờ duyệt từng thuộc tính mới thêm, để không làm chậm luồng đăng bán.

**c) Form tạo/sửa sản phẩm (Seller) — cập nhật so với Mục 3.2**
- Seller chọn danh mục trước → form hiển thị các thuộc tính gợi ý sẵn của danh mục đó (nếu có), seller điền giá trị.
- Có nút **"+ Thêm thuộc tính"** để seller tự đặt tên thuộc tính mới + nhập giá trị, không giới hạn số lượng (trừ khi có quyết định khác — xem phần Câu hỏi cần xác nhận).
- Nếu seller đổi danh mục sau khi đã nhập thuộc tính, cảnh báo rõ dữ liệu thuộc tính cũ có thể không còn phù hợp, yêu cầu xác nhận trước khi đổi.

**d) Hiển thị & tìm kiếm/filter phía Buyer — cập nhật so với Mục 3.3**
- Trang danh sách sản phẩm/tìm kiếm: bộ filter thuộc tính phải tự động đổi theo danh mục đang xem, không còn cố định filter "Size/Màu" cho mọi ngành hàng.
- Elasticsearch: index thuộc tính động dạng key-value linh hoạt, đảm bảo tìm kiếm/filter theo thuộc tính vẫn đúng dù mỗi sản phẩm có bộ thuộc tính khác nhau tùy seller tự thêm.
- Trang chi tiết sản phẩm hiển thị đầy đủ mọi thuộc tính (cả thuộc tính gợi ý và thuộc tính seller tự thêm) của sản phẩm đó.

**e) Dữ liệu cũ — migration bắt buộc**
- Toàn bộ sản phẩm giày hiện có (kể cả dữ liệu demo/test) phải được migrate sang cấu trúc mới, KHÔNG được mất dữ liệu Thương hiệu/Xuất xứ/Chất liệu/Loại đế đang có.
- Map các field cũ này thành thuộc tính động thuộc danh mục "Giày" (tự tạo danh mục "Giày" với đúng bộ thuộc tính gợi ý tương ứng nếu chưa có).
- Viết migration script có thể revert, không sửa trực tiếp dữ liệu mà không có backup.

### 10.4. Yêu cầu dữ liệu (bổ sung vào docs/03-erd-database.md)
Bắt buộc có (đặt tên nhất quán với quy ước hiện tại của dự án):
- Bảng định nghĩa thuộc tính: tên, kiểu dữ liệu (text/số/dropdown một hoặc nhiều lựa chọn), thuộc danh mục nào (nếu là gợi ý mặc định), do seller nào tạo (nếu là thuộc tính tự thêm), trạng thái (đã chuẩn hóa bởi Admin hay chưa).
- Bảng giá trị gợi ý cho thuộc tính kiểu dropdown.
- Bảng lưu giá trị thuộc tính thực tế của từng sản phẩm (liên kết sản phẩm ↔ thuộc tính ↔ giá trị).
- Quan hệ danh mục ↔ thuộc tính gợi ý (1 danh mục có nhiều thuộc tính gợi ý, 1 thuộc tính có thể được gợi ý ở nhiều danh mục sau khi Admin chuẩn hóa).

### 10.5. Ràng buộc kỹ thuật
- Không đổi công nghệ ngoài stack hiện có (Spring Boot, MySQL, Elasticsearch, Vue + Ant Design Vue).
- Không phá vỡ API hiện tại mà `order-service`/`cart-service` đang dùng để đọc thông tin sản phẩm — nếu bắt buộc đổi contract, phải liệt kê rõ các service bị ảnh hưởng và cách cập nhật đồng bộ.
- Ưu tiên tái sử dụng tối đa cấu trúc `san-pham`/`san-pham-chi-tiet` hiện có, chỉ thêm bảng thuộc tính động bên cạnh — không viết lại từ đầu module sản phẩm.

### 10.6. Câu hỏi cần xác nhận trước khi code (Codex PHẢI dừng lại hỏi, không tự quyết)
- Thuộc tính seller tự thêm mới: chỉ hiện với chính shop đó, hay được gợi ý dùng chung ngay cho seller khác cùng danh mục trước khi Admin duyệt/gộp?
- Có giới hạn số lượng thuộc tính tự thêm trên 1 sản phẩm không, hay không giới hạn?
- Thuộc tính tự thêm có bắt buộc là dạng text tự do, hay seller cũng được chọn kiểu dữ liệu (số, dropdown...) ngay khi tự tạo?

### 10.7. Output bổ sung
- Cập nhật `docs/03-erd-database.md` (thêm ERD phần thuộc tính động).
- Cập nhật `docs/04-luong-nghiep-vu.md` (thêm sequence diagram: luồng seller tự thêm thuộc tính, luồng Admin chuẩn hóa/gộp thuộc tính).
- Cập nhật `docs/05-roadmap.md` (chèn task này vào đúng phase phù hợp, không tạo phase rời rạc không liên kết với roadmap gốc).
- Cập nhật `docs/PROGRESS.md` theo đúng format ở Mục 9: ghi rõ đây là nghiệp vụ bổ sung phát sinh sau khi audit thực tế, lý do bổ sung, và liên kết rõ với Mục 3.2/3.3/3.4 gốc (không phải task độc lập tách rời).

Mỗi lần bắt đầu phiên mới, đọc kỹ mục "Trạng thái tổng quan hiện tại" và "Việc tiếp theo cần làm ngay" trước tiên để biết chính xác phải làm gì, sau đó mới đọc sâu vào các entry nhật ký nếu cần thêm ngữ cảnh.