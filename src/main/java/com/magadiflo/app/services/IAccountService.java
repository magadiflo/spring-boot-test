package com.magadiflo.app.services;

import com.magadiflo.app.models.Account;

import java.math.BigDecimal;
import java.util.Optional;

public interface IAccountService {
    Optional<Account> findById(Long id);

    int reviewTotalTransfers(Long bancoId);

    BigDecimal reviewBalance(Long accountId);

    void transfer(Long accountIdOrigen, Long accountIdDestination, BigDecimal amount);
}
