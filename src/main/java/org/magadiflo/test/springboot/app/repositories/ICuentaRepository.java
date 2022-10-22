package org.magadiflo.test.springboot.app.repositories;

import org.magadiflo.test.springboot.app.models.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICuentaRepository extends JpaRepository<Cuenta, Long> {

}
