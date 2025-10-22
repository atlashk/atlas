CREATE TABLE IF NOT EXISTS orders
(
    id                  SERIAL PRIMARY KEY,
    code                CHAR(7)        NOT NULL UNIQUE,
    saga_id             INT            NOT NULL UNIQUE,
    user_id             INT            NOT NULL,
    amount              NUMERIC(11, 2) NOT NULL,
    payment_method      VARCHAR(50)    NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    cancellation_reason VARCHAR(255),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);
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
CREATE
    OR REPLACE FUNCTION fn_audit()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;

-- Apply triggers to tables
CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON orders
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON order_item
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON sequence_generator
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();
