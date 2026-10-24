package com.example.financialaccount.api.account.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StatementResponse(
        UUID accountId,
        String accountNumber,
        int checkDigit,
        BigDecimal currentBalance,
        Instant startDate,
        Instant endDate,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<MovementResponse> movements) {}
