CREATE TABLE IF NOT EXISTS saga
(
    id           INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50) NOT NULL,
    context      JSON,
    status       VARCHAR(20) NOT NULL,
    completed_at DATETIME,
    error        VARCHAR(500),
    created_at   DATETIME    NOT NULL,
    updated_at   DATETIME
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS saga_command
(
    id                  INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    saga_id             INT         NOT NULL,
    name                VARCHAR(50) NOT NULL,
    target_service_name VARCHAR(50) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    completed_at        DATETIME,
    error               VARCHAR(500),
    compensation_error  VARCHAR(500),
    created_at          DATETIME    NOT NULL,
    updated_at          DATETIME,
    INDEX idx_saga_id (saga_id)
) ENGINE = InnoDB;
