package com.example.financialaccount.domain.service;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.account.AccountNumberGenerator;
import com.example.financialaccount.domain.dto.AccountDto;
import com.example.financialaccount.domain.dto.OpenAccountInput;
import com.example.financialaccount.domain.exception.AccountNumberGenerationException;
import com.example.financialaccount.domain.exception.DuplicateIdentityNumberException;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountOpeningService {

    private static final int MAX_ACCOUNT_NUMBER_GENERATION_ATTEMPTS = 5;
    private static final String IDENTITY_NUMBER_UNIQUE_CONSTRAINT = "uk_accounts_identity_number";
    private static final String NUMBER_AND_CHECK_DIGIT_UNIQUE_CONSTRAINT = "uk_accounts_number_check_digit";

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountPersistenceService accountPersistenceService;

    public AccountDto open(OpenAccountInput input) {
        Objects.requireNonNull(input, "Os dados para abertura da conta são obrigatórios.");
        var identityNumber = IdentityNumber.from(input.identityNumber());

        if (accountRepository.existsByIdentityNumber(identityNumber.value())) {
            throw new DuplicateIdentityNumberException();
        }

        for (var attempt = 1; attempt <= MAX_ACCOUNT_NUMBER_GENERATION_ATTEMPTS; attempt++) {
            var account = Account.open(identityNumber, accountNumberGenerator.generate());

            try {
                return AccountDto.from(accountPersistenceService.save(account));
            } catch (DataIntegrityViolationException exception) {
                if (violatesConstraint(exception, IDENTITY_NUMBER_UNIQUE_CONSTRAINT)) {
                    throw new DuplicateIdentityNumberException();
                }

                if (!violatesConstraint(exception, NUMBER_AND_CHECK_DIGIT_UNIQUE_CONSTRAINT)) {
                    throw exception;
                }
            }
        }

        throw new AccountNumberGenerationException();
    }

    private boolean violatesConstraint(DataIntegrityViolationException exception, String expectedConstraint) {
        var cause = exception.getCause();

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                return expectedConstraint.equals(constraintViolationException.getConstraintName());
            }

            cause = cause.getCause();
        }

        return false;
    }
}
