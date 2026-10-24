package com.example.financialaccount.domain.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record MonetaryAmount(BigDecimal value) {

    private static final int SCALE = 2;

    public MonetaryAmount {
        Objects.requireNonNull(value, "O valor monetário é obrigatório.");

        if (value.signum() <= 0) {
            throw new IllegalArgumentException("O valor monetário deve ser maior que zero.");
        }

        if (value.scale() > SCALE) {
            throw new IllegalArgumentException("O valor monetário deve ter no máximo duas casas decimais.");
        }

        value = value.setScale(SCALE, RoundingMode.UNNECESSARY);
    }
}
