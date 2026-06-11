package com.diobank.ledger.domain.policy;

import com.diobank.ledger.domain.exception.InvalidPostingException;
import com.diobank.ledger.domain.model.DoubleEntry;

public class PostingPolicy {

    public static void validate(DoubleEntry entry) {
        if (!entry.debit().amount().equals(entry.credit().amount())) {
            throw new InvalidPostingException("Debit amount must exactly equal Credit amount");
        }

        if (entry.debit().accountId().equals(entry.credit().accountId())) {
            throw new InvalidPostingException("Self-transfers are not allowed. Debit and Credit accounts must be different.");
        }
        
        if (entry.debit().amount().amount() <= 0) {
            throw new InvalidPostingException("Transfer amount must be strictly greater than 0");
        }
    }
}
