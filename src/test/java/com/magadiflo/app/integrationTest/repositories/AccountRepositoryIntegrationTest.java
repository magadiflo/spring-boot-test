package com.magadiflo.app.integrationTest.repositories;

import com.magadiflo.app.models.Account;
import com.magadiflo.app.repositories.IAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
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

    @Test
    void should_save_an_account() {
        Account account = new Account(null, "Are", new BigDecimal("1500"));

        Account accountDB = this.accountRepository.save(account);

        assertNotNull(accountDB.getId());
        assertEquals("Are", accountDB.getPerson());
        assertEquals(1500D, accountDB.getBalance().doubleValue());
    }

    @Test
    void should_update_an_account() {
        Account account = new Account(null, "Are", new BigDecimal("1500"));

        Account accountDB = this.accountRepository.save(account);

        assertNotNull(accountDB.getId());
        assertEquals("Are", accountDB.getPerson());
        assertEquals(1500D, accountDB.getBalance().doubleValue());

        accountDB.setBalance(new BigDecimal("3800"));
        accountDB.setPerson("Karen Caldas");

        Account accountUpdated = this.accountRepository.save(accountDB);

        assertEquals(accountDB.getId(), accountUpdated.getId());
        assertEquals("Karen Caldas", accountUpdated.getPerson());
        assertEquals(3800D, accountUpdated.getBalance().doubleValue());
    }

    @Test
    void should_delete_an_account() {
        Optional<Account> accountDB = this.accountRepository.findById(1L);
        assertTrue(accountDB.isPresent());

        this.accountRepository.delete(accountDB.get());

        Optional<Account> accountDelete = this.accountRepository.findById(1L);
        assertTrue(accountDelete.isEmpty());
    }
}