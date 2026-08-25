ALTER TABLE seller_receivable
    ADD COLUMN available_at DATETIME(6) NULL,
    ADD COLUMN released_amount DOUBLE NOT NULL DEFAULT 0,
    ADD COLUMN paid_at DATETIME(6) NULL,
    ADD COLUMN payout_batch_id VARCHAR(36) NULL;

UPDATE seller_receivable
SET available_at = COALESCE(available_at, DATE_ADD(created_at, INTERVAL 7 DAY)),
    released_amount = CASE WHEN status = 'PAID' THEN net_amount ELSE released_amount END;

CREATE INDEX idx_receivable_release ON seller_receivable (status, available_at);

CREATE TABLE payout_batch (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    reference_code VARCHAR(40) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    item_count INT NOT NULL,
    total_amount DOUBLE NOT NULL,
    created_by_staff_id VARCHAR(36) NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME(6) NULL,
    paid_at DATETIME(6) NULL
);

CREATE TABLE payout_batch_item (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    batch_id VARCHAR(36) NOT NULL,
    receivable_id VARCHAR(36) NOT NULL UNIQUE,
    seller_id VARCHAR(36) NOT NULL,
    amount DOUBLE NOT NULL,
    created_at DATETIME(6) NULL,
    INDEX idx_payout_batch_item_batch (batch_id),
    CONSTRAINT fk_payout_batch_item_batch FOREIGN KEY (batch_id) REFERENCES payout_batch(id)
);
