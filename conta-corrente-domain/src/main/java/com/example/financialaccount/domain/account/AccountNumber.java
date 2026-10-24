package com.example.financialaccount.domain.account;

import java.util.Objects;
import java.util.regex.Pattern;

public record AccountNumber(String value, int checkDigit) {

    public static final int LENGTH = 10;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d{" + LENGTH + "}");

    public AccountNumber {
        Objects.requireNonNull(value, "O número da conta é obrigatório.");

        if (!NUMBER_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("O número da conta deve ter 10 dígitos.");
        }

        var expectedCheckDigit = AccountCheckDigit.calculate(value);

        if (checkDigit != expectedCheckDigit) {
            throw new IllegalArgumentException("O dígito verificador da conta é inválido.");
        }
    }

    public static AccountNumber from(String value) {
        Objects.requireNonNull(value, "O número da conta é obrigatório.");
        return new AccountNumber(value, AccountCheckDigit.calculate(value));
    }

    public String formatted() {
        return value + "-" + checkDigit;
    }
}
