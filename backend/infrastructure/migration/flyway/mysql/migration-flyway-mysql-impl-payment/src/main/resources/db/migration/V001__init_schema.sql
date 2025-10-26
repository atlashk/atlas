CREATE TABLE IF NOT EXISTS payment_gateway
(
    id         INT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(50),
    name       INT      NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_code (code)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payment
(
    id                     INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                INT            NOT NULL,
    order_id               INT            NOT NULL,
    saga_id                INT            NOT NULL,
    amount                 DECIMAL(19, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL,
    payment_gateway_id     INT            NOT NULL,
    payment_method         VARCHAR(50)    NOT NULL,
    payment_method_details TEXT,
    status                 VARCHAR(50)    NOT NULL,
    transaction_id         VARCHAR(255),
    next_action            TEXT,
    error_code             VARCHAR(100),
    error_message          VARCHAR(500),
    cancellation_reason    VARCHAR(500),
    created_at             DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    UNIQUE INDEX idx_order_id (order_id),
    UNIQUE INDEX idx_saga_id (saga_id),
    INDEX idx_payment_gateway_id (payment_gateway_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payment_event
(
    id                 INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_gateway_id INT         NOT NULL,
    payment_id         INT,
    payload            TEXT        NOT NULL,
    headers            TEXT,
    status             VARCHAR(50) NOT NULL,
    error              VARCHAR(500),
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment_gateway_id (payment_gateway_id),
    INDEX idx_payment_id (payment_id)
) ENGINE = InnoDB;
