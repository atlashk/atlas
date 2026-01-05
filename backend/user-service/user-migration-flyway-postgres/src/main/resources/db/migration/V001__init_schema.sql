CREATE TABLE IF NOT EXISTS users
(
    id           SERIAL PRIMARY KEY,
    username     VARCHAR(255) NOT NULL UNIQUE,
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    role         VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP
);
