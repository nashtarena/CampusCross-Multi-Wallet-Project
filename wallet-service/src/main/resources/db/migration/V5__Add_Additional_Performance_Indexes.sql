-- V5__Add_Additional_Performance_Indexes.sql

-- Composite index for transaction queries by account and status
CREATE INDEX idx_transactions_from_status ON transactions(from_account_id, status);
CREATE INDEX idx_transactions_to_status ON transactions(to_account_id, status);

-- Index for transaction type filtering
CREATE INDEX idx_transactions_type_created ON transactions(transaction_type, created_at DESC);

-- Index for risk score queries
CREATE INDEX idx_risk_scores_user_level ON risk_scores(user_id, risk_level);
CREATE INDEX idx_risk_scores_created ON risk_scores(created_at DESC);

-- Index for disbursement batch queries
CREATE INDEX idx_disbursement_batches_created ON disbursement_batches(created_at DESC);
CREATE INDEX idx_disbursement_items_batch_status ON disbursement_items(batch_id, status);

-- Index for user lookups
CREATE INDEX idx_users_email_lower ON users(LOWER(email));
CREATE INDEX idx_users_student_id_lower ON users(LOWER(student_id));

-- Partial index for pending transactions (common query)
CREATE INDEX idx_transactions_pending ON transactions(created_at DESC) WHERE status = 'pending';

-- Partial index for active wallets
CREATE INDEX idx_wallets_active ON wallets(user_id) WHERE status = 'active';

-- Index for audit log queries by date
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id, created_at DESC);

-- Add missing foreign key indexes (if not already covered)
CREATE INDEX IF NOT EXISTS idx_currency_accounts_wallet_currency ON currency_accounts(wallet_id, currency_code);

-- Update table statistics
ANALYZE users;
ANALYZE wallets;
ANALYZE currency_accounts;
ANALYZE transactions;
ANALYZE risk_scores;
ANALYZE disbursement_batches;
ANALYZE disbursement_items;