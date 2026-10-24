package com.example.financialaccount.domain.account;

import java.util.Objects;

public final class AccountCheckDigit {

    private static final int MODULE = 11;
    private static final int MIN_WEIGHT = 2;
    private static final int MAX_WEIGHT = 9;

    private AccountCheckDigit() {
    }

    public static int calculate(String accountNumber) {
        Objects.requireNonNull(accountNumber, "O número da conta é obrigatório.");

        if (accountNumber.isEmpty()) {
            throw new IllegalArgumentException("O número da conta é obrigatório.");
        }

        var sum = 0;
        var weight = MIN_WEIGHT;

        for (var index = accountNumber.length() - 1; index >= 0; index--) {
            var digit = Character.digit(accountNumber.charAt(index), 10);

            if (digit < 0) {
                throw new IllegalArgumentException("O número da conta deve conter apenas dígitos.");
            }

            sum += digit * weight;
            weight = weight == MAX_WEIGHT ? MIN_WEIGHT : weight + 1;
        }

        var checkDigit = MODULE - (sum % MODULE);
        return checkDigit >= 10 ? 0 : checkDigit;
    }
}
