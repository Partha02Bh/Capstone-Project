-- Reward Module: add rewards table
-- Run this after existing schema.sql

CREATE TABLE IF NOT EXISTS rewards (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT NOT NULL,
    account_id         BIGINT NOT NULL,
    transaction_id     BIGINT,                     -- links to transactions.id
    transaction_amount DECIMAL(15, 2) NOT NULL,
    points_earned      INT NOT NULL DEFAULT 0,
    awarded_at         DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (account_id) REFERENCES accounts(id)
    -- transaction_id is a soft link (no FK) since transactions table uses a separate PK strategy
);