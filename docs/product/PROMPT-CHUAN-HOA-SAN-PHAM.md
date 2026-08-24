# PROMPT CHO CODEX: Chuẩn hóa domain SẢN PHẨM (catalog-service) — mô hình đa ngành hàng, thuộc tính động thực sự

> Đây là prompt **độc lập, làm riêng trước** phần Sản phẩm (catalog-service) trong dự án Marketplace đa gian hàng. KHÔNG động vào seller-service, payout-service, order flow, promotion — những phần đó xử lý ở prompt khác, sau khi domain Sản phẩm đã chuẩn.
>
> Lý do tách riêng: domain Sản phẩm là nền tảng để mọi domain khác (giỏ hàng, đơn hàng, tìm kiếm, thống kê) build lên trên. Nếu model Sản phẩm sai/nửa vời, mọi thứ code sau sẽ phải đập lại. Vì vậy: **làm đúng cái này trước, xong hẳn mới tính tiếp cái khác.**

---

## 0. QUY TẮC LÀM VIỆC QUA NHIỀU PHIÊN (bắt buộc, giữ nguyên như dự án gốc)

Dự án làm qua nhiều phiên rời rạc, mỗi phiên không nhớ phiên trước. Bắt buộc:

### Đầu phiên
1. Kiểm tra file `docs/product/PROGRESS-PRODUCT.md` đã tồn tại chưa.
   - **Chưa có**: đây là phiên khởi tạo — đọc toàn bộ source thật của `catalog-service` (entity, migration, controller, service, cả phần FE liên quan tới form sản phẩm ở Admin/Seller), đối chiếu với DB thật (danh sách bảng hiện có: `brand`, `category`, `category_attribute_suggestion`, `color`, `material`, `origin`, `outbox`, `product`, `product_attribute_definition`, `product_attribute_option`, `product_attribute_value`, `product_attribute_value_option`, `product_variant`, `size`, `sole_type`) rồi thực hiện Mục 1–8 bên dưới.
   - **Đã có**: đọc kỹ để biết đã làm tới đâu, task nào dở, vướng mắc gì.
2. Không suy đoán trạng thái code — luôn đọc source thật trước khi code tiếp.
3. Không nhảy cóc task, không làm lại task đã DONE trừ khi được ghi rõ cần sửa lại.

### Cuối phiên
1. Cập nhật `docs/product/PROGRESS-PRODUCT.md` theo format ở Mục 9.
2. Nếu bị chặn do thiếu quyết định nghiệp vụ — dừng, ghi rõ câu hỏi, không tự đoán bừa.
3. Không kết thúc phiên mà không cập nhật file log.

---

## 0.5. QUY TẮC TỰ CHẠY LIÊN TỤC — KHÔNG DỪNG XIN XÁC NHẬN GIỮA CHỪNG

Đây là quy tắc quan trọng để không phải dán lại prompt nhiều lần: **một khi đã bắt đầu, tự động chạy hết toàn bộ các task trong roadmap theo đúng thứ tự, không dừng lại hỏi "có muốn tiếp tục không" sau mỗi task nhỏ.** Chỉ dừng lại giữa chừng trong đúng 3 trường hợp:

1. **Bị chặn thật sự** — thiếu quyết định nghiệp vụ mà Mục 3.5 chưa chốt, hoặc phát hiện mâu thuẫn giữa quyết định đã chốt với thực tế source code (theo đúng Mục 10).
2. **Phát hiện rủi ro không thể đảo ngược** — thao tác sẽ xóa dữ liệu/code không thể khôi phục và có khả năng ảnh hưởng ngoài phạm vi domain Sản phẩm đã nêu ở đầu prompt.
3. **Đã hoàn thành toàn bộ checklist ở Mục 9** — lúc này dừng lại báo cáo tổng kết, không tự ý làm tiếp sang domain khác (seller, payout, order...) vì đó là phạm vi của prompt khác.

