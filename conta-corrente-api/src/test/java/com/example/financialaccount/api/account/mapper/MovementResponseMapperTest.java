package com.example.financialaccount.api.account.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.financialaccount.domain.dto.AccountMovementDto;
import com.example.financialaccount.domain.movement.MovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MovementResponseMapperTest {

    @Test
    void mapsMovementTypeToApiRepresentation() {
        var movement = new AccountMovementDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                MovementType.WITHDRAWAL,
                new BigDecimal("15.00"),
                new BigDecimal("85.00"),
                Instant.parse("2026-10-23T18:00:00Z"));

        var response = MovementResponseMapper.toResponse(movement);

        assertEquals("WITHDRAWAL", response.type());
        assertEquals(new BigDecimal("15.00"), response.amount());
        assertEquals(new BigDecimal("85.00"), response.balanceAfter());
    }
}
