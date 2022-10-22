package org.magadiflo.test.springboot.app.repositories;

import org.magadiflo.test.springboot.app.models.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBancoRepository extends JpaRepository<Banco, Long> {

}
