# Manual catalog migrations

- `p1_product_domain_reset.sql` là schema reset/cutover chuẩn cho domain Sản phẩm mới. Script phá hủy dữ liệu catalog cũ và chỉ được chạy trên database dev/tạm đã chọn rõ.
- `p1_product_domain_verify.sql` kiểm tra bảng/cột/index/FK sau khi chạy schema P1.
- `p1_product_domain_seed.sql` seed mới đa ngành, fresh-only, dùng sau schema P1.
- `p1_product_domain_seed_verify.sql` kiểm tra invariant/count/consumer/outbox của seed P1.
- `m10_dynamic_attributes_up.sql` và `m10_dynamic_attributes_down.sql` là migration thử nghiệm của model half-migrated cũ. Không dùng hai script M10 trong runbook chuẩn hóa sản phẩm mới vì chúng giữ lại sáu bảng hard-code và `product_attribute_value_option`.
