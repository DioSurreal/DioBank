# Account Service Design

## Service Purpose

Account Service is responsible for managing account metadata, account lifecycle, account ownership, and account status.

It is the authoritative source of account information within the banking platform.

The service owns all account-related business rules and lifecycle transitions. Any data owned by this service must remain consistent, auditable, and authoritative.

---

# Responsibilities

Account Service owns:

* Account Metadata
* Account Lifecycle
* Account Status Management
* Account Ownership
* Account Status History

---

# Service Boundaries

## Create Account

### Request

```text
customer_id
account_type
account_owner_type
```

### Response

```text
account_id
account_number
```

Creates a new bank account for an existing customer.

---

## Get Account By Account Number

### Request

```text
account_number
```

### Response

```text
account_id
customer_id
account_number
account_type
account_owner_type
status
created_at
updated_at
```

Returns account information by account number.

---

## Get Account By Account ID

### Request

```text
account_id
```

### Response

```text
account_id
customer_id
account_number
account_type
account_owner_type
status
created_at
updated_at
```

Returns account information by account ID.

---

## Get Accounts By Customer ID

### Request

```text
customer_id
```

### Response

```text
List<Account>
```

Returns all accounts belonging to a customer.

---

## Freeze Account

### Request

```text
account_id
```

### Response

```text
Success
```

Changes account status from ACTIVE to FROZEN.

---

## Activate Account

### Request

```text
account_id
```

### Response

```text
Success
```

Changes account status from FROZEN to ACTIVE.

---

## Close Account

### Request

```text
account_id
```

### Response

```text
Success
```

Changes account status to CLOSED.

Closed accounts cannot be reactivated.

---

# Published Events

## Account Status Changed

Published whenever account status changes.

### Event Payload

```text
account_id
account_number
status
updated_at
```

### Purpose

Allows downstream services to maintain local account status caches and validate account status before processing business operations.

Examples:

* Transaction Service
* Payment Service
* Card Service

---

# Audit Logging

Every account lifecycle operation must generate an audit log entry.

Examples:

* Account Created
* Account Frozen
* Account Activated
* Account Closed

---

# Domain Model

## Account

Represents a bank account.

### Fields

```text
account_id
customer_id
account_number
account_type
account_owner_type
status
created_at
updated_at
```

---

## AccountLogHistory

Represents an immutable audit record.

### Fields

```text
account_log_id
account_id
topic
details
created_at
```

---

## AccountType

```text
SAVINGS
CURRENT
```

### SAVINGS

Personal banking account used for:

* Transfers
* Deposits
* Withdrawals

### CURRENT

Business-oriented account typically used for:

* High-volume transactions
* Commercial payments
* Check-related operations

---

## AccountOwnerType

```text
INDIVIDUAL
BUSINESS
```

---

## AccountStatus

```text
ACTIVE
FROZEN
CLOSED
```

---

## AccountLogTopic

```text
ACCOUNT_CREATED
ACCOUNT_FROZEN
ACCOUNT_ACTIVATED
ACCOUNT_CLOSED
```

---

## AccountFactory

Responsible for creating valid Account aggregates.

```java
Account create(
    CustomerId customerId,
    AccountType accountType,
    AccountOwnerType ownerType
)
```

---

## GenerateAccountNumberPort

Responsible for generating unique account numbers.

Examples:

```text
00000000000000001
00000000000000002
00000000000000003
```

Implementation details belong to the Infrastructure Layer.

---

## AccountStatusPolicy

Defines valid account status transitions.

Methods:

```java
boolean canFreeze(Account account);

boolean canActivate(Account account);

boolean canClose(Account account);
```

---

# Domain Invariants

## Account Creation

The following rules must always be true:

* account_id must be unique
* account_number must be unique
* account_number is immutable
* account_type must be valid
* account_owner_type must be valid

---

## Account Status Rules

### ACTIVE

An active account may participate in normal banking operations.

Examples:

* Transfer
* Deposit
* Withdrawal
* Payment

---

### FROZEN

A frozen account cannot perform financial transactions.

Allowed transitions:

```text
FROZEN -> ACTIVE
FROZEN -> CLOSED
```

---

### CLOSED

A closed account is permanently terminated.

Allowed transitions:

```text
CLOSED -> none
```

A closed account cannot be reactivated.

---

## Audit Requirements

Every account status transition must create an audit log record.

Examples:

```text
ACTIVE -> FROZEN
FROZEN -> ACTIVE
ACTIVE -> CLOSED
FROZEN -> CLOSED
```

---

# Database Design

## account

```sql
account_id UUID PRIMARY KEY

customer_id UUID NOT NULL

account_number VARCHAR NOT NULL UNIQUE

account_type VARCHAR NOT NULL

account_owner_type VARCHAR NOT NULL

status VARCHAR NOT NULL

created_at TIMESTAMP NOT NULL

updated_at TIMESTAMP NOT NULL
```

### Constraints

```sql
CHECK (
    status IN (
        'ACTIVE',
        'FROZEN',
        'CLOSED'
    )
)

CHECK (
    account_type IN (
        'SAVINGS',
        'CURRENT'
    )
)

CHECK (
    account_owner_type IN (
        'INDIVIDUAL',
        'BUSINESS'
    )
)
```

---

## account_log_history

```sql
account_log_id BIGSERIAL PRIMARY KEY

account_id UUID NOT NULL

topic VARCHAR NOT NULL

details JSONB NOT NULL

created_at TIMESTAMP NOT NULL
```

---

# Ownership Boundaries

## Account Service Owns

* Account creation
* Account retrieval
* Account status transitions
* Account lifecycle management
* Account audit history

---

# Error Handling

Reject requests when:

* account_id is invalid
* account_number is invalid
* account status transition is not allowed
* account does not exist

---

# Explicit Non-Goals

Account Service must never own:

* Account balances
* Ledger entries
* Deposits
* Withdrawals
* Transfers
* Payment processing
* Customer profile management

Customer data belongs to Customer Service.

Balance and transaction data belong to Ledger Service and Transaction Service.
