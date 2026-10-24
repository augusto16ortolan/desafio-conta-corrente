package com.example.financialaccount.domain.identitynumber;

public enum IdentityNumberType {
    CPF(11),
    CNPJ(14);

    private final int length;

    IdentityNumberType(int length) {
        this.length = length;
    }

    public int length() {
        return length;
    }

    public static IdentityNumberType fromLength(int length) {
        for (var type : values()) {
            if (type.length == length) {
                return type;
            }
        }

        throw new IllegalArgumentException("CPF ou CNPJ deve ter 11 ou 14 dígitos.");
    }
}
