package com.magadiflo.app.repositories;

import com.magadiflo.app.models.Bank;

import java.util.List;
import java.util.Optional;

public interface IBankRepository {
    List<Bank> findAll();

    Optional<Bank> findById(Long id);

    Bank update(Bank bank);
}
