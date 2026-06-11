package com.diobank.ledger.application.service;

import com.diobank.ledger.application.port.in.CreateAccountBalanceUseCase;
import com.diobank.ledger.application.port.in.command.CreateAccountBalanceCommand;
import com.diobank.ledger.application.port.in.result.CreateAccountBalanceResult;
import com.diobank.ledger.application.port.out.AccountBalancePort;
import com.diobank.ledger.domain.model.AccountBalance;

public class AccountBalanceCommandService implements CreateAccountBalanceUseCase {

    private final AccountBalancePort accountBalancePort;

    public AccountBalanceCommandService(AccountBalancePort accountBalancePort) {
        this.accountBalancePort = accountBalancePort;
    }

    @Override
    public CreateAccountBalanceResult createAccountBalance(CreateAccountBalanceCommand command) {
        var accountId = command.accountId();
        
        var existing = accountBalancePort.load(accountId);
        if (existing.isPresent()) {
            return new CreateAccountBalanceResult(accountId, true);
        }

        var newBalance = AccountBalance.createInitial(accountId);
        accountBalancePort.save(newBalance);

        return new CreateAccountBalanceResult(accountId, false);
    }
}
