-- Test schema for repository tests

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    subscription_status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);