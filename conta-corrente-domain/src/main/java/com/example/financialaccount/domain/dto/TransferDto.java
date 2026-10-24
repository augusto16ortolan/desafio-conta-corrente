package com.example.financialaccount.domain.dto;

import java.util.UUID;

public record TransferDto(
        UUID transferReference, AccountMovementDto debitMovement, AccountMovementDto creditMovement) {}
