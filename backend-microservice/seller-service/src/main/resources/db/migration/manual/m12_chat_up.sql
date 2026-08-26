CREATE TABLE chat_conversation (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    buyer_name VARCHAR(255) NOT NULL,
    shop_name VARCHAR(255) NOT NULL,
    shop_logo_url VARCHAR(1000) NULL,
    last_message VARCHAR(500) NULL,
    last_message_at DATETIME(6) NOT NULL,
    buyer_unread_count INT NOT NULL DEFAULT 0,
    seller_unread_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_chat_customer_seller UNIQUE (customer_id, seller_id),
    INDEX idx_chat_customer_last (customer_id, last_message_at),
    INDEX idx_chat_seller_last (seller_id, last_message_at)
);

CREATE TABLE chat_message (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    sender_type VARCHAR(16) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    read_at DATETIME(6) NULL,
    INDEX idx_chat_message_conversation_created (conversation_id, created_at),
    CONSTRAINT fk_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversation(id)
);
