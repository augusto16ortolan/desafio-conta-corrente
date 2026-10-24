package com.example.financialaccount.domain.identitynumber;

import java.util.Objects;
import java.util.Locale;
import java.util.regex.Pattern;

public record IdentityNumber(String value, IdentityNumberType type) {

    private static final int MINIMUM_REMAINDER_FOR_CHECK_DIGIT = 2;
    private static final Pattern ALLOWED_FORMATTING = Pattern.compile("[.\\-\\/\\s]");
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");
    private static final Pattern CPF_FORMAT = Pattern.compile("\\d{11}");
    private static final Pattern CNPJ_FORMAT = Pattern.compile("[A-Z0-9]{12}\\d{2}");

    public IdentityNumber {
        value = normalize(value);
        type = Objects.requireNonNull(type, "O tipo do documento é obrigatório.");

        if (value.length() != type.length() || !isValid(value, type)) {
            throw new IllegalArgumentException("CPF ou CNPJ inválido.");
        }
    }

    public static IdentityNumber from(String value) {
        var normalizedValue = normalize(value);
        var type = IdentityNumberType.fromLength(normalizedValue.length());
        return new IdentityNumber(normalizedValue, type);
    }

    private static String normalize(String value) {
        Objects.requireNonNull(value, "O CPF ou CNPJ é obrigatório.");

        var normalizedValue = ALLOWED_FORMATTING.matcher(value).replaceAll("");

        return normalizedValue.toUpperCase(Locale.ROOT);
    }

    private static boolean isValid(String value, IdentityNumberType type) {
        return switch (type) {
            case CPF -> isValidCpf(value);
            case CNPJ -> isValidCnpj(value);
        };
    }

    private static boolean hasRepeatedCharacters(String value) {
        return value.chars().distinct().count() == 1;
    }

    private static boolean isValidCpf(String value) {
        if (!CPF_FORMAT.matcher(value).matches() || hasRepeatedCharacters(value)) {
            return false;
        }

        var firstCheckDigit = calculateCpfDigit(value.substring(0, 9), 10);
        var secondCheckDigit = calculateCpfDigit(value.substring(0, 9) + firstCheckDigit, 11);

        return firstCheckDigit == Character.digit(value.charAt(9), 10)
                && secondCheckDigit == Character.digit(value.charAt(10), 10);
    }

    private static boolean isValidCnpj(String value) {
        if (!CNPJ_FORMAT.matcher(value).matches()) {
            return false;
        }

        if (DIGITS_ONLY.matcher(value).matches() && hasRepeatedCharacters(value)) {
            return false;
        }

        var firstCheckDigit = calculateCnpjDigit(value.substring(0, 12));
        var secondCheckDigit = calculateCnpjDigit(value.substring(0, 12) + firstCheckDigit);

        return firstCheckDigit == Character.digit(value.charAt(12), 10)
                && secondCheckDigit == Character.digit(value.charAt(13), 10);
    }

    private static int calculateCpfDigit(String value, int initialWeight) {
        var sum = 0;
        var weight = initialWeight;

        for (var index = 0; index < value.length(); index++) {
            sum += Character.digit(value.charAt(index), 10) * weight;
            weight--;
        }

        var remainder = sum % 11;
        return remainder < MINIMUM_REMAINDER_FOR_CHECK_DIGIT ? 0 : 11 - remainder;
    }

    private static int calculateCnpjDigit(String value) {
        var sum = 0;
        var weight = value.length() == 12 ? 5 : 6;

        for (var index = 0; index < value.length(); index++) {
            sum += cnpjCharacterValue(value.charAt(index)) * weight;
            weight = weight == 2 ? 9 : weight - 1;
        }

        var remainder = sum % 11;
        return remainder < MINIMUM_REMAINDER_FOR_CHECK_DIGIT ? 0 : 11 - remainder;
    }

    private static int cnpjCharacterValue(char character) {
        return character - '0';
    }
}
