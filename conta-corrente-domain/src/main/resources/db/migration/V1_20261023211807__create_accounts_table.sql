CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    identity_number VARCHAR(14) NOT NULL,
    identity_number_type VARCHAR(4) NOT NULL,
    account_number VARCHAR(10) NOT NULL,
    check_digit INTEGER NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_accounts_identity_number UNIQUE (identity_number),
    CONSTRAINT uk_accounts_number_check_digit UNIQUE (account_number, check_digit),
    CONSTRAINT ck_accounts_identity_number_type CHECK (identity_number_type IN ('CPF', 'CNPJ')),
    CONSTRAINT ck_accounts_check_digit_range CHECK (check_digit BETWEEN 0 AND 9),
    CONSTRAINT ck_accounts_balance_non_negative CHECK (balance >= 0)
);
