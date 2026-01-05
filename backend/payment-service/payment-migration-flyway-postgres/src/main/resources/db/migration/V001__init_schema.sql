CREATE TABLE IF NOT EXISTS payment_gateway
(
    id         SERIAL       NOT NULL PRIMARY KEY,
    code       VARCHAR(50)  NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment
(
    id                     SERIAL         NOT NULL PRIMARY KEY,
    user_id                INTEGER        NOT NULL,
    order_id               INTEGER        NOT NULL UNIQUE,
    saga_id                INTEGER        NOT NULL UNIQUE,
    amount                 DECIMAL(19, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL,
    payment_gateway_id     INTEGER        NOT NULL,
    payment_method         VARCHAR(50),
    payment_method_details JSONB,
    status                 VARCHAR(50)    NOT NULL,
    transaction_id         VARCHAR(255),
    next_action            JSONB,
    error                  VARCHAR(500),
    cancellation_reason    VARCHAR(500),
    created_at             TIMESTAMP      NOT NULL,
    updated_at             TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_payment_user_id ON payment (user_id);
CREATE INDEX IF NOT EXISTS idx_payment_payment_gateway_id ON payment (payment_gateway_id);

CREATE TABLE IF NOT EXISTS payment_event
(
    id                 SERIAL      NOT NULL PRIMARY KEY,
    payment_gateway_id INTEGER     NOT NULL,
    payment_id         INTEGER,
    payload            TEXT        NOT NULL,
    headers            TEXT,
    status             VARCHAR(50) NOT NULL,
    error              VARCHAR(500),
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_payment_event_payment_gateway_id ON payment_event (payment_gateway_id);
CREATE INDEX IF NOT EXISTS idx_payment_event_payment_id ON payment_event (payment_id);
