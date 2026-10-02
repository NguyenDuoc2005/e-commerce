# Kế hoạch chuyển Storefront Search sang Elasticsearch

## Bối cảnh dự án

- Backend Spring Boot microservice nằm trong `backend-microservice`.
- Frontend Vue 3 nằm trong `FE`.
- `catalog-service` sở hữu dữ liệu product, variant, attribute và stock.
- MySQL vẫn là source of truth.
- Catalog hiện đã ghi transactional outbox.
- Debezium + Kafka Connect đang đồng bộ product document sang Elasticsearch.
- Elasticsearch sử dụng index/alias `products`.
- API public product hiện vẫn đọc MySQL và filter/sort/page trong application memory.
- Mục tiêu là thêm search API đọc Elasticsearch, chưa thay thế endpoint MySQL cũ.
- Chi tiết sản phẩm, giỏ hàng và checkout vẫn phải xác nhận giá/tồn kho từ MySQL — Elasticsearch KHÔNG BAO GIỜ được dùng làm nguồn giá/tồn kho để tính tiền.

Thứ tự thực hiện bắt buộc: **Phase 1 (backend MVP) → test bằng Postman/cURL → Phase 2 (nối FE) → Phase 3 (autocomplete/facet/reindex nâng cao)**. Không được nhảy cóc làm autocomplete/tiếng Việt/facet trước khi Phase 1 đã chứng minh storefront đọc đúng dữ liệu từ Elasticsearch.

Mỗi phase chỉ thực hiện khi phase trước đã báo cáo hoàn thành kèm test/build pass. Nếu đang thực hiện Phase X, không tự ý làm thêm việc của Phase X+1.

---

## Phase 1: Backend Elasticsearch Search MVP

Hãy phân tích và triển khai bước đầu tiên để đưa Elasticsearch thành read path cho tính năng tìm kiếm sản phẩm storefront trong dự án này.

### Trước khi sửa code

1. Đọc `HE_THONG_HIEN_TAI_MARKETPLACE.md`, `ARCHITECTURE.md`.
2. Kiểm tra source thật của `catalog-service`.
3. Xác định:
    - Cấu trúc payload đang được ghi vào catalog outbox.
    - Mapping/template thực tế của index `products` (không đoán, phải lấy từ cluster hoặc file định nghĩa mapping trong repo).
    - Document Elasticsearch hiện có những field nào, field nào là nested object.
    - Spring Data Elasticsearch đang được cấu hình và sử dụng ở đâu.
    - API product public hiện tại trả response format gì.
    - Field nào trong ES document có khả năng bị lệch/trễ so với MySQL do độ trễ của CDC (ví dụ tồn kho, trạng thái active/inactive) — liệt kê rõ trong báo cáo cuối phase.
4. Kiểm tra git status và không ghi đè những thay đổi không liên quan.

### Triển khai API mới

`GET /api/v1/permitall/products/search`

API MVP hỗ trợ:

- `q`: tìm theo tên sản phẩm, mô tả, category hoặc tên shop nếu document có field tương ứng.
- `categoryId`.
- `sellerId`.
- `minPrice`.
- `maxPrice`.
- `page`, mặc định 0.
- `size`, mặc định 20 và giới hạn tối đa hợp lý (ví dụ 100).
- `sort`:
    - `relevance`
    - `newest`
    - `price_asc`
    - `price_desc`
    - `rating_desc`

### Yêu cầu kỹ thuật

- Query trực tiếp Elasticsearch, không tải toàn bộ document rồi filter bằng Java.
- Dùng bool query với full-text query và filter phù hợp (filter context cho các điều kiện chính xác như categoryId, sellerId, price range để tận dụng cache).
- Nếu variant hoặc attribute là nested object trong mapping thì phải dùng nested query đúng với mapping hiện tại — không được query phẳng lên field nested vì sẽ ra sai kết quả một cách âm thầm.
- Chỉ trả sản phẩm public/active; không để lộ sản phẩm inactive, bị delist hoặc seller không hợp lệ nếu document có đủ dữ liệu để kiểm tra.
- Khi có `q`, mặc định sort theo relevance.
- Khi không có `q`, dùng sort ổn định và có tie-breaker phụ (ví dụ thêm `_id` hoặc `created_at`) để kết quả không nhảy giữa các trang.
- Ghi chú rõ trong code/docs giới hạn `from + size` mặc định của Elasticsearch (thường là 10000): API MVP này chưa cần xử lý phân trang sâu vượt giới hạn đó, nhưng phải throw lỗi rõ ràng (không phải lỗi ES thô) nếu client truyền page/size vượt ngưỡng, và ghi chú đây là việc cần làm ở Phase 3 nếu dùng `search_after`.
- Response phải có:
    - `items`
    - `page`
    - `size`
    - `totalElements`
    - `totalPages`
