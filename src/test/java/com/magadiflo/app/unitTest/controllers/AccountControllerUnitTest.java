package com.magadiflo.app.unitTest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magadiflo.app.controllers.AccountController;
import com.magadiflo.app.data.DataTest;
import com.magadiflo.app.models.Account;
import com.magadiflo.app.models.dto.TransactionDTO;
import com.magadiflo.app.services.IAccountService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(AccountController.class)
class AccountControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private IAccountService accountService;

    @Test
    void should_find_an_account() throws Exception {
        // Given
        Long accountId = 1L;
        when(this.accountService.findById(accountId)).thenReturn(DataTest.account001());

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{id}", accountId));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.person").value("Martín"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(2000));
        verify(this.accountService).findById(eq(accountId));
    }

    @Test
    void should_return_empty_when_account_does_not_exist() throws Exception {
        // Given
        Long accountId = 10L;
        when(this.accountService.findById(accountId)).thenReturn(Optional.empty());

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{id}", accountId));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isNotFound());
        verify(this.accountService).findById(eq(accountId));
    }

    @Test
    void should_transfer_an_amount_between_accounts() throws Exception {
        // Given
        TransactionDTO dto = new TransactionDTO(1L, 1L, 2L, new BigDecimal("100"));
        doNothing().when(this.accountService).transfer(dto.bankId(), dto.accountIdOrigin(), dto.accountIdDestination(), dto.amount());

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
    void should_find_all_accounts() throws Exception {
        // Given
        List<Account> accountList = List.of(DataTest.account001().get(), DataTest.account002().get());
        when(this.accountService.findAll()).thenReturn(accountList);

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts"));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].person").value("Martín"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value(2000))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].person").value("Alicia"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].balance").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", Matchers.is(accountList.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(accountList.size())))
                .andExpect(MockMvcResultMatchers.content().json(this.objectMapper.writeValueAsString(accountList)));

        verify(this.accountService).findAll();
    }
}