CREATE TABLE IF NOT EXISTS notification
(
    id              INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         INT         NOT NULL,
    type            VARCHAR(50) NOT NULL,
    channel         VARCHAR(50) NOT NULL,
    message         TEXT,
    metadata        JSON,
    delivery_status VARCHAR(50) NOT NULL,
    delivery_error  VARCHAR(500),
    read_at         DATETIME,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE = InnoDB;
