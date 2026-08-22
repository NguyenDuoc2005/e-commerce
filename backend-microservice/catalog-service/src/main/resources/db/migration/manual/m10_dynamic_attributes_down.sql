USE `ecommerce_catalog`;

SET @m10_version = 'M10_20260822_1';
SET @m10_now = UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000;

UPDATE `product` p
JOIN `product_m10_backup` b
  ON b.product_id = p.id AND b.migration_version = @m10_version
SET p.brand_id = b.brand_id,
    p.origin_id = b.origin_id,
    p.category_id = b.category_id,
    p.sole_type_id = b.sole_type_id,
    p.material_id = b.material_id,
    p.last_modified_date = @m10_now;

INSERT INTO `outbox` (`id`, `aggregate_type`, `aggregate_id`, `event_type`, `payload`, `created_at`)
SELECT UUID(), 'Product', p.id, 'ProductUpdated',
       JSON_OBJECT(
         'id', p.id,
         'name', p.name,
         'description', p.description,
         'sellerId', p.seller_id,
         'categoryId', p.category_id,
         'category', c.name,
         'price', (SELECT MIN(v.sale_price) FROM product_variant v WHERE v.product_id = p.id AND v.status = 0),
         'brandId', p.brand_id,
         'brand', b.name,
         'imageUrl', (SELECT v.image_url FROM product_variant v WHERE v.product_id = p.id AND v.status = 0 ORDER BY v.created_date LIMIT 1),
         'attributes', JSON_ARRAY()
       ), CURRENT_TIMESTAMP(6)
FROM product p
LEFT JOIN category c ON c.id = p.category_id
LEFT JOIN brand b ON b.id = p.brand_id;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `product_attribute_value_option`;
DROP TABLE IF EXISTS `product_attribute_value`;
DROP TABLE IF EXISTS `category_attribute_suggestion`;
DROP TABLE IF EXISTS `product_attribute_option`;
DROP TABLE IF EXISTS `product_attribute_definition`;
SET FOREIGN_KEY_CHECKS = 1;

UPDATE `m10_migration_manifest`
SET `rolled_back_at` = CURRENT_TIMESTAMP(6)
WHERE `migration_version` = @m10_version;

SELECT m.product_count AS expected_products,
       COUNT(p.id) AS restored_products,
       COALESCE(SUM(CRC32(CONCAT_WS('|', p.id, p.brand_id, p.origin_id, p.category_id, p.sole_type_id, p.material_id))), 0) AS restored_checksum,
       m.legacy_checksum AS expected_checksum
FROM m10_migration_manifest m
JOIN product_m10_backup b ON b.migration_version = m.migration_version
JOIN product p ON p.id = b.product_id
WHERE m.migration_version = @m10_version
GROUP BY m.product_count, m.legacy_checksum;
