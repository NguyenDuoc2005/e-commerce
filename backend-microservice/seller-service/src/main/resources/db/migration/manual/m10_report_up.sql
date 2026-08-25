CREATE TABLE IF NOT EXISTS report (
    id VARCHAR(36) PRIMARY KEY,
    reporter_id VARCHAR(36) NOT NULL,
    reporter_type VARCHAR(16) NOT NULL,
    target_type VARCHAR(16) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    reason_code VARCHAR(48) NOT NULL,
    description TEXT NULL,
    evidence_urls JSON NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    action_taken VARCHAR(48) NULL,
    resolution_note TEXT NULL,
    reviewed_by_staff_id VARCHAR(36) NULL,
    reviewed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_report_target (target_type, target_id),
    INDEX idx_report_status (status),
    INDEX idx_report_created_at (created_at)
);
