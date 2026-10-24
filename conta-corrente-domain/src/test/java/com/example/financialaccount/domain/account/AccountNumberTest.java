package com.example.financialaccount.domain.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AccountNumberTest {

    @Test
    void createsAccountNumberWithCalculatedCheckDigit() {
        var accountNumber = AccountNumber.from("0000000001");

        assertEquals(9, accountNumber.checkDigit());
        assertEquals("0000000001-9", accountNumber.formatted());
    }

    @Test
    void rejectsNumberWithInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> AccountNumber.from("123456789"));
    }

    @Test
    void rejectsNumberWithNonNumericCharacters() {
        assertThrows(IllegalArgumentException.class, () -> AccountNumber.from("123456789A"));
    }

    @Test
    void rejectsAccountNumberWithUnexpectedCheckDigit() {
        assertThrows(IllegalArgumentException.class, () -> new AccountNumber("0000000001", 0));
    }
}
