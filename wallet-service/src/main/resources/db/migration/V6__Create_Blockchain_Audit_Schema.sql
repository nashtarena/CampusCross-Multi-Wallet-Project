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
    current_hash VARCHAR(64) NOT NULL UNIQUE,
    previous_hash VARCHAR(64) NOT NULL,
    merkle_root VARCHAR(64),
    nonce BIGINT DEFAULT 0,
    
    -- Data fields
    event_data JSONB NOT NULL,
    metadata JSONB,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    
    -- Integrity check
    is_verified BOOLEAN DEFAULT FALSE,
    tamper_detected BOOLEAN DEFAULT FALSE,
    
    -- Foreign key only, no @ManyToOne in entity
    CONSTRAINT fk_blockchain_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Genesis block (block 0) - foundation of the chain
INSERT INTO blockchain_audit_chain (
    block_number, audit_id, event_type, entity_type, entity_id, 
    current_hash, previous_hash, merkle_root, event_data, nonce
) VALUES (
    0, 
    'GENESIS-BLOCK', 
    'GENESIS', 
    'SYSTEM', 
    0,
    '0000000000000000000000000000000000000000000000000000000000000000',
    '0000000000000000000000000000000000000000000000000000000000000000',
    '0000000000000000000000000000000000000000000000000000000000000000',
    '{"message": "CampusCross Wallet Blockchain Audit System Initialized", "version": "1.0", "initialized": "2024-01-01T00:00:00"}'::jsonb,
    0
);

-- Indexes for performance
CREATE INDEX idx_blockchain_audit_block_number ON blockchain_audit_chain(block_number);
CREATE INDEX idx_blockchain_audit_entity ON blockchain_audit_chain(entity_type, entity_id);
CREATE INDEX idx_blockchain_audit_user ON blockchain_audit_chain(user_id);
CREATE INDEX idx_blockchain_audit_created ON blockchain_audit_chain(created_at DESC);
CREATE INDEX idx_blockchain_audit_hash ON blockchain_audit_chain(current_hash);
CREATE INDEX idx_blockchain_audit_prev_hash ON blockchain_audit_chain(previous_hash);
CREATE INDEX idx_blockchain_audit_event_type ON blockchain_audit_chain(event_type);

-- Audit verification log
CREATE TABLE blockchain_verification_log (
    id BIGSERIAL PRIMARY KEY,
    verification_id VARCHAR(100) NOT NULL UNIQUE,
    start_block BIGINT NOT NULL,
    end_block BIGINT NOT NULL,
    total_blocks BIGINT NOT NULL,
    verified_blocks BIGINT NOT NULL,
    tampered_blocks BIGINT DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'TAMPERED')),
    verification_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB
);

CREATE INDEX idx_verification_log_created ON blockchain_verification_log(created_at DESC);
CREATE INDEX idx_verification_log_status ON blockchain_verification_log(status);

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

-- Trigger to prevent genesis block modification
CREATE OR REPLACE FUNCTION prevent_genesis_modification()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.block_number = 0 THEN
        RAISE EXCEPTION 'Cannot modify genesis block';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_genesis_update
BEFORE UPDATE ON blockchain_audit_chain
FOR EACH ROW
EXECUTE FUNCTION prevent_genesis_modification();

-- Trigger to prevent genesis block deletion
CREATE OR REPLACE FUNCTION prevent_genesis_deletion()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.block_number = 0 THEN
        RAISE EXCEPTION 'Cannot delete genesis block';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_genesis_delete
BEFORE DELETE ON blockchain_audit_chain
FOR EACH ROW
EXECUTE FUNCTION prevent_genesis_deletion();