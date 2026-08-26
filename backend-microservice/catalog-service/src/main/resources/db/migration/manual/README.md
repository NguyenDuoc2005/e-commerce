# Manual catalog migrations

Quyết định hiện hành là **migration an toàn, không reset database**. Runbook chuẩn:

1. Backup schema/data theo quy trình của môi trường.
2. Chạy `p1_product_domain_preflight.sql` (read-only). Chỉ đi tiếp khi schema đúng là
   `ecommerce_catalog` và verdict là `CANONICAL_READY`, hoặc review riêng mọi sai khác.
3. `p1_product_domain_reset.sql` giữ tên cũ để tương thích runbook, nhưng đã được harden
   thành schema additive: chỉ `CREATE TABLE IF NOT EXISTS`, không `DROP`, không tắt FK và
   không ghi đè bảng/dữ liệu hiện có.
4. Chạy `p1_product_domain_verify.sql`; mọi actual phải bằng expected.
5. Chỉ trên dev/staging cần demo, chạy `p1_product_domain_seed.sql`. Seed có dữ liệu giày,
   điện thoại/phụ kiện và mỹ phẩm; dùng `INSERT IGNORE`, chạy lại không ghi đè dữ liệu.
6. Chạy `p1_product_domain_seed_verify.sql`.

`p1_legacy_product_deprecation_audit.sql` chỉ audit sáu bảng hard-code
`brand/color/material/origin/size/sole_type`, cột/FK và dependency SQL. Script không xóa gì.
Nếu verdict yêu cầu review, phải đối soát dữ liệu lịch sử và xin xác nhận rõ trước khi viết/chạy
DDL xóa vật lý. Nếu verdict là `ALREADY_ABSENT_NOTHING_TO_DROP`, không có thao tác delete.

`m10_dynamic_attributes_up.sql` và `m10_dynamic_attributes_down.sql` là migration thử nghiệm
của model half-migrated cũ, chỉ giữ để truy vết lịch sử và không thuộc runbook hiện hành.