Ngoài 3 trường hợp trên, làm xong 1 task → cập nhật `PROGRESS-PRODUCT.md` → tự chuyển sang task tiếp theo trong roadmap ngay, không chờ người dùng gõ thêm lệnh.

### Bắt buộc có bằng chứng trước khi đánh dấu DONE

Không được ghi 1 task là DONE trong `PROGRESS-PRODUCT.md` chỉ dựa trên việc "đã viết code/file xong". Với từng loại task, bằng chứng tối thiểu bắt buộc kèm theo trong log:

- **Task liên quan schema/DB** (tạo bảng, xóa bảng, seed data): phải thực sự chạy DDL/seed trên 1 database thật (dev/tạm), dán lại kết quả câu lệnh kiểm chứng (VD `SHOW TABLES`, `SELECT COUNT(*)`, hoặc script kiểm chứng invariant nêu ở ERD) — không chỉ nộp file `.sql` chưa chạy.
- **Task liên quan API/backend**: phải thực sự gọi thử endpoint (curl/Postman/test tự động) và dán lại request/response mẫu, không chỉ mô tả "đã implement xong".
- **Task liên quan FE**: phải build/chạy thử được, mô tả cụ thể đã thao tác gì trên UI và kết quả quan sát được (không chỉ nói "đã code xong component").
- **Task liên quan tài liệu** (.md thuần túy, không sinh code): không cần bằng chứng chạy thử, nhưng phải tự đối chiếu lại đủ nội dung yêu cầu trước khi ghi DONE.

Nếu không thể chạy thử được vì thiếu môi trường/công cụ — ghi rõ **"KHÔNG THỂ KIỂM CHỨNG — lý do: ..."** thay vì DONE, để người dùng biết cần tự kiểm tra lại, không được ghi DONE khi chưa có bằng chứng.

---

## 1. VẤN ĐỀ HIỆN TẠI — PHẢI SỬA TẬN GỐC, KHÔNG PHẢI VÁ THÊM

Từ soi DB thật, hiện trạng đang là **mô hình lai (half-migrated)**, không dùng được cho đa ngành hàng:

- Các bảng **cứng, hard-code riêng ngành giày**: `brand`, `color`, `material`, `origin`, `size`, `sole_type`. Đây là danh sách cố định do dev định nghĩa sẵn trong schema, không sinh ra được cho ngành hàng khác (điện tử không có "size giày", mỹ phẩm không có "loại đế").
- Các bảng **thuộc tính động mới** đã có mầm: `category_attribute_suggestion`, `product_attribute_definition`, `product_attribute_option`, `product_attribute_value`, `product_attribute_value_option`. Nhưng nếu tồn tại song song với các bảng cứng ở trên mà không thay thế hẳn, thì bản chất seller ngành khác vẫn bị kẹt ở phần giày cứng.
- `product_variant` đã tồn tại nhưng cần làm rõ: variant hiện sinh ra dựa trên bảng nào — nếu vẫn dựa trên `size`/`color` cứng thì đây chính là chỗ phải thay bằng cơ chế động.

**Kết luận bắt buộc:** không giữ `brand`, `color`, `material`, `origin`, `size`, `sole_type` như bảng nghiệp vụ cố định nữa. Toàn bộ phải trở thành dữ liệu bên trong hệ thống thuộc tính động — vừa dùng chung cơ chế cho mọi ngành hàng, vừa migrate được dữ liệu giày hiện có mà không mất mát.

---

## 2. MỤC TIÊU

Thiết kế lại domain Sản phẩm để **1 seller ngành giày, 1 seller ngành điện tử, 1 seller ngành mỹ phẩm cùng dùng chung 1 cơ chế đăng sản phẩm**, không ai bị ép theo field của ngành khác, nhưng vẫn tìm kiếm/filter/hiển thị nhất quán trên toàn sàn.

---

## 3. NGHIỆP VỤ CHI TIẾT — PHẢI HIỂU ĐÚNG BẢN CHẤT, KHÔNG GỘP CHUNG

