package org.magadiflo.test.springboot.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.magadiflo.test.springboot.app.data.Datos;
import org.magadiflo.test.springboot.app.exceptions.DineroInsuficienteException;
import org.magadiflo.test.springboot.app.models.Banco;
import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.repositories.IBancoRepository;
import org.magadiflo.test.springboot.app.repositories.ICuentaRepository;
import org.magadiflo.test.springboot.app.services.ICuentaService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

@SpringBootTest
class SpringBootTestApplicationTests {

    @MockBean
    ICuentaRepository cuentaRepository;
    @MockBean
    IBancoRepository bancoRepository;
    @Autowired
    ICuentaService cuentaService;

    @BeforeEach
    void setUp() {
//        this.cuentaRepository = Mockito.mock(ICuentaRepository.class);
//        this.bancoRepository = Mockito.mock(IBancoRepository.class);
//        this.cuentaService = new CuentaServiceImpl(cuentaRepository, bancoRepository);
    }

    @Test
    void contextLoads() {
        Mockito.when(this.cuentaRepository.findById(1L)).thenReturn(Datos.cuenta001());
        Mockito.when(this.cuentaRepository.findById(2L)).thenReturn(Datos.cuenta002());
        Mockito.when(this.bancoRepository.findById(1L)).thenReturn(Datos.banco());

        BigDecimal saldoOrigen = this.cuentaService.revisarSaldo(1L);
        BigDecimal saldoDestino = this.cuentaService.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        this.cuentaService.transferir(1L, 2L, new BigDecimal("100"), 1L);

        saldoOrigen = this.cuentaService.revisarSaldo(1L);
        saldoDestino = this.cuentaService.revisarSaldo(2L);

        assertEquals("900", saldoOrigen.toPlainString());
        assertEquals("2100", saldoDestino.toPlainString());

        int total = this.cuentaService.revisarTotalTransferencias(1L);

        assertEquals(1, total);

        //Verificamos el número de veces que se llaman los métodos de cada repository
        Mockito.verify(this.cuentaRepository, Mockito.times(3)).findById(1L);
        Mockito.verify(this.cuentaRepository, Mockito.times(3)).findById(2L);
        Mockito.verify(this.cuentaRepository, Mockito.times(2))
                .save(Mockito.any(Cuenta.class));

        Mockito.verify(this.bancoRepository, Mockito.times(2)).findById(1L);
        Mockito.verify(this.bancoRepository).save(Mockito.any(Banco.class));

        Mockito.verify(this.cuentaRepository, Mockito.times(6)).findById(Mockito.anyLong());
        Mockito.verify(this.cuentaRepository, Mockito.never()).findAll();
    }

    @Test
    void contextLoads2() {
        Mockito.when(this.cuentaRepository.findById(1L)).thenReturn(Datos.cuenta001());
        Mockito.when(this.cuentaRepository.findById(2L)).thenReturn(Datos.cuenta002());
        Mockito.when(this.bancoRepository.findById(1L)).thenReturn(Datos.banco());

        BigDecimal saldoOrigen = this.cuentaService.revisarSaldo(1L);
        BigDecimal saldoDestino = this.cuentaService.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        assertThrows(DineroInsuficienteException.class, () -> {
            this.cuentaService.transferir(1L, 2L, new BigDecimal("1200"), 1L);
        });

        saldoOrigen = this.cuentaService.revisarSaldo(1L);
        saldoDestino = this.cuentaService.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        int total = this.cuentaService.revisarTotalTransferencias(1L);

        assertEquals(0, total);

        //Verificamos el número de veces que se llaman los métodos de cada repository
        Mockito.verify(this.cuentaRepository, Mockito.times(3)).findById(1L);
        Mockito.verify(this.cuentaRepository, Mockito.times(3)).findById(2L);
        Mockito.verify(this.cuentaRepository, Mockito.never()).save(Mockito.any(Cuenta.class));

        Mockito.verify(this.bancoRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(this.bancoRepository, Mockito.never()).save(Mockito.any(Banco.class));

        Mockito.verify(this.cuentaRepository, Mockito.times(6)).findById(Mockito.anyLong());
        Mockito.verify(this.cuentaRepository, Mockito.never()).findAll();
    }

    @Test
    void contextLoad3() {
        Mockito.when(this.cuentaRepository.findById(1L)).thenReturn(Datos.cuenta001());

        Cuenta cuenta1 = this.cuentaService.findById(1L);
        Cuenta cuenta2 = this.cuentaService.findById(1L);

        assertSame(cuenta1, cuenta2);
        assertEquals("Martín", cuenta1.getPersona());
        assertEquals("Martín", cuenta2.getPersona());

        Mockito.verify(this.cuentaRepository, Mockito.times(2)).findById(1L);
    }
}
