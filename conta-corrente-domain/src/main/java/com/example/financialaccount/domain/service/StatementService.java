package com.example.financialaccount.domain.service;

import com.example.financialaccount.domain.dto.AccountMovementDto;
import com.example.financialaccount.domain.dto.StatementDto;
import com.example.financialaccount.domain.dto.StatementInput;
import com.example.financialaccount.domain.exception.AccountNotFoundException;
import com.example.financialaccount.domain.movement.AccountMovement;
import com.example.financialaccount.domain.repository.AccountMovementRepository;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository accountMovementRepository;

    @Transactional(readOnly = true)
    public StatementDto getStatement(StatementInput input) {
        Objects.requireNonNull(input, "Os dados para consulta do extrato são obrigatórios.");

        var account = accountRepository.findById(input.accountId())
                .orElseThrow(() -> new AccountNotFoundException(input.accountId()));
        var movements = findMovements(input, input.pageable());

        return new StatementDto(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                input.startDate(),
                input.endDate(),
                movements.getNumber(),
                movements.getSize(),
                movements.getTotalElements(),
                movements.getTotalPages(),
                movements.map(AccountMovementDto::from).getContent());
    }

    private Page<AccountMovement> findMovements(StatementInput input, Pageable pageable) {
        if (input.startDate() != null && input.endDate() != null) {
            return accountMovementRepository.findByAccountIdAndOccurredAtBetween(
                    input.accountId(), input.startDate(), input.endDate(), pageable);
        }

        if (input.startDate() != null) {
            return accountMovementRepository.findByAccountIdAndOccurredAtGreaterThanEqual(
                    input.accountId(), input.startDate(), pageable);
        }

        if (input.endDate() != null) {
            return accountMovementRepository.findByAccountIdAndOccurredAtLessThanEqual(
                    input.accountId(), input.endDate(), pageable);
        }

        return accountMovementRepository.findByAccountId(input.accountId(), pageable);
    }
}
