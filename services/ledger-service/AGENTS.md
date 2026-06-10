# Ledger Service Guide

## Scope

This service owns:

* Ledger entries
* Posting rules
* Double-entry accounting validation

This service is the source of truth for all financial transactions.

---

## Business Invariants

Ledger is the source of truth.

Balance must never be treated as source of truth.

Balance is derived from ledger entries.

All financial state must be recoverable from ledger history.

---

## Ledger Rules

Ledger entries are append-only.

Ledger entries must never be updated.

Ledger entries must never be deleted.

Ledger corrections must be performed using compensating entries.

---

## Double Entry Accounting

Every financial transaction must generate:

* One DEBIT entry
* One CREDIT entry

Total debits must equal total credits.

Reject transactions that violate accounting invariants.

---

## Monetary Rules

Use BIGINT for all monetary values.

Amounts are stored in satang.

Do not use:

* float
* double
* BigDecimal for persistence

Negative amounts are not allowed.

Direction determines money movement.

---

## Idempotency Rules

All write operations must be idempotent.

Duplicate requests must not create duplicate ledger entries.

Use idempotency keys for transaction processing.

---

## Auditability

All ledger operations must be traceable.

Preserve:

* transaction_id
* account_id
* created_at

Audit history must be reconstructable from stored data.

---

## Kafka Rules

Ledger Service publishes events.

Ledger Service does not own balance projections.

Balance projections are maintained by downstream consumers.

---

## Forbidden Actions

Never update account balances directly.

Never delete ledger entries.

Never modify historical ledger entries.

Never bypass double-entry validation.

Never use distributed transactions.

---

## Development Process

Before implementing any feature:

1. Explain implementation plan.
2. List affected files.
3. Explain risks.
4. Wait for approval.

If requirements are unclear:

STOP.

Ask questions.

Do not guess.
