package com.example.financialaccount.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.dto.RegisterMovementInput;
import com.example.financialaccount.domain.exception.AccountNotFoundException;
import com.example.financialaccount.domain.exception.InsufficientBalanceException;
import com.example.financialaccount.domain.exception.IdempotencyKeyConflictException;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.money.MonetaryAmount;
import com.example.financialaccount.domain.movement.AccountMovement;
import com.example.financialaccount.domain.movement.MovementType;
import com.example.financialaccount.domain.repository.AccountMovementRepository;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountMovementServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    private AccountMovementService accountMovementService;

    @BeforeEach
    void setUp() {
        accountMovementService = new AccountMovementService(accountRepository, accountMovementRepository);
    }

    @Test
    void depositsAndRegistersMovement() {
        var account = accountWithBalance();
        var input = input(account.getId(), "deposit-1", "15.00");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(account.getId(), "deposit-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));
        when(accountMovementRepository.save(any(AccountMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var movement = accountMovementService.deposit(input);

        assertEquals(MovementType.DEPOSIT, movement.type());
        assertEquals(new BigDecimal("25.00"), movement.balanceAfter());
        assertEquals(new BigDecimal("25.00"), account.getBalance());
        verify(accountMovementRepository).save(any(AccountMovement.class));
    }

    @Test
    void returnsExistingMovementForRepeatedIdempotencyKey() {
        var account = accountWithBalance();
        var existingMovement = AccountMovement.register(
                account, MovementType.DEPOSIT, new MonetaryAmount(new BigDecimal("5.00")), "deposit-1");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(account.getId(), "deposit-1"))
                .thenReturn(Optional.of(existingMovement));

        var repeatedMovement = accountMovementService.deposit(input(account.getId(), "deposit-1", "5.00"));

        assertEquals(existingMovement.getId(), repeatedMovement.id());
        assertEquals(new BigDecimal("10.00"), account.getBalance());
        verifyNoInteractions(accountRepository);
        verify(accountMovementRepository, never()).save(any());
    }

    @Test
    void rejectsReusedIdempotencyKeyWithDifferentAmount() {
        var account = accountWithBalance();
        var existingMovement = AccountMovement.register(
                account, MovementType.DEPOSIT, new MonetaryAmount(new BigDecimal("5.00")), "deposit-1");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(account.getId(), "deposit-1"))
                .thenReturn(Optional.of(existingMovement));

        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> accountMovementService.deposit(input(account.getId(), "deposit-1", "6.00")));

        verifyNoInteractions(accountRepository);
        verify(accountMovementRepository, never()).save(any());
    }

    @Test
    void rejectsReusedIdempotencyKeyForDifferentMovementType() {
        var account = accountWithBalance();
        var existingMovement = AccountMovement.register(
                account, MovementType.WITHDRAWAL, new MonetaryAmount(new BigDecimal("5.00")), "movement-1");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(account.getId(), "movement-1"))
                .thenReturn(Optional.of(existingMovement));

        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> accountMovementService.deposit(input(account.getId(), "movement-1", "5.00")));

        verifyNoInteractions(accountRepository);
    }

    @Test
    void rejectsWithdrawalWithoutSufficientBalance() {
        var account = accountWithBalance();
        var input = input(account.getId(), "withdrawal-1", "10.01");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(account.getId(), "withdrawal-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> accountMovementService.withdraw(input));

        assertEquals(new BigDecimal("10.00"), account.getBalance());
        verify(accountMovementRepository, never()).save(any());
    }

    @Test
    void withdrawsAndRegistersMovement() {
        var account = accountWithBalance();
        var input = input(account.getId(), "withdrawal-1", "4.00");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(account.getId(), "withdrawal-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));
        when(accountMovementRepository.save(any(AccountMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var movement = accountMovementService.withdraw(input);

        assertEquals(MovementType.WITHDRAWAL, movement.type());
        assertEquals(new BigDecimal("6.00"), movement.balanceAfter());
        assertEquals(new BigDecimal("6.00"), account.getBalance());
    }

    @Test
    void returnsExistingMovementFoundAfterLock() {
        var account = accountWithBalance();
        var existingMovement = AccountMovement.register(
                account, MovementType.DEPOSIT, new MonetaryAmount(new BigDecimal("5.00")), "deposit-1");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(account.getId(), "deposit-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingMovement));
        when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

        var movement = accountMovementService.deposit(input(account.getId(), "deposit-1", "5.00"));

        assertEquals(existingMovement.getId(), movement.id());
        assertEquals(new BigDecimal("10.00"), account.getBalance());
        verify(accountMovementRepository, never()).save(any());
    }

    @Test
    void rejectsWithdrawalForMissingAccount() {
        var accountId = UUID.randomUUID();
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(accountId, "withdrawal-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountMovementService.withdraw(input(accountId, "withdrawal-1", "1.00")));

        verify(accountMovementRepository, never()).save(any());
    }

    @Test
    void rejectsMovementForMissingAccount() {
        var accountId = UUID.randomUUID();
        var input = input(accountId, "deposit-1", "1.00");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(accountId, "deposit-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountMovementService.deposit(input));

        verify(accountMovementRepository, never()).save(any());
    }

    private Account accountWithBalance() {
        var account = Account.open(
                IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));
        account.deposit(new BigDecimal("10.00"));
        return account;
    }

    private RegisterMovementInput input(UUID accountId, String idempotencyKey, String amount) {
        return new RegisterMovementInput(accountId, new MonetaryAmount(new BigDecimal(amount)), idempotencyKey);
    }
}
