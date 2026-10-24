package com.example.financialaccount.domain.dto;

import com.example.financialaccount.domain.movement.AccountMovement;
import com.example.financialaccount.domain.movement.MovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountMovementDto(
        UUID id,
        UUID accountId,
        MovementType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant occurredAt) {

    public static AccountMovementDto from(AccountMovement movement) {
        return new AccountMovementDto(
                movement.getId(),
                movement.getAccount().getId(),
                movement.getType(),
                movement.getAmount(),
                movement.getBalanceAfter(),
                movement.getOccurredAt());
    }
}
