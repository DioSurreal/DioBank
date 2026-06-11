package com.diobank.ledger.domain.model;

import java.util.Objects;

public record DoubleEntry(LedgerEntry debit, LedgerEntry credit) {
    public DoubleEntry {
        Objects.requireNonNull(debit, "Debit entry must not be null");
        Objects.requireNonNull(credit, "Credit entry must not be null");

        if (debit.direction() != Direction.DEBIT) {
            throw new IllegalArgumentException("First entry must be a DEBIT");
        }
        if (credit.direction() != Direction.CREDIT) {
            throw new IllegalArgumentException("Second entry must be a CREDIT");
        }
        if (!debit.transactionId().equals(credit.transactionId())) {
            throw new IllegalArgumentException("Debit and Credit must belong to the same transaction");
        }
    }
}
