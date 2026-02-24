CREATE TABLE IF NOT EXISTS stock
(
    product_id         VARCHAR(64) NOT NULL PRIMARY KEY,
    available_quantity INT         NOT NULL,
    reserved_quantity  INT         NOT NULL,
    created_at         DATETIME    NOT NULL,
    updated_at         DATETIME
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
