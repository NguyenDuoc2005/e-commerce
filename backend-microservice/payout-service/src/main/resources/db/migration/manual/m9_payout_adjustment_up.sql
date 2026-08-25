CREATE TABLE IF NOT EXISTS payout_adjustment (
    id VARCHAR(36) PRIMARY KEY,
    dispute_id VARCHAR(36) NOT NULL UNIQUE,
    order_seller_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    amount DOUBLE NOT NULL,
    type VARCHAR(40) NOT NULL DEFAULT 'DISPUTE_ADJUSTMENT',
    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_payout_adjustment_seller_created (seller_id, created_at),
    INDEX idx_payout_adjustment_order_seller (order_seller_id)
);
