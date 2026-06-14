package com.diobank.ledger.infrastructure.config;

import com.diobank.ledger.application.port.out.AccountBalancePort;
import com.diobank.ledger.application.port.out.LedgerRepositoryPort;
import com.diobank.ledger.application.service.AccountBalanceCommandService;
import com.diobank.ledger.application.service.AccountBalanceQueryService;
import com.diobank.ledger.application.service.LedgerTransactionService;
import com.diobank.ledger.infrastructure.grpc.GlobalExceptionInterceptor;
import com.diobank.ledger.infrastructure.grpc.LedgerGrpcService;
import com.diobank.ledger.application.port.in.CreateAccountBalanceUseCase;
import com.diobank.ledger.application.port.in.GetBalanceUseCase;
import com.diobank.ledger.application.port.in.PostEntryUseCase;
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

    @Bean
    public LedgerGrpcService ledgerGrpcService(
            PostEntryUseCase postEntryUseCase,
            CreateAccountBalanceUseCase createAccountBalanceUseCase,
            GetBalanceUseCase getBalanceUseCase) {
        return new LedgerGrpcService(postEntryUseCase, createAccountBalanceUseCase, getBalanceUseCase);
    }

    @Bean
    public GlobalExceptionInterceptor globalExceptionInterceptor() {
        return new GlobalExceptionInterceptor();
    }
}
