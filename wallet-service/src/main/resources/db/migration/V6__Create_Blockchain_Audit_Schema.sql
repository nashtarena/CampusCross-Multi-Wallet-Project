-- Blockchain-style Audit Trail System
-- V6__Create_Blockchain_Audit_Schema.sql

CREATE TABLE blockchain_audit_chain (
    id BIGSERIAL PRIMARY KEY,
    block_number BIGINT NOT NULL UNIQUE,
    audit_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    user_id BIGINT,
    
    -- Blockchain-style fields
    current_hash VARCHAR(64) NOT NULL UNIQUE, -- SHA-256 of this block
    previous_hash VARCHAR(64) NOT NULL, -- Link to previous block
    merkle_root VARCHAR(64), -- Hash of all transactions in this block
    nonce BIGINT DEFAULT 0, -- For proof-of-work style validation
    
    -- Data fields
    event_data JSONB NOT NULL,
    metadata JSONB,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    
    -- Integrity check
    is_verified BOOLEAN DEFAULT FALSE,
    tamper_detected BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Genesis block (block 0) - foundation of the chain
INSERT INTO blockchain_audit_chain (
    block_number, audit_id, event_type, entity_type, entity_id, 
    current_hash, previous_hash, merkle_root, event_data
) VALUES (
    0, 
    'GENESIS-BLOCK', 
    'GENESIS', 
    'SYSTEM', 
    0,
    '0000000000000000000000000000000000000000000000000000000000000000',
    '0000000000000000000000000000000000000000000000000000000000000000',
    '0000000000000000000000000000000000000000000000000000000000000000',
    '{"message": "CampusCross Wallet Blockchain Audit System Initialized"}'::jsonb
);

-- Indexes for performance
CREATE INDEX idx_blockchain_audit_block_number ON blockchain_audit_chain(block_number);
CREATE INDEX idx_blockchain_audit_entity ON blockchain_audit_chain(entity_type, entity_id);
CREATE INDEX idx_blockchain_audit_user ON blockchain_audit_chain(user_id);
CREATE INDEX idx_blockchain_audit_created ON blockchain_audit_chain(created_at DESC);
CREATE INDEX idx_blockchain_audit_hash ON blockchain_audit_chain(current_hash);
CREATE INDEX idx_blockchain_audit_prev_hash ON blockchain_audit_chain(previous_hash);

-- Audit verification log
CREATE TABLE blockchain_verification_log (
    id BIGSERIAL PRIMARY KEY,
    verification_id VARCHAR(100) NOT NULL UNIQUE,
    start_block BIGINT NOT NULL,
    end_block BIGINT NOT NULL,
    total_blocks BIGINT NOT NULL,
    verified_blocks BIGINT NOT NULL,
    tampered_blocks BIGINT DEFAULT 0,
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, TAMPERED
    verification_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB
);

CREATE INDEX idx_verification_log_created ON blockchain_verification_log(created_at DESC);

-- Quick stats view
CREATE OR REPLACE VIEW blockchain_audit_stats AS
SELECT 
    COUNT(*) as total_blocks,
    MAX(block_number) as latest_block,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(DISTINCT entity_type) as entity_types,
    COUNT(*) FILTER (WHERE is_verified = true) as verified_blocks,
    COUNT(*) FILTER (WHERE tamper_detected = true) as tampered_blocks,
    MIN(created_at) as chain_start,
    MAX(created_at) as chain_end
FROM blockchain_audit_chain
WHERE block_number > 0;