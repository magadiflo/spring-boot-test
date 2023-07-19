package com.magadiflo.app.data;

import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.Bank;

import java.math.BigDecimal;
import java.util.Optional;

public class DataTest {
    public static Optional<Account> account001() {
        return Optional.of(new Account(1L, "Martín", new BigDecimal("2000")));
    }

    public static Optional<Account> account002() {
        return Optional.of(new Account(2L, "Alicia", new BigDecimal("1000")));
    }

    public static Optional<Bank> bank() {
        return Optional.of(new Bank(1L, "Banco de la Nación", 0));
    }
}
