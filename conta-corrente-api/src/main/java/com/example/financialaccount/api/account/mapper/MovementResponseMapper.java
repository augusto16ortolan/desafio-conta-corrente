package com.example.financialaccount.api.account.mapper;

import com.example.financialaccount.api.account.response.MovementResponse;
import com.example.financialaccount.domain.dto.AccountMovementDto;

public final class MovementResponseMapper {

    private MovementResponseMapper() {}

    public static MovementResponse toResponse(AccountMovementDto movement) {
        return new MovementResponse(
                movement.id(),
                movement.accountId(),
                movement.type().name(),
                movement.amount(),
                movement.balanceAfter(),
                movement.occurredAt());
    }
}