### 3.1. Phân biệt bắt buộc: 2 loại thuộc tính khác bản chất nhau

Đây là phần quan trọng nhất của toàn bộ prompt này — nhầm giữa 2 loại này là nguyên nhân khiến model cũ sai.

| | **Thuộc tính sinh biến thể** ("Phân loại hàng") | **Thuộc tính mô tả / thông số kỹ thuật** |
|---|---|---|
| Ví dụ | Màu sắc, Size, Dung lượng bộ nhớ, Phiên bản | Chất liệu, Xuất xứ, Thương hiệu, Chip xử lý, Trọng lượng, Bảo hành |
| Có sinh ra `product_variant` riêng không | **CÓ** — mỗi tổ hợp giá trị = 1 variant, có SKU/giá/tồn kho/ảnh riêng | **KHÔNG** — chỉ là dữ liệu mô tả đi kèm sản phẩm, dùng chung cho mọi variant |
| Số lượng tối đa trên 1 sản phẩm | Tối đa **2 trục** (giống Shopee: VD trục 1 = Màu, trục 2 = Size) | Không giới hạn |
| Bắt buộc theo danh mục không | Không bắt buộc, seller tự quyết định sản phẩm có cần phân loại hay không | Danh mục có gợi ý sẵn, seller có thể thêm mới tự do |
| Ai định nghĩa giá trị | Seller tự đặt tên trục + tự nhập danh sách giá trị (VD trục "Màu" → seller tự gõ "Đỏ", "Xanh") | Seller chọn từ gợi ý hoặc tự gõ giá trị mới, tùy kiểu dữ liệu |

**Bắt buộc:** thiết kế 2 cơ chế lưu trữ và luồng nhập liệu **tách biệt rõ ràng** cho 2 loại này, không gộp chung 1 bảng "attribute" rồi phân biệt bằng 1 cờ mập mờ khiến logic sinh variant và logic hiển thị thông số lẫn lộn nhau.

### 3.2. Luồng seller tạo sản phẩm — step by step, bắt buộc mô tả đủ các bước

1. Seller chọn **danh mục** sản phẩm (danh mục là cây, do Platform Admin quản lý chung toàn sàn).
2. Hệ thống hiển thị:
   - Danh sách **thuộc tính mô tả gợi ý** của danh mục đó (nếu danh mục "Giày" → gợi ý sẵn "Chất liệu", "Xuất xứ"; danh mục "Điện thoại" → gợi ý sẵn "Dung lượng pin", "Chip xử lý" — dữ liệu gợi ý này do Admin cấu hình dần theo thời gian, không hard-code trong code).
   - Seller điền giá trị cho từng thuộc tính gợi ý (không bắt buộc phải điền hết, trừ khi Admin đánh dấu 1 số thuộc tính là bắt buộc theo danh mục).
   - Seller có nút **"+ Thêm thông số khác"** để tự đặt tên thuộc tính mô tả mới nếu gợi ý sẵn chưa đủ, hệ thống auto-complete gợi ý tên đã có trong cùng danh mục để tránh trùng ý khác tên (VD "Chất liệu" vs "Chất liệu vải").
3. Riêng phần **Phân loại hàng** (sinh biến thể) tách hẳn UI khác:
   - Seller chọn: sản phẩm này có phân loại hay không (VD với sản phẩm không có size/màu như 1 cuốn sách thì không cần).
   - Nếu có, seller tự đặt tên tối đa 2 trục phân loại (không giới hạn tên trục là gì — có thể là "Màu", "Size", "Dung lượng", "Phiên bản"... tùy ngành hàng seller tự đặt), rồi tự nhập danh sách giá trị cho từng trục.
   - Hệ thống tự sinh ra bảng tổ hợp (VD 3 màu x 4 size = 12 dòng variant), seller nhập giá/tồn kho/ảnh riêng cho từng dòng, có thể xóa bớt tổ hợp không bán.
