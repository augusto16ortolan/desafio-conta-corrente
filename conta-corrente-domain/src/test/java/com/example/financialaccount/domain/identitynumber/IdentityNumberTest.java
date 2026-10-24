package com.example.financialaccount.domain.identitynumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IdentityNumberTest {

    @ParameterizedTest
    @MethodSource("validIdentityNumbers")
    void createsNormalizedIdentityNumber(String rawValue, String expectedValue, IdentityNumberType expectedType) {
        var identityNumber = IdentityNumber.from(rawValue);

        assertEquals(expectedValue, identityNumber.value());
        assertEquals(expectedType, identityNumber.type());
    }

    @ParameterizedTest
    @MethodSource("invalidIdentityNumbers")
    void rejectsInvalidIdentityNumber(String value) {
        assertThrows(IllegalArgumentException.class, () -> IdentityNumber.from(value));
    }

    private static Arguments[] validIdentityNumbers() {
        return new Arguments[] {
                Arguments.of("529.982.247-25", "52998224725", IdentityNumberType.CPF),
                Arguments.of("04.252.011/0001-10", "04252011000110", IdentityNumberType.CNPJ)
        };
    }

    private static String[] invalidIdentityNumbers() {
        return new String[] {
                "111.111.111-11",
                "529.982.247-26",
                "04.252.011/0001-11",
                "1234567890",
                "529.982.247-2A"
        };
    }
}
