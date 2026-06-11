# Ledger Service Agent Guide

## Purpose

Ledger Service is responsible for:

* Maintaining immutable ledger entries
* Maintaining account balances
* Enforcing double-entry accounting
* Preventing double spending

## Architecture

This service follows:

* Domain Driven Design (DDD)
* Hexagonal Architecture
* Clean Code principles

Layers:

* Domain
* Application
* Infrastructure
* Presentation (gRPC)

## Database Ownership

Ledger Service owns:

* account_balance
* ledger_entries

Ledger Service must never own:

* customer
* kyc
* account profile
* notification
* transaction workflow

## Business Rules

* Ledger entries are immutable.
* Never update ledger entries.
* Never delete ledger entries.
* Corrections must use compensating entries.
* Debit amount must equal credit amount.
* Amount must be greater than zero.
* Account balance must never become negative.
* transaction_id must be unique.

## Concurrency Rules

* Concurrency is applied only to account_balance.
* Use PostgreSQL row locking.
* Use SELECT ... FOR UPDATE for balance mutation.
* Do not lock ledger_entries.

## AI Agent Instructions

Before implementing anything:

1. Read DESIGN.md completely.
2. Do not assume missing requirements.
3. Ask questions when requirements are ambiguous.
4. Do not introduce Kafka, CQRS, Event Sourcing, Saga, Redis, RocksDB, or additional infrastructure unless explicitly requested.
5. Keep solutions simple and aligned with the current architecture.
6. Financial correctness is more important than performance.
7. Do not generate code before presenting an implementation plan and receiving approval.