4. Nếu seller đổi danh mục sau khi đã nhập thông số mô tả → cảnh báo rõ dữ liệu thông số cũ (gắn theo danh mục cũ) có thể không còn phù hợp, yêu cầu xác nhận trước khi đổi (áp dụng cho thông số mô tả; phân loại hàng do seller tự đặt tên nên không phụ thuộc danh mục, giữ nguyên khi đổi danh mục).

### 3.3. Vai trò Platform Admin — hậu kiểm, không tiền kiểm

Bổ sung màn **"Quản lý thuộc tính sản phẩm"** trong Admin sàn, tách 2 tab tương ứng 2 loại ở Mục 3.1:

- **Tab Thông số mô tả**: xem toàn bộ thông số đang được seller dùng trên toàn sàn, gộp thông số trùng ý khác tên, gắn/bỏ gắn thông số vào danh mục làm gợi ý mặc định, ẩn/xóa thông số rác.
- **Tab Phân loại hàng**: xem các tên trục phân loại + giá trị seller đang tự đặt (VD thống kê "Màu" bị viết thành "Color", "Màu sắc", "Mau" nhiều biến thể khác nhau) để **gợi ý chuẩn hóa dần** (không bắt buộc, chỉ là gợi ý khi seller sau gõ tên trục để giảm phân mảnh dữ liệu tên trục, không chặn seller tự đặt tên khác nếu muốn).
- **Nguyên tắc:** seller được tạo tự do ngay lúc đăng sản phẩm, Admin dọn dẹp sau — không chặn/duyệt từng thuộc tính mới thêm.

### 3.4. Hiển thị & tìm kiếm phía Buyer

- Trang danh sách sản phẩm/tìm kiếm: bộ lọc phải **tự đổi theo danh mục đang xem** — không còn filter cứng "Size/Màu" áp dụng cho mọi ngành hàng. Bộ lọc lấy từ chính các thông số mô tả + tên trục phân loại đang thực sự tồn tại trong tập sản phẩm của danh mục đó (không lấy toàn bộ danh sách gợi ý danh mục, để tránh hiện filter rỗng không có sản phẩm nào khớp).
- Elasticsearch: index thông số dạng key-value linh hoạt (không cố định field cứng `brand`, `color`... trong mapping), đảm bảo filter đúng dù mỗi sản phẩm có bộ thông số khác nhau.
- Trang chi tiết sản phẩm: hiển thị đầy đủ thông số mô tả (nhóm theo thứ tự Admin cấu hình nếu có) + bảng chọn phân loại hàng (dropdown/nút chọn Màu → Size như chuẩn UI Shopee, cập nhật giá/ảnh/tồn kho theo đúng variant được chọn).

### 3.5. QUYẾT ĐỊNH THIẾT KẾ ĐÃ CHỐT — Codex làm theo đúng đây, KHÔNG hỏi lại

Đây là các điểm lẽ ra sẽ là "câu hỏi cần xác nhận", nhưng đã được chốt trước dựa trên cách các sàn thật (Shopee/Tiki) vận hành, để tránh mất thêm 1 vòng hỏi-đáp không cần thiết. Nếu trong lúc code phát sinh mâu thuẫn với quyết định nào ở đây thì mới dừng lại hỏi, còn không thì cứ theo đúng đây mà làm:

**a) Số trục phân loại hàng: cố định tối đa 2, không cấu hình linh hoạt hơn.**
Giống chuẩn thật của Shopee (VD: Màu × Size). Không làm thành "N trục tùy chỉnh" vì sẽ phức tạp hóa không cần thiết cả UI lẫn logic sinh tổ hợp, trong khi thực tế gần như không ngành hàng nào cần quá 2 trục để phân biệt SKU.

