package com.diobank.ledger.application.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID accountId) {
        super("Account balance not found for accountId: " + accountId);
    }
}
