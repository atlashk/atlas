CREATE TABLE IF NOT EXISTS sequence_generator
(
    seq_name   VARCHAR(50) PRIMARY KEY,
    seq_value  INT      NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE = InnoDB;
