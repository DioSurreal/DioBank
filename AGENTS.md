# DioBank Repository Guide

## Purpose
This repository is the home for DioBank's service-oriented backend, supporting ledgering, transactions, accounts, and customer data.

## Working Rules
- Keep service boundaries explicit.
- Prefer event-driven integration where possible.
- Treat financial mutations as auditable and idempotent.
- Add or update documentation when behavior changes.

## Current Structure
- `docs/` contains architecture and contract notes.
- `services/` contains service-specific guidance.
- `infra/` contains local development and platform assets.
- `scripts/` contains helper scripts for local workflows.
- `prompts/` contains reusable task prompts for agents.

