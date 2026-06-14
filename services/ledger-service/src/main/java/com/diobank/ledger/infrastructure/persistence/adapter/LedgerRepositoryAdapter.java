package com.diobank.ledger.infrastructure.persistence.adapter;

import com.diobank.ledger.application.port.out.LedgerRepositoryPort;
import com.diobank.ledger.domain.model.DoubleEntry;
import com.diobank.ledger.domain.model.LedgerEntry;
import com.diobank.ledger.infrastructure.persistence.repository.LedgerEntryJdbcRepository;

public class LedgerRepositoryAdapter implements LedgerRepositoryPort {

    private final LedgerEntryJdbcRepository repository;

    public LedgerRepositoryAdapter(LedgerEntryJdbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean saveDoubleEntryIfAbsent(DoubleEntry entry) {
        int insertedDebit = insertEntry(entry.debit());
        if (insertedDebit == 0) {
            return false; // Idempotency collision
        }
        insertEntry(entry.credit());
        return true;
    }

    private int insertEntry(LedgerEntry entry) {
        return repository.insertIfAbsent(
                entry.ledgerId(),
                entry.transactionId(),
                entry.accountId(),
                entry.direction().name(),
                entry.amount().amount(),
                entry.createdAt()
        );
    }
}
