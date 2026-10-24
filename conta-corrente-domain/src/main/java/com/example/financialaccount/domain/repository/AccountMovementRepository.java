package com.example.financialaccount.domain.repository;

import com.example.financialaccount.domain.movement.AccountMovement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMovementRepository extends JpaRepository<AccountMovement, UUID> {

    Optional<AccountMovement> findByAccountIdAndIdempotencyKey(UUID accountId, String idempotencyKey);

    List<AccountMovement> findAllByTransferReference(UUID transferReference);

    Page<AccountMovement> findByAccountId(UUID accountId, Pageable pageable);

    Page<AccountMovement> findByAccountIdAndOccurredAtGreaterThanEqual(
            UUID accountId, Instant startDate, Pageable pageable);

    Page<AccountMovement> findByAccountIdAndOccurredAtLessThanEqual(
            UUID accountId, Instant endDate, Pageable pageable);

    Page<AccountMovement> findByAccountIdAndOccurredAtBetween(
            UUID accountId, Instant startDate, Instant endDate, Pageable pageable);
}
