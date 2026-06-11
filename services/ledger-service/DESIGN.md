# Ledger Service Design

## Service Purpose

Ledger Service records immutable accounting events.

It acts as the financial journal of the platform.

Ledger Service is not responsible for balance calculation or transaction coordination.

---

## Service Boundary

### Inputs

Transaction Service sends:

* transaction_id
* debit_account_id
* credit_account_id
* amount

via gRPC.

---

### Outputs

Ledger Service persists:

* Debit Ledger Entry
* Credit Ledger Entry

inside PostgreSQL.

---

## Domain Model

### LedgerEntry

Represents a single accounting entry.

Fields:

* ledger_id
* transaction_id
* account_id
* direction
* amount
* created_at

---

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

## Database Design

Table:

ledger_entries

Columns:

* ledger_id UUID PRIMARY KEY
* transaction_id UUID NOT NULL
* account_id VARCHAR NOT NULL
* direction VARCHAR NOT NULL
* amount BIGINT NOT NULL
* created_at TIMESTAMP NOT NULL

Notes:

* transaction_id is not a foreign key
* transaction_id is used only for traceability
* ledger_entries is append-only


account_balance

Columns:

* account_id UUID PRIMARY KEY,
* balance BIGINT NOT NULL,
* updated_at TIMESTAMP NOT NULL

Notes:

* balance is greater than 0
* permistic lock for update balance


---

## Hexagonal Architecture

### Domain Layer

Contains:

* LedgerEntry
* DoubleEntry
* Direction
* Domain validation rules

No framework dependencies allowed.

---

### Application Layer

Contains:

* PostEntryUseCase
* Command models
* Port interfaces

Responsibilities:

* Receive command
* Build DoubleEntry
* Validate posting
* Persist entries

---

### Infrastructure Layer

Contains:

* PostgreSQL adapters
* Repository implementations
* gRPC adapters

Responsibilities:

* Database access
* Transport protocols

---

### Presentation Layer

Contains:

* gRPC endpoints

Responsibilities:

* Convert gRPC requests to application commands
* Return application responses

No business logic allowed.

---

## Transaction Flow

Transaction Service

↓

gRPC

↓

Ledger Service

↓

check balance

↓
update balance

↓

PostEntryUseCase

↓

Create DoubleEntry

↓

Validate Domain Rules

↓

Persist Debit Entry

Persist Credit Entry

↓

Return Success

---

## Explicit Non-Goals

Ledger Service must never:

* Check balances
* Update balances
* Lock accounts
* Handle concurrency
* Publish Kafka events
* Update Redis
* Send notifications
* Coordinate workflows

These responsibilities belong to other services.
