package com.example.financialaccount.domain.movement;

import com.example.financialaccount.domain.account.Account;
import com.example.financialaccount.domain.idempotency.IdempotencyKey;
import com.example.financialaccount.domain.money.MonetaryAmount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "account_movements")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountMovement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "transfer_reference")
    private UUID transferReference;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    private AccountMovement(
            Account account,
            MovementType type,
            MonetaryAmount amount,
            String idempotencyKey,
            UUID transferReference) {
        this.id = UUID.randomUUID();
        this.account = Objects.requireNonNull(account, "A conta é obrigatória.");
        this.type = Objects.requireNonNull(type, "O tipo de movimentação é obrigatório.");
        this.amount = Objects.requireNonNull(amount, "O valor é obrigatório.").value();
        this.balanceAfter = account.getBalance();
        this.idempotencyKey = new IdempotencyKey(idempotencyKey).value();
        this.transferReference = transferReference;
        this.occurredAt = Instant.now();
    }

    public static AccountMovement register(
            Account account, MovementType type, MonetaryAmount amount, String idempotencyKey) {
        return new AccountMovement(account, type, amount, idempotencyKey, null);
    }

    public static AccountMovement registerTransferDebit(
            Account account, MonetaryAmount amount, String idempotencyKey, UUID transferReference) {
        return new AccountMovement(account, MovementType.TRANSFER_DEBIT, amount, idempotencyKey, transferReference);
    }

    public static AccountMovement registerTransferCredit(
            Account account, MonetaryAmount amount, UUID transferReference) {
        return new AccountMovement(
                account,
                MovementType.TRANSFER_CREDIT,
                amount,
                "transfer-credit-" + transferReference,
                transferReference);
    }

    @PrePersist
    void prepareForPersistence() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
