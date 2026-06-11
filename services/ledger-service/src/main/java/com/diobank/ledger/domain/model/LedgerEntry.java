package com.diobank.ledger.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record LedgerEntry(
        UUID ledgerId,
        UUID transactionId,
        UUID accountId,
        Direction direction,
        Money amount,
        Instant createdAt) {

    public LedgerEntry {
        Objects.requireNonNull(ledgerId, "ledgerId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(direction, "direction must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (amount.amount() <= 0) {
            throw new IllegalArgumentException("LedgerEntry amount must be greater than 0");
        }
    }
    
    // Static factory for creating new entries during transaction processing
    public static LedgerEntry create(UUID transactionId, UUID accountId, Direction direction, Money amount) {
        return new LedgerEntry(UUID.randomUUID(), transactionId, accountId, direction, amount, Instant.now());
    }
}
