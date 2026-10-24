package com.example.financialaccount.domain.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AccountTest {

    @Test
    void opensAccountWithZeroBalance() {
        var identityNumber = IdentityNumber.from("529.982.247-25");
        var accountNumber = AccountNumber.from("0000000001");

        var account = Account.open(identityNumber, accountNumber);

        assertNotNull(account.getId());
        assertEquals(identityNumber, account.getIdentityNumber());
        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(new BigDecimal("0.00"), account.getBalance());
        assertNotNull(account.getCreatedAt());
    }
}
