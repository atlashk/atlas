CREATE TABLE IF NOT EXISTS payment
(
    id                  SERIAL         NOT NULL PRIMARY KEY,
    user_id             INTEGER        NOT NULL,
    order_id            INTEGER        NOT NULL UNIQUE,
    saga_id             INTEGER        NOT NULL UNIQUE,
    amount              DECIMAL(19, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL,
    method              VARCHAR(50)    NOT NULL,
    gateway             VARCHAR(50)    NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    transaction_id      VARCHAR(255),
    next_action         TEXT,
    error_code          VARCHAR(100),
    error_message       VARCHAR(500),
    cancellation_reason VARCHAR(500),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for payment table
CREATE INDEX IF NOT EXISTS idx_payment_user_id ON payment (user_id);

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
CREATE TRIGGER trg_audit_payment
    BEFORE UPDATE
    ON payment
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();
