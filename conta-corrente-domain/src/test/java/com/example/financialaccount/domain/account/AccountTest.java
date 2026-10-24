package com.example.financialaccount.domain.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.financialaccount.domain.exception.InsufficientBalanceException;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import java.math.BigDecimal;
import java.time.Instant;
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

    @Test
    void updatesBalanceForDepositAndWithdrawal() {
        var account = Account.open(
                IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));

        account.deposit(new BigDecimal("120.00"));
        account.withdraw(new BigDecimal("20.00"));

        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void rejectsWithdrawalWhenBalanceWouldBecomeNegative() {
        var account = Account.open(
                IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));

        assertThrows(InsufficientBalanceException.class, () -> account.withdraw(new BigDecimal("0.01")));
    }

    @Test
    void rejectsMissingValuesWhenOpeningOrMovingAnAccount() {
        var identityNumber = IdentityNumber.from("529.982.247-25");
        var accountNumber = AccountNumber.from("0000000001");
        var account = Account.open(identityNumber, accountNumber);

        assertThrows(NullPointerException.class, () -> Account.open(null, accountNumber));
        assertThrows(NullPointerException.class, () -> Account.open(identityNumber, null));
        assertThrows(NullPointerException.class, () -> account.deposit(null));
        assertThrows(NullPointerException.class, () -> account.withdraw(null));
    }

    @Test
    void preparesRequiredValuesBeforePersistence() {
        var account = new Account();

        account.prepareForPersistence();

        assertNotNull(account.getId());
        assertNotNull(account.getCreatedAt());
        assertEquals(account.getCreatedAt(), account.getUpdatedAt());
    }

    @Test
    void refreshesUpdateTimestampBeforeUpdatingAnAccount() {
        var account = Account.open(
                IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));
        var createdAt = account.getUpdatedAt();

        account.updateTimestamp();

        var updatedAt = account.getUpdatedAt();
        assertTrue(updatedAt.compareTo(createdAt) >= 0);
        assertTrue(updatedAt.compareTo(Instant.now()) <= 0);
    }
}
