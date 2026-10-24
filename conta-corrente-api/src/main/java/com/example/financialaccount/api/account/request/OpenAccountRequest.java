package com.example.financialaccount.api.account.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpenAccountRequest(
        @NotBlank(message = "O número de identificação é obrigatório.")
        @Size(max = 18, message = "O número de identificação é inválido.")
        String identityNumber) {}
