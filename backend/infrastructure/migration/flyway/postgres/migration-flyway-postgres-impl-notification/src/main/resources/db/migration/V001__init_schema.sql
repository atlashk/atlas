CREATE TABLE IF NOT EXISTS notification
(
    id              SERIAL PRIMARY KEY,
    user_id         INT         NOT NULL,
    type            VARCHAR(50) NOT NULL,
    channel         VARCHAR(50) NOT NULL,
    message         TEXT,
    metadata        JSONB,
    delivery_status VARCHAR(50) NOT NULL,
    delivery_error  VARCHAR(500),
    read_at         TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP
);
CREATE INDEX idx_user_id ON notification (user_id);
CREATE INDEX idx_type ON notification (type);
