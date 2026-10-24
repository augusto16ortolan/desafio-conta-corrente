package com.example.financialaccount.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.dto.StatementInput;
import com.example.financialaccount.domain.exception.AccountNotFoundException;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.money.MonetaryAmount;
import com.example.financialaccount.domain.movement.AccountMovement;
import com.example.financialaccount.domain.movement.MovementType;
import com.example.financialaccount.domain.repository.AccountMovementRepository;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    private StatementService statementService;

    @BeforeEach
    void setUp() {
        statementService = new StatementService(accountRepository, accountMovementRepository);
    }

    @Test
    void returnsCurrentBalanceAndMovementsOrderedByMostRecentFirst() {
        var account = accountWithBalance("100.00");
        var deposit = AccountMovement.register(
                account, MovementType.DEPOSIT, new MonetaryAmount(new BigDecimal("100.00")), "deposit-1");
        var page = new PageImpl<>(List.of(deposit), PageRequest.of(0, 20), 1);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountMovementRepository.findByAccountId(eq(account.getId()), any(Pageable.class)))
                .thenReturn(page);

        var statement = statementService.getStatement(
                new StatementInput(account.getId(), null, null, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "occurredAt"))));

        assertEquals(account.getId(), statement.accountId());
        assertEquals(new BigDecimal("100.00"), statement.currentBalance());
        assertEquals(1, statement.totalElements());
        assertEquals(1, statement.movements().size());
        assertEquals(MovementType.DEPOSIT, statement.movements().getFirst().type());

        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(accountMovementRepository).findByAccountId(eq(account.getId()), pageable.capture());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("occurredAt").getDirection());
    }

    @Test
    void appliesPeriodFilterToRepository() {
        var account = accountWithBalance("10.00");
        var startDate = Instant.parse("2026-10-23T00:00:00Z");
        var endDate = Instant.parse("2026-10-23T23:59:59Z");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountMovementRepository.findByAccountIdAndOccurredAtBetween(
                        eq(account.getId()), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 0));

        var statement = statementService.getStatement(
                new StatementInput(account.getId(), startDate, endDate, PageRequest.of(1, 10)));

        assertEquals(startDate, statement.startDate());
        assertEquals(endDate, statement.endDate());
        assertEquals(1, statement.page());
        assertEquals(10, statement.size());
    }

    @Test
    void rejectsStatementForMissingAccount() {
        var accountId = java.util.UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> statementService.getStatement(new StatementInput(accountId, null, null, PageRequest.of(0, 20))));
    }

    @Test
    void rejectsInvalidStatementPeriod() {
        var accountId = java.util.UUID.randomUUID();
        var startDate = Instant.parse("2026-10-24T00:00:00Z");
        var endDate = Instant.parse("2026-10-23T00:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> new StatementInput(accountId, startDate, endDate, PageRequest.of(0, 20)));
    }

    @Test
    void rejectsInvalidPagination() {
        var accountId = java.util.UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new StatementInput(accountId, null, null, org.springframework.data.domain.Pageable.unpaged()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new StatementInput(accountId, null, null, PageRequest.of(0, 101)));
    }

    private Account accountWithBalance(String balance) {
        var account = Account.open(
                IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));
        account.deposit(new BigDecimal(balance));
        return account;
    }
}
