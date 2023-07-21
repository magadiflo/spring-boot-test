package com.magadiflo.app.services;

import com.magadiflo.app.models.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IAccountService {
    List<Account> findAll();

    Optional<Account> findById(Long id);

    Account save(Account account);

    int reviewTotalTransfers(Long bancoId);

    BigDecimal reviewBalance(Long accountId);

    void transfer(Long bankId, Long accountIdOrigen, Long accountIdDestination, BigDecimal amount);
}
