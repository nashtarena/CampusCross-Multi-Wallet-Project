-- V2__Add_Performance_Indexes.sql

-- Index for quick lookups by user
CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);

-- Indexes for transaction lookups
CREATE INDEX IF NOT EXISTS idx_transactions_from_account_id ON transactions(from_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account_id ON transactions(to_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);

-- Index for audit logs by user and entity
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_id ON audit_logs(entity_id);
