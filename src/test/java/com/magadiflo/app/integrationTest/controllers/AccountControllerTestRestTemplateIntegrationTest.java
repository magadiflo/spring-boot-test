package com.magadiflo.app.integrationTest.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.dto.TransactionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Sql(scripts = {"/test-account-cleanup.sql", "/test-account-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTestRestTemplateIntegrationTest {
    @Autowired
    private TestRestTemplate client;
    @Autowired
    private ObjectMapper objectMapper;
    @LocalServerPort
    private int port;

    @Test
    void should_transfer_amount_between_accounts() throws JsonProcessingException {
        TransactionDTO dto = new TransactionDTO(1L, 1L, 2L, new BigDecimal("500"));

        ResponseEntity<String> response = this.client.postForEntity(this.createAbsolutePath("/api/v1/accounts/transfer"), dto, String.class);
        String jsonString = response.getBody();
        JsonNode jsonNode = this.objectMapper.readTree(jsonString);

        assertNotNull(jsonString);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals("transferencia exitosa", jsonNode.get("message").asText());
    }

    @Test
    void should_find_an_account() {
        ResponseEntity<Account> response = this.client.getForEntity(this.createAbsolutePath("/api/v1/accounts/1"), Account.class);
        Account account = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals("Andrés", account.getPerson());
        assertEquals(1000D, account.getBalance().doubleValue());
    }

    @Test
    void should_find_all_accounts() throws Exception {
        ResponseEntity<Account[]> response = this.client.getForEntity(this.createAbsolutePath("/api/v1/accounts"), Account[].class);
        Account[] accountsDB = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(accountsDB);
        assertEquals(4, accountsDB.length);
        assertEquals(1L, accountsDB[0].getId());
        assertEquals("Andrés", accountsDB[0].getPerson());
        assertEquals(1000D, accountsDB[0].getBalance().doubleValue());

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsBytes(accountsDB));
        assertEquals(1L, jsonNode.get(0).path("id").asLong());
        assertEquals("Andrés", jsonNode.get(0).path("person").asText());
        assertEquals(1000D, jsonNode.get(0).path("balance").asDouble());
    }

    @Test
    void should_save_an_account() {
        Account accountToSave = new Account(null, "Nophy", new BigDecimal("4000"));
        ResponseEntity<Account> response = this.client.postForEntity(this.createAbsolutePath("/api/v1/accounts"), accountToSave, Account.class);
        Account accountDB = response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(accountDB);
        assertEquals(5L, accountDB.getId());
        assertEquals("Nophy", accountDB.getPerson());
        assertEquals(4000D, accountDB.getBalance().doubleValue());
    }

    private String createAbsolutePath(String uri) {
        return String.format("http://localhost:%d%s", this.port, uri);
    }
}