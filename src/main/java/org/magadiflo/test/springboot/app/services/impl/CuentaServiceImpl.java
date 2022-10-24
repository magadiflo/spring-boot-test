package org.magadiflo.test.springboot.app.services.impl;

import org.magadiflo.test.springboot.app.models.Banco;
import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.repositories.IBancoRepository;
import org.magadiflo.test.springboot.app.repositories.ICuentaRepository;
import org.magadiflo.test.springboot.app.services.ICuentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaServiceImpl implements ICuentaService {

    private final ICuentaRepository cuentaRepository;
    private final IBancoRepository bancoRepository;

    public CuentaServiceImpl(ICuentaRepository cuentaRepository, IBancoRepository bancoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.bancoRepository = bancoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> findAll() {
        return this.cuentaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta findById(Long id) {
        return this.cuentaRepository.findById(id).orElseThrow();
    }

    @Override
    @Transactional
    public Cuenta save(Cuenta cuenta) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public int revisarTotalTransferencias(Long bancoId) {
        Banco banco = this.bancoRepository.findById(bancoId).orElseThrow();
        return banco.getTotalTransferencia();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal revisarSaldo(Long cuentaId) {
        Cuenta cuenta = this.cuentaRepository.findById(cuentaId).orElseThrow();
        return cuenta.getSaldo();
    }

    @Override
    @Transactional
    public void transferir(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, Long bancoId) {
        Cuenta cOrigen = this.cuentaRepository.findById(cuentaOrigenId).orElseThrow();
        Cuenta cDestino = this.cuentaRepository.findById(cuentaDestinoId).orElseThrow();

        cOrigen.debito(monto);
        cDestino.credito(monto);

        this.cuentaRepository.save(cOrigen);
        this.cuentaRepository.save(cDestino);

        Banco banco = this.bancoRepository.findById(bancoId).orElseThrow();
        int totalTransferencias = banco.getTotalTransferencia();
        banco.setTotalTransferencia(++totalTransferencias);
        this.bancoRepository.save(banco);
    }
}
