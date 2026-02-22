CREATE TABLE IF NOT EXISTS users
(
    id           VARCHAR(64)  NOT NULL PRIMARY KEY,
    username     VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME,
    UNIQUE INDEX idx_username (username),
    UNIQUE INDEX idx_email (email),
    UNIQUE INDEX idx_phone_number (phone_number)
) ENGINE = InnoDB;
