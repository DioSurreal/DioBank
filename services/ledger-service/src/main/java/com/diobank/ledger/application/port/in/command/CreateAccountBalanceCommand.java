package com.diobank.ledger.application.port.in.command;

import java.util.UUID;

public record CreateAccountBalanceCommand(UUID accountId) {}
