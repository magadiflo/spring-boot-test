package com.magadiflo.app.controllers;

import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.dto.TransactionDTO;
import com.magadiflo.app.services.IAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/accounts")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> listAllAccounts() {
        return ResponseEntity.ok(this.accountService.findAll());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Account> details(@PathVariable Long id) {
        return this.accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Account> saveAccount(@RequestBody Account account) {
        Account accountDB = this.accountService.save(account);
        URI accountURI = URI.create("/api/v1/accounts" + accountDB.getId());
        return ResponseEntity.created(accountURI).body(accountDB);
    }

    @PostMapping(path = "/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionDTO dto) {
        this.accountService.transfer(dto.bankId(), dto.accountIdOrigin(), dto.accountIdDestination(), dto.amount());

        Map<String, Object> response = new HashMap<>();
        response.put("datetime", LocalDateTime.now());
        response.put("status", HttpStatus.OK);
        response.put("code", HttpStatus.OK.value());
        response.put("message", "transferencia exitosa");
        response.put("transaction", dto);

        return ResponseEntity.ok(response);
    }
}
