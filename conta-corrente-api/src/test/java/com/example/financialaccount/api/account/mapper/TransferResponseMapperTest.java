package com.example.financialaccount.api.account.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.financialaccount.domain.dto.AccountMovementDto;
import com.example.financialaccount.domain.dto.TransferDto;
import com.example.financialaccount.domain.movement.MovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransferResponseMapperTest {

    @Test
    void mapsDebitAndCreditMovements() {
        var reference = UUID.randomUUID();
        var transfer = new TransferDto(
                reference,
                movement(MovementType.TRANSFER_DEBIT, "90.00"),
                movement(MovementType.TRANSFER_CREDIT, "10.00"));

        var response = TransferResponseMapper.toResponse(transfer);

        assertEquals(reference, response.transferReference());
        assertEquals("TRANSFER_DEBIT", response.debitMovement().type());
        assertEquals("TRANSFER_CREDIT", response.creditMovement().type());
    }

    private AccountMovementDto movement(MovementType type, String balanceAfter) {
        return new AccountMovementDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                type,
                new BigDecimal("10.00"),
                new BigDecimal(balanceAfter),
                Instant.parse("2026-10-23T18:00:00Z"));
    }
}
