package com.example.financialaccount.api.account.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.dto.StatementDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StatementResponseMapperTest {

    @Test
    void mapsStatementPaginationAndBalance() {
        var statement = new StatementDto(
                UUID.randomUUID(),
                AccountNumber.from("0000000001"),
                new BigDecimal("70.00"),
                null,
                null,
                0,
                20,
                0,
                0,
                List.of());

        var response = StatementResponseMapper.toResponse(statement);

        assertEquals("0000000001", response.accountNumber());
        assertEquals(new BigDecimal("70.00"), response.currentBalance());
        assertEquals(20, response.size());
        assertEquals(0, response.totalElements());
    }
}
