-- P1 schema verification. Run against the same explicitly selected temporary catalog database.

SELECT 'legacy_table_count' AS check_name, COUNT(*) AS actual_value
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('brand', 'color', 'material', 'origin', 'size', 'sole_type', 'product_attribute_value_option');

SELECT 'target_table_count' AS check_name, COUNT(*) AS actual_value
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'category', 'product', 'product_image', 'product_attribute_definition',
    'product_attribute_option', 'category_attribute_suggestion', 'product_attribute_value',
    'variant_axis_name_suggestion', 'product_variant_axis', 'product_variant_axis_value',
    'product_variant', 'product_variant_axis_value_mapping',
    'product_attribute_moderation_audit', 'outbox'
  );

SELECT 'required_column_count' AS check_name, COUNT(*) AS actual_value
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
    (table_name = 'product_attribute_definition' AND column_name = 'default_unit')
    OR (table_name = 'product_variant' AND column_name = 'combination_key' AND is_nullable = 'NO')
    OR (table_name = 'product_image' AND column_name IN ('product_id', 'url', 'display_order', 'status'))
  );

SELECT 'product_variant_unique_combination' AS check_name, COUNT(*) AS actual_value
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'product_variant'
  AND index_name = 'uk_product_variant_combination'
  AND non_unique = 0;

SELECT 'foreign_key_count' AS check_name, COUNT(*) AS actual_value
FROM information_schema.referential_constraints
WHERE constraint_schema = DATABASE();

