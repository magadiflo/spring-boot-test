package org.magadiflo.test.springboot.app.services;

import org.magadiflo.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;
import java.util.List;

public interface ICuentaService {

    List<Cuenta> findAll();

    Cuenta findById(Long id);

    Cuenta save(Cuenta cuenta);

    int revisarTotalTransferencias(Long bancoId);

    BigDecimal revisarSaldo(Long cuentaId);

    void transferir(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, Long bancoId);

}
