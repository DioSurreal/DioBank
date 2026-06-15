package com.diobank.ledger.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("account_balance")
public class AccountBalanceEntity {

    @Id
    private UUID accountId;
    private long balance;
    private Instant updatedAt;

    public AccountBalanceEntity() {}

    public AccountBalanceEntity(UUID accountId, long balance, Instant updatedAt) {
        this.accountId = accountId;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
