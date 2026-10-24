package com.example.financialaccount.domain.repository;

import com.example.financialaccount.domain.account.Account;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByIdentityNumber(String identityNumber);
}