**b) Sản phẩm KHÔNG có trục phân loại nào vẫn phải có đúng 1 `product_variant` mặc định (ẩn, `is_default = true`).**
Đây là điểm quan trọng nhất về mặt kiến trúc: toàn bộ giá/tồn kho/giỏ hàng/đơn hàng/đối soát **luôn luôn thao tác trên `product_variant`**, không bao giờ thao tác trực tiếp trên `product`. Nhờ vậy `cart-service`, `order-service` không cần viết 2 nhánh logic riêng cho "sản phẩm có phân loại" và "sản phẩm không có phân loại" — với hệ thống, mọi sản phẩm đều "có variant", chỉ khác là 1 variant hay nhiều variant.

**c) Giá trị gợi ý cho thông số kiểu dropdown (`product_attribute_option`): seller được tự thêm giá trị mới ngay lúc nhập, không cần chờ duyệt.**
Áp dụng đúng nguyên tắc hậu kiểm xuyên suốt: giá trị mới seller thêm có `is_verified = false`, xuất hiện ngay trong gợi ý cho các sản phẩm sau (kể cả seller khác cùng danh mục) để tránh mỗi seller tự gõ lại, Admin gộp/chuẩn hóa dần sau.

**d) "Loại đế" — thiết kế là thông số mô tả thường, KHÔNG phải trục phân loại.**
Lý do: thực tế mua giày, khách không chọn SKU riêng theo loại đế như cách chọn theo Size/Màu — loại đế chỉ là thông tin mô tả đi kèm. Khi seed data mẫu mới, "Loại đế" là 1 `product_attribute_definition` bình thường thuộc danh mục "Giày", không tạo `product_variant_axis`.

**e) API versioning: đổi contract trực tiếp (breaking change), KHÔNG giữ song song v1/v2.**
Vì dự án chưa lên production, việc duy trì 2 version song song chỉ tốn công vô ích. Sửa thẳng `catalog-service` + cập nhật đồng bộ `cart-service`/`order-service`/Elasticesearch trong cùng 1 đợt migration, có thể tách nhỏ theo task trong roadmap nhưng không cần lớp tương thích ngược.

**f) Cơ chế Admin "gộp" 2 thuộc tính trùng ý khác tên: dùng self-reference, không sửa dữ liệu từng dòng.**
`product_attribute_definition` có thêm field `merged_into_definition_id` (nullable, tự tham chiếu). Khi Admin gộp "Chất liệu" vào "Chất liệu vải", chỉ cần set field này — toàn bộ `product_attribute_value` cũ đọc qua tầng service sẽ tự resolve sang definition chuẩn, không cần chạy script sửa hàng loạt `product_attribute_value` đang tồn tại. Nếu Admin muốn dọn hẳn, script batch update chạy nền sau, không chặn thao tác gộp.

**g) Lưu trữ giá trị thuộc tính: 1 bảng `product_attribute_value` duy nhất, nhiều cột theo kiểu dữ liệu.**
Không tách bảng riêng theo từng kiểu dữ liệu (text/number/dropdown). Dùng 1 bảng với các cột `value_text`, `value_number`, `attribute_option_id` (nullable tùy kiểu `attribute_definition.data_type`) — đơn giản hơn khi query, tránh phải join nhiều bảng khi hiển thị/filter.

---

## 4. YÊU CẦU ERD (viết vào `docs/product/erd-san-pham.md`)

Bắt buộc thiết kế lại, đặt tên nhất quán với quy ước hiện có của dự án. Yêu cầu tối thiểu các nhóm bảng sau (tên gợi ý, có thể điều chỉnh cho khớp convention hiện tại nhưng phải giữ đúng ý nghĩa và quan hệ):

**Nhóm Danh mục & gợi ý:**
- `category` (giữ nguyên, cây danh mục toàn sàn).
- `category_attribute_suggestion` — danh mục ↔ thông số mô tả gợi ý (many-to-many, 1 thông số có thể gợi ý ở nhiều danh mục sau khi Admin chuẩn hóa).

