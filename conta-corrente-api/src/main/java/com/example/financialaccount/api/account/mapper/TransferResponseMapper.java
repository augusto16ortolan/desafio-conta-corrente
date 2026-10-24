package com.example.financialaccount.api.account.mapper;

import com.example.financialaccount.api.account.response.TransferResponse;
import com.example.financialaccount.domain.dto.TransferDto;

public final class TransferResponseMapper {

    private TransferResponseMapper() {}

    public static TransferResponse toResponse(TransferDto transfer) {
        return new TransferResponse(
                transfer.transferReference(),
                MovementResponseMapper.toResponse(transfer.debitMovement()),
                MovementResponseMapper.toResponse(transfer.creditMovement()));
    }
}
