CREATE TABLE IF NOT EXISTS users
(
    id           VARCHAR(64) PRIMARY KEY,
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) UNIQUE,
    password     VARCHAR(255), -- Password can be NULL for users authenticated via external providers
    role         VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS federated_identity
(
    user_id          VARCHAR(64)  NOT NULL,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP,
    CONSTRAINT pk_federated_identity PRIMARY KEY (user_id, provider),
    CONSTRAINT uq_provider_provider_user_id UNIQUE (provider, provider_user_id)
);
