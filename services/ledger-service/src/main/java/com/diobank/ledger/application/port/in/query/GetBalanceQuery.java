package com.diobank.ledger.application.port.in.query;

import java.util.UUID;

public record GetBalanceQuery(UUID accountId) {}
