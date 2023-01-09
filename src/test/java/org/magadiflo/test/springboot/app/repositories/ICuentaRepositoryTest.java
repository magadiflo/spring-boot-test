package org.magadiflo.test.springboot.app.repositories;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.magadiflo.test.springboot.app.models.Cuenta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Pruebas unitarias al repositorio
 * ********************************
 */

/**
 * @DataJpaTest
 * *************
 * Autoconfigura la la Base de Datos H2 (bd en memoria), con una conexión por defecto.
 * Configura el contexto de persistencia, todo lo que es JPA/Hibernate, realiza un
 * entity scan para escanear las entidades mapeadas a las tablas de la BD, activa el log SQL
 * para mostrar las consultas y operaciones que se están realizando en el contexto test, etc..
 *
 * Permitirá hacer las pruebas con conexión a la BD (h2) definida en el application.properties
 * del directorio src/test/resources
 */

/**
 * Ejecutando Pruebas Unitarias
 * *********************************
 * Cada vez que ejecutamos las pruebas, crea las tablas en memoria (H2) y al finalizar
 * la ejecución de todas las pruebas las elimina.
 *
 * Cada método test es independiente, es decir si un método test realiza alguna modificación
 * en alguna tabla, ya sea una eliminación, una actualización, etc. al finalizar ese método
 * test, se realiza un rollback para que cuando inicie el siguiente test se empiece con los
 * datos originales, es decir sin modificación. De esa forma todos los test iniciarán siempre
 * con el mismo estado de las tablas.
 */

/**
 * Tener en cuenta:
 * ****************
 * El repositorio que vamos a probar es ICuentaRepository donde definimos dos métodos personalizados
 * y además hacemos que herede de JpaRepository:
 *
 * findByPersona(persona);
 * encuentraPorPersona(persona);
 *
 * Ahora, cuando hagamos pruebas, únicamente probaremos los métodos que nosotros creemos,
 * en este caso, deberíamos probar solo esos dos métodos personalizados.
 * Ahora, como en el ICuentaRepository estamos extendiendo de JpaRepository, tendremos muchos más métodos,
 * pero son métodos que ya vienen, y nosotros no debemos probar eso porque ya nos los proporcionan
 * probados.
 *
 * CONCLUSIÓN: Los únicos métodos que debemos probar son los métodos que agregamos a nuestra interfaz,
 * en nuestro caso el findByPersona(persona) y el encuentraPorPersona(persona).
 *
 * Ahora, siguiendo el tutorial, se hacen pruebas de los métodos que no hemos creado
 * como el .findAll(), .save(...), etc. pero solo para temas de aprendizaje, ya en el campo real
 * deberíamos hacer pruebas solo de aquellos métodos que nosotros hemos creado.
 */
@Tag(value = "integracion_jpa")
@DataJpaTest
public class ICuentaRepositoryTest {

    @Autowired
    ICuentaRepository cuentaRepository;

    @Test
    void testFindById() {
        Optional<Cuenta> cuentaOptional = this.cuentaRepository.findById(1L);

        assertTrue(cuentaOptional.isPresent());
        assertEquals("Martín", cuentaOptional.orElseThrow().getPersona());
    }

    @Test
    void testFindByPersona() {
        Optional<Cuenta> cuentaOptional = this.cuentaRepository.findByPersona("Martín");

        assertTrue(cuentaOptional.isPresent());
        assertEquals("Martín", cuentaOptional.orElseThrow().getPersona());
        assertEquals("1000.00", cuentaOptional.orElseThrow().getSaldo().toPlainString());
    }

    @Test
    void testFindByPersonaThrowException() {
        Optional<Cuenta> cuentaOptional = this.cuentaRepository.findByPersona("Tinkler");
        assertThrows(NoSuchElementException.class, cuentaOptional::orElseThrow);
        assertFalse(cuentaOptional.isPresent());
    }

    @Test
    void testFindAll() {
        List<Cuenta> cuentas = this.cuentaRepository.findAll();
        assertFalse(cuentas.isEmpty());
        assertEquals(2, cuentas.size());
    }

    @Test
    void testSave() {
        // Given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));

        // When
        Cuenta cuenta = this.cuentaRepository.save(cuentaPepe);
        //Cuenta cuenta = this.cuentaRepository.findByPersona("Pepe").orElseThrow();
        //Cuenta cuenta = this.cuentaRepository.findById(cuentaSave.getId()).orElseThrow();

        // Then
        assertNotNull(cuenta.getId());
        assertEquals("Pepe", cuenta.getPersona());
        assertEquals("3000", cuenta.getSaldo().toPlainString());
        //assertEquals(3, cuenta.getId()); no es recomendable usar el id ya que podría cambiar
    }

    @Test
    void testUpdate() {
        // Given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));

        // When (saving)
        Cuenta cuenta = this.cuentaRepository.save(cuentaPepe);

        // Then
        assertEquals("Pepe", cuenta.getPersona());
        assertEquals("3000", cuenta.getSaldo().toPlainString());

        // When (Updating)
        cuenta.setSaldo(new BigDecimal("3800"));
        Cuenta cuentaActualizada = this.cuentaRepository.save(cuenta);

        // Then
        assertEquals("Pepe", cuentaActualizada.getPersona());
        assertEquals("3800", cuentaActualizada.getSaldo().toPlainString());
    }

    @Test
    void testDelete() {
        // Given
        Cuenta cuenta = this.cuentaRepository.findById(1L).orElseThrow();
        assertEquals("Martín", cuenta.getPersona());

        // When
        this.cuentaRepository.delete(cuenta);

        assertThrows(NoSuchElementException.class, () -> {
            this.cuentaRepository.findById(1L).orElseThrow();
        });

        assertEquals(1, this.cuentaRepository.findAll().size());
    }
}
