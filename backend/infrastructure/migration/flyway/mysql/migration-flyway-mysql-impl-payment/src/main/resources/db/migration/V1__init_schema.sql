CREATE TABLE IF NOT EXISTS payment_intent (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    stripe_payment_intent_id VARCHAR(255) NOT NULL UNIQUE,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL,
    client_secret VARCHAR(255),
    description TEXT,
    metadata TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_stripe_payment_intent_id (stripe_payment_intent_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payment (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_intent_id INT NOT NULL,
    stripe_charge_id VARCHAR(255),
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method_type VARCHAR(50), -- card, bank_account, etc.
    card_brand VARCHAR(50), -- visa, mastercard, etc. (from Stripe response)
    card_last4 VARCHAR(4), -- from Stripe response
    failure_code VARCHAR(100),
    failure_message TEXT,
    receipt_url VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_payment_intent_id (payment_intent_id),
    FOREIGN KEY (payment_intent_id) REFERENCES payment_intents(id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS webhook_event (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    stripe_event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    event_data TEXT NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    processed_at DATETIME,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stripe_event_id (stripe_event_id),
    INDEX idx_event_type (event_type),
    INDEX idx_processed (processed)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS outbox_message (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message_payload TEXT NOT NULL,
    message_class VARCHAR(255) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at DATETIME,
    error TEXT,
    retries INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;
