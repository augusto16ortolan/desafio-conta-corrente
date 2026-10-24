package com.example.financialaccount.domain.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RandomAccountNumberGeneratorTest {

    private final RandomAccountNumberGenerator generator = new RandomAccountNumberGenerator();

    @Test
    void generatesNumericAccountNumberWithExpectedLengthAndCheckDigit() {
        var accountNumber = generator.generate();

        assertTrue(accountNumber.value().matches("\\d{" + AccountNumber.LENGTH + "}"));
        assertEquals(AccountCheckDigit.calculate(accountNumber.value()), accountNumber.checkDigit());
    }
}
