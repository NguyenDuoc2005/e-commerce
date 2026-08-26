-- Read-only preflight for the product-domain canonical schema.
-- Run with ecommerce_catalog selected. This file never mutates schema or data.

SET NAMES utf8mb4;

SELECT DATABASE() AS selected_schema,
       CASE WHEN DATABASE() = 'ecommerce_catalog' THEN 'EXPECTED_SCHEMA' ELSE 'STOP_WRONG_SCHEMA' END AS verdict;

SELECT t.table_name,
       CASE
         WHEN t.table_name IN ('brand','color','material','origin','size','sole_type') THEN 'LEGACY_HARD_CODE'
         WHEN t.table_name = 'product_attribute_value_option' THEN 'LEGACY_HALF_MIGRATED'
         WHEN t.table_name IN (
           'category','product','product_image','product_attribute_definition',
           'product_attribute_option','category_attribute_suggestion','product_attribute_value',
           'variant_axis_name_suggestion','product_variant_axis','product_variant_axis_value',
           'product_variant','product_variant_axis_value_mapping',
           'product_attribute_moderation_audit','outbox'
         ) THEN 'CANONICAL'
         ELSE 'UNCLASSIFIED_REVIEW_REQUIRED'
       END AS classification,
       t.table_rows AS estimated_rows
FROM information_schema.tables t
WHERE t.table_schema = DATABASE()
ORDER BY classification, t.table_name;

SELECT c.table_name, c.column_name, c.column_type, c.is_nullable
FROM information_schema.columns c
WHERE c.table_schema = DATABASE()
  AND (
    (c.table_name = 'product' AND c.column_name IN ('brand_id','origin_id','material_id','sole_type_id'))
    OR (c.table_name = 'product_variant' AND c.column_name IN ('color_id','size_id','seller_id','code'))
    OR c.table_name = 'product_attribute_value_option'
  )
ORDER BY c.table_name, c.ordinal_position;

SELECT k.table_schema, k.table_name, k.column_name, k.constraint_name,
       k.referenced_table_schema, k.referenced_table_name, k.referenced_column_name
FROM information_schema.key_column_usage k
WHERE k.referenced_table_name IS NOT NULL
  AND (
    (k.table_schema = DATABASE()
      AND (k.table_name IN ('brand','color','material','origin','size','sole_type')
           OR k.referenced_table_name IN ('brand','color','material','origin','size','sole_type')))
    OR (k.referenced_table_schema = DATABASE() AND k.table_schema <> DATABASE())
  )
ORDER BY k.table_schema, k.table_name, k.column_name;

SELECT 'schema_state' AS check_name,
       CASE
         WHEN legacy.legacy_tables = 0 AND target.target_tables = 14 AND contract.required_columns = 6
           THEN 'CANONICAL_READY'
         WHEN legacy.legacy_tables > 0 THEN 'LEGACY_REVIEW_REQUIRED_NO_DROP'
         ELSE 'INCOMPLETE_REVIEW_REQUIRED'
       END AS verdict,
       legacy.legacy_tables,
       target.target_tables,
       contract.required_columns
FROM (
  SELECT COUNT(*) AS legacy_tables
  FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name IN ('brand','color','material','origin','size','sole_type','product_attribute_value_option')
) legacy
CROSS JOIN (
  SELECT COUNT(*) AS target_tables
  FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name IN (
      'category','product','product_image','product_attribute_definition',
      'product_attribute_option','category_attribute_suggestion','product_attribute_value',
      'variant_axis_name_suggestion','product_variant_axis','product_variant_axis_value',
      'product_variant','product_variant_axis_value_mapping',
      'product_attribute_moderation_audit','outbox'
    )
) target
CROSS JOIN (
  SELECT COUNT(*) AS required_columns
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND (
      (table_name = 'product_attribute_definition' AND column_name = 'default_unit')
      OR (table_name = 'product_variant' AND column_name = 'combination_key' AND is_nullable = 'NO')
      OR (table_name = 'product_image' AND column_name IN ('product_id','url','display_order','status'))
    )
) contract;
