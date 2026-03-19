CREATE TABLE IF NOT EXISTS users
(
    id          VARCHAR(64)  NOT NULL PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    password    VARCHAR(255), -- Password can be NULL for users authenticated via external providers
    role        VARCHAR(20)  NOT NULL,
    idp_user_id VARCHAR(255),
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    UNIQUE INDEX idx_email (email),
    UNIQUE INDEX idx_phone_number (phone_number)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS federated_identity
(
    user_id          VARCHAR(64)  NOT NULL,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME,
    PRIMARY KEY (user_id, provider),
    UNIQUE INDEX idx_provider_provider_user_id (provider, provider_user_id)
) ENGINE = InnoDB;
