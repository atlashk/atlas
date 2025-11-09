CREATE TABLE IF NOT EXISTS outbox_message
(
    id           INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message      TEXT        NOT NULL,
    status       VARCHAR(50) NOT NULL,
    processed_at DATETIME,
    error        VARCHAR(500),
    retries      INT DEFAULT 0,
    created_at   DATETIME    NOT NULL,
    updated_at   DATETIME
) ENGINE = InnoDB;
