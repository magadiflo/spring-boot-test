package com.magadiflo.app.repositories;

import com.magadiflo.app.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IAccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByPerson(String person);

    @Query(value = "SELECT a FROM Account AS a WHERE a.person = ?1")
    Optional<Account> findAccountByPerson(String person);
}
