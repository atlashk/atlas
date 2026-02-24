CREATE TABLE IF NOT EXISTS stock
(
    product_id         VARCHAR(64) NOT NULL PRIMARY KEY,
    available_quantity INT         NOT NULL,
    reserved_quantity  INT         NOT NULL,
    created_at         TIMESTAMP   NOT NULL,
    updated_at         TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservation
(
    id         SERIAL      NOT NULL PRIMARY KEY,
    order_id   VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    quantity   INT         NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP
);
CREATE UNIQUE INDEX idx_order_id_product_id ON reservation (order_id, product_id);
