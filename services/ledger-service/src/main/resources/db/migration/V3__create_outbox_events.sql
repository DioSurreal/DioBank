-- V3__create_outbox_events.sql
CREATE TABLE ledger.outbox_events (
    event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    event_key VARCHAR(192) NOT NULL UNIQUE,
    partition_key VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    CONSTRAINT chk_outbox_events_status
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT chk_outbox_events_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_outbox_events_status_created_at ON ledger.outbox_events (status, created_at);
CREATE INDEX idx_outbox_events_created_at ON ledger.outbox_events (created_at);
CREATE INDEX idx_outbox_events_aggregate_id ON ledger.outbox_events (aggregate_id);
CREATE INDEX idx_outbox_events_partition_key ON ledger.outbox_events (partition_key);

