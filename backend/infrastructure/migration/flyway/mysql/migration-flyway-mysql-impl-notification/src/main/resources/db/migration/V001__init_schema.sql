CREATE TABLE IF NOT EXISTS notification
(
    id              INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         INT         NOT NULL,
    type            VARCHAR(50) NOT NULL,
    channel         VARCHAR(50) NOT NULL,
    message         TEXT,
    metadata        JSON,
    delivered_at    DATETIME,
    delivery_status VARCHAR(50) NOT NULL,
    delivery_error  VARCHAR(500),
    read_at         DATETIME,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE = InnoDB;
