package org.magadiflo.test.springboot.app.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
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
 * Pruebas unitarias al servicio
 * *****************************
 */

/**
 * @SpringBootTest
 * ***************
 * SpringBoot usa esta anotación para que pueda integrar JUnit y Mockito, que es parte de
 * la autoconfiguración y el contexto de prueba. Internamente usa una anotación
 * @ExtendWith(SpringExtension.class), para trabajar con una extensión propia de Spring
 * que permita trabajar con Jupiter JUnit 5.
 *
 * NOTA: Otros tutoriales, como el de amigosCode, usan para probar un servicio,
 * la extensión de Mockito: @ExtendWith(MockitoExtension.class), que es similar a cómo
 * realizamos la "sección 3: Mockito" de este curso.
 */

/**
 * (B) Uso de anotaciones (2 formas)
 * *********************************
 * 1° Forma, usando anotaciones de Mockito: @Mock, @InjectMocks
 * 2° Forma, usando anotaciones de Spring
 */
@SpringBootTest
class CuentaServiceImplTest {
    /**
     * 2° Forma, usando anotaciones de Spring
     * **************************************
     * 1. Las dependencias de la clase a probar las anotamos con @MockBean de Spring.
     *    Estos atributos los definimos del tipo de interfaz.
     * 2. Nos aseguramos que la clase concreta del servicio a probar esté anotado con @Service,
     *    o con aquella anotación que le permita ser manejado por el contendedor de Spring.
     * 3. Definimos un atributo para la clase a probar (cuentaService) del tipo de su Interfaz
     *    y lo anotamos con @Autowired. Será Spring quien haga la inyección de dependencia y
     *    defina su clase concreta.
     *
     * Explicación adicional sobre esta 2° Forma
     * ******************************************
     * A diferencia de la anotación de Mockito que usa el @InjectMocks junto a una clase
     * implementada, aquí, con spring usamos nuestro tradicional @Autowired y una clase genérica,
     * en este caso la interfaz ICuentaService para realizar la inyección de dependencia.
     * Obviamente, la clase CuentaServiceImpl debe estar anotada con @Service, para que se
     * registre como un componente del framework en el contenedor de Spring.
     *
     * @MockBean
     * *********
     * Se usa para agregar objetos simulados al contexto de la aplicación Spring. El simulacro
     * reemplazará cualquier bean existente del mismo tipo en el contexto de la aplicación.
     * Si no se define un bean del mismo tipo, se agregará uno nuevo.
     *
     * Cuando utilizamos la anotación en un campo, el simulacro se inyectará
     * en el campo, además de registrarse en el contexto de la aplicación.
     */
    @MockBean
    ICuentaRepository cuentaRepository;
    @MockBean
    IBancoRepository bancoRepository;
    @Autowired
    ICuentaService cuentaService;


    /**
     * 1° Forma, usando anotaciones de Mockito
     * ***************************************
     * 1. Las dependencias de la clase a probar las anotamos con @Mock. Estos atributos
     *    pueden ser del tipo de interfaces o clases concretas.
     * 2. Definimos un atributo para la clase a probar (cuentaService). Como este atributo
     *    será anotado con @InjectMocks, es necesario que el tipo del atributo sea una
     *    clase concreta y no una interfaz.

     @Mock
     ICuentaRepository cuentaRepository;
     @Mock
     IBancoRepository bancoRepository;
     @InjectMocks
     CuentaServiceImpl cuentaService;
     */

    /**
     * (A) Sin anotaciones, usando Mockito de manera manual
     * *****************************************************
     * 1° Definimos como atributos las dependencias que tiene la clase a probar.
     *    En este caso definimos del tipo de interfaz para esos dos atributos.
     * 2° Definimos como atributo la clase a probar, en este caso declaramos
     *    su interfaz y en el método setUp() la creamos como clase concreta.
     *

     ICuentaRepository cuentaRepository;
     IBancoRepository bancoRepository;
     ICuentaService cuentaService;

     @BeforeEach
     void setUp() {
     this.cuentaRepository = Mockito.mock(ICuentaRepository.class);
     this.bancoRepository = Mockito.mock(IBancoRepository.class);
     this.cuentaService = new CuentaServiceImpl(this.cuentaRepository, this.bancoRepository);
     }
     */

