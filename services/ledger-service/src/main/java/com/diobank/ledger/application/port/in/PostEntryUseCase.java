package com.diobank.ledger.application.port.in;

import com.diobank.ledger.application.port.in.command.PostEntryCommand;
import com.diobank.ledger.application.port.in.result.PostEntryResult;

public interface PostEntryUseCase {
    PostEntryResult postEntry(PostEntryCommand command);
}
