CREATE TABLE IF NOT EXISTS users(
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    subscription_status VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet(
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS transfer(
    id SERIAL PRIMARY KEY,
    from_wallet BIGINT REFERENCES wallet(id) NOT NULL,
    to_wallet   BIGINT REFERENCES wallet(id) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transfer_amount NUMERIC(18, 2) NOT NULL,
    transfer_date TIMESTAMP WITH TIME ZONE
);