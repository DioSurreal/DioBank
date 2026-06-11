package com.diobank.ledger.application.port.in.result;

import java.util.UUID;

public record CreateAccountBalanceResult(UUID accountId, boolean alreadyExisted) {}
