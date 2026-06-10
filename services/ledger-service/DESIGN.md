# Ledger Service Design

## Purpose

Ledger Service is responsible for maintaining the immutable financial record of the banking platform.

Ledger is the source of truth for all monetary movements.

All balances, reports, reconciliations, settlements, and snapshots must be reconstructable from ledger history.

Financial correctness always takes precedence over performance.

---

# System Context

High-level transaction flow:

BFF

↓

Transaction Service

↓

Ledger Service

↓

PostgreSQL

↓

Outbox Publisher

↓

Kafka

↓

Projection Service

↓

RocksDB

↓

Redis

↓

Notification Service

---

Responsibilities by component:

Transaction Service

* Transfer validation
* Insufficient funds validation
* Idempotency validation
* Concurrency control
* Command generation

Ledger Service

* Double-entry accounting
* Ledger persistence
* Transaction recording
* Outbox generation

Projection Service

* Balance projection maintenance
* Projection rebuild
* Snapshot consumption

Snapshot Service

* Periodic snapshot generation
* Snapshot storage
* Recovery acceleration

Notification Service

* User notifications
* SMS
* Email
* Push notifications

---

# Core Principles

1. Ledger is the source of truth.
2. Ledger entries are append-only.
3. Historical records must never be modified.
4. Double-entry accounting is mandatory.
5. All financial operations must be auditable.
6. Financial correctness is more important than performance.
7. Recovery must always be possible.
8. Every operation must be deterministic.

---

# Service Responsibilities

Ledger Service owns:

* Ledger entries
* Transaction recording
* Posting validation
* Double-entry validation
* Outbox event generation

Ledger Service does NOT own:

* Balance calculations
* Balance projections
* Customer information
* Account metadata
* Notification delivery
* Snapshot generation
* Clearing
* Settlement

---

# Consistency Model

Ledger entries are strongly consistent.

Balance projections are eventually consistent.

Temporary divergence between:

* Ledger
* RocksDB
* Redis

is acceptable.

Ledger remains the source of truth.

If inconsistency exists:

Ledger wins.

---

# Transaction Processing Flow

Transaction Service sends:

TransferCommand

↓

Ledger Service validates:

* idempotency key
* posting rules
* amount > 0

↓

Create:

Debit Entry

Credit Entry

↓

Create Outbox Event

↓

Persist all records within a single database transaction

↓

Commit

↓

Return Success

---

# Double Entry Accounting

Every financial transaction must create:

* One Debit Entry
* One Credit Entry

Example:

Transfer 100 THB

Account A

Debit 100

Account B

Credit 100

Invariant:

Total Debits == Total Credits

Any transaction violating this invariant must be rejected.

---

# Monetary Rules

All monetary values use:

BIGINT

Unit:

Satang

Examples:

100 THB = 10000

1000 THB = 100000

Forbidden:

* float
* double

Monetary calculations must be deterministic.

---

# Database Ownership

Ledger Service owns:

* money_transactions
* ledger_entries
* outbox_events

Ledger Service must never modify:

* customer
* kyc
* account

---

# Balance Validation Strategy

Ledger Service does NOT validate balances.

Balance validation belongs to Transaction Service.

Transaction Service checks balance projection stored in RocksDB.

If balance is sufficient:

TransferCommand

↓

Ledger Service

Ledger Service records accounting truth only.

---

# Idempotency Strategy

Transaction Service performs:

Request-level idempotency.

Ledger Service performs:

Persistence-level idempotency.

Duplicate transaction requests must never create duplicate ledger entries.

Duplicate requests should return existing results whenever possible.

---

# Event Publishing Strategy

Outbox Pattern is mandatory.

Flow:

Database Transaction

↓

Commit

↓

Outbox Publisher

↓

Kafka

↓

Projection Service
    (Kafka Streams)

↓

RocksDB

Events are published only after successful database commit.

No event may be published before transaction commit.

---

# Recovery Strategy

Ledger remains recoverable even if:

* Kafka fails
* Projection Service fails
* Redis fails
* Notification Service fails

If Kafka is unavailable:

Outbox records remain stored.

Publisher retries later.

No financial records are lost.

---

# Replay Strategy

Projection Services must support replay.

Replay flow:

Kafka

↓

Projection Service
    (Kafka Streams)
↓

RocksDB Rebuild

If replay is unavailable:

Restore latest snapshot

↓

Replay delta events

Ledger remains source of truth.

---

# Snapshot Strategy

Ledger Service does not generate snapshots.

Snapshots are owned by Snapshot Service.

Snapshots exist only to accelerate recovery.

Snapshots never replace ledger history.

If a snapshot differs from ledger:

Ledger wins.

Projection must be rebuilt.

---

# Failure Scenarios

Scenario 1

Database Commit Success

Kafka Publish Failure

Result:

Outbox retry.

No financial data loss.

---

Scenario 2

Duplicate Request

Result:

Return existing transaction.

No duplicate ledger entries.

---

Scenario 3

Projection Lost

Result:

Replay Kafka events.

Rebuild RocksDB.

---

Scenario 4

Redis Lost

Result:

Repopulate from RocksDB.

---

Scenario 5

Notification Failure

Result:

Financial transaction remains successful.

Retry notification later.

---

# Auditability

Every financial operation must preserve:

* transaction_id
* account_id
* created_at
* correlation_id

Audit trails must remain reconstructable.

Historical ledger data must never be altered.

---

# Security Requirements

All service-to-service communication:

gRPC + TLS

Sensitive data:

Encrypted at rest.

Access control:

Least privilege.

Audit logging:

Mandatory.

---

# Hot Account Strategy

Transactions affecting the same account must be serialized.

Preferred strategy:

Single Writer Principle

Partition key:

account_id

Future scaling options:

* Account sharding
* Split ledger architecture
* Dedicated hot-account processing

---

# Scaling Strategy

Current target:

Average TPS: 20,000

Peak TPS: 200,000

Future scaling options:

* PostgreSQL partitioning
* Ledger archival
* Read replicas
* Kafka partition expansion
* Projection horizontal scaling

---

# Invariants

The following must always be true:

Ledger is source of truth.

Ledger entries are immutable.

Total Debits == Total Credits.

Balance is derived from ledger.

No duplicate transaction processing.

No financial data loss.

All financial operations are auditable.

If any invariant is violated:

The transaction must fail.
