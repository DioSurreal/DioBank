package com.diobank.ledger.application.port.in.command;

import java.util.UUID;

public record PostEntryCommand(UUID transactionId, UUID debitAccountId, UUID creditAccountId, long amount) {}
