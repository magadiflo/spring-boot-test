package com.magadiflo.app.integrationTest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magadiflo.app.models.dto.TransactionDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerWebTestClientIntegrationTest {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
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
}
