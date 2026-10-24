CREATE TABLE account_movements (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    transfer_reference UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_account_movements_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT uk_account_movements_idempotency
        UNIQUE (account_id, idempotency_key),
    CONSTRAINT ck_account_movements_type
        CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_DEBIT', 'TRANSFER_CREDIT')),
    CONSTRAINT ck_account_movements_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_account_movements_balance_after_non_negative CHECK (balance_after >= 0)
);

CREATE INDEX idx_account_movements_account_occurred_at
    ON account_movements (account_id, occurred_at DESC);
