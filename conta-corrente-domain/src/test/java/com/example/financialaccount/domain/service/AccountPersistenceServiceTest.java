package com.example.financialaccount.domain.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountPersistenceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountPersistenceService accountPersistenceService;

    @Test
    void savesAndFlushesAccount() {
        var account = Account.open(IdentityNumber.from("529.982.247-25"), AccountNumber.from("0000000001"));
        when(accountRepository.saveAndFlush(account)).thenReturn(account);

        var savedAccount = accountPersistenceService.save(account);

        assertSame(account, savedAccount);
        verify(accountRepository).saveAndFlush(account);
    }
}
