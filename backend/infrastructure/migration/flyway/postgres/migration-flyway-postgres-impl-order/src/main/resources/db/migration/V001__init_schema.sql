CREATE TABLE IF NOT EXISTS orders
(
    id                  SERIAL PRIMARY KEY,
    saga_id             INT,
    code                CHAR(7)        NOT NULL UNIQUE,
    status              VARCHAR(50)    NOT NULL,
    user_id             INT            NOT NULL,
    address_street      VARCHAR(255)   NOT NULL,
    address_city        VARCHAR(100)   NOT NULL,
    address_country     CHAR(2)        NOT NULL,
    address_postal_code VARCHAR(20)    NOT NULL,
    amount              NUMERIC(11, 2) NOT NULL,
    payment_gateway_id  INT            NOT NULL,
    cancellation_reason VARCHAR(255),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_saga_id ON orders (saga_id);
CREATE INDEX idx_user_id ON orders (user_id);

CREATE TABLE IF NOT EXISTS order_item
(
    id            SERIAL PRIMARY KEY,
    order_id      INT           NOT NULL,
    product_id    INT           NOT NULL,
    product_price NUMERIC(9, 2) NOT NULL,
    quantity      INT           NOT NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP              DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_order_id ON order_item (order_id);
CREATE INDEX idx_product_id ON order_item (product_id);

CREATE TABLE IF NOT EXISTS sequence_generator
(
    seq_name   VARCHAR(50) PRIMARY KEY,
    seq_value  INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP          DEFAULT CURRENT_TIMESTAMP
);

-- Trigger function for auditing
CREATE OR REPLACE FUNCTION fn_audit()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;

-- Apply triggers to tables
CREATE TRIGGER trg_audit_orders
    BEFORE UPDATE
    ON orders
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit_order_item
    BEFORE UPDATE
    ON order_item
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit_sequence_generator
    BEFORE UPDATE
    ON sequence_generator
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();
