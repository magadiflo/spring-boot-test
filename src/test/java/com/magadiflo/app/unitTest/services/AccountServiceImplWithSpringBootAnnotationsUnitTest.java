package com.magadiflo.app.unitTest.services;

import com.magadiflo.app.data.DataTest;
import com.magadiflo.app.exceptions.InsufficientMoneyException;
import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.Bank;
import com.magadiflo.app.repositories.IAccountRepository;
import com.magadiflo.app.repositories.IBankRepository;
import com.magadiflo.app.services.IAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AccountServiceImplWithSpringBootAnnotationsUnitTest {
    @MockBean
    IAccountRepository accountRepository;
    @MockBean
    IBankRepository bankRepository;
    @Autowired
    IAccountService accountService;

    @Test
    void canTransferBetweenAccounts() {
        Long accountIdOrigen = 1L;
        Long accountIdDestination = 2L;
        Long bankId = 1L;

        when(this.accountRepository.findById(accountIdOrigen)).thenReturn(DataTest.account001());
        when(this.accountRepository.findById(accountIdDestination)).thenReturn(DataTest.account002());
        when(this.bankRepository.findById(bankId)).thenReturn(DataTest.bank());

        BigDecimal balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        BigDecimal balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(2000D, balanceOriginal.doubleValue());
        assertEquals(1000D, balanceDestination.doubleValue());

        this.accountService.transfer(bankId, accountIdOrigen, accountIdDestination, new BigDecimal("500"));

        balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(1500D, balanceOriginal.doubleValue());
        assertEquals(1500D, balanceDestination.doubleValue());

        int total = this.accountService.reviewTotalTransfers(bankId);
        assertEquals(1, total);

        verify(this.accountRepository, times(3)).findById(accountIdOrigen);
        verify(this.accountRepository, times(3)).findById(accountIdDestination);
        verify(this.accountRepository, times(2)).save(any(Account.class));

        verify(this.bankRepository, times(2)).findById(bankId);
        verify(this.bankRepository).save(any(Bank.class));
    }

    @Test
    void willThrowExceptionWhenBalanceIsLessThanAmountToBeTransfer() {
        Long accountIdOrigen = 1L;
        Long accountIdDestination = 2L;
        Long bankId = 1L;

        when(this.accountRepository.findById(accountIdOrigen)).thenReturn(DataTest.account001());
        when(this.accountRepository.findById(accountIdDestination)).thenReturn(DataTest.account002());
        when(this.bankRepository.findById(bankId)).thenReturn(DataTest.bank());

        BigDecimal balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        BigDecimal balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(2000D, balanceOriginal.doubleValue());
        assertEquals(1000D, balanceDestination.doubleValue());

        InsufficientMoneyException exception = assertThrows(InsufficientMoneyException.class, () -> {
            this.accountService.transfer(bankId, accountIdOrigen, accountIdDestination, new BigDecimal("2500"));
        });

        assertEquals(InsufficientMoneyException.class, exception.getClass());

        balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(2000D, balanceOriginal.doubleValue());
        assertEquals(1000D, balanceDestination.doubleValue());

        int total = this.accountService.reviewTotalTransfers(bankId);
        assertEquals(0, total);

        verify(this.accountRepository, times(3)).findById(accountIdOrigen);
        verify(this.accountRepository, times(3)).findById(accountIdDestination);
        verify(this.accountRepository, never()).save(any(Account.class));

        verify(this.bankRepository, times(1)).findById(bankId);
        verify(this.bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void canVerifyThatTwoInstancesAreTheSame() {
        when(this.accountRepository.findById(1L)).thenReturn(DataTest.account001());

        Account account1 = this.accountService.findById(1L).get();
        Account account2 = this.accountService.findById(1L).get();

        assertSame(account1, account2);
        assertEquals("Martín", account1.getPerson());
        assertEquals("Martín", account2.getPerson());
        verify(this.accountRepository, times(2)).findById(1L);
    }

    @Test
    void should_find_all_accounts() {
        List<Account> accountsRepo = List.of(DataTest.account001().get(), DataTest.account002().get());
        when(this.accountRepository.findAll()).thenReturn(accountsRepo);

        List<Account> accounts = this.accountService.findAll();

        assertFalse(accounts.isEmpty());
        assertEquals(accountsRepo.size(), accounts.size());
        assertTrue(accounts.contains(DataTest.account002().get()));
        verify(this.accountRepository).findAll();
    }

    @Test
    void should_save_an_account() {
        Long idBD = 10L;
        Account account = new Account(null, "Martín", new BigDecimal("2000"));
        doAnswer(invocation -> {
            Account accountDB = invocation.getArgument(0);
            accountDB.setId(idBD);
            return accountDB;
        }).when(this.accountRepository).save(any(Account.class));

        Account accountSaved = this.accountService.save(account);

        assertNotNull(accountSaved);
        assertNotNull(accountSaved.getId());
        assertEquals(idBD, accountSaved.getId());
        assertEquals(account.getPerson(), accountSaved.getPerson());
        assertEquals(account.getBalance(), accountSaved.getBalance());
    }
}