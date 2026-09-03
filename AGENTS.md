# PROMPT CHO CODEX: Kiểm thử toàn luồng (E2E) và tự sửa lỗi hệ thống Marketplace

> Dán nguyên file này làm system/task prompt cho Codex (hoặc agent coding tương đương).
> Mục tiêu: KHÔNG viết unit test. Mục tiêu là chạy thật hệ thống, đi qua toàn bộ luồng nghiệp vụ như
> một buyer/seller/admin thật, phát hiện chỗ nào sai lệch giữa code – contract – dữ liệu – tài liệu,
> rồi tự sửa tận gốc, lặp lại cho tới khi không còn lỗi phát sinh trong toàn bộ luồng.

---

## 0. Bối cảnh bắt buộc phải đọc trước khi làm gì khác

Repo này là marketplace e-commerce, kiến trúc Spring Boot microservice (Eureka + Spring Cloud Gateway),
mỗi domain một MySQL schema riêng, FE Vue 3 SPA. Tài liệu `HỆ THỐNG HIỆN TẠI` (as-is, chốt ngày 27/08/2026)
mô tả chi tiết: kiến trúc, service/port, JWT/role, mô hình seller/shop, schema DB, luồng nghiệp vụ,
API contract, FE routes, search/Kafka, và đặc biệt là **mục 11 – Giới hạn và rủi ro hiện tại** liệt kê
các lỗi/khoảng trống đã xác nhận từ source.

Trước khi test, agent phải:

1. Đọc lại toàn bộ tài liệu as-is này (không tự suy diễn ngoài source).
2. Đối chiếu lại với source code hiện tại (controller/service/entity/gateway/config) — vì source có thể
   đã thay đổi so với tài liệu kể từ ngày chốt. **Source code luôn là nguồn sự thật cao hơn tài liệu.**
3. Dựng lại toàn bộ stack local (script `run-all.ps1` / tương đương trong repo) trước khi test bất cứ luồng nào.
   Không test bằng cách đọc code suông — phải có request/response thật, DB thật.

## 1. Nguyên tắc bắt buộc trong suốt quá trình

- **Không viết/thêm unit test.** Test bằng cách gọi API thật qua gateway (`http://localhost:8080`), giống
  một buyer/seller/admin thật, theo đúng thứ tự nghiệp vụ (đăng ký → duyệt seller → tạo sản phẩm →
  giỏ hàng → checkout → xử lý đơn → payout → dispute...).
- **Test cả luồng, không test rời từng endpoint.** Một luồng coi là "qua" chỉ khi state cuối cùng ở
  DB/response đúng như kỳ vọng nghiệp vụ, không chỉ HTTP 200.
- **Mỗi khi phát hiện lỗi**: xác định nguyên nhân gốc (root cause) trong code (không patch triệu chứng),
  sửa trực tiếp trong source, sau đó re-run lại đúng luồng đó và toàn bộ luồng liên quan (regression trong
  phạm vi flow, không phải toàn repo) để chắc chắn không phá thứ khác.
- **Không được im lặng bỏ qua lỗi.** Nếu một lỗi không thể fix ngay (ví dụ cần quyết định nghiệp vụ),
  phải liệt kê rõ trong báo cáo cuối, không được tự ý "giả định cho qua".
- **Không hard-code / mock dữ liệu để né lỗi.** Nếu 1 luồng fail vì thiếu 1 endpoint (ví dụ contract giữa
  hai service lệch), phải bổ sung/sửa đúng chỗ (controller/service tương ứng), không sửa test cho khớp lỗi.
- Ghi log lại: mỗi luồng đã test, input dùng, kết quả trước khi sửa, chỗ đã sửa, kết quả sau khi sửa.

## 2. Chuẩn bị môi trường trước khi test

1. Reset dữ liệu sạch bằng script reset (lưu ý: DROP toàn bộ 8 database, chỉ chạy nếu chấp nhận mất dữ liệu cũ).
2. Start toàn bộ backend + Eureka + Gateway + FE.
3. Xác nhận tất cả service `UP` qua `/actuator/health` và có mặt đủ trong Eureka trước khi test bất kỳ luồng nào.
4. Xác nhận Kafka/Elasticsearch/Kafka Connect đang chạy hay không, và ghi rõ trạng thái này vào báo cáo vì nó
   ảnh hưởng tới luồng search — không coi search lỗi nếu do hạ tầng CDC chưa bật, nhưng phải nói rõ ràng.

## 3. Danh sách luồng bắt buộc phải test full end-to-end

