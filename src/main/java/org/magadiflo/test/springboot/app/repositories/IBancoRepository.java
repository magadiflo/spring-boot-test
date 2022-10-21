package org.magadiflo.test.springboot.app.repositories;

import org.magadiflo.test.springboot.app.models.Banco;

import java.util.List;

public interface IBancoRepository {

    List<Banco> findAll();

    Banco findById(Long id);

    void update(Banco banco);

}
