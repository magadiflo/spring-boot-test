package org.magadiflo.test.springboot.app.data;

import org.magadiflo.test.springboot.app.models.Banco;
import org.magadiflo.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;
import java.util.Optional;

public class Datos {
    public static Optional<Cuenta> cuenta001() {
        return Optional.of(new Cuenta(1L, "Martín", new BigDecimal("1000")));
    }

    public static Optional<Cuenta> cuenta002() {
        return Optional.of(new Cuenta(2L, "Gaspar", new BigDecimal("2000")));
    }

    public static Optional<Banco> banco() {
        return Optional.of(new Banco(1L, "Banco de la Nación", 0));
    }
}
