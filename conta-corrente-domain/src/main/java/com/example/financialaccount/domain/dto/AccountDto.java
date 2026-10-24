package com.example.financialaccount.domain.dto;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountDto(
        UUID id,
        IdentityNumber identityNumber,
        AccountNumber accountNumber,
        BigDecimal balance,
        Instant createdAt) {

    public static AccountDto from(Account account) {
        return new AccountDto(
                account.getId(),
                account.getIdentityNumber(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCreatedAt());
    }
}
