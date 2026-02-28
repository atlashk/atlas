CREATE TABLE IF NOT EXISTS brand
(
    id         VARCHAR(64) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category
(
    id         VARCHAR(64) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product
(
    id           VARCHAR(64) PRIMARY KEY,
    name         VARCHAR(255)  NOT NULL,
    type         VARCHAR(20)   NOT NULL,
    price        DECIMAL(9, 2) NOT NULL,
    published_at TIMESTAMP     NOT NULL,
    in_stock     BOOLEAN       NOT NULL,
    brand_id     VARCHAR(64)   NOT NULL,
    version      BIGINT DEFAULT 0,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP
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
    category_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (product_id, category_id)
);
