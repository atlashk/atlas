CREATE TABLE IF NOT EXISTS payment_gateway
(
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(50)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME,
    UNIQUE INDEX idx_code (code)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payment
(
    id                     VARCHAR(64)    NOT NULL PRIMARY KEY,
    user_id                VARCHAR(64)    NOT NULL,
    order_id               VARCHAR(64)    NOT NULL,
    saga_id                INT            NOT NULL,
    amount                 DECIMAL(19, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL,
    payment_gateway_id     INT            NOT NULL,
    payment_method         VARCHAR(50),
    payment_method_details JSON,
    status                 VARCHAR(50)    NOT NULL,
    transaction_id         VARCHAR(255),
    next_action            JSON,
    error                  VARCHAR(500),
    cancellation_reason    VARCHAR(500),
    trace_id               VARCHAR(64),
    span_id                VARCHAR(32),
    created_at             DATETIME       NOT NULL,
    updated_at             DATETIME,
    INDEX idx_user_id (user_id),
    UNIQUE INDEX idx_order_id (order_id),
    UNIQUE INDEX idx_saga_id (saga_id),
    INDEX idx_payment_gateway_id (payment_gateway_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payment_event
(
    id                 INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_gateway_id INT         NOT NULL,
    payment_id         VARCHAR(64),
    payload            TEXT        NOT NULL,
    headers            TEXT,
    status             VARCHAR(50) NOT NULL,
    error              VARCHAR(500),
    created_at         DATETIME    NOT NULL,
    updated_at         DATETIME,
    INDEX idx_payment_gateway_id (payment_gateway_id),
    INDEX idx_payment_id (payment_id)
) ENGINE = InnoDB;
