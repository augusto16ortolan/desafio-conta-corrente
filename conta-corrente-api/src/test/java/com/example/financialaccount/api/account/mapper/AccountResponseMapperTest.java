package com.example.financialaccount.api.account.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.dto.AccountDto;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountResponseMapperTest {

    @Test
    void mapsDomainAccountWithoutExposingDomainTypes() {
        var id = UUID.randomUUID();
        var createdAt = Instant.parse("2026-10-23T18:00:00Z");
        var account = new AccountDto(
                id,
                IdentityNumber.from("529.982.247-25"),
                AccountNumber.from("0000000001"),
                new BigDecimal("0.00"),
                createdAt);

        var response = AccountResponseMapper.toResponse(account);

        assertEquals(id, response.id());
        assertEquals("52998224725", response.identityNumber());
        assertEquals("CPF", response.identityNumberType());
        assertEquals("0000000001", response.accountNumber());
        assertEquals(9, response.checkDigit());
        assertEquals(createdAt, response.createdAt());
    }
}
