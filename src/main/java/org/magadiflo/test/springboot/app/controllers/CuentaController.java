package org.magadiflo.test.springboot.app.controllers;

import org.magadiflo.test.springboot.app.models.Cuenta;
import org.magadiflo.test.springboot.app.services.ICuentaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(path = "/api/cuentas")
public class CuentaController {

    private final ICuentaService cuentaService;

    public CuentaController(ICuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Cuenta detalle(@PathVariable Long id) {
        return this.cuentaService.findById(id);
    }
}
