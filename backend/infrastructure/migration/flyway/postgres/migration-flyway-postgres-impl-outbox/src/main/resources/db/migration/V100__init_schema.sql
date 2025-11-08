CREATE TABLE IF NOT EXISTS outbox_message
(
    id           SERIAL PRIMARY KEY,
    message      TEXT        NOT NULL,
    status       VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP,
    error        TEXT,
    retries      INT DEFAULT 0,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP
);
