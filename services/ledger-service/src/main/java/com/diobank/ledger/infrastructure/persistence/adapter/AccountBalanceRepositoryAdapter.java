package com.diobank.ledger.infrastructure.persistence.adapter;

import com.diobank.ledger.application.port.out.AccountBalancePort;
import com.diobank.ledger.domain.model.AccountBalance;
import com.diobank.ledger.domain.model.Money;
import com.diobank.ledger.infrastructure.persistence.entity.AccountBalanceEntity;
import com.diobank.ledger.infrastructure.persistence.repository.AccountBalanceJdbcRepository;

import java.util.Optional;
import java.util.UUID;

public class AccountBalanceRepositoryAdapter implements AccountBalancePort {

    private final AccountBalanceJdbcRepository repository;

    public AccountBalanceRepositoryAdapter(AccountBalanceJdbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AccountBalance> load(UUID accountId) {
        return repository.findById(accountId).map(this::mapToDomain);
    }

    @Override
    public Optional<AccountBalance> loadForUpdate(UUID accountId) {
        return repository.findByIdForUpdate(accountId).map(this::mapToDomain);
    }

    @Override
    public void updateBalance(AccountBalance accountBalance) {
        AccountBalanceEntity entity = new AccountBalanceEntity(
                accountBalance.getAccountId(),
                accountBalance.getBalance().amount(),
                accountBalance.getUpdatedAt()
        );
        repository.save(entity);
    }

    @Override
    public boolean createIfAbsent(UUID accountId) {
        return repository.insertIfAbsent(accountId) > 0;
    }

    private AccountBalance mapToDomain(AccountBalanceEntity entity) {
        return new AccountBalance(entity.getAccountId(), Money.of(entity.getBalance()), entity.getUpdatedAt());
    }
}
