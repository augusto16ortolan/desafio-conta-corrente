package com.example.financialaccount.domain.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public record StatementInput(UUID accountId, Instant startDate, Instant endDate, Pageable pageable) {

    private static final int MAXIMUM_PAGE_SIZE = 100;

    public StatementInput {
        Objects.requireNonNull(accountId, "O identificador da conta é obrigatório.");
        Objects.requireNonNull(pageable, "A paginação é obrigatória.");

        if (pageable.isUnpaged()) {
            throw new IllegalArgumentException("A paginação é obrigatória.");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }

        if (pageable.getPageSize() < 1 || pageable.getPageSize() > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException("O tamanho da página deve estar entre 1 e 100.");
        }
    }
}
