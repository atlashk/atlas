CREATE TABLE IF NOT EXISTS orders
(
    id              INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    saga_id         INT            NOT NULL,
    code            CHAR(7)        NOT NULL,
    user_id         INT            NOT NULL,
    amount          DECIMAL(11, 2) NOT NULL,
    payment_method  VARCHAR(50)    NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    canceled_reason VARCHAR(255),
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_saga_id (saga_id),
    UNIQUE INDEX idx_code (code),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS order_item
(
    id            INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id      INT           NOT NULL,
    product_id    INT           NOT NULL,
    product_price DECIMAL(9, 2) NOT NULL,
    quantity      INT           NOT NULL,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS sequence_generator
(
    seq_name   VARCHAR(50) PRIMARY KEY,
    seq_value  INT      NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- Saga tables

CREATE TABLE IF NOT EXISTS saga
(
    id            INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    context       TEXT,
    status        VARCHAR(20) NOT NULL,
    completed_at  DATETIME,
    error_message TEXT,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS saga_command
(
    id                         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    saga_id                    INT         NOT NULL,
    name                       VARCHAR(50) NOT NULL,
    target_service_name        VARCHAR(50) NOT NULL,
    status                     VARCHAR(20) NOT NULL,
    completed_at               DATETIME,
    error_message              TEXT,
    compensation_error_message TEXT,
    created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_saga_id (saga_id)
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