- Ưu tiên tái sử dụng DTO storefront hiện tại nếu phù hợp; không trả trực tiếp Elasticsearch entity hoặc internal field.
- Validate parameter và trả lỗi 400 rõ ràng cho giá/page/size không hợp lệ.
- Khi Elasticsearch unavailable, API search mới trả lỗi 503 có message rõ ràng.
- **Không fallback âm thầm về MySQL trong phase này.**
- Giữ nguyên API MySQL hiện tại để rollback và so sánh.
- Không sửa luồng tạo/cập nhật sản phẩm.
- Không chuyển product detail, cart hoặc checkout sang đọc Elasticsearch.
- Không tạo microservice mới.

### Testing

Viết unit test cho query/filter/sort builder, bao gồm các trường hợp:

1. Search chỉ có từ khóa.
2. Search có price range.
3. Filter category và seller.
4. Sort giá tăng/giảm.
5. Page/size không hợp lệ.
6. Elasticsearch unavailable.

- Nếu repo đã có convention integration test phù hợp thì thêm integration test; không dựng framework test hoàn toàn mới nếu chưa cần thiết.
- Chạy test và build của module bị ảnh hưởng.

### Tài liệu

- Thêm ví dụ request/response.
- Ghi rõ API nào đọc Elasticsearch và API nào vẫn đọc MySQL.
- Cập nhật `HE_THONG_HIEN_TAI_MARKETPLACE.md` sau khi implementation đã chạy được.
- Không ghi rằng tính năng hoàn thành nếu test/build chưa đạt.

### Báo cáo khi hoàn thành

1. Những file đã thay đổi.
2. Mapping/document thực tế đã tìm thấy.
3. Query Elasticsearch đã triển khai.
4. Test/build đã chạy và kết quả.
5. Field nào có nguy cơ lệch dữ liệu giữa MySQL và ES do độ trễ CDC.
6. Những giới hạn còn lại cho phase tiếp theo.

---

## Phase 2: Nối frontend

**Chỉ thực hiện phase này sau khi Phase 1 đã báo cáo hoàn thành và test/build pass.**

Hãy nối trang danh sách/tìm kiếm sản phẩm của Vue frontend với API Elasticsearch search mới:

`GET /api/v1/permitall/products/search`

### Yêu cầu

- Trước tiên kiểm tra router, product page, service API và query state hiện tại.
- Không xóa API MySQL cũ.
- Tạo API client/type TypeScript rõ ràng cho search response.
- Đồng bộ trạng thái search lên URL query parameters để refresh/back/forward vẫn hoạt động.
- Hỗ trợ: từ khóa, category, seller, khoảng giá, sort, pagination.
- Debounce ô tìm kiếm khoảng 300–500ms.
- Hủy request cũ hoặc tránh response cũ ghi đè response mới (race condition khi gõ nhanh).
- Có loading, empty state và error state.
- Khi search API trả 503, hiển thị thông báo tìm kiếm tạm thời không khả dụng.
- Không dùng dữ liệu Elasticsearch làm dữ liệu xác nhận khi add to cart hoặc checkout.
- Giữ nguyên product detail và checkout hiện tại.
- Chạy type-check/build frontend sau khi sửa.

### Báo cáo khi hoàn thành

1. File đã thay đổi.
2. Kết quả type-check/build.
3. Trường hợp nào đã test thủ công (search, filter, sort, pagination, lỗi 503).

---

## Phase 3: Search nâng cao

**Chỉ thực hiện phase này sau khi Phase 1 và 2 đã ổn định.**

### Trước khi triển khai

- Đánh giá mapping hiện tại và đề xuất index mới (ví dụ `products_v3`) nếu thay đổi analyzer/mapping không tương thích ngược. Không sửa mapping trực tiếp trên index đang phục vụ traffic.
- Xác định rõ kiểu facet sẽ làm: **single-select facet** (facet của 1 field tính trên tập đã lọc bởi các field khác, không tự lọc theo chính field đó) hay **multi-select facet** (phức tạp hơn, cho phép chọn nhiều giá trị cùng field). Nếu không chắc, mặc định dùng single-select và ghi rõ trong báo cáo.

### Nâng cấp

- Tìm kiếm tiếng Việt có dấu và không dấu.
- Autocomplete (dùng field riêng, ví dụ edge_ngram hoặc search_as_you_type, tách biệt khỏi field full-text chính để không ảnh hưởng relevance).
- Typo tolerance/fuzzy search có kiểm soát.
- Highlight từ khóa.
- Facet/aggregation cho category, shop, khoảng giá, màu và size — nêu rõ chi phí hiệu năng của aggregation trên tập dữ liệu lớn, đề xuất giới hạn hoặc cache nếu cần.
- Xử lý phân trang sâu vượt giới hạn `from+size` bằng `search_after`.
- Reindex version mới bằng alias, không sửa mapping trực tiếp trên index đang dùng.
- Script hoặc quy trình rebuild toàn bộ index từ MySQL.
- Test consistency giữa MySQL và Elasticsearch.
- Metrics cho latency, error rate và zero-result query.

### Báo cáo khi hoàn thành

1. Mapping mới (nếu có) và lý do đổi.
2. Kế hoạch/kết quả reindex.
3. Kiểu facet đã chọn và lý do.
4. Test consistency MySQL vs ES.
5. Giới hạn còn lại nếu có.