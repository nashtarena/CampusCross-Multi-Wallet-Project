-- CampusCross Wallet Database Schema
-- Week 1 - Core Tables for Multi-Currency Wallet

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    student_id VARCHAR(50) UNIQUE,
    mobile_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Wallets table (one wallet per user)
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'suspended', 'closed')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Currency accounts (sub-ledger for each currency per wallet)
CREATE TABLE currency_accounts (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL CHECK (currency_code IN ('USD', 'EUR', 'GBP', 'JPY')),
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE,
    UNIQUE (wallet_id, currency_code)
);

-- Transactions table (double-entry ledger)
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('P2P', 'CONVERSION', 'CAMPUS', 'REMITTANCE', 'DISBURSEMENT')),
    from_account_id BIGINT,
    to_account_id BIGINT,
    amount DECIMAL(19, 4) NOT NULL CHECK (amount > 0),
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'failed', 'cancelled')),
    reference_id VARCHAR(100) UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (from_account_id) REFERENCES currency_accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES currency_accounts(id)
);

-- Audit log table (for compliance and tracking)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Add triggers to auto-update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_currency_accounts_updated_at BEFORE UPDATE ON currency_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();