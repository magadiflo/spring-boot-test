package com.magadiflo.app.integrationTest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.dto.TransactionDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(scripts = {"/test-account-cleanup.sql", "/test-account-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccountControllerMockMvcIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @LocalServerPort
    private int port;

    @Test
    void should_find_all_accounts() throws Exception {
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get(this.createAbsolutePath("/api/v1/accounts")));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", Matchers.is(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].person").value("Andrés"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].person").value("Pedro"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].person").value("Liz"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].person").value("Karen"));
    }

    @Test
    void should_find_an_account() throws Exception {
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get(this.createAbsolutePath("/api/v1/accounts/{id}"), 1));
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.person").value("Andrés"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(1000));
    }

    @Test
    void should_return_empty_when_account_does_not_exist() throws Exception {
        // Given
        Long accountId = 10L;

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{id}", accountId));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void should_transfer_an_amount_between_accounts() throws Exception {
        // Given
        TransactionDTO dto = new TransactionDTO(1L, 1L, 2L, new BigDecimal("1000"));

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(dto)));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.datetime").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("transferencia exitosa"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transaction.accountIdOrigin").value(dto.accountIdOrigin()));

        String jsonResponse = response.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = this.objectMapper.readTree(jsonResponse);

        String dateTime = jsonNode.get("datetime").asText();
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);

        assertEquals(LocalDate.now(), localDateTime.toLocalDate());
    }

    @Test
    void should_save_an_account() throws Exception {
        // Given
        Long idDB = 5L;
        Account account = new Account(null, "Martín", new BigDecimal("2000"));

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(account)));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.header().string("Location", "/api/v1/accounts/" + idDB))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(idDB.intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.person", Matchers.is("Martín")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance", Matchers.is(2000)));
    }

    @Test
    void should_delete_an_account() throws Exception {
        ResultActions responseDelete = this.mockMvc.perform(MockMvcRequestBuilders.delete(this.createAbsolutePath("/api/v1/accounts/{id}"), 1));
        responseDelete.andExpect(MockMvcResultMatchers.status().isNoContent());

        ResultActions responseGet = this.mockMvc.perform(MockMvcRequestBuilders.get(this.createAbsolutePath("/api/v1/accounts/{id}"), 1));
        responseGet.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private String createAbsolutePath(String uri) {
        return String.format("http://localhost:%d%s", this.port, uri);
    }
}