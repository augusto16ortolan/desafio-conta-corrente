package com.example.financialaccount.domain.dto;

import com.example.financialaccount.domain.account.AccountNumber;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StatementDto(
        UUID accountId,
        AccountNumber accountNumber,
        BigDecimal currentBalance,
        Instant startDate,
        Instant endDate,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<AccountMovementDto> movements) {}
