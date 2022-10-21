package org.magadiflo.test.springboot.app.data;

import org.magadiflo.test.springboot.app.models.Banco;
import org.magadiflo.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;

public class Datos {
    public static Cuenta cuenta001() {
        return new Cuenta(1L, "Martín", new BigDecimal("1000"));
    }

    public static Cuenta cuenta002() {
        return new Cuenta(2L, "Gaspar", new BigDecimal("2000"));
    }

    public static Banco banco() {
        return new Banco(1L, "Banco de la Nación", 0);
    }
}
