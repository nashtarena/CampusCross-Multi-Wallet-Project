CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    source_wallet_id BIGINT,
    target_wallet_id BIGINT,
    amount DECIMAL(19,8) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19,8),
    original_amount DECIMAL(19,8),
    original_currency VARCHAR(3),
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description TEXT NOT NULL,
    reference_id VARCHAR(50),
    merchant_id VARCHAR(50),
    campus_location VARCHAR(100),
    external_transaction_id VARCHAR(100),
    fee_amount DECIMAL(19,8) DEFAULT 0,
    fee_currency VARCHAR(3) DEFAULT 'USD',
    processing_time_ms BIGINT,
    failure_reason TEXT,
    ip_address VARCHAR(45),
    device_fingerprint VARCHAR(255),
    is_flagged BOOLEAN DEFAULT FALSE,
    flag_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    FOREIGN KEY (source_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL,
    FOREIGN KEY (target_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_transactions_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_transactions_source_wallet_id ON transactions(source_wallet_id);
CREATE INDEX idx_transactions_target_wallet_id ON transactions(target_wallet_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_merchant_id ON transactions(merchant_id);
CREATE INDEX idx_transactions_campus_location ON transactions(campus_location);
CREATE INDEX idx_transactions_flagged ON transactions(is_flagged);
