-- Disbursement Tables for Bulk Payments

CREATE TABLE disbursement_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL UNIQUE,
    created_by BIGINT NOT NULL,
    total_count INTEGER NOT NULL,
    success_count INTEGER NOT NULL DEFAULT 0,
    failed_count INTEGER NOT NULL DEFAULT 0,
    total_amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE disbursement_items (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    transaction_id BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES disbursement_batches(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE INDEX idx_disbursement_batches_status ON disbursement_batches(status);
CREATE INDEX idx_disbursement_batches_created_by ON disbursement_batches(created_by);
CREATE INDEX idx_disbursement_items_batch_id ON disbursement_items(batch_id);
CREATE INDEX idx_disbursement_items_status ON disbursement_items(status);