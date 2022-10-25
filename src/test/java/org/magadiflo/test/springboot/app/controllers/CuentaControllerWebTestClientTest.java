package org.magadiflo.test.springboot.app.controllers;

/***
 * PRUEBAS DE INTEGRACIÓN A SERVICIOS REST
 * ***************************************
 * Se harán pruebas reales, es decir ya no con Mocks,
 * sino con los servicios, los request, responses, etc.. reales.
 *
 * Cada vez que hagamos estas pruebas de integración, nuestro
 * backend debe estar levantado (ejm. Puerto 8080), ya que
 * se consumirán los endpoints y estos debe estar funcionando.
 *
 * Para esto, se usarán clientes HTTP, como: RestTemplate y WebClient,
 * son los principales que SpringBoot ofrece para consumir Servicios Rest.
 *
 * WebClient, este cliente es para aplicaciones reactivas en SpringBoot,
 * aquí solo lo usaremos (SpringWebFlux) para ejecutar nuestras pruebas y
 * no en nuestro proyecto o código principal
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.magadiflo.test.springboot.app.models.dto.TransaccionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CuentaControllerWebTestClientTest {

    @Autowired
    private WebTestClient client;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testTransferir() throws JsonProcessingException {
        // GIVEN
        TransaccionDTO dto = new TransaccionDTO();
        dto.setBancoId(1L);
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setMonto(new BigDecimal("100"));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", HttpStatus.OK);
        response.put("code", HttpStatus.OK.value());
        response.put("mensaje", "Transferencia realizada con éxito");
        response.put("transaccion", dto);

        // THEN
        this.client.post().uri("http://localhost:8080/api/cuentas/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto) //Por debajo ser convierte en JSON
                .exchange() //Para enviar la solicitud
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.mensaje").isNotEmpty()
                .jsonPath("$.mensaje").value(Matchers.is("Transferencia realizada con éxito"))
                .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con éxito", valor))
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con éxito")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(this.objectMapper.writeValueAsString(response));
    }
}