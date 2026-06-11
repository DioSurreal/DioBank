package com.diobank.ledger.application.port.in;

import com.diobank.ledger.application.port.in.query.GetBalanceQuery;
import com.diobank.ledger.application.port.in.result.AccountBalanceResult;

public interface GetBalanceUseCase {
    AccountBalanceResult getBalance(GetBalanceQuery query);
}
