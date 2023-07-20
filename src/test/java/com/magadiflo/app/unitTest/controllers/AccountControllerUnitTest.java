package com.magadiflo.app.unitTest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magadiflo.app.controllers.AccountController;
import com.magadiflo.app.data.DataTest;
import com.magadiflo.app.services.IAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.util.Optional;

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
}