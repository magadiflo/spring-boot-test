package org.magadiflo.test.springboot.app.controllers;

import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.models.dto.TransaccionDTO;
import org.magadiflo.test.springboot.app.services.ICuentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/cuentas")
public class CuentaController {

    private final ICuentaService cuentaService;

    public CuentaController(ICuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Cuenta> listar() {
        return this.cuentaService.findAll();
    }


    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Cuenta detalle(@PathVariable Long id) {
        return this.cuentaService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cuenta guardar(@RequestBody Cuenta cuenta) {
        return this.cuentaService.save(cuenta);
    }

    @PostMapping(path = "/transferir")
    public ResponseEntity<?> transferir(@RequestBody TransaccionDTO dto) {
        this.cuentaService.transferir(dto.getCuentaOrigenId(), dto.getCuentaDestinoId(), dto.getMonto(), dto.getBancoId());

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", HttpStatus.OK);
        response.put("code", HttpStatus.OK.value());
        response.put("mensaje", "Transferencia realizada con éxito");
        response.put("transaccion", dto);

        return ResponseEntity.ok(response);
    }
}
