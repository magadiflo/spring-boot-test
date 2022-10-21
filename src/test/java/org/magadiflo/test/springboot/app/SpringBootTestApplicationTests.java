package org.magadiflo.test.springboot.app;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.magadiflo.test.springboot.app.data.Datos;
import org.magadiflo.test.springboot.app.repositories.IBancoRepository;
import org.magadiflo.test.springboot.app.repositories.ICuentaRepository;
import org.magadiflo.test.springboot.app.services.ICuentaService;
import org.magadiflo.test.springboot.app.services.impl.CuentaServiceImpl;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class SpringBootTestApplicationTests {

	ICuentaRepository cuentaRepository;
	IBancoRepository bancoRepository;
	ICuentaService cuentaService;

	@BeforeEach
	void setUp() {
		this.cuentaRepository = Mockito.mock(ICuentaRepository.class);
		this.bancoRepository = Mockito.mock(IBancoRepository.class);
		this.cuentaService = new CuentaServiceImpl(cuentaRepository, bancoRepository);
	}

	@Test
	void contextLoads() {
		Mockito.when(this.cuentaRepository.findById(1L)).thenReturn(Datos.CUENTA_001);
		Mockito.when(this.cuentaRepository.findById(2L)).thenReturn(Datos.CUENTA_002);
		Mockito.when(this.bancoRepository.findById(1L)).thenReturn(Datos.BANCO);

		BigDecimal saldoOrigen = this.cuentaService.revisarSaldo(1L);
		BigDecimal saldoDestino = this.cuentaService.revisarSaldo(2L);

		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());

		this.cuentaService.transferir(1L, 2L, new BigDecimal("100"), 1L);

		saldoOrigen = this.cuentaService.revisarSaldo(1L);
		saldoDestino = this.cuentaService.revisarSaldo(2L);

		assertEquals("900", saldoOrigen.toPlainString());
		assertEquals("2100", saldoDestino.toPlainString());
	}

}
