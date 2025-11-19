CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    wallet_address VARCHAR(50) UNIQUE NOT NULL,
    wallet_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    balance DECIMAL(19,8) NOT NULL DEFAULT 0,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_default BOOLEAN DEFAULT FALSE,
    daily_limit DECIMAL(19,8),
    monthly_limit DECIMAL(19,8),
    daily_spent DECIMAL(19,8) DEFAULT 0,
    monthly_spent DECIMAL(19,8) DEFAULT 0,
    last_daily_reset TIMESTAMP,
    last_monthly_reset TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_wallet_address ON wallets(wallet_address);
CREATE INDEX idx_wallets_status ON wallets(status);
CREATE INDEX idx_wallets_type ON wallets(type);
CREATE INDEX idx_wallets_currency_code ON wallets(currency_code);
CREATE INDEX idx_wallets_created_at ON wallets(created_at);
