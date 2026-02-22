CREATE TABLE IF NOT EXISTS brand
(
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS category
(
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS product
(
    id             VARCHAR(64)   NOT NULL PRIMARY KEY,
    name           VARCHAR(255)  NOT NULL,
    price          DECIMAL(9, 2) NOT NULL,
    quantity       INT           NOT NULL,
    stock_status   VARCHAR(20)   NOT NULL,
    available_from DATETIME      NOT NULL,
    is_active      TINYINT(1)    NOT NULL,
    brand_id       INT           NOT NULL,
    version        BIGINT DEFAULT 0,
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS product_details
(
    product_id  VARCHAR(64) NOT NULL PRIMARY KEY,
    description TEXT,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS product_attribute
(
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(64)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    value      VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME,
    INDEX idx_product_id (product_id),
    UNIQUE INDEX idx_product_id_name (product_id, name)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS product_category
(
    product_id  VARCHAR(64) NOT NULL,
    category_id INT         NOT NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (product_id, category_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS reservation
(
    id         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id   VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    quantity   INT         NOT NULL,
    created_at DATETIME    NOT NULL,
    updated_at DATETIME,
    UNIQUE INDEX idx_order_id_product_id (order_id, product_id)
) ENGINE = InnoDB;
