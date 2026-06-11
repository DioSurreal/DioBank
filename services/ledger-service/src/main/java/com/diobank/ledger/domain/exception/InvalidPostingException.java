package com.diobank.ledger.domain.exception;

public class InvalidPostingException extends RuntimeException {
    public InvalidPostingException(String message) {
        super(message);
    }
}
