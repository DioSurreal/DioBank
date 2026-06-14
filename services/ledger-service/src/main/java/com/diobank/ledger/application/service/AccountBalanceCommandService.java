package com.diobank.ledger.application.service;

import com.diobank.ledger.application.port.in.CreateAccountBalanceUseCase;
import com.diobank.ledger.application.port.in.command.CreateAccountBalanceCommand;
import com.diobank.ledger.application.port.in.result.CreateAccountBalanceResult;
import com.diobank.ledger.application.port.out.AccountBalancePort;
import org.springframework.transaction.annotation.Transactional;

public class AccountBalanceCommandService implements CreateAccountBalanceUseCase {

    private final AccountBalancePort accountBalancePort;

    public AccountBalanceCommandService(AccountBalancePort accountBalancePort) {
        this.accountBalancePort = accountBalancePort;
    }

    @Override
    @Transactional
    public CreateAccountBalanceResult createAccountBalance(CreateAccountBalanceCommand command) {
        var accountId = command.accountId();
        
        // Database-driven idempotency: ON CONFLICT DO NOTHING
        boolean created = accountBalancePort.createIfAbsent(accountId);

        // If created == true, it's a new account. If false, it already existed.
        return new CreateAccountBalanceResult(accountId, !created);
    }
}
