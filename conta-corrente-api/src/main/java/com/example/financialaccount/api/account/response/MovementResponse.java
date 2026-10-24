package com.example.financialaccount.api.account.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementResponse(
        UUID id,
        UUID accountId,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant occurredAt) {}
