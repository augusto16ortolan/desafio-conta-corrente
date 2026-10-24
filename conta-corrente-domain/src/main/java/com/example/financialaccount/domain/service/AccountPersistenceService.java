package com.example.financialaccount.domain.service;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountPersistenceService {

    private final AccountRepository accountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Account save(Account account) {
        return accountRepository.saveAndFlush(account);
    }
}
