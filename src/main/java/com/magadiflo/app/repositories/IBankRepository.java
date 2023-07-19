package com.magadiflo.app.repositories;

import com.magadiflo.app.models.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBankRepository extends JpaRepository<Bank, Long> {

}
