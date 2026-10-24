package com.example.financialaccount.domain.exception;

public class IdempotencyKeyConflictException extends RuntimeException {

    public IdempotencyKeyConflictException() {
        super("A chave de idempotência já foi utilizada em uma operação diferente.");
    }
}