Với mỗi luồng dưới đây: thực hiện đúng trình tự request thật qua gateway, kiểm tra response, kiểm tra
dữ liệu ghi vào đúng bảng/đúng schema, kiểm tra các bảng liên quan khác có được cập nhật đồng bộ không.

### 3.1. Đăng ký & đăng nhập
- Register buyer mới → login buyer → login admin (staff có sẵn) → kiểm tra JWT payload (`role`, `roles`,
  `userId`, và khi có seller: `sellerId/sellerStatus/sellerSlug/shopName`).
- Test refresh token flow thật (gọi endpoint FE dùng khi 401) — xác nhận có hoạt động hay không.
- Test trường hợp `seller-service` down khi login: buyer vẫn login được nhưng không có role `SELLER`.

### 3.2. Đăng ký & duyệt shop
- Buyer đăng ký shop → kiểm tra `seller=PENDING_APPROVAL` + `seller_status_history`.
- Test race: đăng ký 2 lần khi hồ sơ mới nhất khác `REJECTED/CLOSED` → phải bị chặn.
- Admin approve → login lại buyer → xác nhận JWT có `SELLER` + `sellerId`.
- Admin reject → xác nhận không có role SELLER, không tạo được đơn seller nào.
- Admin suspend một seller APPROVED → kiểm tra token cũ đã phát trước đó (chưa hết hạn) có bị chặn ngay
  không hay vẫn dùng được tới khi hết hạn (đây là điểm rủi ro đã biết — xác nhận hành vi thật, không suy diễn).

### 3.3. Seller tạo sản phẩm (catalog)
- Tạo category cây, gán attribute suggestion, tạo product với: ảnh, thuộc tính động, 2 trục biến thể,
  nhiều SKU/variant.
- Xác nhận transaction ghi đúng cả product aggregate lẫn dòng `outbox`.
- Seller B cố gắng sửa/xem sản phẩm của seller A qua API seller → phải bị chặn bởi `sellerId` context.
- Test category không phải leaf, quá 2 trục biến thể, combination trùng → phải bị reject đúng.

### 3.4. Tìm kiếm & xem sản phẩm public
- Gọi `GET /api/v1/permitall/products` và `/permitall/products/{id}` → xác nhận chỉ trả sản phẩm `ACTIVE`.
- Xác nhận filter theo keyword/category/giá/attribute/sort hoạt động đúng trên dữ liệu thật.
- Xác nhận rõ ràng đây là đọc từ MySQL, không phụ thuộc Elasticsearch — nếu Kafka Connect down, path này
  vẫn phải hoạt động bình thường.

### 3.5. Giỏ hàng
- Buyer thêm variant từ nhiều seller khác nhau vào giỏ → xác nhận `shopGroups` group đúng theo seller.
- Xác nhận snapshot giá/tồn kho/seller tại thời điểm thêm có đúng không, và có refresh lại khi giá đổi không.
- Test xóa item (endpoint hiện dùng PUT thay vì DELETE) — xác nhận hoạt động đúng, không phải chỉ đúng contract cũ.

### 3.6. Checkout multi-seller + VNPay (trọng tâm — nhiều rủi ro đã biết ở mục 11.1/11.2)
- Checkout giỏ có ≥2 seller, có áp voucher, bằng COD → kiểm tra: `orders` tạo đúng 1, tách đúng số
  `order_seller`/`order_item` theo seller, trừ tồn kho đúng, giảm quantity voucher đúng, xóa đúng cart item
  đã checkout (không xóa nhầm item chưa checkout).
- Checkout bằng VNPay: đi hết luồng redirect → return → verify HMAC → chuyển trạng thái đúng
  (`LUU_TAM` → `CHO_XAC_NHAN`), rồi mới trừ tồn kho/voucher/cart. Test HMAC sai → phải reject, không được
  tạo đơn "coi như thành công".
- **Test cụ thể tính server-authoritative**: gửi `tongTien/phiShip/giamGia/tongCong` sai lệch từ FE giả lập
  (client tự sửa số) → xác nhận backend có tự tính lại từ catalog/promotion/shipping hay chấp nhận số client
  gửi. Nếu chấp nhận số client gửi (lỗ hổng), đây là lỗi phải fix: tổng tiền/subtotal từng `order_seller`
  phải được server tính lại từ dữ liệu catalog/voucher/shipping thật, không tin theo request.
