package org.magadiflo.test.springboot.app.controllers;

/***
 * TEST DE INTEGRACIÓN USANDO RestTemplate
 * ***************************************
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
    void testTransferir() throws JsonProcessingException {
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

        JsonNode jsonNode = this.objectMapper.readTree(json);
        assertEquals("Transferencia realizada con éxito", jsonNode.path("mensaje").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals(100d, jsonNode.path("transaccion").path("monto").asDouble());
        assertEquals(1L, jsonNode.path("transaccion").path("cuentaOrigenId").asLong());

        Map<String, Object> resp = new HashMap<>();
        resp.put("date", LocalDate.now().toString());
        resp.put("status", HttpStatus.OK);
        resp.put("code", HttpStatus.OK.value());
        resp.put("mensaje", "Transferencia realizada con éxito");
        resp.put("transaccion", dto);

        assertEquals(this.objectMapper.writeValueAsString(resp), json);
    }
}