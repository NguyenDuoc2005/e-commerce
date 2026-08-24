-- P1 seed verification. Every *_violations result must be 0.
SELECT '01_legacy_table_count' check_name, COUNT(*) actual_value FROM information_schema.tables
WHERE table_schema=DATABASE() AND table_name IN ('brand','color','material','origin','size','sole_type','product_attribute_value_option');

SELECT '02_products_without_variant_violations' check_name, COUNT(*) actual_value
FROM product p LEFT JOIN product_variant v ON v.product_id=p.id WHERE v.id IS NULL;

SELECT '03_default_variant_invariant_violations' check_name, COUNT(*) actual_value FROM (
 SELECT p.id, COUNT(DISTINCT a.id) axes, COUNT(DISTINCT v.id) variants, SUM(v.is_default=b'1') defaults
 FROM product p LEFT JOIN product_variant_axis a ON a.product_id=p.id LEFT JOIN product_variant v ON v.product_id=p.id
 GROUP BY p.id HAVING (axes=0 AND (variants<>1 OR defaults<>1)) OR (axes>0 AND defaults<>0)
) violations;

SELECT '04_axis_limit_violations' check_name, COUNT(*) actual_value FROM
(SELECT product_id FROM product_variant_axis GROUP BY product_id HAVING COUNT(*)>2) violations;

SELECT '05_variant_mapping_violations' check_name, COUNT(*) actual_value FROM (
 SELECT v.id, COUNT(DISTINCT a.id) axes, COUNT(DISTINCT m.axis_value_id) mappings
 FROM product_variant v JOIN product p ON p.id=v.product_id
 LEFT JOIN product_variant_axis a ON a.product_id=p.id
 LEFT JOIN product_variant_axis_value_mapping m ON m.product_variant_id=v.id
 WHERE v.is_default=b'0' GROUP BY v.id HAVING axes<>mappings
) violations;

SELECT '06_shoe_variant_count' check_name, COUNT(*) actual_value FROM product_variant v
JOIN product p ON p.id=v.product_id WHERE p.code='PROD-SHOE-001';

SELECT '07_serum_default_and_mapping' check_name,
CONCAT('default=',SUM(v.is_default=b'1'),',mapping=',COUNT(m.axis_value_id)) actual_value
FROM product_variant v JOIN product p ON p.id=v.product_id
LEFT JOIN product_variant_axis_value_mapping m ON m.product_variant_id=v.id WHERE p.code='PROD-SERUM-001';

SELECT '08_shoe_filter_names' check_name, GROUP_CONCAT(name ORDER BY name SEPARATOR ', ') actual_value FROM (
 SELECT d.name FROM category_attribute_suggestion s JOIN product_attribute_definition d ON d.id=s.attribute_definition_id
 WHERE s.category_id='32000000-0000-0000-0000-000000000103' AND s.filterable=b'1' AND d.code IN ('ATTR-SOLE-TYPE','ATTR-HEEL-DROP')
 UNION SELECT name FROM product_variant_axis WHERE product_id='37000000-0000-0000-0000-000000000001'
) names;

SELECT '09_phone_filter_names' check_name, GROUP_CONCAT(name ORDER BY name SEPARATOR ', ') actual_value FROM (
 SELECT d.name FROM category_attribute_suggestion s JOIN product_attribute_definition d ON d.id=s.attribute_definition_id
 WHERE s.category_id='32000000-0000-0000-0000-000000000105' AND s.filterable=b'1' AND d.code IN ('ATTR-CHIP','ATTR-BATTERY')
 UNION SELECT name FROM product_variant_axis WHERE product_id='37000000-0000-0000-0000-000000000002'
) names;

SELECT '10_seed_counts' check_name, CONCAT('categories=',(SELECT COUNT(*) FROM category),
',definitions=',(SELECT COUNT(*) FROM product_attribute_definition),',options=',(SELECT COUNT(*) FROM product_attribute_option),
',products=',(SELECT COUNT(*) FROM product),',axes=',(SELECT COUNT(*) FROM product_variant_axis),
',axis_values=',(SELECT COUNT(*) FROM product_variant_axis_value),',variants=',(SELECT COUNT(*) FROM product_variant)) actual_value;

SELECT '11_consumer_variant_orphan_violations' check_name, COUNT(*) actual_value FROM (
 SELECT product_variant_id FROM ecommerce_cart.cart_detail UNION ALL
 SELECT product_variant_id FROM ecommerce_order.order_item UNION ALL
 SELECT product_variant_id FROM ecommerce_promotion.promotion_campaign_product
) c LEFT JOIN product_variant v ON v.id=c.product_variant_id WHERE c.product_variant_id IS NOT NULL AND v.id IS NULL;

SELECT '12_outbox_contract' check_name, CONCAT('count=',COUNT(*),',legacy_brand_fields=',
COALESCE(SUM(JSON_CONTAINS_PATH(payload,'one','$.brand','$.brandId')),0),',with_attributes=',
COALESCE(SUM(JSON_CONTAINS_PATH(payload,'one','$.attributes')),0),',with_variants=',
COALESCE(SUM(JSON_CONTAINS_PATH(payload,'one','$.variants')),0)) actual_value FROM outbox;
