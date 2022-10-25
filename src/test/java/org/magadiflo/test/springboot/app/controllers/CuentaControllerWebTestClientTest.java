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
import java.util.List;
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

    @Test
    @Order(4)
    void testListar() {
        this.client.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].persona").isEqualTo("Martín")
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].saldo").isEqualTo(900) //900, por el orden en que se ejecutaron los test
                .jsonPath("$[1].persona").isEqualTo("Gaspar")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].saldo").isEqualTo(2100)
                .jsonPath("$").isArray()
                .jsonPath("$").value(Matchers.hasSize(2));
    }

    @Test
    @Order(5)
    void testListar2() {
        this.client.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .consumeWith(resp -> {
                    List<Cuenta> cuentas = resp.getResponseBody();

                    assertNotNull(cuentas);
                    assertEquals(2, cuentas.size());
                    assertEquals(1L, cuentas.get(0).getId());
                    assertEquals("Martín", cuentas.get(0).getPersona());
                    assertEquals(Double.parseDouble("900"), cuentas.get(0).getSaldo().doubleValue());

                    assertEquals(2L, cuentas.get(1).getId());
                    assertEquals("Gaspar", cuentas.get(1).getPersona());
                    assertEquals(Double.parseDouble("2100"), cuentas.get(1).getSaldo().doubleValue());
                })
                .hasSize(2) //2 elementos en el arreglo
                .value(Matchers.hasSize(2)); //2 elementos del arreglo
    }

    @Test
    @Order(6)
    void testGuardar() {
        // GIVEN
        Cuenta cuenta = new Cuenta(null, "Tinkler", new BigDecimal("3000"));

        // WHEN
        this.client.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)// En automático el bodyValue transforma a cuenta en un objeto JSON
                .exchange()

                // THEN
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.persona").value(Matchers.is("Tinkler"))
                .jsonPath("$.saldo").isEqualTo(3000);
    }

    @Test
    @Order(7)
    void testGuardar2() {
        // GIVEN
        Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("4000"));

        // WHEN
        this.client.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)// En automático el bodyValue transforma a cuenta en un objeto JSON
                .exchange()

                // THEN
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(resp -> {
                    Cuenta c = resp.getResponseBody();

                    assertNotNull(c);
                    assertEquals("Pepe", c.getPersona());
                    assertEquals(4, c.getId());
                    assertEquals(Double.parseDouble("4000"), c.getSaldo().doubleValue());
                });
    }


}