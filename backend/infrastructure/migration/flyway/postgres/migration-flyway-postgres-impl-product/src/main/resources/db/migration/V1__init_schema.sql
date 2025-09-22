CREATE TABLE IF NOT EXISTS brand
(
    id
    SERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS category
(
    id
    SERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS product
(
    id
    SERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    price DECIMAL
(
    9,
    2
) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR
(
    20
) NOT NULL,
    available_from TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL,
    brand_id INT NOT NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS product_details
(
    product_id
    INT
    PRIMARY
    KEY,
    description
    TEXT,
    created_at
    TIMESTAMP
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,
    updated_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_attribute
(
    id
    SERIAL
    PRIMARY
    KEY,
    product_id
    INT
    NOT
    NULL,
    name
    VARCHAR
(
    255
) NOT NULL,
    value VARCHAR
(
    255
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE
(
    product_id,
    name
)
    );

CREATE TABLE IF NOT EXISTS product_category
(
    product_id
    INT
    NOT
    NULL,
    category_id
    INT
    NOT
    NULL,
    created_at
    TIMESTAMP
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,
    updated_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP,
    PRIMARY
    KEY
(
    product_id,
    category_id
)
    );

CREATE TABLE IF NOT EXISTS outbox_message
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    message_payload
    TEXT
    NOT
    NULL,
    message_class
    VARCHAR
(
    255
) NOT NULL,
    message_key VARCHAR
(
    255
) NOT NULL,
    destination VARCHAR
(
    255
) NOT NULL,
    status VARCHAR
(
    20
) NOT NULL,
    processed_at TIMESTAMP,
    error TEXT,
    retries INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Trigger function for auditing
CREATE
OR REPLACE FUNCTION fn_audit()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at
= now();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Apply triggers to tables
CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON brand
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON category
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON product
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON product_details
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON product_attribute
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON product_category
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON outbox_message
    FOR EACH ROW
    EXECUTE FUNCTION fn_audit();
