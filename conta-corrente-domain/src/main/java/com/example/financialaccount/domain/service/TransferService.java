package com.example.financialaccount.domain.service;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.dto.AccountMovementDto;
import com.example.financialaccount.domain.dto.TransferDto;
import com.example.financialaccount.domain.dto.TransferInput;
import com.example.financialaccount.domain.exception.AccountNotFoundException;
import com.example.financialaccount.domain.exception.IdempotencyKeyConflictException;
import com.example.financialaccount.domain.movement.AccountMovement;
import com.example.financialaccount.domain.movement.MovementType;
import com.example.financialaccount.domain.repository.AccountMovementRepository;
import com.example.financialaccount.domain.repository.AccountRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository accountMovementRepository;

    @Transactional
    @Retryable(
            retryFor = TransientDataAccessException.class,
            backoff = @Backoff(delay = 100, multiplier = 2))
    public TransferDto transfer(TransferInput input) {
        Objects.requireNonNull(input, "Os dados da transferência são obrigatórios.");

        if (input.sourceAccountId().equals(input.destinationAccountId())) {
            throw new IllegalArgumentException("A conta de origem deve ser diferente da conta de destino.");
        }

        var existingMovement = accountMovementRepository.findByAccountIdAndIdempotencyKey(
                input.sourceAccountId(), input.idempotencyKey());
        if (existingMovement.isPresent()) {
            return existingTransfer(existingMovement.get(), input);
        }

        var accounts = accountRepository.findAllByIdInForUpdate(
                List.of(input.sourceAccountId(), input.destinationAccountId()));
        var sourceAccount = findAccount(accounts, input.sourceAccountId());
        var destinationAccount = findAccount(accounts, input.destinationAccountId());

        var movementAfterLock = accountMovementRepository.findByAccountIdAndIdempotencyKey(
                input.sourceAccountId(), input.idempotencyKey());
        if (movementAfterLock.isPresent()) {
            return existingTransfer(movementAfterLock.get(), input);
        }

        sourceAccount.withdraw(input.amount().value());
        destinationAccount.deposit(input.amount().value());

        var transferReference = UUID.randomUUID();
        var debitMovement = AccountMovement.registerTransferDebit(
                sourceAccount, input.amount(), input.idempotencyKey(), transferReference);
        var creditMovement = AccountMovement.registerTransferCredit(destinationAccount, input.amount(), transferReference);
        accountMovementRepository.saveAll(List.of(debitMovement, creditMovement));

        return new TransferDto(
                transferReference, AccountMovementDto.from(debitMovement), AccountMovementDto.from(creditMovement));
    }

    private Account findAccount(List<Account> accounts, UUID accountId) {
        return accounts.stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private TransferDto existingTransfer(AccountMovement debitMovement, TransferInput input) {
        if (debitMovement.getType() != MovementType.TRANSFER_DEBIT
                || debitMovement.getTransferReference() == null
                || debitMovement.getAmount().compareTo(input.amount().value()) != 0) {
            throw new IdempotencyKeyConflictException();
        }

        var movements = accountMovementRepository.findAllByTransferReference(debitMovement.getTransferReference());
        var creditMovement = movements.stream()
                .filter(movement -> movement.getType() == MovementType.TRANSFER_CREDIT)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("A transferência não possui lançamento de crédito."));

        if (!creditMovement.getAccount().getId().equals(input.destinationAccountId())) {
            throw new IdempotencyKeyConflictException();
        }

        return new TransferDto(
                debitMovement.getTransferReference(),
                AccountMovementDto.from(debitMovement),
                AccountMovementDto.from(creditMovement));
    }
}
