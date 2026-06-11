package com.diobank.ledger.domain.model;

import com.diobank.ledger.domain.exception.InsufficientBalanceException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AccountBalance {
    private final UUID accountId;
    private Money balance;
    private Instant updatedAt;

    public AccountBalance(UUID accountId, Money balance, Instant updatedAt) {
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public static AccountBalance createInitial(UUID accountId) {
        return new AccountBalance(accountId, Money.ZERO, Instant.now());
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Money getBalance() {
        return balance;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void deduct(Money amount) {
        if (this.balance.amount() < amount.amount()) {
            throw new InsufficientBalanceException("Insufficient funds for account " + accountId);
        }
        this.balance = this.balance.minus(amount);
        this.updatedAt = Instant.now();
    }

    public void add(Money amount) {
        this.balance = this.balance.plus(amount);
        this.updatedAt = Instant.now();
    }
}
