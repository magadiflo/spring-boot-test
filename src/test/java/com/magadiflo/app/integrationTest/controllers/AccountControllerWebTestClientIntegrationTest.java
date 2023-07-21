package com.magadiflo.app.integrationTest.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.dto.TransactionDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerWebTestClientIntegrationTest {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void should_transfer_amount_between_two_accounts() {
        // Given
        TransactionDTO dto = new TransactionDTO(1L, 1L, 2L, new BigDecimal("100"));

        // When
        WebTestClient.ResponseSpec response = this.client.post().uri("/api/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").value(Matchers.is("transferencia exitosa"))
                .jsonPath("$.message").value(message -> assertEquals("transferencia exitosa", message))
                .jsonPath("$.message").isEqualTo("transferencia exitosa")
                .jsonPath("$.transaction.accountIdOrigin").isEqualTo(dto.accountIdOrigin())
                .jsonPath("$.datetime").value(datetime -> {
                    LocalDateTime localDateTime = LocalDateTime.parse(datetime.toString());
                    assertEquals(LocalDate.now(), localDateTime.toLocalDate());
                });
    }

    @Test
    @Order(2)
    void should_transfer_amount_between_two_accounts_with_consumeWith() {
        // Given
        TransactionDTO dto = new TransactionDTO(1L, 1L, 2L, new BigDecimal("20"));

        // When
        WebTestClient.ResponseSpec response = this.client.post().uri("/api/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> {
                    try {
                        JsonNode jsonNode = this.objectMapper.readTree(result.getResponseBody());

                        assertEquals("transferencia exitosa", jsonNode.path("message").asText());
                        assertEquals(dto.accountIdOrigin(), jsonNode.path("transaction").path("accountIdOrigin").asLong());
                        assertEquals(dto.amount().doubleValue(), jsonNode.path("transaction").path("amount").asDouble());

                        LocalDateTime localDateTime = LocalDateTime.parse(jsonNode.path("datetime").asText());
                        assertEquals(LocalDate.now(), localDateTime.toLocalDate());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Order(3)
    void should_find_an_account_with_jsonPath() throws JsonProcessingException {
        // Given
        Long id = 1L;
        Account expectedAccount = new Account(id, "Martín", new BigDecimal("1880"));

        // When
        WebTestClient.ResponseSpec response = this.client.get().uri("/api/v1/accounts/{id}", id).exchange();

        // Then
        response.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.person").isEqualTo(expectedAccount.getPerson())
                .jsonPath("$.balance").isEqualTo(expectedAccount.getBalance().doubleValue())
                .json(this.objectMapper.writeValueAsString(expectedAccount));

    }

    @Test
    @Order(4)
    void should_find_an_account_with_consumeWith() throws JsonProcessingException {
        // Given
        Long id = 2L;
        Account expectedAccount = new Account(2L, "Alicia", new BigDecimal("1120.00"));

        // When
        WebTestClient.ResponseSpec response = this.client.get().uri("/api/v1/accounts/{id}", id).exchange();

        // Then
        response.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)//Se espera recibir un json que tenga exactamente los mismos atributos que la clase Account
                .consumeWith(result -> {
                    Account accountDB = result.getResponseBody();

                    assertNotNull(accountDB);
                    assertEquals(expectedAccount, accountDB);
                    assertEquals(expectedAccount.getPerson(), accountDB.getPerson());
                    assertEquals(expectedAccount.getBalance(), accountDB.getBalance());
                });

    }

    @Test
    @Order(5)
    void should_find_all_accounts_with_jsonPath() {
        // When
        WebTestClient.ResponseSpec response = this.client.get().uri("/api/v1/accounts").exchange();

        // Then
        response.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$").value(Matchers.hasSize(2))
                .jsonPath("$.size()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].person").isEqualTo("Martín")
                .jsonPath("$[0].balance").isEqualTo(1880)
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].person").isEqualTo("Alicia")
                .jsonPath("$[1].balance").isEqualTo(1120);
    }

    @Test
    @Order(5)
    void should_find_all_accounts_with_consumeWith() {
        // When
        WebTestClient.ResponseSpec response = this.client.get().uri("/api/v1/accounts").exchange();

        // Then
        response.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Account.class)
                .consumeWith(result -> {
                    List<Account> accountListDB = result.getResponseBody();

                    assertNotNull(accountListDB);
                    assertFalse(accountListDB.isEmpty());
                    assertEquals(2, accountListDB.size());
                    assertEquals(1L, accountListDB.get(0).getId());
                    assertEquals("Martín", accountListDB.get(0).getPerson());
                    assertEquals(1880D, accountListDB.get(0).getBalance().doubleValue());
                    assertEquals(2L, accountListDB.get(1).getId());
                    assertEquals("Alicia", accountListDB.get(1).getPerson());
                    assertEquals(1120D, accountListDB.get(1).getBalance().doubleValue());
                })
                .hasSize(2)
                .value(Matchers.hasSize(2));
    }
}
