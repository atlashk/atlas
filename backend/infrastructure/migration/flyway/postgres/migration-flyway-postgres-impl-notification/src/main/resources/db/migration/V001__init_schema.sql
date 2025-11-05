CREATE TABLE IF NOT EXISTS notification
(
    id              SERIAL PRIMARY KEY,
    user_id         INT         NOT NULL,
    type            VARCHAR(50) NOT NULL,
    channel         VARCHAR(50) NOT NULL,
    message         TEXT,
    metadata        JSONB,
    delivered_at    TIMESTAMP,
    delivery_status VARCHAR(50) NOT NULL,
    delivery_error  VARCHAR(500),
    read_at         TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_id ON notification (user_id);
CREATE INDEX idx_type ON notification (type);

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

-- Apply trigger to notification table
CREATE TRIGGER trg_audit_notification
    BEFORE UPDATE
    ON notification
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();
