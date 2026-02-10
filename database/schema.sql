USE banking_system_db;

-- 1. USERS (With detailed KYC info)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15), 
    role VARCHAR(20) NOT NULL, -- 'ROLE_USER' or 'ROLE_ADMIN'
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. ACCOUNTS (1 User can have multiple accounts, e.g., Savings/Current)
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(16) UNIQUE NOT NULL, -- Real Banking Format (e.g., 908712345678)
    account_type VARCHAR(20) DEFAULT 'SAVINGS', -- SAVINGS, CURRENT
    balance DECIMAL(15, 2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, FROZEN
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. TRANSACTIONS (Using UUIDs for security & detailed tracking)
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(36) PRIMARY KEY, -- UUID (e.g., 550e8400-e29b...)
    source_account_number VARCHAR(16), 
    target_account_number VARCHAR(16),
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- TRANSFER, CREDIT, DEBIT
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED
    reason_code VARCHAR(100), -- Why it failed (e.g., "Insufficient Funds")
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
    -- Note: We link by Account Number string to allow external transfers in future
);
