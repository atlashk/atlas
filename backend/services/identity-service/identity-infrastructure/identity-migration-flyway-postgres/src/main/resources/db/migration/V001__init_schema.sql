CREATE TABLE IF NOT EXISTS users
(
    id           VARCHAR(64) PRIMARY KEY,
    username     VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255), -- Password can be NULL for users authenticated via external providers
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    role         VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP
);
