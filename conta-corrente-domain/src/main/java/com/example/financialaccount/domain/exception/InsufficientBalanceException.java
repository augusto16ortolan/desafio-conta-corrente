package com.example.financialaccount.domain.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("A conta não possui saldo suficiente para esta movimentação.");
    }
}
