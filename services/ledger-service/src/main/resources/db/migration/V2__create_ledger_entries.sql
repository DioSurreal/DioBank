-- V2__create_ledger_entries.sql
CREATE TABLE ledger.ledger_entries (
    ledger_entry_id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_id VARCHAR(128) NOT NULL,
    direction VARCHAR(8) NOT NULL,
    amount BIGINT NOT NULL CHECK (amount > 0),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_ledger_entries_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES ledger.money_transactions (transaction_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_ledger_entries_direction
        CHECK (direction IN ('DEBIT', 'CREDIT'))
);

CREATE INDEX idx_ledger_entries_transaction_id ON ledger.ledger_entries (transaction_id);
CREATE INDEX idx_ledger_entries_account_id ON ledger.ledger_entries (account_id);
CREATE INDEX idx_ledger_entries_created_at ON ledger.ledger_entries (created_at);

CREATE OR REPLACE FUNCTION ledger.prevent_ledger_entries_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'ledger_entries are immutable and cannot be updated';
END;
$$;

CREATE OR REPLACE FUNCTION ledger.prevent_ledger_entries_delete()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'ledger_entries are immutable and cannot be deleted';
END;
$$;

CREATE TRIGGER trg_ledger_entries_no_update
BEFORE UPDATE ON ledger.ledger_entries
FOR EACH ROW
EXECUTE FUNCTION ledger.prevent_ledger_entries_update();

CREATE TRIGGER trg_ledger_entries_no_delete
BEFORE DELETE ON ledger.ledger_entries
FOR EACH ROW
EXECUTE FUNCTION ledger.prevent_ledger_entries_delete();

