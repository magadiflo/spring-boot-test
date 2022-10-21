package org.magadiflo.test.springboot.app.repositories;

import org.magadiflo.test.springboot.app.models.Cuenta;

import java.util.List;

public interface ICuentaRepository {

    List<Cuenta> findAll();

    Cuenta findById(Long id);

    void update(Cuenta cuenta);

}
