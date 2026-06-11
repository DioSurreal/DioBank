-- V1__create_account_balance.sql
-- Description: Creates the account_balance table.
-- This table is an operational balance projection used heavily for transaction processing.
-- It is owned exclusively by the Ledger Service.

CREATE TABLE account_balance (
    -- account_id: Unique identifier for the account, mapped to Account Service's ID
    account_id UUID PRIMARY KEY,

    -- balance: The operational balance stored in satang (THB only).
    -- This field is protected by pessimistic locking during transactions.
    balance BIGINT NOT NULL,

    -- updated_at: Timestamp of the last balance mutation.
    updated_at TIMESTAMPTZ NOT NULL,

    -- Constraint: Balance must never fall below 0 (no overdraft allowed)
    CONSTRAINT chk_account_balance_non_negative CHECK (balance >= 0)
);

-- Note: An index is implicitly created on the PRIMARY KEY (account_id).
-- Since queries will always lock/fetch by account_id, no additional indexes are required here.
