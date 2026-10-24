package com.example.financialaccount.domain.idempotency;

import java.util.Objects;

public record IdempotencyKey(String value) {

    private static final int MAXIMUM_LENGTH = 100;

    public IdempotencyKey {
        Objects.requireNonNull(value, "A chave de idempotência é obrigatória.");
        value = value.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("A chave de idempotência é obrigatória.");
        }

        if (value.length() > MAXIMUM_LENGTH) {
            throw new IllegalArgumentException("A chave de idempotência deve ter no máximo 100 caracteres.");
        }
    }
}
