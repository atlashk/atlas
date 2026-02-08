CREATE TABLE IF NOT EXISTS cart
(
    id         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    VARCHAR(64) NOT NULL,
    created_at DATETIME    NOT NULL,
    updated_at DATETIME,
    UNIQUE INDEX idx_user_id (user_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cart_item
(
    id         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cart_id    INT         NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    quantity   INT         NOT NULL,
    created_at DATETIME    NOT NULL,
    updated_at DATETIME,
    UNIQUE INDEX idx_cart_id_product_id (cart_id, product_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS orders
(
    id                     VARCHAR(64)    NOT NULL PRIMARY KEY,
    saga_id                INT,
    status                 VARCHAR(50)    NOT NULL,
    user_id                VARCHAR(64)    NOT NULL,
    user_first_name        VARCHAR(255)   NOT NULL,
    user_last_name         VARCHAR(255)   NOT NULL,
    user_email             VARCHAR(255)   NOT NULL,
    user_phone_number      VARCHAR(255)   NOT NULL,
    address_street         VARCHAR(255)   NOT NULL,
    address_city           VARCHAR(100)   NOT NULL,
    address_country        CHAR(2)        NOT NULL,
    address_postal_code    VARCHAR(20)    NOT NULL,
    amount                 DECIMAL(11, 2) NOT NULL,
    payment_gateway_id     INT            NOT NULL,
    payment_gateway_name   VARCHAR(255),
    payment_method         VARCHAR(50),
    payment_method_details TEXT,
    payment_transaction_id VARCHAR(255),
    cancellation_reason    VARCHAR(255),
    created_at             DATETIME       NOT NULL,
    updated_at             DATETIME,
    UNIQUE INDEX idx_saga_id (saga_id),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS order_item
(
    id            INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id      VARCHAR(64)   NOT NULL,
    product_id    VARCHAR(64)   NOT NULL,
    product_name  VARCHAR(255)  NOT NULL,
    product_price DECIMAL(9, 2) NOT NULL,
    quantity      INT           NOT NULL,
    created_at    DATETIME      NOT NULL,
    updated_at    DATETIME,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE = InnoDB;
