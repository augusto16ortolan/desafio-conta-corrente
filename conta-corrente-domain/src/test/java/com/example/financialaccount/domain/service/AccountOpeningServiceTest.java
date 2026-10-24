package com.example.financialaccount.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.account.AccountNumberGenerator;
import com.example.financialaccount.domain.dto.OpenAccountInput;
import com.example.financialaccount.domain.exception.AccountNumberGenerationException;
import com.example.financialaccount.domain.exception.DuplicateIdentityNumberException;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class AccountOpeningServiceTest {

    private static final String IDENTITY_NUMBER = "529.982.247-25";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private AccountPersistenceService accountPersistenceService;

    private AccountOpeningService accountOpeningService;

    @BeforeEach
    void setUp() {
        accountOpeningService = new AccountOpeningService(
                accountRepository,
                accountNumberGenerator,
                accountPersistenceService);
    }

    @Test
    void opensAccountWhenIdentityNumberIsAvailable() {
        var generatedAccountNumber = AccountNumber.from("0000000001");
        when(accountRepository.existsByIdentityNumber("52998224725")).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn(generatedAccountNumber);
        when(accountPersistenceService.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var account = accountOpeningService.open(new OpenAccountInput(IDENTITY_NUMBER));

        assertEquals("52998224725", account.identityNumber().value());
        assertEquals(generatedAccountNumber, account.accountNumber());
        assertEquals("0.00", account.balance().toPlainString());
    }

    @Test
    void rejectsIdentityNumberAlreadyLinkedToAccount() {
        when(accountRepository.existsByIdentityNumber("52998224725")).thenReturn(true);

        assertThrows(
                DuplicateIdentityNumberException.class,
                () -> accountOpeningService.open(new OpenAccountInput(IDENTITY_NUMBER)));

        verifyNoInteractions(accountNumberGenerator, accountPersistenceService);
    }

    @Test
    void retriesWhenGeneratedAccountNumberCollides() {
        var firstAccountNumber = AccountNumber.from("0000000001");
        var secondAccountNumber = AccountNumber.from("0000000011");
        when(accountRepository.existsByIdentityNumber("52998224725")).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn(firstAccountNumber, secondAccountNumber);
        when(accountPersistenceService.save(any(Account.class)))
                .thenThrow(uniqueConstraintViolation("uk_accounts_number_check_digit"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var account = accountOpeningService.open(new OpenAccountInput(IDENTITY_NUMBER));

        assertEquals(secondAccountNumber, account.accountNumber());
        verify(accountNumberGenerator, times(2)).generate();
        verify(accountPersistenceService, times(2)).save(any(Account.class));
    }

    @Test
    void rejectsIdentityNumberWhenDatabaseReportsConcurrentDuplicate() {
        when(accountRepository.existsByIdentityNumber("52998224725")).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn(AccountNumber.from("0000000001"));
        when(accountPersistenceService.save(any(Account.class)))
                .thenThrow(uniqueConstraintViolation("uk_accounts_identity_number"));

        assertThrows(
                DuplicateIdentityNumberException.class,
                () -> accountOpeningService.open(new OpenAccountInput(IDENTITY_NUMBER)));
    }

    @Test
    void failsAfterFiveAccountNumberCollisions() {
        when(accountRepository.existsByIdentityNumber("52998224725")).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn(AccountNumber.from("0000000001"));
        when(accountPersistenceService.save(any(Account.class)))
                .thenThrow(uniqueConstraintViolation("uk_accounts_number_check_digit"));

        assertThrows(
                AccountNumberGenerationException.class,
                () -> accountOpeningService.open(new OpenAccountInput(IDENTITY_NUMBER)));

        verify(accountNumberGenerator, times(5)).generate();
        verify(accountPersistenceService, times(5)).save(any(Account.class));
    }

    @Test
    void propagatesUnexpectedPersistenceViolation() {
        var persistenceException = new DataIntegrityViolationException("Unexpected persistence error");
        when(accountRepository.existsByIdentityNumber("52998224725")).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn(AccountNumber.from("0000000001"));
        when(accountPersistenceService.save(any(Account.class))).thenThrow(persistenceException);

        var thrownException = assertThrows(
                DataIntegrityViolationException.class,
                () -> accountOpeningService.open(new OpenAccountInput(IDENTITY_NUMBER)));

        assertSame(persistenceException, thrownException);
    }

    private DataIntegrityViolationException uniqueConstraintViolation(String constraintName) {
        var sqlException = new SQLException("duplicate key", "23505");
        var cause = new ConstraintViolationException("duplicate key", sqlException, constraintName);
        return new DataIntegrityViolationException("Could not persist account", cause);
    }
}
