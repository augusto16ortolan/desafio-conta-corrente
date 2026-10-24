package com.example.financialaccount.domain.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IdempotencyKeyTest {

    @Test
    void removesSpacesAroundTheKey() {
        var key = new IdempotencyKey("  deposit-1  ");

        assertEquals("deposit-1", key.value());
    }

    @Test
    void rejectsBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> new IdempotencyKey("   "));
    }

    @Test
    void rejectsKeyLongerThanOneHundredCharacters() {
        var key = "a".repeat(101);

        assertThrows(IllegalArgumentException.class, () -> new IdempotencyKey(key));
    }
}
