package com.diobank.ledger.application.port.in.result;

import java.util.UUID;

public record PostEntryResult(UUID transactionId, boolean isIdempotentFallback) {}
