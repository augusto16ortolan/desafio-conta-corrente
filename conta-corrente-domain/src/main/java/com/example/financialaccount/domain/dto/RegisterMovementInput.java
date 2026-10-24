package com.example.financialaccount.domain.dto;

import com.example.financialaccount.domain.money.MonetaryAmount;
import java.util.Objects;
import java.util.UUID;

public record RegisterMovementInput(UUID accountId, MonetaryAmount amount, String idempotencyKey) {

    public RegisterMovementInput {
        Objects.requireNonNull(accountId, "O identificador da conta é obrigatório.");
        Objects.requireNonNull(amount, "O valor da movimentação é obrigatório.");
        Objects.requireNonNull(idempotencyKey, "A chave de idempotência é obrigatória.");
    }
}
