package com.example.financialaccount.domain.account;

import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.identitynumber.IdentityNumberType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    private UUID id;

    @Column(name = "identity_number", nullable = false, length = 14)
    private String identityNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_number_type", nullable = false, length = 4)
    private IdentityNumberType identityNumberType;

    @Column(name = "account_number", nullable = false, length = AccountNumber.LENGTH)
    private String accountNumber;

    @Column(name = "check_digit", nullable = false)
    private int checkDigit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Account(IdentityNumber identityNumber, AccountNumber accountNumber) {
        this.id = UUID.randomUUID();
        this.identityNumber = identityNumber.value();
        this.identityNumberType = identityNumber.type();
        this.accountNumber = accountNumber.value();
        this.checkDigit = accountNumber.checkDigit();
        this.balance = new BigDecimal("0.00");
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public static Account open(IdentityNumber identityNumber, AccountNumber accountNumber) {
        Objects.requireNonNull(identityNumber, "O número de identificação é obrigatório.");
        Objects.requireNonNull(accountNumber, "O número da conta é obrigatório.");
        return new Account(identityNumber, accountNumber);
    }

    public IdentityNumber getIdentityNumber() {
        return new IdentityNumber(identityNumber, identityNumberType);
    }

    public AccountNumber getAccountNumber() {
        return new AccountNumber(accountNumber, checkDigit);
    }

    @PrePersist
    void prepareForPersistence() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        updatedAt = createdAt;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
