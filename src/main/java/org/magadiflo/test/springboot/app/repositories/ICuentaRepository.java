package org.magadiflo.test.springboot.app.repositories;

import org.magadiflo.test.springboot.app.models.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ICuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByPersona(String persona);

    @Query("SELECT c FROM Cuenta AS c WHERE c.persona = ?1")
    Optional<Cuenta> encuentraPorPersona(String persona);

}
