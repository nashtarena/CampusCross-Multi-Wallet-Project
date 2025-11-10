-- Risk Scores Table for Fraud Detection

CREATE TABLE risk_scores (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    risk_score DECIMAL(5, 2) NOT NULL CHECK (risk_score >= 0 AND risk_score <= 100),
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    risk_factors TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_risk_scores_transaction_id ON risk_scores(transaction_id);
CREATE INDEX idx_risk_scores_user_id ON risk_scores(user_id);
CREATE INDEX idx_risk_scores_risk_level ON risk_scores(risk_level);
CREATE INDEX idx_risk_scores_status ON risk_scores(status);