- Test voucher shop A áp cho đơn hàng chỉ có sản phẩm shop B → phải bị từ chối, không được giảm giá sai seller.
- Test gián đoạn giữa chừng (giả lập catalog/promotion tạm lỗi khi đang trừ stock/giảm voucher) → xác nhận có
  bị lệch state (order tạo rồi nhưng stock/voucher không trừ, hoặc ngược lại) hay không; nếu có, đánh dấu rõ
  và đề xuất/áp dụng cơ chế idempotency hoặc bù trừ tối thiểu cho lần chạy này.
- Test gọi thẳng `/api/orders/**` không kèm JWT buyer hợp lệ (vì đây là contract legacy nằm ngoài prefix
  buyer) → xác nhận có bị giả mạo `customerId` qua body hay không; nếu có, đây là lỗ hổng bắt buộc phải vá.

### 3.7. Seller xử lý đơn & payout
- Đi hết state machine `order_seller`: `CHO_XAC_NHAN → DA_XAC_NHAN → CHO_GIAO → DANG_GIAO → HOAN_THANH`.
- Khi `HOAN_THANH`: xác nhận `seller_receivable` tạo đúng, commission áp đúng theo category (hoặc default 5%),
  `seller_wallet.pending_amount` cộng đúng.
- Test hủy đơn ở từng bước hợp lệ/không hợp lệ theo state machine.
- Test buyer hủy đơn khi không phải tất cả sub-order còn `CHO_XAC_NHAN` → phải bị chặn.
- Test aggregate trạng thái `orders` (root) so với các `order_seller` con — xác nhận buyer nhìn thấy root
  status có phản ánh đúng thực tế các sub-order hay bị lệch (rủi ro đã biết ở mục 11.3); nếu lệch, bổ sung
  logic cập nhật root status theo sub-order.
- Chạy scheduler chuyển receivable `PENDING → AVAILABLE` sau hold period, rồi tạo payout batch → xác nhận
  ví seller giảm `available`, tăng `paid` đúng.

### 3.8. Voucher, Campaign, Flash Sale
- Voucher sàn (admin) và voucher shop (seller) — test seller không được sửa/xóa voucher sàn.
- Campaign seller chỉ được chọn variant thuộc seller đó.
- Flash Sale: seller đăng ký → admin approve/reject → seller withdraw trước/sau khi Flash Sale bắt đầu
  (sau khi bắt đầu phải không rút được nếu đã approved) → public API chỉ trả sản phẩm approved.

### 3.9. Follow / Review / Chat
- Follow/unfollow shop approved, test unique constraint theo customer+shop.
- Review: chỉ được review variant đã mua trong sub-order `HOAN_THANH`, không review trùng lần 2.
- **Test cụ thể**: tạo review có gọi tới `GET /internal/catalog/product-details/{id}` và
  `POST /internal/catalog/products/{id}/rating` hay không — đây là điểm nghi ngờ lệch contract giữa
  seller-service và catalog-service (mục 11.5). Nếu 2 endpoint internal này không tồn tại/không khớp,
  bổ sung đúng contract ở `InternalCatalogController` để review + đồng bộ rating hoạt động thật, không chỉ
  ghi được review mà rating sản phẩm không cập nhật.
- Chat: gửi/nhận tin nhắn buyer–seller, mark read, xác nhận unread count đúng.

### 3.10. Dispute & Report
- Buyer tạo dispute trên sub-order `HOAN_THANH` → seller phản hồi → admin resolve với từng loại quyết định
  (refund toàn phần / một phần / từ chối).
- Với mỗi loại resolve, xác nhận `payout_adjustment` tạo đúng và trạng thái ví/receivable điều chỉnh đúng
  theo receivable đang ở `PENDING/AVAILABLE/PAID`.
- Report: tạo report cho từng loại target, admin resolve với từng action, xác nhận action thực sự có hiệu
  lực (ví dụ `PRODUCT_DELISTED` phải thực sự làm sản phẩm hết hiển thị public).

### 3.11. Admin vận hành chung
- Quản lý customer/staff, category, product attribute moderation, banner, thống kê — đi hết CRUD thật,
  không chỉ gọi GET.

## 4. Danh sách rủi ro đã biết — bắt buộc verify cụ thể và fix nếu còn tồn tại

Đây là các điểm tài liệu as-is đã ghi nhận là rủi ro/giới hạn tại thời điểm chốt. Agent phải tự kiểm chứng
lại trên source hiện tại (có thể đã đổi khác) và xử lý nếu vẫn còn:

1. Checkout chưa server-authoritative hoàn toàn (tổng tiền, phân bổ ship/discount xuống từng `order_seller`,
   voucher không được xác minh đúng seller).
