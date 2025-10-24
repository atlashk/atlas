CREATE TABLE IF NOT EXISTS outbox_message
(
    id           INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message      TEXT        NOT NULL,
    status       VARCHAR(50) NOT NULL,
    processed_at DATETIME,
    error        TEXT,
    retries      INT                  DEFAULT 0,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;
