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
