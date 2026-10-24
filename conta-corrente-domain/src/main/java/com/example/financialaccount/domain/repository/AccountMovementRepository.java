package com.example.financialaccount.domain.repository;

import com.example.financialaccount.domain.movement.AccountMovement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMovementRepository extends JpaRepository<AccountMovement, UUID> {

    Optional<AccountMovement> findByAccountIdAndIdempotencyKey(UUID accountId, String idempotencyKey);

    List<AccountMovement> findAllByTransferReference(UUID transferReference);
}
