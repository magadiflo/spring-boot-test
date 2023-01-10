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

/**
 * Prueba de Integración
 * ********************
 *
 * Probaremos el controlador CuentaController, pero usando pruebas de integración real,
 * sin MockMvc, no simular el request, el response, ni el servidor http, etc. sino que
 * sean peticiones http reales, consumir un api rest de nuestro backend, o sea del
 * controlador y realizar pruebas, pero todo real.
 *
 * Levantaremos un servidor real que estará desplegada en algún puerto con el objeto
 * HttpServletRequest y Response pero reales, no Mocks.
 *
 * A todo lo anterior se le conoce como Pruebas de integración a servicios rest, por lo
 * tanto cada vez que realicemos nuestras pruebas de integración, la aplicación que estemos
 * probando debe estar levantada, es decir en este caso la application que contenga el
 * CuentaController debe estar levantado para que se pueda comunicar, de lo contrario
 * no se va a poder establecer la comunicación a los endpoints.
 *
 * Es una prueba distinta, en este caso usando clientes http como RestTemplate o WebClient,
 * dos tecnologías que usa spring para consumir servicios rest.
 * */

/**
 * SpringBootTest.WebEnvironment.RANDOM_PORT
 * *****************************************
 * Para que automáticamente nuestras pruebas unitarias levanten un servidor real
 * en un puerto aleatorio. Este servidor tendrá el contexto de aplicación de SpringBoot,
 * quien nos permitirá hacer inyección de dependencia usando el @Autowired.
 *
 * Cuando estamos en el mismo proyecto, nuestras pruebas unitarias (clase test) o sea
 * en el mismo proyecto del backend (controlador que queremos probar) no es necesario
 * colocar toda la url, ejm:
 *      this.client.post().uri("http://localhost:8080/api/cuentas/transferir")...
 * simplemente podemos colocar:
 *      this.client.post().uri("/api/cuentas/transferir")...
 * ¿Cuál es la diferencia?, es que con la segunda forma solo levantamos una instancia, un
 * servidor, el de las pruebas, lo que significa que tanto aplicación del backend, como las
 * pruebas a ejecutar estarán en el mismo servidor. Mientras que con el de la primera forma
 * (colocando toda la url) necesitamos levantar primero el proyecto donde está el controlador
 * a probar, que por defecto se levanta en el puerto 8080 y luego habría que ejecutar las
 * pruebas y este se levante con un puerto aleatorio.
 */

/**
 * WebTestClient
 * *************
 * WebClient, es la implementación real para consumir servicios Rest, pero en este caso
 * necesitamos el WebTestClient que tiene características de pruebas unitarias.
 *
 * Con WebTestClient, enviamos el DTO en el cuerpo del request, pero este DTO
 * en automático será convertido por WebTestClient en un objeto JSON, es decir, no es
 * necesario convertirlo con el ObjectMapper, tal como lo hacíamos en MockMvc.
 *
 * Ahora, si en la respuesta del WebTestClient nos trae un objeto json y queremos
 * probar el objeto completo, es necesario convertir primero el objeto esperado
 * con el ObjectMapper a un objeto JSON.
 */

/**
 * @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
 * *****************************************************
 * Con esta anotación a nivel de clase ordenaremos los métodos test
 * mediante la anotación @Order(...)
 *
 * ¡Ojo! Como es una prueba contra una base de datos real,
 * dependiendo de qué tests se ejecuten primero, estos pueden
 * modificar los datos de la BD y eso puede afectar la
 * ejecución de los test siguientes.
 *
 * Como recomendación, cuando utilizamos estas pruebas de integración
 * y tenemos métodos test que algunos modifican datos podríamos dar
 * algún tipo de prioridad, es decir, primero que se ejecute este
 * método, luego este otro, etc... un orden, pero solo en estas
 * PRUEBAS DE INTEGRACIÓN, para que un método que se ejecutó antes
 * no afecte a otro que se ejecute después
 */

/**
 * Dos Formas de hacer comprobaciones de las respuestas obtenidas
 * **************************************************************
 * FORMA 1° usando la forma nativa del .jsonPath(...)
 * FORMA 2° usando el consumeWith(...)
 */
@Tag(value = "integracion_web_client")
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
                .exchange() //Para enviar la solicitud. Lo que venga después del .exchange() es la respuesta.

                // THEN
                .expectStatus().isOk()
                .expectBody()

                // FORMA 2° usando el consumeWith(...)
                .consumeWith(respuesta -> { //Expresión lambda que permitirá consumir la respuesta y hacer los asserts con él
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

                // FORMA 1° usando la forma nativa del .jsonPath
                .jsonPath("$.mensaje").isNotEmpty()
                .jsonPath("$.mensaje").value(Matchers.is("Transferencia realizada con éxito"))
                .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con éxito", valor))
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con éxito")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())

                // Si queremos probar el json completo, es necesario tener el ObjectMapper para hacer la conversión
                .json(this.objectMapper.writeValueAsString(response));
    }

    @Test
    @Order(1)
    @DisplayName(value = "probando el método handler detalle() y haciendo la comprobación con jsonPath()")
    void testDetalleConJsonPath() throws Exception {
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
    @DisplayName(value = "probando el método handler detalle() y haciendo la comprobación con consumeWith()")
    void testDetalleConConsumeWith() {
        this.client.get().uri("/api/cuentas/2")
                .exchange() //Realizamos el request
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                /**
                 * expectBody(Cuenta.class)
                 * ************************
                 * Colocamos dentro la clase Cuenta, porque los atributos devueltos
                 * en el JSON son iguales a los atributos de la clase Cuenta. Y eso es,
                 * porque el método handler de esa uri retorna un objeto del tipo
                 * de la clase Cuenta.
                 */
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
    @DisplayName(value = "probando el método listar con JsonPath que viene en una estructura de arreglo")
    void testListarConJsonPath() {
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
    @DisplayName(value = "probando el método listar con expectBodyList y consumeWith")
    void testListarConConsumeWith() {
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
    @DisplayName(value = "probando el método guardar, usando el JsonPath para la comprobación")
    void testGuardarConJsonPath() {
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
    @DisplayName(value = "probando el método guardar, usando el consumeWith")
    void testGuardarConConsumeWith() {
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

    @Test
    @Order(8)
    void testEliminar() {
        // Dentro del método aplicamos varias peticiones antes y después de eliminar
        // para asegurarnos de que efectivamente se eliminó el registro
        this.client.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(4);

        this.client.delete().uri("/api/cuentas/4").exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        this.client.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(3);

        this.client.get().uri("/api/cuentas/4").exchange()
                /**
                 * Error genérico
                 * ***************
                 * Al hacer la búsqueda de una cuenta que no existe, el método lanza un
                 * NoSuchElementException, que al final el servidor lo traduce en un error 500.
                 */
                //.expectStatus().is5xxServerError();


                /**
                 * Error personalizado
                 * *******************
                 * El error notFound() es lanzado cuando en el método detalle no se encuentra la cuenta,
                 * pero para eso tuvimos que utilizar el try-catch, capturar la excepción NoSuchElementException,
                 * y retornar el NotFound()
                 */
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }
}