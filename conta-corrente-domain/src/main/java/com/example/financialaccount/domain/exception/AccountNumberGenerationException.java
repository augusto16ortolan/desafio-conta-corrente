package com.example.financialaccount.domain.exception;

public class AccountNumberGenerationException extends RuntimeException {

    public AccountNumberGenerationException() {
        super("Não foi possível gerar um número de conta disponível.");
    }
}
