package com.example.financialaccount.api.account.mapper;

import com.example.financialaccount.api.account.response.AccountResponse;
import com.example.financialaccount.domain.dto.AccountDto;

public final class AccountResponseMapper {

    private AccountResponseMapper() {}

    public static AccountResponse toResponse(AccountDto account) {
        return new AccountResponse(
                account.id(),
                account.identityNumber().value(),
                account.identityNumber().type().name(),
                account.accountNumber().value(),
                account.accountNumber().checkDigit(),
                account.balance(),
                account.createdAt());
    }
}
