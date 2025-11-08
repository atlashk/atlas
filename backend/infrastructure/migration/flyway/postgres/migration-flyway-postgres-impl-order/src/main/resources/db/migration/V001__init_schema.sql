CREATE TABLE IF NOT EXISTS orders
(
    id                     SERIAL PRIMARY KEY,
    saga_id                INT,
    code                   CHAR(7)        NOT NULL UNIQUE,
    status                 VARCHAR(50)    NOT NULL,
    user_id                INT            NOT NULL,
    user_first_name        VARCHAR(255)   NOT NULL,
    user_last_name         VARCHAR(255)   NOT NULL,
    user_email             VARCHAR(255)   NOT NULL,
    user_phone_number      VARCHAR(255)   NOT NULL,
    address_street         VARCHAR(255)   NOT NULL,
    address_city           VARCHAR(100)   NOT NULL,
    address_country        CHAR(2)        NOT NULL,
    address_postal_code    VARCHAR(20)    NOT NULL,
    amount                 NUMERIC(11, 2) NOT NULL,
    payment_gateway_id     INT            NOT NULL,
    payment_gateway_name   VARCHAR(255),
    payment_method         VARCHAR(50),
    payment_method_details TEXT,
    payment_transaction_id VARCHAR(255),
    cancellation_reason    VARCHAR(255),
    created_at             TIMESTAMP      NOT NULL,
    updated_at             TIMESTAMP
);
CREATE INDEX idx_saga_id ON orders (saga_id);
CREATE INDEX idx_user_id ON orders (user_id);

CREATE TABLE IF NOT EXISTS order_item
(
    id            SERIAL PRIMARY KEY,
    order_id      INT           NOT NULL,
    product_id    INT           NOT NULL,
    product_name  VARCHAR(255)  NOT NULL,
    product_price NUMERIC(9, 2) NOT NULL,
    quantity      INT           NOT NULL,
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP
);
CREATE INDEX idx_order_id ON order_item (order_id);
CREATE INDEX idx_product_id ON order_item (product_id);

CREATE TABLE IF NOT EXISTS sequence_generator
(
    seq_name   VARCHAR(50) PRIMARY KEY,
    seq_value  INT       NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
