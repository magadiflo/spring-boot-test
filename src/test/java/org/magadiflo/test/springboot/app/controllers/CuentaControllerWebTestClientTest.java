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
 * [Actualización]: le quitamos el http://localhost:8080 a la
 * ...uri("/api/cuentas/transferir"), de esa forma solo necesitamos
 * ejecutar nuestro archivo de test y no es necesario levantar el backend
 * previamente (esto siempre y cuando estamos en el mismo servidor)
 *
 * Para esto, se usarán clientes HTTP, como: RestTemplate y WebClient,
 * son los principales que SpringBoot ofrece para consumir Servicios Rest.
 *
 * WebClient, este cliente es para aplicaciones reactivas en SpringBoot,
 * aquí solo lo usaremos (SpringWebFlux) para ejecutar nuestras pruebas y
 * no en nuestro proyecto o código principal
 *
 *
 * NOTA:
 * Solo en PRUEBAS DE INTEGRACIÓN podríamos darle un orden a los métodos de prueba,
 * ya que esto evitaría que la modificación realizada por un método no afecte
 * la ejecución de otro método
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.models.dto.TransaccionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(3)
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

        // WHEN
        this.client.post().uri("/api/cuentas/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto) //Por debajo ser convierte en JSON
                .exchange() //Para enviar la solicitud

                // THEN
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(respuesta -> {
                    // Queremos convertir la respuesta en una estructura json
                    try {
                        JsonNode json = this.objectMapper.readTree(respuesta.getResponseBody());

                        assertEquals("Transferencia realizada con éxito", json.path("mensaje").asText());
                        assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
                        assertEquals(LocalDate.now().toString(), json.path("date").asText());
                        assertEquals("100", json.path("transaccion").path("monto").asText());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .jsonPath("$.mensaje").isNotEmpty()
                .jsonPath("$.mensaje").value(Matchers.is("Transferencia realizada con éxito"))
                .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con éxito", valor))
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con éxito")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(this.objectMapper.writeValueAsString(response));
    }

    // A continuación se muestran dos formas de hacer la comprobación:
    // 1) Con jsonPath(...) ----> testDetalle()
    // 2) con consumeWith(...) ---> testDetalle2()
    @Test
    @Order(1)
    void testDetalle() throws Exception {
        Cuenta cuenta = new Cuenta(1L, "Martín", new BigDecimal("1000"));

        this.client.get().uri("/api/cuentas/1")
                .exchange() //Realizamos el request
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.persona").isEqualTo("Martín")
                .jsonPath("$.saldo").isEqualTo(1000)
                .json(this.objectMapper.writeValueAsString(cuenta)); //Esperamos obtener el json completo
    }

    @Test
    @Order(2)
    void testDetalle2() {
        this.client.get().uri("/api/cuentas/2")
                .exchange() //Realizamos el request
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(resp -> {
                    Cuenta cuenta = resp.getResponseBody();

                    assertNotNull(cuenta);
                    assertEquals("Gaspar", cuenta.getPersona());
                    assertEquals("2000.00", cuenta.getSaldo().toPlainString());
                });
    }
}