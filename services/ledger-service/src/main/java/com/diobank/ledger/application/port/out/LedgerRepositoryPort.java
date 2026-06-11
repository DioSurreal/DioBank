package com.diobank.ledger.application.port.out;

import com.diobank.ledger.domain.model.DoubleEntry;
import java.util.UUID;

public interface LedgerRepositoryPort {
    void saveDoubleEntry(DoubleEntry entry);
    boolean existsByTransactionId(UUID transactionId);
}
