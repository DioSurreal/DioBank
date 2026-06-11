# Ledger Service Design

## Service Purpose

Ledger Service is responsible for maintaining financial consistency.

Ledger Service owns:

* Immutable ledger history
* Operational account balances

Ledger Service is the accounting source of truth.

All financial operations must be recorded through double-entry accounting.

Ledger Service guarantees that balance mutation and ledger persistence occur within the same database transaction.

---

## Service Boundary

### Inputs

Transaction Service sends:

* transaction_id
* debit_account_id
* credit_account_id
* amount

via gRPC.

Transaction Service is responsible for:

* Request validation
* Workflow orchestration
* Calling Ledger Service

---

### Outputs

Ledger Service persists:

* Debit Ledger Entry
* Credit Ledger Entry
* Updated Account Balance

inside PostgreSQL.

---

## Domain Model

### LedgerEntry

Represents a single immutable accounting entry.

Fields:

* ledger_id
* transaction_id
* account_id
* direction
* amount
* created_at

---

### AccountBalance

Represents operational balance state.

Fields:

* account_id
* balance
* updated_at

Responsibilities:

* Balance validation
* Balance mutation

---
### Account Creation Flow

Account Service owns account lifecycle.

When a new account is created,
Account Service must call Ledger Service
CreateAccountBalance(account_id).

Ledger Service creates:

account_balance
(
    account_id,
    balance = 0
)

### DoubleEntry

Represents a valid accounting posting.

Contains:

* debit entry
* credit entry

Responsibilities:

* Ensure debit amount equals credit amount
* Ensure debit direction is DEBIT
* Ensure credit direction is CREDIT

---

### Direction

Enum:

* DEBIT
* CREDIT

---

## Domain Validation

The domain layer must enforce:

* amount > 0
* debit amount == credit amount
* debit direction == DEBIT
* credit direction == CREDIT

Invalid postings must be rejected before persistence.

---

## Balance Validation Rules

Ledger Service owns balance validation.

Before creating ledger entries:

1. Lock debit account balance
2. Verify sufficient funds
3. Update balances
4. Create ledger entries
5. Commit transaction

Insufficient balance must reject the operation.

Ledger entries remain the accounting source of truth.

account_balance is an operational balance cache
used for transaction processing.

Periodic reconciliation jobs must verify that:

account_balance

==

SUM(ledger_entries)

for every account.

If divergence is detected,
ledger_entries is authoritative.

---

## Database Design

### ledger_entries

Columns:

* ledger_id UUID PRIMARY KEY
* transaction_id UUID NOT NULL
* account_id UUID NOT NULL
* direction VARCHAR NOT NULL
* amount BIGINT NOT NULL
* created_at TIMESTAMP NOT NULL

Notes:

* Immutable
* Append-only
* Never updated
* Never deleted
* transaction_id is used for traceability

---

### account_balance

Columns:

* account_id UUID PRIMARY KEY
* balance BIGINT NOT NULL
* updated_at TIMESTAMP NOT NULL

Notes:

* Stores operational balance
* Used for transaction processing
* Protected by pessimistic locking
* Balance stored in satang

---

## Concurrency Strategy

Ledger Service owns concurrency control.

Balance updates must use:

SELECT ... FOR UPDATE

before mutation.

Purpose:

* Prevent double spending
* Prevent lost updates
* Serialize modifications per account

Concurrency is enforced only on account balances.

Ledger entries remain append-only and do not require locking.

---

## Transaction Flow

Transaction Service

↓

gRPC

↓

Ledger Service

↓

Lock Debit Account

(SELECT FOR UPDATE)

↓

Validate Balance

↓

Update Account Balances

↓

Create DoubleEntry

↓

Validate Domain Rules

↓

Persist Debit Entry

↓

Persist Credit Entry

↓

Commit Database Transaction

↓

Return Success

---

## Atomicity Guarantee

The following operations must occur within a single PostgreSQL transaction:

* Balance validation
* Balance mutation
* Debit ledger insert
* Credit ledger insert

Either all operations succeed or all operations fail.

Partial success is not allowed.

---

## Ownership Boundaries

### Ledger Service Owns

* ledger_entries
* account_balance
* double-entry validation
* balance validation
* balance mutation
* financial consistency

### Transaction Service Owns

* request orchestration
* API workflow
* transaction lifecycle
* external coordination

### Account Service Owns

Account metadata only:

* account_id
* customer_id
* account_status
* account_type
* account_name

Account Service does not own balances.

---

## Accounting Rules

Every successful transaction must create:

* One DEBIT entry
* One CREDIT entry

Invariant:

Total Debit = Total Credit

Accounting balance must never be violated.

---

## Error Handling

Reject when:

* amount <= 0
* insufficient funds
* invalid account
* malformed posting

Database rollback must occur automatically.

No ledger entry may exist without a successful balance mutation.

No balance mutation may exist without corresponding ledger entries.

---

## Explicit Non-Goals

Ledger Service must never:

* Manage customer profiles
* Manage KYC
* Manage account metadata
* Send notifications
* Publish Kafka events
* Manage Redis
* Orchestrate workflows
* Handle UI concerns

These responsibilities belong to other services.
