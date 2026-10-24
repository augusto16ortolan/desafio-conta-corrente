package com.example.financialaccount.domain.exception;

public class DuplicateIdentityNumberException extends RuntimeException {

    public DuplicateIdentityNumberException() {
        super("Já existe uma conta vinculada a este número de identificação.");
    }
}
