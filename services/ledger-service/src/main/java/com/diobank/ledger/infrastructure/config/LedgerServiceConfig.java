package com.diobank.ledger.infrastructure.config;

import com.diobank.ledger.application.port.out.AccountBalancePort;
import com.diobank.ledger.application.port.out.LedgerRepositoryPort;
import com.diobank.ledger.application.service.AccountBalanceCommandService;
import com.diobank.ledger.application.service.AccountBalanceQueryService;
import com.diobank.ledger.application.service.LedgerTransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LedgerServiceConfig {

    @Bean
    public LedgerTransactionService ledgerTransactionService(
            AccountBalancePort accountBalancePort,
            LedgerRepositoryPort ledgerRepositoryPort) {
        return new LedgerTransactionService(accountBalancePort, ledgerRepositoryPort);
    }

    @Bean
    public AccountBalanceCommandService accountBalanceCommandService(
            AccountBalancePort accountBalancePort) {
        return new AccountBalanceCommandService(accountBalancePort);
    }

    @Bean
    public AccountBalanceQueryService accountBalanceQueryService(
            AccountBalancePort accountBalancePort) {
        return new AccountBalanceQueryService(accountBalancePort);
    }
}
