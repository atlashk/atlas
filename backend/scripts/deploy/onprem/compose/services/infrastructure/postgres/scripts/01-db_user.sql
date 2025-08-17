CREATE DATABASE db_user;

\c db_user;

-- Create tables
CREATE TABLE IF NOT EXISTS users
(
    id           SERIAL PRIMARY KEY,
    username     VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    role         VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outbox_message
(
    id              BIGSERIAL PRIMARY KEY,
    message_payload TEXT         NOT NULL,
    message_class   VARCHAR(255) NOT NULL,
    message_key     VARCHAR(255) NOT NULL,
    destination     VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    processed_at    TIMESTAMP,
    error           TEXT,
    retries         INT                   DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Trigger function for auditing
CREATE OR REPLACE FUNCTION fn_audit()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers to tables
CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();

CREATE TRIGGER trg_audit
    BEFORE UPDATE
    ON outbox_message
    FOR EACH ROW
EXECUTE FUNCTION fn_audit();

-- Seed data
INSERT INTO users (id,
                   username,
                   password,
                   first_name,
                   last_name,
                   email,
                   phone_number,
                   role)
VALUES (1,
        'admin',
        '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2',
        'Atlas',
        'Admin',
        '0nfyGkH+0gr94SirOesbVBiKm53ZvmKJ6eHb6S4Rkykgs2u2hlsW9SL0/g==',
        'IztcSvy+JXBxWWk3Q+1RIrAW8JOqPqVa0HePA+UPxYS0FgL9Yq4=',
        'ADMIN'),
       (2,
        'user',
        '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2',
        'John',
        'Doe',
        '8vheIMl1kmFVlPzc9NDbpocdNNroW7BZZOzB/mla3ku3vSaseCg7mtwA',
        'e5k0u/kv8e5KgiWN50y9+x1MYIIvT6h9JtWl5+b7o8j3Yuf8Bwg=',
        'USER');
