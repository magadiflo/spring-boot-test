package org.magadiflo.test.springboot.app.services;

import org.magadiflo.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;

public interface ICuentaService {

    Cuenta findById(Long id);

    int revisarTotalTransferencias(Long bancoId);

    BigDecimal revisarSaldo(Long cuentaId);

    void transferir(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, Long bancoId);

}
