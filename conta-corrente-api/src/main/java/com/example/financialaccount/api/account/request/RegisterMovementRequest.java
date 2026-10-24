package com.example.financialaccount.api.account.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegisterMovementRequest(
        @NotNull(message = "O valor da movimentação é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor da movimentação deve ser maior que zero.")
        @Digits(integer = 17, fraction = 2, message = "O valor deve ter no máximo duas casas decimais.")
        BigDecimal amount) {}
