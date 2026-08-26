ALTER TABLE promotion_campaign
    ADD COLUMN campaign_type VARCHAR(32) NOT NULL DEFAULT 'STANDARD' AFTER seller_id,
    ADD COLUMN registration_start_date BIGINT NULL AFTER campaign_type,
    ADD COLUMN registration_end_date BIGINT NULL AFTER registration_start_date,
    ADD COLUMN created_by_staff_id VARCHAR(36) NULL AFTER registration_end_date;

ALTER TABLE promotion_campaign_product
    ADD COLUMN seller_id VARCHAR(36) NULL AFTER promotion_campaign_id,
    ADD COLUMN registration_status VARCHAR(32) NULL AFTER seller_id,
    ADD COLUMN rejection_reason VARCHAR(500) NULL AFTER registration_status,
    ADD COLUMN reviewed_by_staff_id VARCHAR(36) NULL AFTER rejection_reason,
    ADD COLUMN reviewed_at BIGINT NULL AFTER reviewed_by_staff_id,
    ADD INDEX idx_flash_registration_campaign_status (promotion_campaign_id, registration_status),
    ADD INDEX idx_flash_registration_seller (seller_id, registration_status);

UPDATE promotion_campaign
SET campaign_type = 'STANDARD'
WHERE campaign_type IS NULL OR campaign_type = '';

UPDATE promotion_campaign_product
SET registration_status = 'APPROVED'
WHERE registration_status IS NULL;