**Nhóm Thông số mô tả (không sinh biến thể):**
- `product_attribute_definition` — tên thông số, kiểu dữ liệu (`TEXT` / `NUMBER` / `SELECT_ONE` / `SELECT_MULTI`), do seller nào tạo (null nếu do Admin định nghĩa sẵn), cờ `is_verified`, field tự tham chiếu `merged_into_definition_id` (nullable — dùng cho cơ chế gộp ở Mục 3.5.f).
- `product_attribute_option` — giá trị gợi ý cho thông số kiểu `SELECT_ONE`/`SELECT_MULTI`, có `is_verified` (theo Mục 3.5.c).
- `product_attribute_value` — **1 bảng duy nhất** (theo Mục 3.5.g) lưu giá trị thực tế của 1 sản phẩm cho 1 thông số: `product_id`, `attribute_definition_id`, và các cột theo kiểu (`value_text`, `value_number`, `attribute_option_id` — chỉ 1 trong 3 có giá trị tùy `data_type` của definition tương ứng).

**Nhóm Phân loại hàng (sinh biến thể) — cần làm mới, tách khỏi thông số mô tả:**
- `product_variant_axis` (mới) — tối đa **2 dòng/sản phẩm** (theo Mục 3.5.a), mỗi dòng là 1 trục do seller tự đặt tên (VD "Màu", "Size"), thuộc `product_id`, có `display_order` (1 hoặc 2).
- `product_variant_axis_value` (mới) — giá trị của 1 trục (VD trục "Màu" có các dòng "Đỏ", "Xanh").
- `product_variant` (đã có, review lại) — 1 tổ hợp giá trị cụ thể của các trục, có SKU/giá/tồn kho/ảnh riêng; thêm cờ `is_default` (theo Mục 3.5.b — dùng khi sản phẩm không có trục phân loại nào, chỉ có đúng 1 variant mặc định); liên kết tới `product_variant_axis_value` qua bảng trung gian `product_variant_axis_value_mapping` để 1 variant biết nó ứng với giá trị nào ở từng trục.

**Bảng cũ cần xóa:** `brand`, `color`, `material`, `origin`, `size`, `sole_type` — xóa hẳn, không giữ lại (theo Mục 5, dự án chưa có data thật nên không cần giữ làm tham chiếu lịch sử).

Phải có sơ đồ Mermaid ERD thể hiện rõ quan hệ toàn bộ các bảng trên.

---

## 5. DỮ LIỆU CŨ — XÓA THẲNG, KHÔNG MIGRATE

Dự án **chưa có người dùng thật, toàn bộ data hiện tại là data giả/demo** — không cần viết migration script, không cần đối chiếu số liệu trước/sau, không cần khả năng revert. Làm đơn giản như sau:

1. Xóa hẳn các bảng nghiệp vụ cứng cũ: `brand`, `color`, `material`, `origin`, `size`, `sole_type` (và toàn bộ dữ liệu bên trong) — không giữ lại làm bảng tham chiếu lịch sử.
2. Xóa/reset toàn bộ dữ liệu `product`, `product_variant` cũ đang phụ thuộc các bảng trên (vì cấu trúc thay đổi hẳn, cố map lại dữ liệu cũ không đáng công).
3. Sau khi model mới (Mục 4) đã tạo xong bằng migration schema (chỉ tạo bảng, không cần script chuyển data), viết lại **1 bộ seed data mẫu mới** cho domain Sản phẩm bằng model mới, tối thiểu đủ:
   - 1 danh mục "Giày" (gợi ý thông số: Chất liệu, Xuất xứ, Thương hiệu, Loại đế) + vài sản phẩm mẫu có đủ 2 trục phân loại (Màu × Size).
   - 1 danh mục khác hoàn toàn khác ngành (VD "Điện thoại", gợi ý thông số: Dung lượng, Chip xử lý, Bảo hành) + vài sản phẩm mẫu, để tự kiểm chứng model thực sự dùng chung được cho nhiều ngành hàng, không chỉ chạy đúng cho giày.
   - Ít nhất 1 sản phẩm mẫu **không có trục phân loại nào** (test đúng cơ chế `is_default variant` ở Mục 3.5.b).
