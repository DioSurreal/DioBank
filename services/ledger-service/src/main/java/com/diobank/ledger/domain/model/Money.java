package com.diobank.ledger.domain.model;

public record Money(long amount) {
    public Money {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public static final Money ZERO = new Money(0);

    public Money plus(Money other) {
        return new Money(Math.addExact(this.amount, other.amount));
    }

    public Money minus(Money other) {
        if (this.amount < other.amount) {
            throw new IllegalArgumentException("Cannot subtract more than current amount");
        }
        return new Money(Math.subtractExact(this.amount, other.amount));
    }
}
