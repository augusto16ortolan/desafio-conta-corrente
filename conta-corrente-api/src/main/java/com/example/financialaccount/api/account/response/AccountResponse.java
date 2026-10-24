package com.example.financialaccount.api.account.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String identityNumber,
        String identityNumberType,
        String accountNumber,
        int checkDigit,
        BigDecimal balance,
        Instant createdAt) {}
