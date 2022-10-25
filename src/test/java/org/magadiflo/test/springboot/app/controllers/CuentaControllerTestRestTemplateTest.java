package org.magadiflo.test.springboot.app.controllers;

/***
 * TEST DE INTEGRACIÓN USANDO RestTemplate
 * ***************************************
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.models.dto.TransaccionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    @Test
    @Order(2)
    void testDetalle() {
        ResponseEntity<Cuenta> response = this.client.getForEntity(this.crearUri("/api/cuentas/1"), Cuenta.class);
        Cuenta cuenta = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        assertNotNull(cuenta);
        assertEquals(1L, cuenta.getId());
        assertEquals("Martín", cuenta.getPersona());
        assertEquals(Double.parseDouble("900"), cuenta.getSaldo().doubleValue());
        assertEquals(new Cuenta(1L, "Martín", new BigDecimal("900.00")), cuenta);
    }

    @Test
    @Order(3)
    void testListar() throws JsonProcessingException {
        ResponseEntity<Cuenta[]> response = this.client.getForEntity(this.crearUri("/api/cuentas"), Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        assertNotNull(cuentas);
        assertEquals(2, cuentas.size());

        assertEquals(1L, cuentas.get(0).getId());
        assertEquals("Martín", cuentas.get(0).getPersona());
        assertEquals(Double.parseDouble("900"), cuentas.get(0).getSaldo().doubleValue());

        assertEquals(2L, cuentas.get(1).getId());
        assertEquals("Gaspar", cuentas.get(1).getPersona());
        assertEquals(Double.parseDouble("2100"), cuentas.get(1).getSaldo().doubleValue());

        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(cuentas));
        assertEquals(1L, jsonNode.get(0).path("id").asLong());
        assertEquals("Martín", jsonNode.get(0).path("persona").asText());
        assertEquals(900D, jsonNode.get(0).path("saldo").asDouble());

        assertEquals(2L, jsonNode.get(1).path("id").asLong());
        assertEquals("Gaspar", jsonNode.get(1).path("persona").asText());
        assertEquals(2100D, jsonNode.get(1).path("saldo").asDouble());
    }

    @Test
    @Order(4)
    void testGuardar() {
        Cuenta cuenta = new Cuenta(null, "Pepa", new BigDecimal("3800"));
        ResponseEntity<Cuenta> response = this.client.postForEntity(this.crearUri("/api/cuentas"), cuenta, Cuenta.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Cuenta cuentaCreada = response.getBody();

        assertNotNull(cuentaCreada);
        assertEquals(3L, cuentaCreada.getId());
        assertEquals("Pepa", cuentaCreada.getPersona());
        assertEquals(Double.parseDouble("3800"), cuentaCreada.getSaldo().doubleValue());
    }

    @Test
    @Order(5)
    void testEliminar() {
        ResponseEntity<Cuenta[]> response = this.client.getForEntity(this.crearUri("/api/cuentas"), Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(response.getBody());
        assertEquals(3, cuentas.size());

        Map<String, Object> pathVariables = new HashMap<>();
        pathVariables.put("id", 3);

        ResponseEntity<Void> exchange = this.client.exchange(this.crearUri("/api/cuentas/{id}"), HttpMethod.DELETE, null, Void.class, pathVariables);
        assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());
        assertFalse(exchange.hasBody());

        //this.client.delete(this.crearUri("/api/cuentas/3")); //Antes y después de eliminar usamos los otros métodos para comprobar que se eliminó

        response = this.client.getForEntity(this.crearUri("/api/cuentas"), Cuenta[].class);
        cuentas = Arrays.asList(response.getBody());
        assertEquals(2, cuentas.size());

        ResponseEntity<Cuenta> responseDetalle = this.client.getForEntity(this.crearUri("/api/cuentas/3"), Cuenta.class);
        assertEquals(HttpStatus.NOT_FOUND, responseDetalle.getStatusCode());
        assertFalse(responseDetalle.hasBody());
    }
}