4. Không cần viết script backup/rollback cho bước xóa — chỉ cần chạy migration schema mới (drop bảng cũ, tạo bảng mới) trên môi trường dev, không phải môi trường có data thật.

---

## 6. ẢNH HƯỞNG TỚI CÁC SERVICE KHÁC — PHẢI LIỆT KÊ RÕ, KHÔNG ĐƯỢC BỎ QUA

Vì `cart-service`, `order-service`, Elasticsearch sync đang đọc dữ liệu sản phẩm theo contract cũ (rất có thể đang dùng thẳng field `brand`, `color`, `size`...), bắt buộc:

1. Liệt kê toàn bộ endpoint/API hiện tại của `catalog-service` đang trả về field theo bảng cứng cũ.
2. Với mỗi endpoint, mô tả rõ contract mới (response sẽ trả `attributes: [{name, value}]` và `variantAxes`/`variant` theo cấu trúc mới như thế nào).
3. Liệt kê từng service khác (`cart-service`, `order-service`, sync Elasticsearch qua Outbox/Debezium) đang phụ thuộc field nào, cần sửa gì để tương thích — không được để vỡ luồng đặt hàng/giỏ hàng hiện có mà không có kế hoạch cập nhật đồng bộ.
4. Nếu cần version API tạm thời (v1 cũ giữ song song v2 mới trong lúc chuyển đổi), nêu rõ chiến lược, thời điểm bỏ v1.

---

## 7. RÀNG BUỘC KỸ THUẬT

1. Không đổi công nghệ ngoài stack hiện có (Spring Boot, MySQL, Elasticsearch, Vue + Ant Design Vue).
2. Ưu tiên tái sử dụng tối đa cấu trúc `product`/`product_variant` hiện có — chỉ thiết kế lại phần thuộc tính/phân loại xung quanh, không viết lại toàn bộ module sản phẩm từ đầu.
3. Được phép xóa thẳng bảng `brand`, `color`, `material`, `origin`, `size`, `sole_type` cùng dữ liệu bên trong ngay khi model mới đã tạo xong (theo Mục 5) — không cần giữ lại, không cần archive.
4. Ngôn ngữ tài liệu: tiếng Việt, đặt tên bảng/field mới nhất quán quy ước hiện tại của dự án.

---

## 8. OUTPUT BẮT BUỘC

```
docs/product/
├── PROGRESS-PRODUCT.md          ← log tiến độ riêng cho domain sản phẩm (format Mục 9)
├── 00-audit-hien-trang.md       ← đối chiếu DB thật + code thật, liệt kê chính xác chỗ nào đang hard-code
├── 01-nghiep-vu-thuoc-tinh.md   ← toàn bộ Mục 3 viết chi tiết, có ví dụ cụ thể cho ít nhất 3 ngành hàng (giày, điện tử, mỹ phẩm)
├── erd-san-pham.md              ← ERD đầy đủ + Mermaid diagram (nội dung Mục 4)
├── seed-data-plan.md            ← mô tả bộ data mẫu mới sẽ seed (nội dung Mục 5), KHÔNG cần migration script
├── api-contract-changes.md      ← nội dung Mục 6, liệt kê từng endpoint/service bị ảnh hưởng
└── roadmap-san-pham.md          ← chia task nhỏ theo thứ tự làm (ERD → xóa bảng cũ + seed mới → backend API → FE Seller form → FE Admin quản lý thuộc tính → FE Buyer filter/search)
```

Mỗi file phải chi tiết, có ví dụ cụ thể minh họa bằng số liệu/tên thật, không viết chung chung.

---

## 9. FORMAT BẮT BUỘC `docs/product/PROGRESS-PRODUCT.md`

