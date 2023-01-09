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

/**
 * Pruebas unitarias a controlador del tipo Rest CuentaController
 * **************************************************************
 */

/**
 * @WebMvcTest **************
 * Como probaremos un controlador, debemos configurar el contexto Mvc Test
 * usando esa anotación. Dentro de la anotación indicamos el controlador
 * que vamos a probar
 */
@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    /**
     * MockMvc
     * *******
     * Implementación de mockito para probar un controlador.
     * Es el contexto de MVC, pero falso, el servidor HTTP es simulado: request, response, etc.
     * es decir, no estamos trabajando sobre un servidor real HTTP.
     */
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaServiceImpl cuentaService;

    /**
     * ObjectMapper
     * ************
     * Nos permite convertir cualquier objeto en un JSON y
     * viceversa, un JSON en un objeto que por su puesto debe
     * existir esa clase, donde los atributos de la clase coincidan
     * con los nombres de atributos del json y viceversa.
     */
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testDetalle() throws Exception {
        // GIVEN
        /**
         * Como nuestra clase a probar es un Controlador: CuentaController, este tiene
         * como dependencia al service CuentaServiceImpl, pero a nosotros nos interesa probar
         * únicamente el controlador, por lo tanto las dependencias (servicios) los
         * mockearemos con Mockito.
         *
         * Ahora, aquí probaremos el método detalle() del controlador, este método
         * en su interior hace uso del servicio: this.cuentaService.findById(...), por lo que
         * necesitamos mockear la respuesta de ese servicio y eso lo hacemos con Mockito
         * con el método Mockito.when(...) tal como sigue a continuación:
         */
        Mockito.when(this.cuentaService.findById(1L)).thenReturn(Datos.cuenta001().orElseThrow());

        // WHEN
        /**
         * mockMvc.perform(...)
         * ********************
         * Se realiza la llamada simulada al controlador real mediante la ruta (endpoint),
         * por eso usamos el MockMvc.
         *
         * Hay que recordar que, a través de la url (endpoint) más el método http
         * se puede saber a qué método del controlador se hará la prueba. En este caso, se usa
         * la clase MockMvcRequestBuilders para decirle que a través del método .get(...)
         * haga la llamada a la url definida en su interior. Esto corresponde al método
         * detalle(...) del CuentaController.
         *
         * El controlador real recibirá esa llamada y como internamente hace uso del
         * this.cuentaService.findById(...) el cual mockeamos en la parte superior, pues este
         * controlador real devolverá el valor que le definimos al mock para que devuelva.
         *
         * El valor devuelto por el controlador es usado en el método .andExpect(...)
         * para verificar si los valores devueltos son los esperados.
         */
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

        // Una forma de capturar el argumento (cuenta) para agregarle un id y retornarlo
        /*
        Mockito.when(this.cuentaService.save(Mockito.any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });
        */

        // Otra forma de capturar el argument (cuenta) agregarle un id y retornarlo
        Mockito.doAnswer(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        }).when(this.cuentaService).save(Mockito.any(Cuenta.class));

        // WHEN
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(cuenta)))
                // THEN
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

                // Una forma de comparar es con Matchers.is(...) dentro del .jsonPath(...)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.persona", Matchers.is("Tinkler")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saldo", Matchers.is(3000)))

                // Otra forma de comparar es con .value(...) después de .jsonPath(...)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.persona").value("Tinkler"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saldo").value(3000));

        Mockito.verify(this.cuentaService).save(Mockito.any(Cuenta.class));
    }
}