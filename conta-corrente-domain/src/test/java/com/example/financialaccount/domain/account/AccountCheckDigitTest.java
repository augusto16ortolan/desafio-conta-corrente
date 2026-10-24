package com.example.financialaccount.domain.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AccountCheckDigitTest {

    @Test
    void rejectsEmptyAccountNumber() {
        assertThrows(IllegalArgumentException.class, () -> AccountCheckDigit.calculate(""));
    }

    @ParameterizedTest
    @CsvSource({
            "0000000001, 9",
            "0000000011, 6",
            "1234567890, 0",
            "0000000006, 0"
    })
    void calculatesCheckDigitUsingModuloEleven(String accountNumber, int expectedCheckDigit) {
        assertEquals(expectedCheckDigit, AccountCheckDigit.calculate(accountNumber));
    }
}
