package org.magadiflo.test.springboot.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.magadiflo.test.springboot.app.data.Datos;
import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.models.dto.TransaccionDTO;
import org.magadiflo.test.springboot.app.services.impl.CuentaServiceImpl;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@WebMvcTest(CuentaController.class) //Testearemos el controller CuentaController
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc; //Es el contexto de MVC, pero falso (simulado: request, response, etc.)

    @MockBean
    private CuentaServiceImpl cuentaService;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testDetalle() throws Exception {
        // GIVEN
        // Cuando en el controlador se llame internamente al método this.cuentaService.findById(...), el mock actuará
        // para simular la respuesta de ese método (this.cuentaService.findById(...))
        Mockito.when(this.cuentaService.findById(1L)).thenReturn(Datos.cuenta001().orElseThrow());

        // WHEN
        // Se realiza la llamada al controlador mediante la ruta (endpoint)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.persona").value("Martín")) //$, hace referencia a la raíz del json
                .andExpect(MockMvcResultMatchers.jsonPath("$.saldo").value("1000"));

        //Verificar que efectivamente el método this.cuentaService.findById(...) sea llamado
        Mockito.verify(this.cuentaService).findById(1L);
    }

    @Test
    void testTransferir() throws Exception {
        // GIVEN
        TransaccionDTO dto = new TransaccionDTO();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setMonto(new BigDecimal("100"));
        dto.setBancoId(1L);

        System.out.println(this.objectMapper.writeValueAsString(dto));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", HttpStatus.OK);
        response.put("code", HttpStatus.OK.value());
        response.put("mensaje", "Transferencia realizada con éxito");
        response.put("transaccion", dto);

        System.out.println(this.objectMapper.writeValueAsString(response));

        // WHEN
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/cuentas/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto)))
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensaje").value("Transferencia realizada con éxito"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transaccion.cuentaOrigenId").value(dto.getCuentaOrigenId()))
                .andExpect(MockMvcResultMatchers.content().json(this.objectMapper.writeValueAsString(response)));
    }

    @Test
    void testListar() throws Exception {
        // GIVEN
        List<Cuenta> cuentas = Arrays.asList(Datos.cuenta001().orElseThrow(), Datos.cuenta002().orElseThrow());
        Mockito.when(this.cuentaService.findAll()).thenReturn(cuentas);

        // WHEN
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/cuentas").contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].persona").value("Martín"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].persona").value("Gaspar"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].saldo").value("1000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].saldo").value("2000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.content().json(this.objectMapper.writeValueAsString(cuentas)));

        Mockito.verify(this.cuentaService).findAll();
    }

    @Test
    void testGuardar() throws Exception {
        // GIVEN
        Cuenta cuenta = new Cuenta(null, "Tinkler", new BigDecimal("3000"));
        Mockito.when(this.cuentaService.save(Mockito.any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        // WHEN
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(cuenta)))
                // THEN
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.persona", Matchers.is("Tinkler")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saldo", Matchers.is(3000)));

        Mockito.verify(this.cuentaService).save(Mockito.any());
    }
}