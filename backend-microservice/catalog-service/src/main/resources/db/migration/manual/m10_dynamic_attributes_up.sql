USE `ecommerce_catalog`;

SET @m10_version = 'M10_20260822_1';
SET @m10_now = UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000;

CREATE TABLE IF NOT EXISTS `m10_migration_manifest` (
  `migration_version` varchar(50) NOT NULL,
  `executed_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `rolled_back_at` timestamp(6) NULL,
  `product_count` bigint NOT NULL,
  `legacy_checksum` bigint unsigned NOT NULL,
  PRIMARY KEY (`migration_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_m10_backup` (
  `migration_version` varchar(50) NOT NULL,
  `product_id` varchar(36) NOT NULL,
  `brand_id` varchar(36) NULL,
  `origin_id` varchar(36) NULL,
  `category_id` varchar(36) NULL,
  `sole_type_id` varchar(36) NULL,
  `material_id` varchar(36) NULL,
  `backed_up_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`migration_version`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `product_m10_backup`
  (`migration_version`, `product_id`, `brand_id`, `origin_id`, `category_id`, `sole_type_id`, `material_id`)
SELECT @m10_version, `id`, `brand_id`, `origin_id`, `category_id`, `sole_type_id`, `material_id`
FROM `product`;

INSERT INTO `m10_migration_manifest` (`migration_version`, `product_count`, `legacy_checksum`)
SELECT @m10_version,
       COUNT(*),
       COALESCE(SUM(CRC32(CONCAT_WS('|', `id`, `brand_id`, `origin_id`, `category_id`, `sole_type_id`, `material_id`))), 0)
FROM `product`
ON DUPLICATE KEY UPDATE
  `product_count` = VALUES(`product_count`),
  `legacy_checksum` = VALUES(`legacy_checksum`);

CREATE TABLE IF NOT EXISTS `product_attribute_definition` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint NULL,
  `last_modified_date` bigint NULL,
  `code` varchar(100) NOT NULL,
  `name` varchar(255) NOT NULL,
  `normalized_name` varchar(255) NOT NULL,
  `data_type` varchar(30) NOT NULL,
  `creator_seller_id` varchar(36) NULL,
  `normalization_status` varchar(30) NOT NULL,
  `merged_into_attribute_id` varchar(36) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attribute_code` (`code`),
  KEY `idx_attribute_normalized_name` (`normalized_name`),
  KEY `idx_attribute_creator_status` (`creator_seller_id`, `normalization_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_option` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint NULL,
  `last_modified_date` bigint NULL,
  `attribute_id` varchar(36) NOT NULL,
  `value` varchar(500) NOT NULL,
  `normalized_value` varchar(500) NOT NULL,
  `creator_seller_id` varchar(36) NULL,
  `merged_into_option_id` varchar(36) NULL,
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attribute_option_normalized` (`attribute_id`, `normalized_value`),
  CONSTRAINT `fk_attribute_option_definition` FOREIGN KEY (`attribute_id`) REFERENCES `product_attribute_definition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `category_attribute_suggestion` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint NULL,
  `last_modified_date` bigint NULL,
  `category_id` varchar(36) NOT NULL,
  `attribute_id` varchar(36) NOT NULL,
  `default_suggestion` bit NOT NULL DEFAULT b'1',
  `filterable` bit NOT NULL DEFAULT b'0',
  `required_value` bit NOT NULL DEFAULT b'0',
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_attribute` (`category_id`, `attribute_id`),
  CONSTRAINT `fk_category_attribute_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `fk_category_attribute_definition` FOREIGN KEY (`attribute_id`) REFERENCES `product_attribute_definition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_value` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint NULL,
  `last_modified_date` bigint NULL,
  `product_id` varchar(36) NOT NULL,
  `attribute_id` varchar(36) NOT NULL,
  `text_value` text NULL,
  `number_value` decimal(19,4) NULL,
  `unit` varchar(50) NULL,
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_attribute_value` (`product_id`, `attribute_id`),
  CONSTRAINT `fk_product_attribute_value_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_product_attribute_value_definition` FOREIGN KEY (`attribute_id`) REFERENCES `product_attribute_definition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_value_option` (
  `product_attribute_value_id` varchar(36) NOT NULL,
  `option_id` varchar(36) NOT NULL,
  PRIMARY KEY (`product_attribute_value_id`, `option_id`),
  CONSTRAINT `fk_product_value_option_value` FOREIGN KEY (`product_attribute_value_id`) REFERENCES `product_attribute_value` (`id`),
  CONSTRAINT `fk_product_value_option_option` FOREIGN KEY (`option_id`) REFERENCES `product_attribute_option` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_moderation_audit` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint NULL,
  `last_modified_date` bigint NULL,
  `action` varchar(30) NOT NULL,
  `actor_user_id` varchar(36) NULL,
  `source_attribute_id` varchar(36) NOT NULL,
  `target_attribute_id` varchar(36) NULL,
  `reason` varchar(1000) NULL,
  `affected_product_count` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_attribute_audit_source` (`source_attribute_id`, `created_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `category` (`id`, `status`, `created_date`, `last_modified_date`, `code`, `name`)
VALUES ('32000000-0000-0000-0000-000000000010', 0, @m10_now, @m10_now, 'DM-GIAY', 'Giay');

INSERT IGNORE INTO `product_attribute_definition`
  (`id`, `status`, `created_date`, `last_modified_date`, `code`, `name`, `normalized_name`, `data_type`, `creator_seller_id`, `normalization_status`)
VALUES
  ('41000000-0000-0000-0000-000000000001', 0, @m10_now, @m10_now, 'ATTR-BRAND', 'Thuong hieu', 'thuong hieu', 'TEXT', NULL, 'STANDARDIZED'),
  ('41000000-0000-0000-0000-000000000002', 0, @m10_now, @m10_now, 'ATTR-ORIGIN', 'Xuat xu', 'xuat xu', 'TEXT', NULL, 'STANDARDIZED'),
  ('41000000-0000-0000-0000-000000000003', 0, @m10_now, @m10_now, 'ATTR-MATERIAL', 'Chat lieu', 'chat lieu', 'TEXT', NULL, 'STANDARDIZED'),
  ('41000000-0000-0000-0000-000000000004', 0, @m10_now, @m10_now, 'ATTR-SOLE', 'Loai de', 'loai de', 'TEXT', NULL, 'STANDARDIZED'),
  ('41000000-0000-0000-0000-000000000005', 0, @m10_now, @m10_now, 'ATTR-SHOE-LINE', 'Dong giay', 'dong giay', 'TEXT', NULL, 'STANDARDIZED');

INSERT IGNORE INTO `category_attribute_suggestion`
  (`id`, `status`, `created_date`, `last_modified_date`, `category_id`, `attribute_id`, `default_suggestion`, `filterable`, `required_value`, `display_order`)
VALUES
  ('41100000-0000-0000-0000-000000000001', 0, @m10_now, @m10_now, '32000000-0000-0000-0000-000000000010', '41000000-0000-0000-0000-000000000001', b'1', b'1', b'0', 0),
  ('41100000-0000-0000-0000-000000000002', 0, @m10_now, @m10_now, '32000000-0000-0000-0000-000000000010', '41000000-0000-0000-0000-000000000002', b'1', b'1', b'0', 1),
  ('41100000-0000-0000-0000-000000000003', 0, @m10_now, @m10_now, '32000000-0000-0000-0000-000000000010', '41000000-0000-0000-0000-000000000003', b'1', b'1', b'0', 2),
  ('41100000-0000-0000-0000-000000000004', 0, @m10_now, @m10_now, '32000000-0000-0000-0000-000000000010', '41000000-0000-0000-0000-000000000004', b'1', b'1', b'0', 3),
  ('41100000-0000-0000-0000-000000000005', 0, @m10_now, @m10_now, '32000000-0000-0000-0000-000000000010', '41000000-0000-0000-0000-000000000005', b'1', b'1', b'0', 4);

INSERT IGNORE INTO `product_attribute_value`
  (`id`, `status`, `created_date`, `last_modified_date`, `product_id`, `attribute_id`, `text_value`, `display_order`)
SELECT UUID(), 0, @m10_now, @m10_now, p.id, '41000000-0000-0000-0000-000000000001', b.name, 0
FROM `product` p JOIN `brand` b ON b.id = p.brand_id WHERE b.name IS NOT NULL;

INSERT IGNORE INTO `product_attribute_value`
  (`id`, `status`, `created_date`, `last_modified_date`, `product_id`, `attribute_id`, `text_value`, `display_order`)
SELECT UUID(), 0, @m10_now, @m10_now, p.id, '41000000-0000-0000-0000-000000000002', o.name, 1
FROM `product` p JOIN `origin` o ON o.id = p.origin_id WHERE o.name IS NOT NULL;

INSERT IGNORE INTO `product_attribute_value`
  (`id`, `status`, `created_date`, `last_modified_date`, `product_id`, `attribute_id`, `text_value`, `display_order`)
SELECT UUID(), 0, @m10_now, @m10_now, p.id, '41000000-0000-0000-0000-000000000003', m.name, 2
FROM `product` p JOIN `material` m ON m.id = p.material_id WHERE m.name IS NOT NULL;

INSERT IGNORE INTO `product_attribute_value`
  (`id`, `status`, `created_date`, `last_modified_date`, `product_id`, `attribute_id`, `text_value`, `display_order`)
SELECT UUID(), 0, @m10_now, @m10_now, p.id, '41000000-0000-0000-0000-000000000004', s.name, 3
FROM `product` p JOIN `sole_type` s ON s.id = p.sole_type_id WHERE s.name IS NOT NULL;

INSERT IGNORE INTO `product_attribute_value`
  (`id`, `status`, `created_date`, `last_modified_date`, `product_id`, `attribute_id`, `text_value`, `display_order`)
SELECT UUID(), 0, @m10_now, @m10_now, p.id, '41000000-0000-0000-0000-000000000005', c.name, 4
FROM `product` p JOIN `category` c ON c.id = p.category_id WHERE c.name IS NOT NULL;

UPDATE `product`
SET `category_id` = '32000000-0000-0000-0000-000000000010',
    `last_modified_date` = @m10_now;

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
         'imageUrl', (SELECT v.image_url FROM product_variant v WHERE v.product_id = p.id AND v.status = 0 ORDER BY v.created_date LIMIT 1),
         'attributes', COALESCE((
           SELECT JSON_ARRAYAGG(JSON_OBJECT(
             'attributeId', av.attribute_id,
             'name', ad.name,
             'dataType', ad.data_type,
             'textValues', JSON_ARRAY(av.text_value),
             'keywordValues', JSON_ARRAY(LOWER(av.text_value)),
             'numberValue', av.number_value,
             'optionIds', JSON_ARRAY()
           ))
           FROM product_attribute_value av
           JOIN product_attribute_definition ad ON ad.id = av.attribute_id
           WHERE av.product_id = p.id
         ), JSON_ARRAY())
       ), CURRENT_TIMESTAMP(6)
FROM product p
LEFT JOIN category c ON c.id = p.category_id;

SELECT m.product_count AS expected_products,
       (SELECT COUNT(*) FROM product_m10_backup b WHERE b.migration_version = @m10_version) AS backed_up_products,
       (SELECT COUNT(DISTINCT product_id) FROM product_attribute_value) AS migrated_products
FROM m10_migration_manifest m
WHERE m.migration_version = @m10_version;
