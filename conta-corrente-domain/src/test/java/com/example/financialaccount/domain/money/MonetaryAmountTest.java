package com.example.financialaccount.domain.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MonetaryAmountTest {

    @Test
    void normalizesValueToTwoDecimalPlaces() {
        var amount = new MonetaryAmount(new BigDecimal("10.5"));

        assertEquals(new BigDecimal("10.50"), amount.value());
    }

    @Test
    void rejectsZeroValue() {
        assertThrows(IllegalArgumentException.class, () -> new MonetaryAmount(BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> new MonetaryAmount(new BigDecimal("-0.01")));
    }

    @Test
    void rejectsValueWithMoreThanTwoDecimalPlaces() {
        assertThrows(IllegalArgumentException.class, () -> new MonetaryAmount(new BigDecimal("10.001")));
    }
}
