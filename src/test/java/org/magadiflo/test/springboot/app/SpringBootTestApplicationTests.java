package org.magadiflo.test.springboot.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
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
import java.util.Arrays;
import java.util.List;

/**
 * @SpringBootTest, Spring Boot usa esta anotación para que pueda integrar JUnit, Mockito, que es parte de
 * la autoconfiguración y el contexto de prueba
 */
@SpringBootTest
class SpringBootTestApplicationTests {

    @MockBean // Anotación de Spring, es similar a la anotación @Mock de Mockito
    ICuentaRepository cuentaRepository;
    @MockBean // Anotación de Spring, es similar a la anotación @Mock de Mockito
    IBancoRepository bancoRepository;

    // A diferencia de la anotación de Mockito que usa el @InjectMocks junto a una clase implementada,
    // aquí, con spring usamos nuestro tradicional @Autowired y una clase genérica, en este caso una
    // la interfaz ICuentaService para realizar la inyección de dependencia. Obviamente, la clase
    // CuentaServiceImpl debe estar anotada con @Service
    // Tomado de Sección 4: Spring Boot: Test de Servicios (Mockito)
    // 63. Uso de anotaciones de Spring @MockBean y @Autowired
    @Autowired
    ICuentaService cuentaService;

    @BeforeEach
    void setUp() {
//        this.cuentaRepository = Mockito.mock(ICuentaRepository.class); // Mockeamos el repositorio de cuenta
//        this.bancoRepository = Mockito.mock(IBancoRepository.class); // Mockeamos el repositorio de banco
//        this.cuentaService = new CuentaServiceImpl(cuentaRepository, bancoRepository); // Instanciamos porque se probará este servicio
    }

    @Test
    void contextLoads() {
        // GIVEN (Dado este contexto)
        Mockito.when(this.cuentaRepository.findById(1L)).thenReturn(Datos.cuenta001());
        Mockito.when(this.cuentaRepository.findById(2L)).thenReturn(Datos.cuenta002());
        Mockito.when(this.bancoRepository.findById(1L)).thenReturn(Datos.banco());

        // WHEN (cuando invocamos el método que queremos probar)
        BigDecimal saldoOrigen = this.cuentaService.revisarSaldo(1L);
        BigDecimal saldoDestino = this.cuentaService.revisarSaldo(2L);

        // THEN (entonces realizamos las pruebas)
        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        this.cuentaService.transferir(1L, 2L, new BigDecimal("100"), 1L);

        saldoOrigen = this.cuentaService.revisarSaldo(1L);
        saldoDestino = this.cuentaService.revisarSaldo(2L);

        assertEquals("900", saldoOrigen.toPlainString());
        assertEquals("2100", saldoDestino.toPlainString());

        // Validamos que haya 1 sola transferencia hasta el momento
        int total = this.cuentaService.revisarTotalTransferencias(1L);
        assertEquals(1, total);

        // Verificamos el número de veces que se llaman los métodos findById() y save() del cuentaRepository
        Mockito.verify(this.cuentaRepository, Mockito.times(3)).findById(1L);
        Mockito.verify(this.cuentaRepository, Mockito.times(3)).findById(2L);
        Mockito.verify(this.cuentaRepository, Mockito.times(2))
                .save(Mockito.any(Cuenta.class));

        // Verificamos el número de veces que se llaman los métodos findById() y save() del bancoRepository
        Mockito.verify(this.bancoRepository, Mockito.times(2)).findById(1L);
        Mockito.verify(this.bancoRepository).save(Mockito.any(Banco.class));

        // Verificamos el número de veces en total que se llaman los métodos findById() y findAll() del cuentaRepository
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

        // Verifica que se lance la excepción cuando el saldo es insuficiente
        assertThrows(DineroInsuficienteException.class, () -> {
            this.cuentaService.transferir(1L, 2L, new BigDecimal("1200"), 1L);
        });

        saldoOrigen = this.cuentaService.revisarSaldo(1L);
        saldoDestino = this.cuentaService.revisarSaldo(2L);

        // Al haberse lanzado la excepción, los saldos no deben haber sufrido modificaciones
        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        // Como se lanzó la excepción, validamos que haya 0 transferencias
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

        // Afirma que lo esperado y lo real se refiere al mismo objeto
        assertSame(cuenta1, cuenta2);

        // Otras formas de verificar que cuenta1 es igual que cuenta2
        Assertions.assertTrue(cuenta1 == cuenta2);
        assertEquals("Martín", cuenta1.getPersona());
        assertEquals("Martín", cuenta2.getPersona());

        Mockito.verify(this.cuentaRepository, Mockito.times(2)).findById(1L);
    }

    @Test
    void testFindAll() {
        // GIVEN
        List<Cuenta> datos = Arrays.asList(Datos.cuenta001().orElseThrow(), Datos.cuenta002().orElseThrow());
        Mockito.when(this.cuentaRepository.findAll()).thenReturn(datos);

        // WHEN
        List<Cuenta> cuentas = this.cuentaService.findAll();

        // THEN
        assertFalse(cuentas.isEmpty());
        assertEquals(2, cuentas.size());
        assertTrue(cuentas.contains(Datos.cuenta001().orElseThrow()));

        Mockito.verify(this.cuentaRepository).findAll();
    }

    @Test
    void testSave() {
        // GIVEN
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        Mockito.when(this.cuentaRepository.save(Mockito.any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        // WHEN
        Cuenta cuenta = this.cuentaService.save(cuentaPepe);

        // THEN
        assertEquals("Pepe", cuenta.getPersona());
        assertEquals(3, cuenta.getId());
        assertEquals("3000", cuenta.getSaldo().toPlainString());

        Mockito.verify(this.cuentaRepository).save(Mockito.any());
    }
}
