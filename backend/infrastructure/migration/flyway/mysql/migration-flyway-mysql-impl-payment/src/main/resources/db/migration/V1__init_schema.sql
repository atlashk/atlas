CREATE TABLE IF NOT EXISTS payment
(
    id                  INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id             INT            NOT NULL,
    order_id            INT            NOT NULL,
    saga_id             INT            NOT NULL,
    amount              DECIMAL(19, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL,
    method              VARCHAR(50)    NOT NULL,
    gateway             VARCHAR(50)    NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    transaction_id      VARCHAR(255),
    next_action         TEXT,
    error_code          VARCHAR(100),
    error_message       VARCHAR(500),
    cancellation_reason VARCHAR(500),
    created_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    UNIQUE INDEX idx_order_id (order_id),
    UNIQUE INDEX idx_saga_id (saga_id),
    UNIQUE INDEX idx_transaction_id (transaction_id)
) ENGINE = InnoDB;

-- Outbox tables

CREATE TABLE IF NOT EXISTS outbox_message
(
    id           INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message      TEXT        NOT NULL,
    status       VARCHAR(20) NOT NULL,
    processed_at DATETIME,
    error        TEXT,
    retries      INT                  DEFAULT 0,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;
