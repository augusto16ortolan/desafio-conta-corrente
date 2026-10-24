package com.example.financialaccount.domain.dto;

import java.util.Objects;

public record OpenAccountInput(String identityNumber) {

    public OpenAccountInput {
        Objects.requireNonNull(identityNumber, "O número de identificação é obrigatório.");
    }
}
