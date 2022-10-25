package org.magadiflo.test.springboot.app.controllers;

/***
 * TEST DE INTEGRACIÓN USANDO RestTemplate
 * ***************************************
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.magadiflo.test.springboot.app.models.dto.TransaccionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CuentaControllerTestRestTemplateTest {

    @Autowired
    private TestRestTemplate client;

    private ObjectMapper objectMapper;

    @LocalServerPort //Importa el puerto de forma automática
    private int puerto;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
    }

    private String crearUri(String uri) {
        return String.format("http://localhost:%d%s", this.puerto, uri);
    }

    @Test
    @Order(1)
    void testTransferir() {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setMonto(new BigDecimal("100"));
        dto.setCuentaDestinoId(2L);
        dto.setCuentaOrigenId(1L);
        dto.setBancoId(1L);

        ResponseEntity<String> response =
                this.client.postForEntity(this.crearUri("/api/cuentas/transferir"), dto, String.class);
        String json = response.getBody();

        System.out.printf("Puerto: %s %n", this.puerto);
        System.out.println(json);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(json);
        assertTrue(json.contains("Transferencia realizada con éxito"));
        assertTrue(json.contains("{\"cuentaOrigenId\":1,\"cuentaDestinoId\":2,\"monto\":100,\"bancoId\":1}"));
    }
}