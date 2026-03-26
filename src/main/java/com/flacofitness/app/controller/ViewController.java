package com.flacofitness.app.controller;

import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final UsuarioService usuarioService;
    private final RutinaService rutinaService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;

    public ViewController(UsuarioService usuarioService,
                          RutinaService rutinaService,
                          PagoService pagoService,
                          AsistenciaService asistenciaService) {
        this.usuarioService = usuarioService;
        this.rutinaService = rutinaService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("usuariosTotales", usuarioService.contarTotal());
        model.addAttribute("usuariosActivos", usuarioService.contarActivos());
        model.addAttribute("rutinasActivas", rutinaService.contarActivas());
        model.addAttribute("pagosRegistrados", pagoService.contarTodos());
        model.addAttribute("pagosPendientes", pagoService.contarPagosPendientes());
        model.addAttribute("asistenciasRegistradas", asistenciaService.contarTodas());
        model.addAttribute("ingresosRegistrados", pagoService.calcularIngresosTotales());
        return "home/index";
    }
}
