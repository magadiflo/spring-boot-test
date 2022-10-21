package org.magadiflo.test.springboot.app.data;

import org.magadiflo.test.springboot.app.models.Banco;
import org.magadiflo.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;

public class Datos {
    public static final Cuenta CUENTA_001 = new Cuenta(1L, "Martín", new BigDecimal("1000"));
    public static final Cuenta CUENTA_002 = new Cuenta(2L, "Gaspar", new BigDecimal("2000"));
    public static final Banco BANCO = new Banco(1L, "Banco de la Nación", 0);
}