2. Không có saga/compensation cho checkout xuyên service (stock, voucher, cart).
3. Root order và sub-order có thể lệch trạng thái (thiếu hàm aggregate).
4. `POST /api/v1/auth/refresh` không tồn tại dù FE có gọi.
5. Contract internal review–catalog (`/internal/catalog/product-details/{id}`,
   `/internal/catalog/products/{id}/rating`) bị thiếu/lệch.
6. Security dựa hoàn toàn vào gateway/network boundary: downstream service `permitAll()`, không có
   mTLS/service token; gateway discovery locator có thể lộ `/{service-id}/internal/**`.
7. Seller bị suspend nhưng token cũ (JWT) vẫn dùng được tới khi hết hạn — chưa có revocation/live-check.
8. `POST /api/v1/notifications/email` bị lộ công khai qua gateway (không nằm trong prefix role-protected).
9. Search pipeline (outbox → Kafka → Elasticsearch) không phải read path chính; xác nhận public API không
   phụ thuộc và không bị gãy khi Kafka Connect down.
10. Các vết legacy: xóa cart item dùng PUT thay vì DELETE, field trùng tên Việt/Anh, `/permitall/profile`
    nhận ID qua URL, OAuth2 route khai báo nhưng chưa có handler hoàn thiện, `.env.stage` trỏ sai port.

Với mỗi mục trên: (a) xác nhận còn tồn tại hay đã được fix từ trước, (b) nếu còn tồn tại và có rủi ro bảo
mật/toàn vẹn dữ liệu thật (đặc biệt mục 1, 2, 3, 5, 6, 7, 8) — bắt buộc fix trong lần chạy này, không chỉ
ghi nhận. Các mục thuần UX/naming legacy (mục 10) có thể chỉ ghi nhận nếu không ảnh hưởng tới đúng/sai
nghiệp vụ, trừ khi việc sửa đơn giản và an toàn.

## 5. Quy trình lặp

1. Chạy lần lượt từng luồng ở mục 3 theo đúng thứ tự (luồng sau phụ thuộc dữ liệu luồng trước).
2. Mỗi lần phát hiện lỗi: dừng, xác định root cause, fix, viết lại 1 dòng log (luồng nào, lỗi gì, sửa ở
   file nào, hàm nào).
3. Sau khi fix, chạy lại **toàn bộ luồng bị ảnh hưởng** (không chỉ bước lỗi) để đảm bảo không phá state.
4. Lặp lại toàn bộ mục 3 và mục 4 từ đầu tới khi một lượt chạy full không phát sinh lỗi mới nào.
5. Không được dừng ở lượt chạy đầu tiên "có vẻ ổn" — phải chạy tối thiểu 2 lượt liên tiếp sạch lỗi để coi
   là đạt.

## 6. Tiêu chí "Done"

Hệ thống được coi là đạt khi:

- Toàn bộ luồng ở mục 3 chạy hết từ đầu tới cuối, đúng state DB kỳ vọng, không có lỗi 5xx/logic sai lệch.
- Toàn bộ 10 điểm rủi ro ở mục 4 đã được xác nhận trạng thái rõ ràng, và các điểm ảnh hưởng tiền/bảo mật/
  toàn vẹn dữ liệu đã được fix thật trong source.
- Không còn service nào down do lỗi phát sinh trong lúc test.
- Có báo cáo cuối cùng liệt kê: luồng đã test, lỗi tìm thấy, cách fix, file đã đổi, và các điểm còn tồn đọng
  (nếu có) kèm lý do vì sao chưa fix được trong phạm vi lần chạy này.

## 7. Định dạng báo cáo cuối cùng yêu cầu

Trả về theo cấu trúc:

```
## Tóm tắt
- Số luồng test / số luồng pass / số lỗi tìm thấy / số lỗi đã fix

## Chi tiết theo luồng (3.1 -> 3.11)
- Luồng: ...
- Kết quả trước fix: ...
- Lỗi phát hiện (nếu có): ...
- File/hàm đã sửa: ...
- Kết quả sau fix (re-run): ...

## Rủi ro đã biết (mục 4) — trạng thái sau kiểm chứng
- Từng mục 1-10: còn tồn tại / đã fix trong lần này / không áp dụng nữa, kèm bằng chứng (request/response,
  dòng code, hoặc log)

## Còn tồn đọng chưa fix được
- Liệt kê rõ, kèm lý do và đề xuất hướng xử lý tiếp theo
```