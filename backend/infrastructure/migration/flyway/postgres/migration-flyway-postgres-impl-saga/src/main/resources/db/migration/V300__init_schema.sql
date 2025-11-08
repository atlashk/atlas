CREATE TABLE IF NOT EXISTS saga
(
    id            SERIAL      NOT NULL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    context       JSONB,
    status        VARCHAR(20) NOT NULL,
    completed_at  TIMESTAMP,
    error_message TEXT,
    created_at    TIMESTAMP   NOT NULL,
    updated_at    TIMESTAMP
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
    created_at                 TIMESTAMP   NOT NULL,
    updated_at                 TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_saga_command_saga_id ON saga_command (saga_id);
