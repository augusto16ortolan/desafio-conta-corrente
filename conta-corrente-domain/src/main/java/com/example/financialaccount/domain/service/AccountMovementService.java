package com.example.financialaccount.domain.service;

import com.example.financialaccount.domain.dto.AccountMovementDto;
import com.example.financialaccount.domain.dto.RegisterMovementInput;
import com.example.financialaccount.domain.exception.AccountNotFoundException;
import com.example.financialaccount.domain.exception.IdempotencyKeyConflictException;
import com.example.financialaccount.domain.movement.AccountMovement;
import com.example.financialaccount.domain.movement.MovementType;
import com.example.financialaccount.domain.repository.AccountMovementRepository;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountMovementService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository accountMovementRepository;

    @Transactional
    @Retryable(
            retryFor = TransientDataAccessException.class,
            backoff = @Backoff(delay = 100, multiplier = 2))
    public AccountMovementDto deposit(RegisterMovementInput input) {
        Objects.requireNonNull(input, "Os dados da movimentação são obrigatórios.");

        var existingMovement = accountMovementRepository.findByAccountIdAndIdempotencyKey(
                input.accountId(), input.idempotencyKey());
        if (existingMovement.isPresent()) {
            return existingMovement(existingMovement.get(), MovementType.DEPOSIT, input);
        }

        var account = accountRepository.findByIdForUpdate(input.accountId())
                .orElseThrow(() -> new AccountNotFoundException(input.accountId()));

        var movementAfterLock = accountMovementRepository.findByAccountIdAndIdempotencyKey(
                input.accountId(), input.idempotencyKey());
        if (movementAfterLock.isPresent()) {
            return existingMovement(movementAfterLock.get(), MovementType.DEPOSIT, input);
        }

        account.deposit(input.amount().value());
        var movement = AccountMovement.register(account, MovementType.DEPOSIT, input.amount(), input.idempotencyKey());
        return AccountMovementDto.from(accountMovementRepository.save(movement));
    }

    @Transactional
    @Retryable(
            retryFor = TransientDataAccessException.class,
            backoff = @Backoff(delay = 100, multiplier = 2))
    public AccountMovementDto withdraw(RegisterMovementInput input) {
        Objects.requireNonNull(input, "Os dados da movimentação são obrigatórios.");

        var existingMovement = accountMovementRepository.findByAccountIdAndIdempotencyKey(
                input.accountId(), input.idempotencyKey());
        if (existingMovement.isPresent()) {
            return existingMovement(existingMovement.get(), MovementType.WITHDRAWAL, input);
        }

        var account = accountRepository.findByIdForUpdate(input.accountId())
                .orElseThrow(() -> new AccountNotFoundException(input.accountId()));

        var movementAfterLock = accountMovementRepository.findByAccountIdAndIdempotencyKey(
                input.accountId(), input.idempotencyKey());
        if (movementAfterLock.isPresent()) {
            return existingMovement(movementAfterLock.get(), MovementType.WITHDRAWAL, input);
        }

        account.withdraw(input.amount().value());
        var movement = AccountMovement.register(account, MovementType.WITHDRAWAL, input.amount(), input.idempotencyKey());
        return AccountMovementDto.from(accountMovementRepository.save(movement));
    }

    private AccountMovementDto existingMovement(
            AccountMovement movement, MovementType expectedType, RegisterMovementInput input) {
        if (movement.getType() != expectedType || movement.getAmount().compareTo(input.amount().value()) != 0) {
            throw new IdempotencyKeyConflictException();
        }

        return AccountMovementDto.from(movement);
    }
}
