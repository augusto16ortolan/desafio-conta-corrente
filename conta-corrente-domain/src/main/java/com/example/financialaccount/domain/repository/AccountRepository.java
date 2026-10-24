package com.example.financialaccount.domain.repository;

import com.example.financialaccount.domain.account.Account;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByIdentityNumber(String identityNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from Account account where account.id = :accountId")
    java.util.Optional<Account> findByIdForUpdate(@Param("accountId") UUID accountId);
}
