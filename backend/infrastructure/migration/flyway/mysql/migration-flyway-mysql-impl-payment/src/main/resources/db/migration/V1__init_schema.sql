CREATE TABLE IF NOT EXISTS payment
(
    id                  INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id             INT            NOT NULL,
    order_id            INT            NOT NULL,
    amount              DECIMAL(19, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL,
    method              VARCHAR(50)    NOT NULL,
    gateway             VARCHAR(50)    NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    transaction_id      VARCHAR(255),
    receipt_url         VARCHAR(500),
    error_code          VARCHAR(100),
    error_message       VARCHAR(500),
    cancellation_reason VARCHAR(500),
    created_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    UNIQUE INDEX idx_user_id (transaction_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS outbox_message
(
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message_payload TEXT         NOT NULL,
    message_class   VARCHAR(255) NOT NULL,
    message_key     VARCHAR(255) NOT NULL,
    destination     VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    processed_at    DATETIME,
    error           TEXT,
    retries         INT                   DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;
