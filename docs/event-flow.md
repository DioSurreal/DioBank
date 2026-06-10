# Event Flow


## Transfer Flow
Mobile App , Web

↓

BFF

↓

Transaction Service

    - balance validation
    - concurrency control
    - idempotency

↓

gRPC

↓

Ledger Service

    - double entry
    - ledger persistence
    - outbox

↓

PostgreSQL

↓

Outbox Publisher

↓

Kafka 

↓

Projection Service
    (Kafka Streams)

    - RocksDB
    - Snapshot
    - Replay

↓

Redis

↓

Notification Service

## Ledger Posted
Publisher:
Ledger Service

Topic:
ledger-posted

↓

Consumer:
Projection Service

## Projection Updated
Publisher:
Projection Service

Topic:
balance-updated

↓

Consumer:
Notification Service
