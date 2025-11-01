CREATE TABLE IF NOT EXISTS outbox_message
(
    id           SERIAL PRIMARY KEY,
    message      TEXT        NOT NULL,
    status       VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP,
    error        TEXT,
    retries      INT                  DEFAULT 0,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);

-- Trigger function for auditing
CREATE OR REPLACE FUNCTION fn_audit()
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
CREATE TRIGGER trg_audit_outbox_message
    BEFORE UPDATE
    ON outbox_message
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();
