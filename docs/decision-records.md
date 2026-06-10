I would not implement the Ledger Service until these are answered:

1.Does money_transactions belong to Ledger Service or Transaction Service?
Answer => Ledger Service
2.Is account.balance authoritative, derived, or only a read model?
Answer => account is meta data and account.balance is snapshot balance
3.What is the full schema for outbox_events?
Answer => i add it at db-design.md
4.What are the exact gRPC request and response contracts between Transaction Service and Ledger Service?
Answer => yes
5.What should Ledger Service return for duplicate idempotency keys: existing success, conflict, or a specific replay response?
Answer => Return existing successful result.

Ledger Service must not create duplicate ledger entries.

Same idempotency key must return same transaction_id and ledger_id.
6.What are the allowed transaction and ledger entry states?
Answer => money_transactions

PENDING
POSTED
FAILED
REJECTED
7.When are completed_at and cleared_at set?
Answer => completed_at

set when ledger transaction successfully posted.

----------------

cleared_at

set by Clearing Service after settlement process completed.
8.Is the platform strictly single-currency, or do we need currency support now?
Answer => now Initial scope is single currency (THB).

All monetary values stored in satang.

Multi-currency support is out of scope for v1.
9.How is hot-account serialization enforced?
Answer => Transaction Service enforces single-writer model.

Requests are routed by account_id.

Same account always processed by same worker.

Future scaling may introduce account sharding.
10.Is Snapshot Service part of the initial implementation scope?
Answer => Not part of initial release.

Phase 1:

Recovery uses Kafka replay.

Phase 2:

Snapshot Service added to accelerate rebuild.
11.What retry and error semantics are expected for balance validation failures, concurrency conflicts, and persistence conflicts?
Answer => Balance validation failure

No retry.

----------------

Concurrency conflict

Retryable.

----------------

Database conflict

Retryable.

----------------

Idempotency conflict

Return existing successful result.
12.Are compensating entries the only allowed correction mechanism, or are reversals a separate concept?
Answer => Compensating entries only.

Historical ledger entries must never be updated or deleted.
13.What are the required audit fields beyond transaction_id, account_id, created_at, and correlation_id?
Answer => transaction_id
account_id
correlation_id
channel
actor_type
client_ip
device_id
created_at
created_by
request_id
idempotency_key
14.What are the recovery targets for replay and rebuild, in concrete time and data-loss terms?
Answer => RPO = 0

No financial data loss allowed.

----------------

RTO

Projection rebuild:
< 30 minutes

----------------

Kafka recovery:
< 15 minutes

----------------

Ledger database recovery:
< 1 hour