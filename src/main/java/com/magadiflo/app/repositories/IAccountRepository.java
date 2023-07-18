package com.magadiflo.app.repositories;

import com.magadiflo.app.models.Account;

import java.util.List;
import java.util.Optional;

public interface IAccountRepository {
    List<Account> findAll();

    Optional<Account> findById(Long id);

    Account update(Account account);
}
