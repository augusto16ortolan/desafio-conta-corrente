package com.example.financialaccount.domain.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.money.MonetaryAmount;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AccountMovementTest {

    @Test
    void registersMovementWithBalanceAfterOperation() {
        var account = Account.open(
                IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));
        account.deposit(new BigDecimal("75.00"));

        var movement = AccountMovement.register(
                account, MovementType.DEPOSIT, new MonetaryAmount(new BigDecimal("75.00")), "deposit-1");

        assertEquals(MovementType.DEPOSIT, movement.getType());
        assertEquals(new BigDecimal("75.00"), movement.getAmount());
        assertEquals(new BigDecimal("75.00"), movement.getBalanceAfter());
        assertEquals("deposit-1", movement.getIdempotencyKey());
    }

    @Test
    void rejectsBlankIdempotencyKey() {
        var account = Account.open(
                IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));

        assertThrows(
                IllegalArgumentException.class,
                () -> AccountMovement.register(
                        account, MovementType.DEPOSIT, new MonetaryAmount(new BigDecimal("1.00")), "   "));
    }
}
