package com.example.financialaccount.api.account.response;

import java.util.UUID;

public record TransferResponse(
        UUID transferReference, MovementResponse debitMovement, MovementResponse creditMovement) {}
