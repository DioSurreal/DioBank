package com.diobank.ledger.application.port.out;

import com.diobank.ledger.domain.model.AccountBalance;
import java.util.Optional;
import java.util.UUID;

public interface AccountBalancePort {
    Optional<AccountBalance> load(UUID accountId);
    Optional<AccountBalance> loadForUpdate(UUID accountId);
    void updateBalance(AccountBalance accountBalance);
    boolean createIfAbsent(UUID accountId);
}
