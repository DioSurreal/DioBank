package com.diobank.ledger.application.service;

import com.diobank.ledger.application.exception.AccountNotFoundException;
import com.diobank.ledger.application.port.in.GetBalanceUseCase;
import com.diobank.ledger.application.port.in.query.GetBalanceQuery;
import com.diobank.ledger.application.port.in.result.AccountBalanceResult;
import com.diobank.ledger.application.port.out.AccountBalancePort;
import org.springframework.transaction.annotation.Transactional;

public class AccountBalanceQueryService implements GetBalanceUseCase {

    private final AccountBalancePort accountBalancePort;

    public AccountBalanceQueryService(AccountBalancePort accountBalancePort) {
        this.accountBalancePort = accountBalancePort;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResult getBalance(GetBalanceQuery query) {
        var account = accountBalancePort.load(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId()));

        return new AccountBalanceResult(account.getAccountId(), account.getBalance().amount());
    }
}
