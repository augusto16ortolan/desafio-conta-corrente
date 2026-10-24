package com.example.financialaccount.domain.account;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomAccountNumberGenerator implements AccountNumberGenerator {

    private static final int RADIX = 10;

    private final SecureRandom random = new SecureRandom();

    @Override
    public AccountNumber generate() {
        var number = new StringBuilder(AccountNumber.LENGTH);

        for (var index = 0; index < AccountNumber.LENGTH; index++) {
            number.append(random.nextInt(RADIX));
        }

        return AccountNumber.from(number.toString());
    }
}
