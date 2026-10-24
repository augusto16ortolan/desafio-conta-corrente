package com.example.financialaccount.api.account.mapper;

import com.example.financialaccount.api.account.response.StatementResponse;
import com.example.financialaccount.domain.dto.StatementDto;

public final class StatementResponseMapper {

    private StatementResponseMapper() {}

    public static StatementResponse toResponse(StatementDto statement) {
        return new StatementResponse(
                statement.accountId(),
                statement.accountNumber().value(),
                statement.accountNumber().checkDigit(),
                statement.currentBalance(),
                statement.startDate(),
                statement.endDate(),
                statement.page(),
                statement.size(),
                statement.totalElements(),
                statement.totalPages(),
                statement.movements().stream().map(MovementResponseMapper::toResponse).toList());
    }
}
