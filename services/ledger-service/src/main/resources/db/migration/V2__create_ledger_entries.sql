-- V2__create_ledger_entries.sql
-- Description: Creates the ledger_entries table.
-- This table is the immutable accounting source of truth.
-- It strictly follows append-only properties for double-entry accounting.

CREATE TABLE ledger_entries (
    -- ledger_id: Unique identifier for the specific ledger entry row.
    ledger_id UUID PRIMARY KEY,

    -- transaction_id: Identifier connecting to the originating business transaction.
    -- Crucially, it acts as the idempotency key for network retries.
    transaction_id UUID NOT NULL,

    -- account_id: The account impacted by this specific entry.
    account_id UUID NOT NULL,

    -- direction: Defines if money is flowing IN (CREDIT) or OUT (DEBIT) of the account.
    direction VARCHAR(10) NOT NULL,

    -- amount: The financial value stored in satang (THB only).
    amount BIGINT NOT NULL,

    -- created_at: Immutable timestamp of when the entry was physically written.
    created_at TIMESTAMPTZ NOT NULL,

    -- Constraint: Direction must be strictly DEBIT or CREDIT.
    CONSTRAINT chk_ledger_entries_direction CHECK (direction IN ('DEBIT', 'CREDIT')),

    -- Constraint: Amount must always be positive (a 0 amount entry is meaningless in double-entry).
    CONSTRAINT chk_ledger_entries_amount_positive CHECK (amount > 0),

    -- Idempotency Constraint: A single transaction can only produce exactly ONE debit and ONE credit.
    -- This composite unique key prevents double-posting if the Transaction Service retries the gRPC call.
    CONSTRAINT uq_ledger_entries_tx_direction UNIQUE (transaction_id, direction)
);

-- Index: To efficiently query the immutable history of a specific account (e.g. for reconciliation jobs).
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries (account_id);

-- Note: Searching by transaction_id is heavily optimized by the implicit unique index 
-- created by uq_ledger_entries_tx_direction.
