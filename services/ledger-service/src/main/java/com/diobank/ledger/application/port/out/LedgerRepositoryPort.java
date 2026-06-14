package com.diobank.ledger.application.port.out;

import com.diobank.ledger.domain.model.DoubleEntry;

public interface LedgerRepositoryPort {
    boolean saveDoubleEntryIfAbsent(DoubleEntry entry);
}
