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
import com.example.financialaccount.domain.dto.TransferInput;
import com.example.financialaccount.domain.exception.AccountNotFoundException;
import com.example.financialaccount.domain.exception.InsufficientBalanceException;
import com.example.financialaccount.domain.exception.IdempotencyKeyConflictException;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.money.MonetaryAmount;
import com.example.financialaccount.domain.movement.AccountMovement;
import com.example.financialaccount.domain.repository.AccountMovementRepository;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountRepository, accountMovementRepository);
    }

    @Test
    void transfersAmountBetweenAccountsAndRegistersBothMovements() {
        var sourceAccount = accountWithBalance("100.00");
        var destinationAccount = account("111.444.777-35", "0000000002");
        var input = input(sourceAccount.getId(), destinationAccount.getId(), "40.00");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(sourceAccount.getId(), "transfer-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(sourceAccount, destinationAccount));

        var transfer = transferService.transfer(input);

        assertEquals(new BigDecimal("60.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("40.00"), destinationAccount.getBalance());
        assertEquals(sourceAccount.getId(), transfer.debitMovement().accountId());
        assertEquals(destinationAccount.getId(), transfer.creditMovement().accountId());
        verify(accountMovementRepository).saveAll(any());
    }

    @Test
    void returnsOriginalTransferForRepeatedIdempotencyKey() {
        var sourceAccount = accountWithBalance("60.00");
        var destinationAccount = account("111.444.777-35", "0000000002");
        var reference = UUID.randomUUID();
        var debit = AccountMovement.registerTransferDebit(
                sourceAccount, new MonetaryAmount(new BigDecimal("40.00")), "transfer-1", reference);
        var credit = AccountMovement.registerTransferCredit(
                destinationAccount, new MonetaryAmount(new BigDecimal("40.00")), reference);
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(sourceAccount.getId(), "transfer-1"))
                .thenReturn(Optional.of(debit));
        when(accountMovementRepository.findAllByTransferReference(reference)).thenReturn(List.of(debit, credit));

        var transfer = transferService.transfer(input(sourceAccount.getId(), destinationAccount.getId(), "40.00"));

        assertEquals(reference, transfer.transferReference());
        assertEquals(debit.getId(), transfer.debitMovement().id());
        assertEquals(credit.getId(), transfer.creditMovement().id());
        verifyNoInteractions(accountRepository);
        verify(accountMovementRepository, never()).saveAll(any());
    }

    @Test
    void rejectsReusedIdempotencyKeyWithDifferentDestinationAccount() {
        var sourceAccount = accountWithBalance("60.00");
        var originalDestinationAccount = account("111.444.777-35", "0000000002");
        var newDestinationAccountId = UUID.randomUUID();
        var reference = UUID.randomUUID();
        var debit = AccountMovement.registerTransferDebit(
                sourceAccount, new MonetaryAmount(new BigDecimal("40.00")), "transfer-1", reference);
        var credit = AccountMovement.registerTransferCredit(
                originalDestinationAccount, new MonetaryAmount(new BigDecimal("40.00")), reference);
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(sourceAccount.getId(), "transfer-1"))
                .thenReturn(Optional.of(debit));
        when(accountMovementRepository.findAllByTransferReference(reference)).thenReturn(List.of(debit, credit));

        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> transferService.transfer(input(sourceAccount.getId(), newDestinationAccountId, "40.00")));

        verifyNoInteractions(accountRepository);
        verify(accountMovementRepository, never()).saveAll(any());
    }

    @Test
    void rejectsTransferWhenSourceAccountHasInsufficientBalance() {
        var sourceAccount = accountWithBalance("10.00");
        var destinationAccount = account("111.444.777-35", "0000000002");
        var input = input(sourceAccount.getId(), destinationAccount.getId(), "10.01");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(sourceAccount.getId(), "transfer-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(sourceAccount, destinationAccount));

        assertThrows(InsufficientBalanceException.class, () -> transferService.transfer(input));

        assertEquals(new BigDecimal("10.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("0.00"), destinationAccount.getBalance());
        verify(accountMovementRepository, never()).saveAll(any());
    }

    @Test
    void rejectsTransferWhenDestinationAccountDoesNotExist() {
        var sourceAccount = accountWithBalance("10.00");
        var destinationAccountId = UUID.randomUUID();
        var input = input(sourceAccount.getId(), destinationAccountId, "1.00");
        when(accountMovementRepository.findByAccountIdAndIdempotencyKey(sourceAccount.getId(), "transfer-1"))
                .thenReturn(Optional.empty());
        when(accountRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(sourceAccount));

        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(input));

        assertEquals(new BigDecimal("10.00"), sourceAccount.getBalance());
        verify(accountMovementRepository, never()).saveAll(any());
    }

    @Test
    void rejectsTransferToSameAccount() {
        var account = accountWithBalance("10.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(input(account.getId(), account.getId(), "1.00")));

        verifyNoInteractions(accountRepository, accountMovementRepository);
    }

    private Account accountWithBalance(String balance) {
        var account = account("529.982.247-25", "0000000001");
        account.deposit(new BigDecimal(balance));
        return account;
    }

    private Account account(String identityNumber, String accountNumber) {
        return Account.open(IdentityNumber.from(identityNumber), AccountNumber.from(accountNumber));
    }

    private TransferInput input(UUID sourceAccountId, UUID destinationAccountId, String amount) {
        return new TransferInput(
                sourceAccountId,
                destinationAccountId,
                new MonetaryAmount(new BigDecimal(amount)),
                "transfer-1");
    }
}
