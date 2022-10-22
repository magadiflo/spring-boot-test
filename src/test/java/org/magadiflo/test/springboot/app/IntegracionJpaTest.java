package org.magadiflo.test.springboot.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.repositories.ICuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/***
 * @DataJpaTest, habilita el contexto de persistencia, bd en memoria, repositorios en spring,
 * inyección de dependencia, etc..
 */
@DataJpaTest
public class IntegracionJpaTest {

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
}
