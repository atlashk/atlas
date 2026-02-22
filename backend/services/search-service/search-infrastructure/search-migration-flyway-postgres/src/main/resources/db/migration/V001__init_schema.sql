CREATE TABLE IF NOT EXISTS brand
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product
(
    id             VARCHAR(64) PRIMARY KEY,
    name           VARCHAR(255)  NOT NULL,
    price          DECIMAL(9, 2) NOT NULL,
    quantity       INT           NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    available_from TIMESTAMP     NOT NULL,
    is_active      BOOLEAN       NOT NULL,
    brand_id       INT           NOT NULL,
    version        BIGINT DEFAULT 0,
    created_at     TIMESTAMP     NOT NULL,
    updated_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_details
(
    product_id  VARCHAR(64) PRIMARY KEY,
    description TEXT,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_attribute
(
    id         SERIAL PRIMARY KEY,
    product_id VARCHAR(64)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    value      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE (product_id, name)
);

CREATE TABLE IF NOT EXISTS product_category
(
    product_id  VARCHAR(64) NOT NULL,
    category_id INT         NOT NULL,
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP,
    PRIMARY KEY (product_id, category_id)
);
