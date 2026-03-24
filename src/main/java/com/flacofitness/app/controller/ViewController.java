package com.flacofitness.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "home/index";
    }

    @GetMapping("/pagos")
    public String pagosList() {
        return "pagos/list";
    }

    @GetMapping("/asistencias")
    public String asistenciasList() {
        return "asistencias/list";
    }
}
