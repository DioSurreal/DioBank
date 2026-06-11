package com.diobank.ledger.application.port.in;

import com.diobank.ledger.application.port.in.command.CreateAccountBalanceCommand;
import com.diobank.ledger.application.port.in.result.CreateAccountBalanceResult;

public interface CreateAccountBalanceUseCase {
    CreateAccountBalanceResult createAccountBalance(CreateAccountBalanceCommand command);
}
