# --- !Ups

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    cash_balance DECIMAL NOT NULL DEFAULT 100000.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    symbol VARCHAR(50) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE holdings (
    user_id BIGINT NOT NULL REFERENCES users(id),
    symbol VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    average_buy_price DECIMAL NOT NULL,
    PRIMARY KEY (user_id, symbol)
);

CREATE TABLE price_cache (
    symbol VARCHAR(50) PRIMARY KEY,
    price DECIMAL NOT NULL,
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE price_cache;
DROP TABLE holdings;
DROP TABLE transactions;
DROP TABLE users;