    @Test
    void contextLoads() {
        /**
         * Ocurre algo curioso según vi y otros alumnos del curso también notaron, es que
         * cuando mockeamos los repositorios cuentaRepository.finById(...) con el 1L y 2L,
         * te retorna el dato definido para cada id, bueno hasta ahí no hay ningún problema,
         * es lo que debería suceder. Pero, en una parte de este test llamamos al método
         * cuentaService.transferir(...) mandándole el identificador de cada cuenta que mockeamos,
         * este método obtiene las cuentas a partir del identificador y llamando al
         * cuentaRepository.findById(...) para cada id y luego aplica el débito y crédito a las
         * cuentas. Bien, hasta ese punto lo que sucede es que tanto cuenta de origen como cuenta
         * de destino se modifican por el débito(...) y crédito(..), luego hay un update(..) de
         * cada cuenta, pero eso no se aplica, la prueba lo pasa. El detalle es que las modificaciones
         * que se hicieron de las cuentas origen y destino fueron de manera local, dentro del método
         * transferir(...) y sucede que al continuar la ejecución del test, se vuelve a revisar los
         * saldos de cambas cuentas y efectivamente allí se obtiene el saldo de cada cuenta ya
         * modificada. La pregunta es, ¿Por qué?
         *
         * Según estuve debugueando, como definimos un valor de retorno para cada mock del
         * cuentaRepository.findById(...) 1L y 2L, se crea una referencia en memoria para cada
         * mock a retornar (por ejm: Cuenta@10756, Cuenta@10819), lo que sucede es que cada
         * vez que llama a ese método cuentaRepository.findById(...) con el 1L o 2L,
         * obtiene siempre la misma referencia según el id, y como en el método transferir(...)
         * se modifica, lo que se modifica es su propiedad saldo, pero la referencia será la misma,
         * por eso es que cuando volvemos a revisarSaldo(...) para cada id, vemos que el saldo cambio.
         *
         * Ahora, estos valores los definiremos dentro de un método estático que retornará siempre
         * un nuevo objeto de cuenta, esto con la finalidad de que cada método a probar tenga
         * un nuevo objeto de Cuenta y no sea común entre ellos. Ya que por ejemplo, inicialmente
         * se los tenían definidos como constantes y al ejecutar los test, un test modificaba los
         * valores internos de esas constantes y eso afectaba a otros test. Para solucionar ese problema
         * es que se los definió dentro de un método estático.
         */
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
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            this.cuentaService.transferir(1L, 2L, new BigDecimal("1200"), 1L);
        });

        Assertions.assertEquals(DineroInsuficienteException.class, exception.getClass(), () -> "se espera que lance la excepción DineroInsuficienteException");

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
        assertEquals( datos, cuentas);
        assertTrue(cuentas.contains(Datos.cuenta001().orElseThrow()));

        Mockito.verify(this.cuentaRepository).findAll();
    }

    @Test
    void testSave() {
        // GIVEN
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));

        /* Una forma de retornar el objeto que se le pasa con el id asignado
        Mockito.when(this.cuentaRepository.save(Mockito.any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });
        */

        // Otra forma de retornar el objeto que se le pasa con el id asignado
        Mockito.doAnswer(invocation -> {
            Cuenta cuenta = invocation.getArgument(0);
            cuenta.setId(3L);
            return cuenta;
        }).when(this.cuentaRepository).save(Mockito.any(Cuenta.class));

        // WHEN
        Cuenta cuenta = this.cuentaService.save(cuentaPepe);

        // THEN
        assertEquals("Pepe", cuenta.getPersona());
        assertEquals(3, cuenta.getId());
        assertEquals("3000", cuenta.getSaldo().toPlainString());

        Mockito.verify(this.cuentaRepository).save(Mockito.any());
    }
}