```markdown
# PROGRESS-PRODUCT.md — Nhật ký chuẩn hóa domain Sản phẩm

## Trạng thái tổng quan hiện tại
- Giai đoạn: [Audit / Tài liệu hóa / Migration / Backend API / Frontend Seller / Frontend Admin / Frontend Buyer]
- Task đang làm dở: [mô tả cụ thể]
- Việc tiếp theo cần làm ngay: [mô tả đủ rõ để phiên sau làm luôn]

## Câu hỏi / quyết định cần người dùng xác nhận
- [liệt kê nếu có]

## Checklist
- [ ] Audit hiện trạng DB + code thật
- [ ] Tài liệu nghiệp vụ thuộc tính (01-nghiep-vu-thuoc-tinh.md)
- [ ] ERD mới (erd-san-pham.md)
- [ ] Kế hoạch seed data mẫu mới (seed-data-plan.md)
- [ ] Xóa bảng cũ + tạo bảng mới theo model (schema migration, không cần chuyển data)
- [ ] Chạy seed data mẫu mới (ít nhất 2 ngành hàng khác nhau + 1 sản phẩm không có phân loại)
- [ ] Cập nhật contract API catalog-service
- [ ] Cập nhật cart-service/order-service/Elasticsearch theo contract mới
- [ ] FE Seller: form tạo/sửa sản phẩm (thông số + phân loại hàng)
- [ ] FE Admin: màn Quản lý thuộc tính (2 tab)
- [ ] FE Buyer: filter động theo danh mục, trang chi tiết sản phẩm hiển thị đúng

## Nhật ký chi tiết (entry mới nhất trên cùng)

### [YYYY-MM-DD HH:mm] Phiên #N
**Đã làm:**
-

**File đã tạo/sửa:**
-

**Bằng chứng đã kiểm chứng:** [dán log/kết quả chạy thử cụ thể theo Mục 0.5, hoặc ghi "KHÔNG THỂ KIỂM CHỨNG — lý do: ..."]

**Kết quả:** [DONE / DỞ DANG — dừng ở bước ...]

**Ghi chú/vướng mắc:**

---
```

---

## 10. CÁC ĐIỂM ĐÃ CHỐT SẴN — KHÔNG CẦN HỎI LẠI

Toàn bộ 4 điểm vốn hay bị bỏ ngỏ (số trục phân loại tối đa, cơ chế duyệt giá trị dropdown, hướng migrate "Loại đế", chiến lược version API) **đã được chốt cụ thể ở Mục 3.5** — Codex đọc kỹ mục đó và làm theo, không dừng lại hỏi những điểm này nữa.

Chỉ dừng lại hỏi người dùng nếu trong lúc đọc source thật phát sinh mâu thuẫn với quyết định ở Mục 3.5 (VD: phát hiện `order-service` đang phụ thuộc cứng vào việc "sản phẩm không variant thì không có `variant_id`" theo cách không thể tương thích với quyết định 3.5.b) — khi đó nêu rõ mâu thuẫn cụ thể, không tự ý bỏ qua quyết định đã chốt.

---

## 11. CÂU LỆNH BẮT ĐẦU

**Bắt đầu ngay: kiểm tra `docs/product/PROGRESS-PRODUCT.md`, nếu chưa có thì đọc toàn bộ source `catalog-service` (entity, migration SQL, controller, service) + đối chiếu danh sách bảng DB thật đã liệt kê ở Mục 0, sau đó thực hiện lần lượt Mục 3 → 4 → 5 → 6 để sinh bộ tài liệu (Mục 5 chỉ cần kế hoạch xóa bảng cũ + seed data mẫu mới, KHÔNG viết migration script chuyển dữ liệu), tạo `docs/product/PROGRESS-PRODUCT.md`, rồi tiếp tục tự chạy sang các task tiếp theo trong roadmap (xóa bảng cũ, tạo schema mới, seed data, sửa API, sửa FE...) theo đúng Mục 0.5 — không dừng lại xin xác nhận giữa chừng, trừ khi rơi vào 1 trong 3 trường hợp nêu ở Mục 0.5. Các điểm ở Mục 3.5 đã chốt sẵn, không cần hỏi lại. Mỗi task đánh dấu DONE phải kèm bằng chứng đã chạy thử theo đúng yêu cầu ở Mục 0.5.**