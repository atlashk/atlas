CREATE DATABASE IF NOT EXISTS db_user;

USE db_user;

-- Create tables
CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_username (username),
    UNIQUE INDEX idx_email (email),
    UNIQUE INDEX idx_phone_number (phone_number)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS outbox_message (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message_payload TEXT NOT NULL,
    message_class VARCHAR(255) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at DATETIME,
    error TEXT,
    retries INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- Seed data
-- Admin: Aa@123456 | admin@atlas.org | 0123456789
-- User: Aa@123456 | user@atlas.org | 0987654321
INSERT INTO
    users (
        id,
        username,
        password,
        first_name,
        last_name,
        email,
        phone_number,
        role
    )
VALUES
    (
        1,
        'admin',
        '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2',
        'Atlas',
        'Admin',
        '0nfyGkH+0gr94SirOesbVBiKm53ZvmKJ6eHb6S4Rkykgs2u2hlsW9SL0/g==',
        'IztcSvy+JXBxWWk3Q+1RIrAW8JOqPqVa0HePA+UPxYS0FgL9Yq4=',
        'ADMIN'
    ),
    (
        2,
        'user',
        '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2',
        'John',
        'Doe',
        '8vheIMl1kmFVlPzc9NDbpocdNNroW7BZZOzB/mla3ku3vSaseCg7mtwA',
        'e5k0u/kv8e5KgiWN50y9+x1MYIIvT6h9JtWl5+b7o8j3Yuf8Bwg=',
        'USER'
    );
