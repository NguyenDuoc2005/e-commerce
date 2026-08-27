CREATE TABLE IF NOT EXISTS dispute (
    id VARCHAR(36) PRIMARY KEY,
    order_seller_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    raised_by VARCHAR(15) NOT NULL,
    dispute_type VARCHAR(40) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT NULL,
    evidence_urls TEXT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
    requested_amount DOUBLE NULL,
    resolved_amount DOUBLE NULL,
    resolution_note TEXT NULL,
    resolved_by_staff_id VARCHAR(36) NULL,
    resolved_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_dispute_customer_status (customer_id, status),
    INDEX idx_dispute_seller_status (seller_id, status),
    INDEX idx_dispute_order_seller (order_seller_id),
    INDEX idx_dispute_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS dispute_message (
    id VARCHAR(36) PRIMARY KEY,
    dispute_id VARCHAR(36) NOT NULL,
    sender_type VARCHAR(15) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    message TEXT NOT NULL,
    attachment_urls TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_dispute_message_created (dispute_id, created_at),
    CONSTRAINT fk_dispute_message_dispute FOREIGN KEY (dispute_id) REFERENCES dispute(id)
);
