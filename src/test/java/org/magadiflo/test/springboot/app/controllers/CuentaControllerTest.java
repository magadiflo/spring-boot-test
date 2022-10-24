package org.magadiflo.test.springboot.app.controllers;

import org.junit.jupiter.api.Test;
import org.magadiflo.test.springboot.app.data.Datos;
import org.magadiflo.test.springboot.app.services.impl.CuentaServiceImpl;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(CuentaController.class) //Testearemos el controller CuentaController
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc; //Es el contexto de MVC, pero falso (simulado: request, response, etc.)

    @MockBean
    private CuentaServiceImpl cuentaService;

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
}