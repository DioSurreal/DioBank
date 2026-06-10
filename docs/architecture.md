# Architecture

## Service Landscape
Customer Service

Account Service

Transaction Service

Ledger Service

Projection Service

Notification Service

Cheque Service

Loan Service
## Service Ownership

Customer Service

owns:
- customer
- kyc

--------------------------------

Account Service

owns:
- account metadata

--------------------------------

Transaction Service

owns:
- transfer orchestration
- balance validation
- idempotency

--------------------------------

Ledger Service

owns:
- ledger_entries
- outbox_events

--------------------------------

Projection Service

owns:
- balance projections
- snapshots

##  High Level Architecture Transfer , Withdraw , Deposit


Mobile App,Web
   ↓
API Gateway
   ↓
Authentication Service
   ↓
Transaction Service
   ↓
Ledger Service
   ↓
Database
   ↓
Outbox
   ↓
Kafka
   ↓
Projection Service
    (Kafka Streams)

   ↓
Balance Projection
   ↓
redis cache
   ↓    
Notification


