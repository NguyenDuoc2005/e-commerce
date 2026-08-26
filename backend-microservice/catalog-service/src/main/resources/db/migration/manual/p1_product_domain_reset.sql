-- Product domain P1 target schema (additive compatibility entry point).
-- The historical file name is retained so old runbooks fail safe: this script no longer
-- drops tables or disables foreign-key checks. Existing tables/data are never overwritten.
-- Run p1_product_domain_preflight.sql first and p1_product_domain_verify.sql afterwards.
-- A half-migrated legacy table with the same name is intentionally left untouched and will
-- fail verification; it must be migrated with an environment-specific, reviewed ALTER plan.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `category` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `parent_id` varchar(36) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`code`),
  UNIQUE KEY `uk_category_slug` (`slug`),
  KEY `idx_category_parent_order` (`parent_id`, `display_order`),
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `seller_id` varchar(36) NOT NULL,
  `category_id` varchar(36) NOT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `rating_average` decimal(3,2) DEFAULT NULL,
  `rating_count` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`code`),
  KEY `idx_product_seller_status` (`seller_id`, `status`),
  KEY `idx_product_category_status` (`category_id`, `status`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_product_rating_average` CHECK (`rating_average` IS NULL OR (`rating_average` >= 0 AND `rating_average` <= 5)),
  CONSTRAINT `chk_product_rating_count` CHECK (`rating_count` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_image` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `product_id` varchar(36) NOT NULL,
  `url` varchar(1000) NOT NULL,
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_image_order` (`product_id`, `display_order`),
  CONSTRAINT `fk_product_image_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_product_image_order` CHECK (`display_order` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_definition` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `created_by_seller_id` varchar(36) DEFAULT NULL,
  `merged_into_definition_id` varchar(36) DEFAULT NULL,
  `code` varchar(100) NOT NULL,
  `name` varchar(255) NOT NULL,
  `normalized_name` varchar(255) NOT NULL,
  `data_type` varchar(30) NOT NULL,
  `default_unit` varchar(50) DEFAULT NULL,
  `is_verified` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attribute_definition_code` (`code`),
  KEY `idx_attribute_definition_name_status` (`normalized_name`, `status`),
  KEY `idx_attribute_definition_creator_verified_status` (`created_by_seller_id`, `is_verified`, `status`),
  KEY `idx_attribute_definition_merged_into` (`merged_into_definition_id`),
  CONSTRAINT `fk_attribute_definition_merged_into` FOREIGN KEY (`merged_into_definition_id`) REFERENCES `product_attribute_definition` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_attribute_definition_type` CHECK (`data_type` IN ('TEXT', 'NUMBER', 'SELECT_ONE', 'SELECT_MULTI')),
  CONSTRAINT `chk_attribute_definition_default_unit` CHECK (`default_unit` IS NULL OR `data_type` = 'NUMBER'),
  CONSTRAINT `chk_attribute_definition_not_self_merged` CHECK (`merged_into_definition_id` IS NULL OR `merged_into_definition_id` <> `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_option` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `attribute_definition_id` varchar(36) NOT NULL,
  `created_by_seller_id` varchar(36) DEFAULT NULL,
  `merged_into_option_id` varchar(36) DEFAULT NULL,
  `value` varchar(500) NOT NULL,
  `normalized_value` varchar(500) NOT NULL,
  `is_verified` bit NOT NULL DEFAULT b'0',
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attribute_option_normalized` (`attribute_definition_id`, `normalized_value`),
  KEY `idx_attribute_option_merged_into` (`merged_into_option_id`),
  CONSTRAINT `fk_attribute_option_definition` FOREIGN KEY (`attribute_definition_id`) REFERENCES `product_attribute_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_attribute_option_merged_into` FOREIGN KEY (`merged_into_option_id`) REFERENCES `product_attribute_option` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_attribute_option_not_self_merged` CHECK (`merged_into_option_id` IS NULL OR `merged_into_option_id` <> `id`),
  CONSTRAINT `chk_attribute_option_order` CHECK (`display_order` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `category_attribute_suggestion` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `category_id` varchar(36) NOT NULL,
  `attribute_definition_id` varchar(36) NOT NULL,
  `required_value` bit NOT NULL DEFAULT b'0',
  `filterable` bit NOT NULL DEFAULT b'0',
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_attribute_suggestion` (`category_id`, `attribute_definition_id`),
  KEY `idx_category_attribute_order` (`category_id`, `status`, `display_order`),
  CONSTRAINT `fk_category_attribute_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_category_attribute_definition` FOREIGN KEY (`attribute_definition_id`) REFERENCES `product_attribute_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_category_attribute_order` CHECK (`display_order` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_value` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `product_id` varchar(36) NOT NULL,
  `attribute_definition_id` varchar(36) NOT NULL,
  `attribute_option_id` varchar(36) DEFAULT NULL,
  `value_text` text DEFAULT NULL,
  `value_number` decimal(19,4) DEFAULT NULL,
  `unit` varchar(50) DEFAULT NULL,
  `display_order` int NOT NULL DEFAULT 0,
  `value_slot` varchar(36) GENERATED ALWAYS AS (coalesce(`attribute_option_id`,_utf8mb4'__SINGLE__')) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_attribute_value_slot` (`product_id`, `attribute_definition_id`, `value_slot`),
  KEY `idx_product_attribute_order` (`product_id`, `status`, `display_order`),
  KEY `idx_product_attribute_definition` (`attribute_definition_id`, `status`),
  KEY `idx_product_attribute_number` (`attribute_definition_id`, `value_number`),
  CONSTRAINT `fk_product_attribute_value_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_attribute_value_definition` FOREIGN KEY (`attribute_definition_id`) REFERENCES `product_attribute_definition` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_product_attribute_value_option` FOREIGN KEY (`attribute_option_id`) REFERENCES `product_attribute_option` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_product_attribute_value_exactly_one` CHECK (
    (`value_text` IS NOT NULL) + (`value_number` IS NOT NULL) + (`attribute_option_id` IS NOT NULL) = 1
  ),
  CONSTRAINT `chk_product_attribute_value_unit` CHECK (`unit` IS NULL OR `value_number` IS NOT NULL),
  CONSTRAINT `chk_product_attribute_value_order` CHECK (`display_order` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `variant_axis_name_suggestion` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `merged_into_suggestion_id` varchar(36) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `normalized_name` varchar(255) NOT NULL,
  `is_verified` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_axis_name_suggestion_normalized` (`normalized_name`),
  KEY `idx_axis_name_suggestion_merged_into` (`merged_into_suggestion_id`),
  CONSTRAINT `fk_axis_name_suggestion_merged_into` FOREIGN KEY (`merged_into_suggestion_id`) REFERENCES `variant_axis_name_suggestion` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_axis_name_suggestion_not_self_merged` CHECK (`merged_into_suggestion_id` IS NULL OR `merged_into_suggestion_id` <> `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_variant_axis` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `product_id` varchar(36) NOT NULL,
  `name_suggestion_id` varchar(36) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `normalized_name` varchar(255) NOT NULL,
  `display_order` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_axis_order` (`product_id`, `display_order`),
  UNIQUE KEY `uk_product_axis_name` (`product_id`, `normalized_name`),
  CONSTRAINT `fk_product_axis_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_axis_name_suggestion` FOREIGN KEY (`name_suggestion_id`) REFERENCES `variant_axis_name_suggestion` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_product_axis_order` CHECK (`display_order` IN (1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_variant_axis_value` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `axis_id` varchar(36) NOT NULL,
  `value` varchar(255) NOT NULL,
  `normalized_value` varchar(255) NOT NULL,
  `display_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_axis_value_normalized` (`axis_id`, `normalized_value`),
  KEY `idx_axis_value_order` (`axis_id`, `status`, `display_order`),
  CONSTRAINT `fk_axis_value_axis` FOREIGN KEY (`axis_id`) REFERENCES `product_variant_axis` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_axis_value_order` CHECK (`display_order` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_variant` (
  `id` varchar(36) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `product_id` varchar(36) NOT NULL,
  `sku` varchar(100) NOT NULL,
  `combination_key` varchar(255) NOT NULL,
  `sale_price` decimal(19,2) NOT NULL,
  `quantity` int NOT NULL DEFAULT 0,
  `image_url` varchar(1000) DEFAULT NULL,
  `is_default` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_variant_sku` (`sku`),
  UNIQUE KEY `uk_product_variant_combination` (`product_id`, `combination_key`),
  KEY `idx_product_variant_product_status` (`product_id`, `status`),
  CONSTRAINT `fk_product_variant_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_product_variant_price` CHECK (`sale_price` >= 0),
  CONSTRAINT `chk_product_variant_quantity` CHECK (`quantity` >= 0),
  CONSTRAINT `chk_product_variant_combination_key` CHECK (CHAR_LENGTH(TRIM(`combination_key`)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_variant_axis_value_mapping` (
  `product_variant_id` varchar(36) NOT NULL,
  `axis_value_id` varchar(36) NOT NULL,
  PRIMARY KEY (`product_variant_id`, `axis_value_id`),
  KEY `idx_variant_axis_mapping_axis_value` (`axis_value_id`),
  CONSTRAINT `fk_variant_axis_mapping_variant` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variant` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_variant_axis_mapping_axis_value` FOREIGN KEY (`axis_value_id`) REFERENCES `product_variant_axis_value` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_attribute_moderation_audit` (
  `id` varchar(36) NOT NULL,
  `actor_user_id` varchar(36) DEFAULT NULL,
  `source_definition_id` varchar(36) NOT NULL,
  `target_definition_id` varchar(36) DEFAULT NULL,
  `action` varchar(30) NOT NULL,
  `reason` varchar(1000) DEFAULT NULL,
  `affected_product_count` int NOT NULL DEFAULT 0,
  `created_date` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_attribute_audit_source_created` (`source_definition_id`, `created_date`),
  CONSTRAINT `fk_attribute_audit_source` FOREIGN KEY (`source_definition_id`) REFERENCES `product_attribute_definition` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `outbox` (
  `id` varchar(36) NOT NULL,
  `aggregate_type` varchar(100) NOT NULL,
  `aggregate_id` varchar(100) NOT NULL,
  `event_type` varchar(100) NOT NULL,
  `payload` json DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_outbox_aggregate_id` (`aggregate_id`),
  KEY `idx_outbox_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
