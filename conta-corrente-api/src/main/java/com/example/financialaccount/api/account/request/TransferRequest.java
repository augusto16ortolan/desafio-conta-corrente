package com.example.financialaccount.api.account.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "A conta de destino é obrigatória.") UUID destinationAccountId,
        @NotNull(message = "O valor da transferência é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor da transferência deve ser maior que zero.")
        @Digits(integer = 17, fraction = 2, message = "O valor deve ter no máximo duas casas decimais.")
        BigDecimal amount) {}
