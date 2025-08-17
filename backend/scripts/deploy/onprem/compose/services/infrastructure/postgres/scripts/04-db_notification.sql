CREATE DATABASE db_notification;

\c db_notification;

-- Create tables
CREATE TABLE IF NOT EXISTS outbox_message (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    message_payload TEXT NOT NULL,
    message_class VARCHAR(255) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP,
    error TEXT,
    retries INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger function for auditing
CREATE OR REPLACE FUNCTION fn_audit()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers to tables
CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON outbox_message
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();
