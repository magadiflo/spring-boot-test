package com.magadiflo.app.integrationTest.repositories;

import com.magadiflo.app.models.Account;
import com.magadiflo.app.repositories.IAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryIntegrationTest {
    @Autowired
    private IAccountRepository accountRepository;

    @Test
    void should_find_an_account_by_id() {
        Optional<Account> account = this.accountRepository.findById(1L);

        assertTrue(account.isPresent());
        assertEquals("Martín", account.get().getPerson());
    }

    @Test
    void should_find_an_account_by_person() {
        Optional<Account> account = this.accountRepository.findAccountByPerson("Martín");

        assertTrue(account.isPresent());
        assertEquals("Martín", account.get().getPerson());
        assertEquals(2000D, account.get().getBalance().doubleValue());
    }

    @Test
    void should_not_find_an_account_by_person_that_does_not_exist() {
        Optional<Account> account = this.accountRepository.findAccountByPerson("Pepito");

        assertTrue(account.isEmpty());
    }

    @Test
    void should_find_all_accounts() {
        List<Account> accounts = this.accountRepository.findAll();
        assertFalse(accounts.isEmpty());
        assertEquals(2, accounts.size());
    }
}