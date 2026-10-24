package com.example.financialaccount.domain.dto;

import com.example.financialaccount.domain.idempotency.IdempotencyKey;
import com.example.financialaccount.domain.money.MonetaryAmount;
import java.util.Objects;
import java.util.UUID;

public record TransferInput(
        UUID sourceAccountId,
        UUID destinationAccountId,
        MonetaryAmount amount,
        String idempotencyKey) {

    public TransferInput {
        Objects.requireNonNull(sourceAccountId, "A conta de origem é obrigatória.");
        Objects.requireNonNull(destinationAccountId, "A conta de destino é obrigatória.");
        Objects.requireNonNull(amount, "O valor da transferência é obrigatório.");
        idempotencyKey = new IdempotencyKey(idempotencyKey).value();
    }
}
