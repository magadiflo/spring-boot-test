package com.magadiflo.app.controllers;

import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.dto.TransactionDTO;
import com.magadiflo.app.services.IAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/accounts")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Account> details(@PathVariable Long id) {
        return this.accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
