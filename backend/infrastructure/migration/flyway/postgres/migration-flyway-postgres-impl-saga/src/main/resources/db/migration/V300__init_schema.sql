CREATE TABLE IF NOT EXISTS saga
(
    id            SERIAL      NOT NULL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    context       TEXT,
    status        VARCHAR(20) NOT NULL,
    completed_at  TIMESTAMP,
    error_message TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS saga_command
(
    id                         SERIAL      NOT NULL PRIMARY KEY,
    saga_id                    INTEGER     NOT NULL,
    name                       VARCHAR(50) NOT NULL,
    target_service_name        VARCHAR(50) NOT NULL,
    status                     VARCHAR(20) NOT NULL,
    completed_at               TIMESTAMP,
    error_message              TEXT,
    compensation_error_message TEXT,
    created_at                 TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_saga_command_saga_id ON saga_command (saga_id);

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
CREATE TRIGGER trg_audit_saga
    BEFORE UPDATE
    ON saga
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit_saga_command
    BEFORE UPDATE
    ON saga_command
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();
