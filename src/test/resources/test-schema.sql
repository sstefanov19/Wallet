-- Test schema for repository tests

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    subscription_status VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS transfer (
    id BIGSERIAL PRIMARY KEY,
    from_wallet BIGINT NOT NULL REFERENCES wallet(id),
    to_wallet BIGINT NOT NULL REFERENCES wallet(id),
    currency VARCHAR(3) NOT NULL,
    transfer_amount NUMERIC(18, 2) NOT NULL,
    transfer_date TIMESTAMP WITH TIME ZONE
);