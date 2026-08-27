SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `ecommerce_auth` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_user` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_catalog` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_promotion` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_cart` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_order` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_seller` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_payout` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `ecommerce_user`;

CREATE TABLE IF NOT EXISTS `customer` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(30) DEFAULT NULL,
  `province` varchar(100) DEFAULT NULL,
  `district` varchar(100) DEFAULT NULL,
  `ward` varchar(100) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `avatar` varchar(1000) DEFAULT NULL,
  `identity_number` varchar(30) DEFAULT NULL,
  `date_of_birth` datetime(6) DEFAULT NULL,
  `gender` bit DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `staff` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `province` varchar(100) DEFAULT NULL,
  `district` varchar(100) DEFAULT NULL,
  `ward` varchar(100) DEFAULT NULL,
  `phone_number` varchar(30) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `date_of_birth` datetime(6) DEFAULT NULL,
  `avatar` varchar(1000) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `identity_number` varchar(30) DEFAULT NULL,
  `role_type` tinyint DEFAULT 1,
  `gender` bit DEFAULT NULL,
  `role` tinyint DEFAULT 0,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `customer` (`id`, `status`, `created_date`, `last_modified_date`, `code`, `name`, `phone_number`, `province`, `district`, `ward`, `email`, `address`, `avatar`, `identity_number`, `date_of_birth`, `gender`, `password`) VALUES
('10000000-0000-0000-0000-000000000001', 0, 1720000000000, 1720000000000, 'KH0001', 'Nguyen Van Demo', '0901111111', 'Ha Noi', 'Cau Giay', 'Dich Vong', 'customer1@ecommerce.local', '1 Xuan Thuy, Cau Giay, Ha Noi', NULL, '001000000001', '1995-01-15 00:00:00', 1, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm'),
('10000000-0000-0000-0000-000000000002', 0, 1720000000000, 1720000000000, 'KH0002', 'Tran Thi Sample', '0902222222', 'Ha Noi', 'Dong Da', 'Lang Ha', 'customer2@ecommerce.local', '2 Lang Ha, Dong Da, Ha Noi', NULL, '001000000002', '1996-05-20 00:00:00', 0, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm'),
('10000000-0000-0000-0000-000000000003', 0, 1720000000000, 1720000000000, 'KH0003', 'Le Minh Pending', '0903333333', 'Ho Chi Minh', 'Quan 1', 'Ben Nghe', 'customer3@ecommerce.local', '3 Le Thanh Ton, Quan 1, Ho Chi Minh', NULL, '001000000003', '1994-09-12 00:00:00', 1, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm');

INSERT INTO `staff` (`id`, `status`, `created_date`, `last_modified_date`, `code`, `name`, `province`, `district`, `ward`, `phone_number`, `address`, `date_of_birth`, `avatar`, `email`, `identity_number`, `role_type`, `gender`, `role`, `password`) VALUES
('00000000-0000-0000-0000-000000000001', 0, 1720000000000, 1720000000000, 'ADMIN001', 'Local Admin', 'Ha Noi', 'Cau Giay', 'Dich Vong', '0900000000', 'Local development', '1990-01-01 00:00:00', NULL, 'admin@ecommerce.local', '000000000001', 1, 1, 0, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm'),
('20000000-0000-0000-0000-000000000002', 0, 1720000000000, 1720000000000, 'NV0002', 'Staff Demo', 'Ha Noi', 'Ba Dinh', 'Lieu Giai', '0903333333', '3 Lieu Giai, Ba Dinh, Ha Noi', '1992-03-10 00:00:00', NULL, 'staff@ecommerce.local', '000000000002', 0, 1, 1, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm');

USE `ecommerce_seller`;

CREATE TABLE IF NOT EXISTS `seller` (
  `id` varchar(36) NOT NULL,
  `owner_customer_id` varchar(36) NOT NULL,
  `shop_name` varchar(255) NOT NULL,
  `seller_slug` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `logo_url` varchar(1000) DEFAULT NULL,
  `cover_image_url` varchar(1000) DEFAULT NULL,
  `pickup_address` varchar(1000) NOT NULL,
  `contact_phone` varchar(20) NOT NULL,
  `identity_type` varchar(30) NOT NULL,
  `identity_number` varchar(100) NOT NULL,
  `bank_name` varchar(255) NOT NULL,
  `bank_account_no` varchar(100) NOT NULL,
  `bank_account_holder` varchar(255) NOT NULL,
  `main_category_id` varchar(36) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `rejection_reason` varchar(1000) DEFAULT NULL,
  `approved_by_staff_id` varchar(36) DEFAULT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seller_shop_name` (`shop_name`),
  UNIQUE KEY `uk_seller_slug` (`seller_slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `seller_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `seller_id` varchar(36) NOT NULL,
  `from_status` varchar(30) DEFAULT NULL,
  `to_status` varchar(30) NOT NULL,
  `changed_by_user_id` varchar(36) DEFAULT NULL,
  `changed_by_role` varchar(30) DEFAULT NULL,
  `reason` varchar(1000) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `shop_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `seller_id` varchar(36) NOT NULL,
  `customer_id` varchar(36) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_follow_seller_customer` (`seller_id`, `customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `platform_banner` (
  `id` varchar(36) NOT NULL,
  `title` varchar(255) NOT NULL,
  `image_url` varchar(1000) NOT NULL,
  `target_url` varchar(1000) DEFAULT NULL,
  `position` varchar(50) NOT NULL,
  `active` bit(1) NOT NULL,
  `sort_order` int NOT NULL,
  `start_at` datetime(6) DEFAULT NULL,
  `end_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `danh_gia` (
  `id` varchar(36) NOT NULL,
  `customer_id` varchar(36) NOT NULL,
  `seller_id` varchar(36) NOT NULL,
  `product_id` varchar(36) DEFAULT NULL,
  `product_detail_id` varchar(36) NOT NULL,
  `don_hang_seller_id` varchar(36) NOT NULL,
  `product_rating` int NOT NULL,
  `shop_rating` int NOT NULL,
  `comment` text DEFAULT NULL,
  `image_urls` varchar(4000) DEFAULT NULL,
  `seller_reply` text DEFAULT NULL,
  `seller_replied_at` datetime(6) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_buyer_order_item` (`customer_id`, `don_hang_seller_id`, `product_detail_id`),
  KEY `idx_review_product` (`product_id`, `status`),
  KEY `idx_review_seller` (`seller_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `seller` (`id`, `owner_customer_id`, `shop_name`, `seller_slug`, `description`, `logo_url`, `cover_image_url`, `pickup_address`, `contact_phone`, `identity_type`, `identity_number`, `bank_name`, `bank_account_no`, `bank_account_holder`, `main_category_id`, `status`, `rejection_reason`, `approved_by_staff_id`, `approved_at`, `created_at`, `updated_at`) VALUES
('70000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','Default Sneaker Shop','default-sneaker-shop','Approved sneaker seller for marketplace demo','https://placehold.co/300x300?text=Default+Sneaker','https://placehold.co/1200x400?text=Default+Sneaker+Shop','1 Xuan Thuy, Cau Giay, Ha Noi','0901111111','CCCD','001000000001','Demo Bank','123456789','Nguyen Van Demo','32000000-0000-0000-0000-000000000001','APPROVED',NULL,'00000000-0000-0000-0000-000000000001','2024-07-03 09:00:00','2024-07-03 08:00:00','2024-07-03 09:00:00'),
('70000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000002','Run Active Store','run-active-store','Approved running seller for multi-shop checkout demo','https://placehold.co/300x300?text=Run+Active','https://placehold.co/1200x400?text=Run+Active+Store','2 Lang Ha, Dong Da, Ha Noi','0902222222','CCCD','001000000002','Demo Bank','223456789','Tran Thi Sample','32000000-0000-0000-0000-000000000002','APPROVED',NULL,'00000000-0000-0000-0000-000000000001','2024-07-03 09:30:00','2024-07-03 08:30:00','2024-07-03 09:30:00'),
('70000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000003','Streetwear Pending','streetwear-pending','Seller application waiting for admin approval',NULL,NULL,'3 Le Thanh Ton, Quan 1, Ho Chi Minh','0903333333','CCCD','001000000003','Demo Bank','323456789','Le Minh Pending','32000000-0000-0000-0000-000000000001','PENDING_APPROVAL',NULL,NULL,NULL,'2024-07-03 10:00:00','2024-07-03 10:00:00');

INSERT INTO `seller_status_history` (`seller_id`, `from_status`, `to_status`, `changed_by_user_id`, `changed_by_role`, `reason`, `created_at`) VALUES
('70000000-0000-0000-0000-000000000001','PENDING_APPROVAL','APPROVED','00000000-0000-0000-0000-000000000001','ADMIN','Approved demo seller','2024-07-03 09:00:00'),
('70000000-0000-0000-0000-000000000002','PENDING_APPROVAL','APPROVED','00000000-0000-0000-0000-000000000001','ADMIN','Approved demo seller','2024-07-03 09:30:00'),
('70000000-0000-0000-0000-000000000003','DRAFT','PENDING_APPROVAL','10000000-0000-0000-0000-000000000003','SELLER','Submitted demo application','2024-07-03 10:00:00');

INSERT INTO `shop_follow` (`seller_id`, `customer_id`, `created_at`) VALUES
('70000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000001','2024-07-04 09:00:00'),
('70000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000002','2024-07-04 09:05:00'),
('70000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000003','2024-07-04 09:10:00'),
('70000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000003','2024-07-04 09:15:00');

INSERT INTO `platform_banner` (`id`, `title`, `image_url`, `target_url`, `position`, `active`, `sort_order`, `start_at`, `end_at`, `created_at`, `updated_at`) VALUES
('71000000-0000-0000-0000-000000000001','Marketplace noi bat','/images/banner_Xuong_1.png','/san-pham','HOME_HERO',b'1',0,NULL,NULL,'2024-07-03 09:00:00','2024-07-03 09:00:00'),
('71000000-0000-0000-0000-000000000002','Running sale','https://placehold.co/1440x480?text=Running+Sale','/shop/run-active-store','HOME_HERO',b'1',1,NULL,NULL,'2024-07-03 09:00:00','2024-07-03 09:00:00');

USE `ecommerce_catalog`;

CREATE TABLE IF NOT EXISTS `brand` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(50) DEFAULT NULL, `name` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `origin` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(50) DEFAULT NULL, `name` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `category` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(50) DEFAULT NULL, `name` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `sole_type` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(50) DEFAULT NULL, `name` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `material` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(50) DEFAULT NULL, `name` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `size` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(50) DEFAULT NULL, `name` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `color` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(50) DEFAULT NULL, `name` varchar(255) DEFAULT NULL, `color` varchar(50) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
    `description` text DEFAULT NULL,
    `seller_id` varchar(36) DEFAULT NULL,
    `rating_average` double DEFAULT NULL,
    `rating_count` bigint NOT NULL DEFAULT 0,
  `brand_id` varchar(36) DEFAULT NULL,
  `origin_id` varchar(36) DEFAULT NULL,
  `category_id` varchar(36) DEFAULT NULL,
  `sole_type_id` varchar(36) DEFAULT NULL,
  `material_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_variant` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `sale_price` double DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `seller_id` varchar(36) DEFAULT NULL,
  `product_id` varchar(36) DEFAULT NULL,
  `size_id` varchar(36) DEFAULT NULL,
  `color_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
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

CREATE TABLE IF NOT EXISTS `product_attribute_definition` (`id` varchar(36) NOT NULL, `status` tinyint NOT NULL DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `code` varchar(100) NOT NULL, `name` varchar(255) NOT NULL, `normalized_name` varchar(255) NOT NULL, `data_type` varchar(30) NOT NULL, `creator_seller_id` varchar(36) DEFAULT NULL, `normalization_status` varchar(30) NOT NULL, `merged_into_attribute_id` varchar(36) DEFAULT NULL, PRIMARY KEY (`id`), UNIQUE KEY `uk_attribute_code` (`code`), KEY `idx_attribute_normalized_name` (`normalized_name`), KEY `idx_attribute_creator_status` (`creator_seller_id`,`normalization_status`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `product_attribute_option` (`id` varchar(36) NOT NULL, `status` tinyint NOT NULL DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `attribute_id` varchar(36) NOT NULL, `value` varchar(500) NOT NULL, `normalized_value` varchar(500) NOT NULL, `creator_seller_id` varchar(36) DEFAULT NULL, `merged_into_option_id` varchar(36) DEFAULT NULL, `display_order` int NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `uk_attribute_option_normalized` (`attribute_id`,`normalized_value`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `category_attribute_suggestion` (`id` varchar(36) NOT NULL, `status` tinyint NOT NULL DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `category_id` varchar(36) NOT NULL, `attribute_id` varchar(36) NOT NULL, `default_suggestion` bit NOT NULL DEFAULT b'1', `filterable` bit NOT NULL DEFAULT b'0', `required_value` bit NOT NULL DEFAULT b'0', `display_order` int NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `uk_category_attribute` (`category_id`,`attribute_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `product_attribute_value` (`id` varchar(36) NOT NULL, `status` tinyint NOT NULL DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `product_id` varchar(36) NOT NULL, `attribute_id` varchar(36) NOT NULL, `text_value` text DEFAULT NULL, `number_value` decimal(19,4) DEFAULT NULL, `unit` varchar(50) DEFAULT NULL, `display_order` int NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `uk_product_attribute_value` (`product_id`,`attribute_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `product_attribute_value_option` (`product_attribute_value_id` varchar(36) NOT NULL, `option_id` varchar(36) NOT NULL, PRIMARY KEY (`product_attribute_value_id`,`option_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `brand` VALUES
('30000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'TH001','Nike'),
('30000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'TH002','Adidas');
INSERT INTO `origin` VALUES ('31000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'XS001','Viet Nam'),('31000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'XS002','USA');
INSERT INTO `category` VALUES ('32000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'DM001','Sneaker'),('32000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'DM002','Running'),('32000000-0000-0000-0000-000000000010',0,1720000000000,1720000000000,'DM-GIAY','Giay');
INSERT INTO `sole_type` VALUES ('33000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'LD001','De cao su'),('33000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'LD002','De foam');
INSERT INTO `material` VALUES ('34000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'CL001','Da tong hop'),('34000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'CL002','Vai mesh');
INSERT INTO `size` VALUES ('35000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'KC039','39'),('35000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'KC040','40'),('35000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'KC041','41');
INSERT INTO `color` VALUES ('36000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'MS001','Trang','#ffffff'),('36000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'MS002','Den','#111111'),('36000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'MS003','Do','#dc2626');

INSERT INTO `product` (`id`, `status`, `created_date`, `last_modified_date`, `code`, `name`, `description`, `seller_id`, `rating_average`, `rating_count`, `brand_id`, `origin_id`, `category_id`, `sole_type_id`, `material_id`) VALUES
('37000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'SP0001','Nike Air Demo','Giay sneaker demo cho local test','70000000-0000-0000-0000-000000000001',5,1,'30000000-0000-0000-0000-000000000001','31000000-0000-0000-0000-000000000002','32000000-0000-0000-0000-000000000010','33000000-0000-0000-0000-000000000001','34000000-0000-0000-0000-000000000001'),
('37000000-0000-0000-0000-000000000002',0,1720000001000,1720000001000,'SP0002','Adidas Run Demo','Giay running demo cho local test','70000000-0000-0000-0000-000000000001',4,1,'30000000-0000-0000-0000-000000000002','31000000-0000-0000-0000-000000000001','32000000-0000-0000-0000-000000000010','33000000-0000-0000-0000-000000000002','34000000-0000-0000-0000-000000000002'),
('37000000-0000-0000-0000-000000000003',0,1720000002000,1720000002000,'SP0003','Adidas Ultraboost Demo','Giay chay bo cua Run Active Store','70000000-0000-0000-0000-000000000002',4.5,2,'30000000-0000-0000-0000-000000000002','31000000-0000-0000-0000-000000000002','32000000-0000-0000-0000-000000000010','33000000-0000-0000-0000-000000000002','34000000-0000-0000-0000-000000000002'),
('37000000-0000-0000-0000-000000000004',0,1720000003000,1720000003000,'SP0004','Nike Tempo Demo','Giay tap hang ngay cua Run Active Store','70000000-0000-0000-0000-000000000002',0,0,'30000000-0000-0000-0000-000000000001','31000000-0000-0000-0000-000000000001','32000000-0000-0000-0000-000000000010','33000000-0000-0000-0000-000000000002','34000000-0000-0000-0000-000000000002');

INSERT INTO `product_attribute_definition` (`id`,`status`,`created_date`,`last_modified_date`,`code`,`name`,`normalized_name`,`data_type`,`creator_seller_id`,`normalization_status`,`merged_into_attribute_id`) VALUES
('41000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'ATTR-BRAND','Thuong hieu','thuong hieu','TEXT',NULL,'STANDARDIZED',NULL),
('41000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'ATTR-ORIGIN','Xuat xu','xuat xu','TEXT',NULL,'STANDARDIZED',NULL),
('41000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'ATTR-MATERIAL','Chat lieu','chat lieu','TEXT',NULL,'STANDARDIZED',NULL),
('41000000-0000-0000-0000-000000000004',0,1720000000000,1720000000000,'ATTR-SOLE','Loai de','loai de','TEXT',NULL,'STANDARDIZED',NULL),
('41000000-0000-0000-0000-000000000005',0,1720000000000,1720000000000,'ATTR-SHOE-LINE','Dong giay','dong giay','TEXT',NULL,'STANDARDIZED',NULL);

INSERT INTO `category_attribute_suggestion` VALUES
('41100000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'32000000-0000-0000-0000-000000000010','41000000-0000-0000-0000-000000000001',b'1',b'1',b'0',0),
('41100000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'32000000-0000-0000-0000-000000000010','41000000-0000-0000-0000-000000000002',b'1',b'1',b'0',1),
('41100000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'32000000-0000-0000-0000-000000000010','41000000-0000-0000-0000-000000000003',b'1',b'1',b'0',2),
('41100000-0000-0000-0000-000000000004',0,1720000000000,1720000000000,'32000000-0000-0000-0000-000000000010','41000000-0000-0000-0000-000000000004',b'1',b'1',b'0',3),
('41100000-0000-0000-0000-000000000005',0,1720000000000,1720000000000,'32000000-0000-0000-0000-000000000010','41000000-0000-0000-0000-000000000005',b'1',b'1',b'0',4);

INSERT INTO `product_attribute_value` (`id`,`status`,`created_date`,`last_modified_date`,`product_id`,`attribute_id`,`text_value`,`number_value`,`unit`,`display_order`) VALUES
('41200000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'37000000-0000-0000-0000-000000000001','41000000-0000-0000-0000-000000000001','Nike',NULL,NULL,0),
('41200000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'37000000-0000-0000-0000-000000000001','41000000-0000-0000-0000-000000000002','USA',NULL,NULL,1),
('41200000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'37000000-0000-0000-0000-000000000001','41000000-0000-0000-0000-000000000003','Da tong hop',NULL,NULL,2),
('41200000-0000-0000-0000-000000000004',0,1720000000000,1720000000000,'37000000-0000-0000-0000-000000000001','41000000-0000-0000-0000-000000000004','De cao su',NULL,NULL,3),
('41200000-0000-0000-0000-000000000005',0,1720000000000,1720000000000,'37000000-0000-0000-0000-000000000001','41000000-0000-0000-0000-000000000005','Sneaker',NULL,NULL,4),
('41200000-0000-0000-0000-000000000006',0,1720000001000,1720000001000,'37000000-0000-0000-0000-000000000002','41000000-0000-0000-0000-000000000001','Adidas',NULL,NULL,0),
('41200000-0000-0000-0000-000000000007',0,1720000001000,1720000001000,'37000000-0000-0000-0000-000000000002','41000000-0000-0000-0000-000000000002','Viet Nam',NULL,NULL,1),
('41200000-0000-0000-0000-000000000008',0,1720000001000,1720000001000,'37000000-0000-0000-0000-000000000002','41000000-0000-0000-0000-000000000003','Vai mesh',NULL,NULL,2),
('41200000-0000-0000-0000-000000000009',0,1720000001000,1720000001000,'37000000-0000-0000-0000-000000000002','41000000-0000-0000-0000-000000000004','De foam',NULL,NULL,3),
('41200000-0000-0000-0000-000000000010',0,1720000001000,1720000001000,'37000000-0000-0000-0000-000000000002','41000000-0000-0000-0000-000000000005','Running',NULL,NULL,4),
('41200000-0000-0000-0000-000000000011',0,1720000002000,1720000002000,'37000000-0000-0000-0000-000000000003','41000000-0000-0000-0000-000000000001','Adidas',NULL,NULL,0),
('41200000-0000-0000-0000-000000000012',0,1720000002000,1720000002000,'37000000-0000-0000-0000-000000000003','41000000-0000-0000-0000-000000000002','USA',NULL,NULL,1),
('41200000-0000-0000-0000-000000000013',0,1720000002000,1720000002000,'37000000-0000-0000-0000-000000000003','41000000-0000-0000-0000-000000000003','Vai mesh',NULL,NULL,2),
('41200000-0000-0000-0000-000000000014',0,1720000002000,1720000002000,'37000000-0000-0000-0000-000000000003','41000000-0000-0000-0000-000000000004','De foam',NULL,NULL,3),
('41200000-0000-0000-0000-000000000015',0,1720000002000,1720000002000,'37000000-0000-0000-0000-000000000003','41000000-0000-0000-0000-000000000005','Running',NULL,NULL,4),
('41200000-0000-0000-0000-000000000016',0,1720000003000,1720000003000,'37000000-0000-0000-0000-000000000004','41000000-0000-0000-0000-000000000001','Nike',NULL,NULL,0),
('41200000-0000-0000-0000-000000000017',0,1720000003000,1720000003000,'37000000-0000-0000-0000-000000000004','41000000-0000-0000-0000-000000000002','Viet Nam',NULL,NULL,1),
('41200000-0000-0000-0000-000000000018',0,1720000003000,1720000003000,'37000000-0000-0000-0000-000000000004','41000000-0000-0000-0000-000000000003','Vai mesh',NULL,NULL,2),
('41200000-0000-0000-0000-000000000019',0,1720000003000,1720000003000,'37000000-0000-0000-0000-000000000004','41000000-0000-0000-0000-000000000004','De foam',NULL,NULL,3),
('41200000-0000-0000-0000-000000000020',0,1720000003000,1720000003000,'37000000-0000-0000-0000-000000000004','41000000-0000-0000-0000-000000000005','Running',NULL,NULL,4);

INSERT INTO `product_variant` VALUES
('38000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'SPCT0001',1200000,'https://placehold.co/600x600?text=Nike+White+39',50,'70000000-0000-0000-0000-000000000001','37000000-0000-0000-0000-000000000001','35000000-0000-0000-0000-000000000001','36000000-0000-0000-0000-000000000001'),
('38000000-0000-0000-0000-000000000002',0,1720000001000,1720000001000,'SPCT0002',1250000,'https://placehold.co/600x600?text=Nike+Black+40',35,'70000000-0000-0000-0000-000000000001','37000000-0000-0000-0000-000000000001','35000000-0000-0000-0000-000000000002','36000000-0000-0000-0000-000000000002'),
('38000000-0000-0000-0000-000000000003',0,1720000002000,1720000002000,'SPCT0003',990000,'https://placehold.co/600x600?text=Adidas+Red+41',42,'70000000-0000-0000-0000-000000000001','37000000-0000-0000-0000-000000000002','35000000-0000-0000-0000-000000000003','36000000-0000-0000-0000-000000000003'),
('38000000-0000-0000-0000-000000000004',0,1720000003000,1720000003000,'SPCT0004',1450000,'https://placehold.co/600x600?text=Ultraboost+White+39',30,'70000000-0000-0000-0000-000000000002','37000000-0000-0000-0000-000000000003','35000000-0000-0000-0000-000000000001','36000000-0000-0000-0000-000000000001'),
('38000000-0000-0000-0000-000000000005',0,1720000004000,1720000004000,'SPCT0005',1500000,'https://placehold.co/600x600?text=Ultraboost+Black+40',25,'70000000-0000-0000-0000-000000000002','37000000-0000-0000-0000-000000000003','35000000-0000-0000-0000-000000000002','36000000-0000-0000-0000-000000000002'),
('38000000-0000-0000-0000-000000000006',0,1720000005000,1720000005000,'SPCT0006',1100000,'https://placehold.co/600x600?text=Nike+Tempo+Red+41',40,'70000000-0000-0000-0000-000000000002','37000000-0000-0000-0000-000000000004','35000000-0000-0000-0000-000000000003','36000000-0000-0000-0000-000000000003');

INSERT INTO `outbox` (`id`, `aggregate_type`, `aggregate_id`, `event_type`, `payload`, `created_at`) VALUES
('39000000-0000-0000-0000-000000000001','Product','37000000-0000-0000-0000-000000000001','ProductCreated',
 JSON_OBJECT('id','37000000-0000-0000-0000-000000000001','name','Nike Air Demo','description','Giay sneaker demo cho local test','sellerId','70000000-0000-0000-0000-000000000001','categoryId','32000000-0000-0000-0000-000000000001','category','Sneaker','price',1200000,'brandId','30000000-0000-0000-0000-000000000001','brand','Nike','imageUrl','https://placehold.co/600x600?text=Nike+White+39'), CURRENT_TIMESTAMP(6)),
('39000000-0000-0000-0000-000000000002','Product','37000000-0000-0000-0000-000000000002','ProductCreated',
 JSON_OBJECT('id','37000000-0000-0000-0000-000000000002','name','Adidas Run Demo','description','Giay running demo cho local test','sellerId','70000000-0000-0000-0000-000000000001','categoryId','32000000-0000-0000-0000-000000000002','category','Running','price',990000,'brandId','30000000-0000-0000-0000-000000000002','brand','Adidas','imageUrl','https://placehold.co/600x600?text=Adidas+Red+41'), CURRENT_TIMESTAMP(6)),
('39000000-0000-0000-0000-000000000003','Product','37000000-0000-0000-0000-000000000003','ProductCreated',
 JSON_OBJECT('id','37000000-0000-0000-0000-000000000003','name','Adidas Ultraboost Demo','description','Giay chay bo cua Run Active Store','sellerId','70000000-0000-0000-0000-000000000002','categoryId','32000000-0000-0000-0000-000000000002','category','Running','price',1450000,'brandId','30000000-0000-0000-0000-000000000002','brand','Adidas','imageUrl','https://placehold.co/600x600?text=Ultraboost+White+39'), CURRENT_TIMESTAMP(6)),
('39000000-0000-0000-0000-000000000004','Product','37000000-0000-0000-0000-000000000004','ProductCreated',
 JSON_OBJECT('id','37000000-0000-0000-0000-000000000004','name','Nike Tempo Demo','description','Giay tap hang ngay cua Run Active Store','sellerId','70000000-0000-0000-0000-000000000002','categoryId','32000000-0000-0000-0000-000000000002','category','Running','price',1100000,'brandId','30000000-0000-0000-0000-000000000001','brand','Nike','imageUrl','https://placehold.co/600x600?text=Nike+Tempo+Red+41'), CURRENT_TIMESTAMP(6));

UPDATE `outbox` o
SET o.payload = JSON_SET(
  o.payload,
  '$.categoryId', '32000000-0000-0000-0000-000000000010',
  '$.category', 'Giay',
  '$.attributes', COALESCE((
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
    WHERE av.product_id = o.aggregate_id
  ), JSON_ARRAY())
)
WHERE o.aggregate_type = 'Product';

USE `ecommerce_promotion`;

CREATE TABLE IF NOT EXISTS `voucher` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `discount_value` double DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `condition_amount` double DEFAULT NULL,
  `max_discount_amount` double DEFAULT NULL,
  `discount_type` bit DEFAULT NULL,
  `discount_method` bit DEFAULT NULL,
  `seller_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `voucher_customer` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(100) DEFAULT NULL,
  `customer_id` varchar(36) DEFAULT NULL,
  `voucher_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `promotion_campaign` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `discount_value` double DEFAULT NULL,
  `description` text DEFAULT NULL,
  `start_date` bigint DEFAULT NULL,
  `end_date` bigint DEFAULT NULL,
  `campaign_status` varchar(50) DEFAULT NULL,
  `seller_id` varchar(36) DEFAULT NULL,
  `campaign_type` varchar(32) NOT NULL DEFAULT 'STANDARD',
  `created_by_staff_id` varchar(36) DEFAULT NULL,
  `registration_end_date` bigint DEFAULT NULL,
  `registration_start_date` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `promotion_campaign_product` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `code` varchar(100) DEFAULT NULL,
  `price_before_discount` double DEFAULT NULL,
  `price_after_discount` double DEFAULT NULL,
  `detail_status` varchar(50) DEFAULT NULL,
  `product_variant_id` varchar(36) DEFAULT NULL,
  `promotion_campaign_id` varchar(36) DEFAULT NULL,
  `registration_status` varchar(32) DEFAULT NULL,
  `rejection_reason` varchar(500) DEFAULT NULL,
  `reviewed_at` bigint DEFAULT NULL,
  `reviewed_by_staff_id` varchar(36) DEFAULT NULL,
  `seller_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `voucher` VALUES
('40000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'WELCOME10','Giam 10% don tu 500k',10,100,'2024-01-01 00:00:00','2099-12-31 23:59:59',500000,150000,0,1,NULL),
('40000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'VIP200','Voucher ca nhan 200k',200000,20,'2024-01-01 00:00:00','2099-12-31 23:59:59',1000000,200000,1,0,NULL),
('40000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'SHOP50','Voucher shop 50k',50000,50,'2024-01-01 00:00:00','2099-12-31 23:59:59',300000,50000,0,0,'70000000-0000-0000-0000-000000000001'),
('40000000-0000-0000-0000-000000000004',0,1720000000000,1720000000000,'RUN80','Voucher Run Active 80k',80000,50,'2024-01-01 00:00:00','2099-12-31 23:59:59',500000,80000,1,0,'70000000-0000-0000-0000-000000000002');
INSERT INTO `voucher_customer` VALUES
('41000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'PGGCT0001','10000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000002');
INSERT INTO `promotion_campaign` VALUES
('42000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'DGG0001','Sale demo 15%',15,'Dot sale demo cho local',1704067200000,4102444799000,'DANG_KICH_HOAT','70000000-0000-0000-0000-000000000001','STANDARD',NULL,NULL,NULL),
('42000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'DGG0002','Run Active sale 10%',10,'Dot sale cua shop Run Active',1704067200000,4102444799000,'DANG_KICH_HOAT','70000000-0000-0000-0000-000000000002','STANDARD',NULL,NULL,NULL),
('42000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'DGG0003','Campaign san demo 12%',12,'Campaign toan san de kiem tra man Admin',1704067200000,4102444799000,'DANG_KICH_HOAT',NULL,'STANDARD','00000000-0000-0000-0000-000000000001',NULL,NULL),
('42000000-0000-0000-0000-000000000004',0,1720000000000,1720000000000,'FS-DEMO01','Flash Sale san demo',0,'Su kien Flash Sale toan san de kiem tra luong Admin, Seller va Buyer',4102444798000,4133980799000,'CHUA_KICH_HOAT',NULL,'FLASH_SALE','00000000-0000-0000-0000-000000000001',4102444797000,1704067200000);
INSERT INTO `promotion_campaign_product` VALUES
('43000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'DGGCT0001',1200000,1020000,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000001','42000000-0000-0000-0000-000000000001',NULL,NULL,NULL,NULL,'70000000-0000-0000-0000-000000000001'),
('43000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'DGGCT0002',1250000,1062500,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000002','42000000-0000-0000-0000-000000000001',NULL,NULL,NULL,NULL,'70000000-0000-0000-0000-000000000001'),
('43000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'DGGCT0003',1450000,1305000,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000004','42000000-0000-0000-0000-000000000002',NULL,NULL,NULL,NULL,'70000000-0000-0000-0000-000000000002'),
('43000000-0000-0000-0000-000000000004',0,1720000000000,1720000000000,'DGGCT0004',1500000,1350000,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000005','42000000-0000-0000-0000-000000000002',NULL,NULL,NULL,NULL,'70000000-0000-0000-0000-000000000002'),
('43000000-0000-0000-0000-000000000005',0,1720000000000,1720000000000,'DGGCT0005',1300000,1144000,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000003','42000000-0000-0000-0000-000000000003',NULL,NULL,NULL,NULL,NULL),
('43000000-0000-0000-0000-000000000006',0,1720000000000,1720000000000,'FSREG-DEMO-APPROVED',1450000,1160000,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000004','42000000-0000-0000-0000-000000000004','APPROVED',NULL,1720000000000,'00000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000002'),
('43000000-0000-0000-0000-000000000007',0,1720000000000,1720000000000,'FSREG-DEMO-PENDING',1250000,999000,'CHUA_KICH_HOAT','38000000-0000-0000-0000-000000000002','42000000-0000-0000-0000-000000000004','PENDING',NULL,NULL,NULL,'70000000-0000-0000-0000-000000000001');

USE `ecommerce_cart`;

CREATE TABLE IF NOT EXISTS `cart` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `customer_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `cart_detail` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `product_variant_id` varchar(36) DEFAULT NULL,
  `cart_id` varchar(36) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `price` double DEFAULT NULL,
  `seller_id` varchar(36) DEFAULT NULL,
  `shop_name` varchar(255) DEFAULT NULL,
  `seller_slug` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `cart` VALUES
('50000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'10000000-0000-0000-0000-000000000001'),
('50000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'10000000-0000-0000-0000-000000000002');
INSERT INTO `cart_detail` VALUES
('51000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'38000000-0000-0000-0000-000000000001','50000000-0000-0000-0000-000000000001',1,1200000,'70000000-0000-0000-0000-000000000001','Default Sneaker Shop','default-sneaker-shop'),
('51000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'38000000-0000-0000-0000-000000000004','50000000-0000-0000-0000-000000000001',2,1450000,'70000000-0000-0000-0000-000000000002','Run Active Store','run-active-store'),
('51000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'38000000-0000-0000-0000-000000000002','50000000-0000-0000-0000-000000000002',1,1250000,'70000000-0000-0000-0000-000000000001','Default Sneaker Shop','default-sneaker-shop');

USE `ecommerce_order`;

CREATE TABLE IF NOT EXISTS `orders` (
  `id` varchar(36) NOT NULL,
  `status` int DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `customer_phone` varchar(30) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  `shipping_fee` double DEFAULT NULL,
  `shipping_address` varchar(500) DEFAULT NULL,
  `total_after_discount` double DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `discount_amount` double DEFAULT NULL,
  `debt_amount` double DEFAULT NULL,
  `refund_amount` double DEFAULT NULL,
  `note` varchar(1000) DEFAULT NULL,
  `payment_method` tinyint DEFAULT NULL,
  `order_type` tinyint DEFAULT NULL,
  `customer_id` varchar(36) DEFAULT NULL,
  `voucher_id` varchar(36) DEFAULT NULL,
  `staff_id` varchar(36) DEFAULT NULL,
  `order_status` tinyint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `order_item` (
  `id` varchar(36) NOT NULL,
  `status` int DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `sale_price` double DEFAULT NULL,
  `product_variant_id` varchar(36) DEFAULT NULL,
  `order_seller_id` varchar(36) DEFAULT NULL,
  `seller_id` varchar(36) DEFAULT NULL,
  `order_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `order_seller` (
  `id` varchar(36) NOT NULL,
  `order_id` varchar(36) DEFAULT NULL,
  `seller_id` varchar(36) DEFAULT NULL,
  `shop_name` varchar(255) DEFAULT NULL,
  `seller_slug` varchar(255) DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `shipping_fee` double DEFAULT NULL,
  `discount_amount` double DEFAULT NULL,
  `total_after_discount` double DEFAULT NULL,
  `order_status` tinyint DEFAULT NULL,
  `created_date` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `order_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` varchar(36) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `payment_time` datetime(6) DEFAULT NULL,
  `note` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `payment_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `payment_time` datetime(6) DEFAULT NULL,
  `transaction_code` varchar(100) DEFAULT NULL,
  `transaction_type` varchar(50) DEFAULT NULL,
  `staff_id` varchar(36) DEFAULT NULL,
  `order_id` varchar(36) DEFAULT NULL,
  `note` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `orders` VALUES
('60000000-0000-0000-0000-000000000001',0,1720000000000,'HD0001','Don da hoan thanh hai shop','0901111111','customer1@ecommerce.local','Nguyen Van Demo',60000,'1 Xuan Thuy, Cau Giay, Ha Noi',2560000,2650000,150000,0,0,'Seed completed multi-shop order',1,2,'10000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000001',NULL,4),
('60000000-0000-0000-0000-000000000002',0,1720003600000,'HD0002','Don cho xac nhan','0902222222','customer2@ecommerce.local','Tran Thi Sample',30000,'2 Lang Ha, Dong Da, Ha Noi',1280000,1250000,0,0,0,'Seed pending order',1,2,'10000000-0000-0000-0000-000000000002',NULL,NULL,0),
('60000000-0000-0000-0000-000000000003',0,1720007200000,'HD0003','Don dang giao hai shop','0901111111','customer1@ecommerce.local','Nguyen Van Demo',60000,'1 Xuan Thuy, Cau Giay, Ha Noi',2150000,2090000,0,0,0,'Seed mixed sub-order statuses',1,2,'10000000-0000-0000-0000-000000000001',NULL,NULL,3),
('60000000-0000-0000-0000-000000000004',0,1720010800000,'HD0004','Don da doi soat','0902222222','customer2@ecommerce.local','Tran Thi Sample',30000,'2 Lang Ha, Dong Da, Ha Noi',1530000,1500000,0,0,0,'Seed paid payout order',0,2,'10000000-0000-0000-0000-000000000002',NULL,NULL,4);
INSERT INTO `order_seller` VALUES
('62000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001','Default Sneaker Shop','default-sneaker-shop',1200000,30000,50000,1180000,4,1720000000000),
('62000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000002','Run Active Store','run-active-store',1450000,30000,100000,1380000,4,1720000000000),
('62000000-0000-0000-0000-000000000003','60000000-0000-0000-0000-000000000002','70000000-0000-0000-0000-000000000001','Default Sneaker Shop','default-sneaker-shop',1250000,30000,0,1280000,0,1720003600000),
('62000000-0000-0000-0000-000000000004','60000000-0000-0000-0000-000000000003','70000000-0000-0000-0000-000000000001','Default Sneaker Shop','default-sneaker-shop',990000,30000,0,1020000,4,1720007200000),
('62000000-0000-0000-0000-000000000005','60000000-0000-0000-0000-000000000003','70000000-0000-0000-0000-000000000002','Run Active Store','run-active-store',1100000,30000,0,1130000,3,1720007200000),
('62000000-0000-0000-0000-000000000006','60000000-0000-0000-0000-000000000004','70000000-0000-0000-0000-000000000002','Run Active Store','run-active-store',1500000,30000,0,1530000,4,1720010800000);
INSERT INTO `order_item` VALUES
('61000000-0000-0000-0000-000000000001',0,1720000000000,'HDCT0001','Nike Air Demo 39',1,1200000,'38000000-0000-0000-0000-000000000001','62000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000001'),
('61000000-0000-0000-0000-000000000002',0,1720000000000,'HDCT0002','Adidas Ultraboost Demo 39',1,1450000,'38000000-0000-0000-0000-000000000004','62000000-0000-0000-0000-000000000002','70000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000001'),
('61000000-0000-0000-0000-000000000003',0,1720003600000,'HDCT0003','Nike Air Demo 40',1,1250000,'38000000-0000-0000-0000-000000000002','62000000-0000-0000-0000-000000000003','70000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000002'),
('61000000-0000-0000-0000-000000000004',0,1720007200000,'HDCT0004','Adidas Run Demo 41',1,990000,'38000000-0000-0000-0000-000000000003','62000000-0000-0000-0000-000000000004','70000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000003'),
('61000000-0000-0000-0000-000000000005',0,1720007200000,'HDCT0005','Nike Tempo Demo 41',1,1100000,'38000000-0000-0000-0000-000000000006','62000000-0000-0000-0000-000000000005','70000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000003'),
('61000000-0000-0000-0000-000000000006',0,1720010800000,'HDCT0006','Adidas Ultraboost Demo 40',1,1500000,'38000000-0000-0000-0000-000000000005','62000000-0000-0000-0000-000000000006','70000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000004');
INSERT INTO `order_status_history` (`order_id`, `status`, `payment_time`, `note`) VALUES
('60000000-0000-0000-0000-000000000001',4,'2024-07-03 10:00:00','Seed demo: don hang hoan thanh'),
('60000000-0000-0000-0000-000000000002',0,'2024-07-03 11:00:00','Seed demo: don hang cho xac nhan'),
('60000000-0000-0000-0000-000000000003',3,'2024-07-03 12:00:00','Seed demo: mot shop hoan thanh, mot shop dang giao'),
('60000000-0000-0000-0000-000000000004',4,'2024-07-03 13:00:00','Seed demo: don hang da hoan thanh va doi soat');
INSERT INTO `payment_history` (`amount`, `payment_time`, `transaction_code`, `transaction_type`, `staff_id`, `order_id`, `note`) VALUES
(2560000,'2024-07-03 10:00:00','PAY-SEED-0001','1',NULL,'60000000-0000-0000-0000-000000000001','Seed demo payment'),
(1530000,'2024-07-03 13:00:00','PAY-SEED-0004','0',NULL,'60000000-0000-0000-0000-000000000004','Seed demo bank transfer');

CREATE TABLE IF NOT EXISTS `dispute` (
  `id` varchar(36) NOT NULL,
  `order_seller_id` varchar(36) NOT NULL,
  `order_id` varchar(36) NOT NULL,
  `seller_id` varchar(36) NOT NULL,
  `customer_id` varchar(36) NOT NULL,
  `raised_by` varchar(15) NOT NULL,
  `dispute_type` varchar(40) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `evidence_urls` text DEFAULT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'OPEN',
  `requested_amount` double DEFAULT NULL,
  `resolved_amount` double DEFAULT NULL,
  `resolution_note` text DEFAULT NULL,
  `resolved_by_staff_id` varchar(36) DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_customer_status` (`customer_id`,`status`),
  KEY `idx_dispute_seller_status` (`seller_id`,`status`),
  KEY `idx_dispute_order_seller` (`order_seller_id`),
  KEY `idx_dispute_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `dispute_message` (
  `id` varchar(36) NOT NULL,
  `dispute_id` varchar(36) NOT NULL,
  `sender_type` varchar(15) NOT NULL,
  `sender_id` varchar(36) NOT NULL,
  `message` text NOT NULL,
  `attachment_urls` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_message_created` (`dispute_id`,`created_at`),
  CONSTRAINT `fk_dispute_message_dispute` FOREIGN KEY (`dispute_id`) REFERENCES `dispute` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `dispute` (`id`,`order_seller_id`,`order_id`,`seller_id`,`customer_id`,`raised_by`,`dispute_type`,`reason`,`description`,`evidence_urls`,`status`,`requested_amount`,`resolved_amount`,`resolution_note`,`resolved_by_staff_id`,`resolved_at`,`created_at`,`updated_at`) VALUES
('90000000-0000-0000-0000-000000000001','62000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','BUYER','ITEM_DAMAGED','Giay bi bong keo sau lan dau su dung','Phan mui giay bi bong keo va co vet nut. Toi muon shop kiem tra va ho tro doi hoac hoan tien.','["https://placehold.co/900x600?text=Bang+chung+giay+bong+keo"]','OPEN',1180000,NULL,NULL,NULL,NULL,'2026-08-23 08:30:00','2026-08-23 08:30:00'),
('90000000-0000-0000-0000-000000000002','62000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000001','BUYER','WRONG_ITEM','Shop giao sai mau va sai kich thuoc','Toi dat mau den size 40 nhung nhan mau trang size 39. Seller da phan hoi nhung hai ben chua thong nhat.','["https://placehold.co/900x600?text=San+pham+giao+sai","https://placehold.co/900x600?text=Tem+size+39"]','SELLER_RESPONDED',1380000,NULL,NULL,NULL,NULL,'2026-08-22 09:15:00','2026-08-22 11:20:00'),
('90000000-0000-0000-0000-000000000003','62000000-0000-0000-0000-000000000004','60000000-0000-0000-0000-000000000003','70000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','BUYER','NOT_AS_DESCRIBED','Chat lieu khong giong mo ta san pham','San pham nhan duoc co chat lieu va mau sac khac anh. Admin dang cho nguoi mua bo sung video mo hop.','["https://placehold.co/900x600?text=Anh+thuc+te+san+pham"]','UNDER_ADMIN_REVIEW',1020000,NULL,NULL,'00000000-0000-0000-0000-000000000001',NULL,'2026-08-21 14:10:00','2026-08-22 08:05:00'),
('90000000-0000-0000-0000-000000000004','62000000-0000-0000-0000-000000000006','60000000-0000-0000-0000-000000000004','70000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000002','BUYER','REFUND_REQUEST','Yeu cau hoan tien do doi y','Nguoi mua yeu cau hoan sau khi da su dung. Bang chung cho thay san pham dung mo ta va khong co loi.','[]','RESOLVED_REJECT_BUYER',1530000,0,'Tu choi hoan tien: san pham dung mo ta, da qua su dung va khong co bang chung loi tu nha ban.','00000000-0000-0000-0000-000000000001','2026-08-20 16:30:00','2026-08-19 10:00:00','2026-08-20 16:30:00');

INSERT INTO `dispute_message` (`id`,`dispute_id`,`sender_type`,`sender_id`,`message`,`attachment_urls`,`created_at`) VALUES
('91000000-0000-0000-0000-000000000001','90000000-0000-0000-0000-000000000002','BUYER','10000000-0000-0000-0000-000000000001','Toi da gui anh san pham va tem size nhan duoc.','["https://placehold.co/900x600?text=Tem+size+39"]','2026-08-22 09:20:00'),
('91000000-0000-0000-0000-000000000002','90000000-0000-0000-0000-000000000002','SELLER','70000000-0000-0000-0000-000000000002','Shop xin loi va de nghi doi dung mau, nhung khach hang muon hoan tien.','[]','2026-08-22 11:20:00'),
('91000000-0000-0000-0000-000000000003','90000000-0000-0000-0000-000000000003','BUYER','10000000-0000-0000-0000-000000000001','Mau thuc te toi hon nhieu va chat lieu khong giong phan mo ta.','["https://placehold.co/900x600?text=Anh+so+sanh+mau"]','2026-08-21 14:15:00'),
('91000000-0000-0000-0000-000000000004','90000000-0000-0000-0000-000000000003','SELLER','70000000-0000-0000-0000-000000000001','Shop da giao dung ma bien the tren don. De nghi khach gui video mo hop.','[]','2026-08-21 18:40:00'),
('91000000-0000-0000-0000-000000000005','90000000-0000-0000-0000-000000000003','ADMIN','00000000-0000-0000-0000-000000000001','Vui long bo sung video mo hop va anh nhan san pham de admin doi chieu.','[]','2026-08-22 08:05:00'),
('91000000-0000-0000-0000-000000000006','90000000-0000-0000-0000-000000000004','BUYER','10000000-0000-0000-0000-000000000002','Toi muon tra lai vi mang khong vua chan.','[]','2026-08-19 10:05:00'),
('91000000-0000-0000-0000-000000000007','90000000-0000-0000-0000-000000000004','SELLER','70000000-0000-0000-0000-000000000002','San pham da qua su dung va don giao dung size da dat.','[]','2026-08-19 15:45:00'),
('91000000-0000-0000-0000-000000000008','90000000-0000-0000-0000-000000000004','ADMIN','00000000-0000-0000-0000-000000000001','Admin da doi chieu don hang va bang chung cua hai ben.','[]','2026-08-20 16:25:00');

USE `ecommerce_seller`;

INSERT INTO `danh_gia` (`id`, `customer_id`, `seller_id`, `product_id`, `product_detail_id`, `don_hang_seller_id`, `product_rating`, `shop_rating`, `comment`, `image_urls`, `seller_reply`, `seller_replied_at`, `status`, `created_at`) VALUES
('72000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001','37000000-0000-0000-0000-000000000001','38000000-0000-0000-0000-000000000001','62000000-0000-0000-0000-000000000001',5,5,'San pham dung mo ta, dong goi tot','["https://placehold.co/400x400?text=Review+1"]','Cam on ban da ung ho shop','2024-07-05 10:00:00','VISIBLE','2024-07-05 09:00:00'),
('72000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000002','37000000-0000-0000-0000-000000000003','38000000-0000-0000-0000-000000000004','62000000-0000-0000-0000-000000000002',4,4,'Giay em chan, giao hang dung hen',NULL,NULL,NULL,'VISIBLE','2024-07-05 09:30:00'),
('72000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001','37000000-0000-0000-0000-000000000002','38000000-0000-0000-0000-000000000003','62000000-0000-0000-0000-000000000004',4,4,'Chat luong tot trong tam gia',NULL,NULL,NULL,'VISIBLE','2024-07-05 10:00:00'),
('72000000-0000-0000-0000-000000000004','10000000-0000-0000-0000-000000000002','70000000-0000-0000-0000-000000000002','37000000-0000-0000-0000-000000000003','38000000-0000-0000-0000-000000000005','62000000-0000-0000-0000-000000000006',5,5,'Form giay dep, se mua lai',NULL,'Run Active cam on ban','2024-07-05 11:00:00','VISIBLE','2024-07-05 10:30:00');

USE `ecommerce_payout`;

CREATE TABLE IF NOT EXISTS `commission_config` (
  `id` varchar(36) NOT NULL,
  `category_id` varchar(36) DEFAULT NULL,
  `rate_percent` double NOT NULL,
  `active` bit NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `seller_wallet` (
  `id` varchar(36) NOT NULL,
  `seller_id` varchar(36) NOT NULL,
  `pending_amount` double NOT NULL,
  `available_amount` double NOT NULL,
  `paid_amount` double NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seller_wallet_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `seller_receivable` (
  `id` varchar(36) NOT NULL,
  `order_seller_id` varchar(36) NOT NULL,
  `order_id` varchar(36) DEFAULT NULL,
  `seller_id` varchar(36) NOT NULL,
  `gross_amount` double NOT NULL,
  `commission_rate` double NOT NULL,
  `commission_amount` double NOT NULL,
  `net_amount` double NOT NULL,
  `status` varchar(30) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_receivable_order_seller` (`order_seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `commission_config` VALUES
('80000000-0000-0000-0000-000000000001',NULL,5,1,'2024-07-03 10:00:00'),
('80000000-0000-0000-0000-000000000002','32000000-0000-0000-0000-000000000002',7,1,'2024-07-03 10:00:00');
INSERT INTO `seller_wallet` VALUES
('81000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001',969000,0,1121000,'2024-07-05 12:00:00'),
('81000000-0000-0000-0000-000000000002','70000000-0000-0000-0000-000000000002',1283400,0,1422900,'2024-07-05 12:00:00');
INSERT INTO `seller_receivable` VALUES
('82000000-0000-0000-0000-000000000001','62000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001',1180000,5,59000,1121000,'PAID','2024-07-03 10:00:00'),
('82000000-0000-0000-0000-000000000002','62000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000002',1380000,7,96600,1283400,'PENDING','2024-07-03 10:00:00'),
('82000000-0000-0000-0000-000000000003','62000000-0000-0000-0000-000000000004','60000000-0000-0000-0000-000000000003','70000000-0000-0000-0000-000000000001',1020000,5,51000,969000,'PENDING','2024-07-03 12:00:00'),
('82000000-0000-0000-0000-000000000004','62000000-0000-0000-0000-000000000006','60000000-0000-0000-0000-000000000004','70000000-0000-0000-0000-000000000002',1530000,7,107100,1422900,'PAID','2024-07-03 13:00:00');
