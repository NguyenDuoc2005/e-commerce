-- Read-only audit for deprecating the six hard-coded shoe tables.
-- This deliberately emits evidence and a verdict only; it never drops a table/column.

SET NAMES utf8mb4;

SELECT t.table_name, t.table_rows AS estimated_rows, t.engine
FROM information_schema.tables t
WHERE t.table_schema = DATABASE()
  AND t.table_name IN ('brand','color','material','origin','size','sole_type')
ORDER BY t.table_name;

SELECT k.table_name, k.column_name, k.constraint_name,
       k.referenced_table_name, k.referenced_column_name
FROM information_schema.key_column_usage k
WHERE k.table_schema = DATABASE()
  AND k.referenced_table_name IN ('brand','color','material','origin','size','sole_type')
ORDER BY k.referenced_table_name, k.table_name, k.column_name;

SELECT c.table_name, c.column_name, c.column_type, c.is_nullable
FROM information_schema.columns c
WHERE c.table_schema = DATABASE()
  AND (
    (c.table_name = 'product' AND c.column_name IN ('brand_id','origin_id','material_id','sole_type_id'))
    OR (c.table_name = 'product_variant' AND c.column_name IN ('color_id','size_id'))
  )
ORDER BY c.table_name, c.ordinal_position;

SELECT 'view' AS object_type, v.table_name AS object_name
FROM information_schema.views v
WHERE v.table_schema = DATABASE()
  AND LOWER(v.view_definition) REGEXP '(^|[^a-z_])(brand|color|material|origin|size|sole_type)([^a-z_]|$)'
UNION ALL
SELECT 'trigger', t.trigger_name
FROM information_schema.triggers t
WHERE t.trigger_schema = DATABASE()
  AND LOWER(t.action_statement) REGEXP '(^|[^a-z_])(brand|color|material|origin|size|sole_type)([^a-z_]|$)'
UNION ALL
SELECT 'routine', r.routine_name
FROM information_schema.routines r
WHERE r.routine_schema = DATABASE()
  AND LOWER(COALESCE(r.routine_definition,'')) REGEXP '(^|[^a-z_])(brand|color|material|origin|size|sole_type)([^a-z_]|$)';

SELECT 'deprecation_gate' AS check_name,
       CASE
         WHEN legacy_tables = 0 AND legacy_columns = 0 AND inbound_fks = 0
           THEN 'ALREADY_ABSENT_NOTHING_TO_DROP'
         ELSE 'REVIEW_REQUIRED_REQUEST_EXPLICIT_APPROVAL_BEFORE_DROP'
       END AS verdict,
       legacy_tables, legacy_columns, inbound_fks
FROM (
  SELECT
    (SELECT COUNT(*) FROM information_schema.tables
     WHERE table_schema = DATABASE()
       AND table_name IN ('brand','color','material','origin','size','sole_type')) AS legacy_tables,
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND ((table_name = 'product' AND column_name IN ('brand_id','origin_id','material_id','sole_type_id'))
         OR (table_name = 'product_variant' AND column_name IN ('color_id','size_id')))) AS legacy_columns,
    (SELECT COUNT(*) FROM information_schema.key_column_usage
     WHERE table_schema = DATABASE()
       AND referenced_table_name IN ('brand','color','material','origin','size','sole_type')) AS inbound_fks
) evidence;
