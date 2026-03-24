package com.flacofitness.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "home/index";
    }

    @GetMapping("/rutinas")
    public String rutinasList() {
        return "rutinas/list";
    }

    @GetMapping("/rutinas/nueva")
    public String rutinasForm() {
        return "rutinas/form";
    }

    @GetMapping("/rutinas/demo")
    public String rutinasDetail() {
        return "rutinas/detail";
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
