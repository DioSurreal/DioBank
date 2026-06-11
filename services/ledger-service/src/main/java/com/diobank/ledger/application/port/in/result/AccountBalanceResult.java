package com.diobank.ledger.application.port.in.result;

import java.util.UUID;

public record AccountBalanceResult(UUID accountId, long balance) {}
