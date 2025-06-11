CREATE DATABASE IF NOT EXISTS db_auth;

USE db_auth;

CREATE TABLE IF NOT EXISTS users
(
    id      INT          NOT NULL PRIMARY KEY,
    username     VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_username (username),
    UNIQUE INDEX idx_email (email),
    UNIQUE INDEX idx_phone_number (phone_number)
) ENGINE = InnoDB;

-- Password: Aa@123456
INSERT INTO users (id, username, password, email, phone_number, role)
VALUES (1, 'admin', '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2',
        'admin@atlas.org', '0987654321', 'ADMIN'),
       (2, 'user', '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2', 'user@atlas.org',
        '0987321654', 'USER');

CREATE TABLE session
(
    id            VARCHAR(255) PRIMARY KEY,
    user_id       INT          NOT NULL,
    device_id     VARCHAR(255) NOT NULL,
    refresh_token VARCHAR(1000) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
