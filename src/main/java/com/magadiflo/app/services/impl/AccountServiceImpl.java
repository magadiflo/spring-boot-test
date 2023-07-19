package com.magadiflo.app.services.impl;

import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.Bank;
import com.magadiflo.app.repositories.IAccountRepository;
import com.magadiflo.app.repositories.IBankRepository;
import com.magadiflo.app.services.IAccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AccountServiceImpl implements IAccountService {
    private final IAccountRepository accountRepository;
    private final IBankRepository bankRepository;

    public AccountServiceImpl(IAccountRepository accountRepository, IBankRepository bankRepository) {
        this.accountRepository = accountRepository;
        this.bankRepository = bankRepository;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return this.accountRepository.findById(id);
    }

    @Override
    public int reviewTotalTransfers(Long bancoId) {
        Bank bank = this.bankRepository.findById(bancoId)
                .orElseThrow(() -> new NoSuchElementException("No existe el banco buscado"));
        return bank.getTotalTransfers();
    }

    @Override
    public BigDecimal reviewBalance(Long accountId) {
        Account account = this.accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("No existe la cuenta buscada"));
        return account.getBalance();
    }

    @Override
    public void transfer(Long bankId, Long accountIdOrigen, Long accountIdDestination, BigDecimal amount) {
        Account accountOrigen = this.accountRepository.findById(accountIdOrigen)
                .orElseThrow(() -> new NoSuchElementException("No existe el id de la cuenta origen"));
        Account accountDestination = this.accountRepository.findById(accountIdDestination)
                .orElseThrow(() -> new NoSuchElementException("No existe el id de la cuenta destino"));

        accountOrigen.debit(amount);
        accountDestination.credit(amount);

        Bank bank = this.bankRepository.findById(bankId)
                .orElseThrow(() -> new NoSuchElementException("No existe el id del banco"));

        bank.setTotalTransfers(bank.getTotalTransfers() + 1);

        this.accountRepository.update(accountOrigen);
        this.accountRepository.update(accountDestination);
        this.bankRepository.update(bank);
    }
}
