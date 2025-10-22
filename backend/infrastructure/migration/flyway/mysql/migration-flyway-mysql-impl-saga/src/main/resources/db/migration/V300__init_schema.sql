CREATE TABLE IF NOT EXISTS saga
(
    id            INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    context       TEXT,
    status        VARCHAR(20) NOT NULL,
    completed_at  DATETIME,
    error_message TEXT,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS saga_command
(
    id                         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    saga_id                    INT         NOT NULL,
    name                       VARCHAR(50) NOT NULL,
    target_service_name        VARCHAR(50) NOT NULL,
    status                     VARCHAR(20) NOT NULL,
    completed_at               DATETIME,
    error_message              TEXT,
    compensation_error_message TEXT,
    created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_saga_id (saga_id)
) ENGINE = InnoDB;
