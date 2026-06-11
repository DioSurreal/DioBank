-- V1__create_money_transactions.sql

CREATE SCHEMA IF NOT EXISTS ledger;

CREATE TABLE ledger.money_transactions (
    transaction_id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    transaction_type VARCHAR(64) NOT NULL,
    channel VARCHAR(64) NOT NULL,
    amount BIGINT NOT NULL CHECK (amount > 0),
    sender_bank_code VARCHAR(32) NOT NULL,
    receiver_bank_code VARCHAR(32) NOT NULL,
    from_account_id VARCHAR(128) NOT NULL,
    to_account_id VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,
    correlation_id VARCHAR(128) NOT NULL,
    request_id VARCHAR(128) NOT NULL,
    created_by VARCHAR(128) NOT NULL,
    actor_type VARCHAR(32) NOT NULL,
    client_ip VARCHAR(64),
    device_id VARCHAR(128),
    failure_reason VARCHAR(512),
    requested_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_money_transactions_status
        CHECK (status IN ('PENDING', 'POSTED', 'FAILED', 'REJECTED')),
    CONSTRAINT chk_money_transactions_completed_at
        CHECK (
            (status = 'PENDING' AND completed_at IS NULL)
            OR (status = 'POSTED' AND completed_at IS NOT NULL)
            OR (status = 'FAILED' AND completed_at IS NOT NULL)
            OR (status = 'REJECTED' AND completed_at IS NOT NULL)
        )
);

CREATE INDEX idx_money_transactions_status ON ledger.money_transactions (status);
CREATE INDEX idx_money_transactions_requested_at ON ledger.money_transactions (requested_at);
CREATE INDEX idx_money_transactions_completed_at ON ledger.money_transactions (completed_at);
CREATE INDEX idx_money_transactions_created_at ON ledger.money_transactions (created_at);

