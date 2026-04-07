package com.flacofitness.app.controller;

import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.PlanService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final UsuarioService usuarioService;
    private final PlanService planService;
    private final RutinaService rutinaService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;

    public ViewController(UsuarioService usuarioService,
                          PlanService planService,
                          RutinaService rutinaService,
                          PagoService pagoService,
                          AsistenciaService asistenciaService) {
        this.usuarioService = usuarioService;
        this.planService = planService;
        this.rutinaService = rutinaService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("usuariosTotales", usuarioService.contarTotal());
        model.addAttribute("usuariosActivos", usuarioService.contarActivos());
        model.addAttribute("planesActivos", planService.contarActivos());
        model.addAttribute("rutinasActivas", rutinaService.contarActivas());
        model.addAttribute("pagosRegistrados", pagoService.contarTodos());
        model.addAttribute("pagosPendientes", pagoService.contarPagosPendientes());
        model.addAttribute("asistenciasRegistradas", asistenciaService.contarTodas());
        model.addAttribute("asistenciasHoy", asistenciaService.contarHoy());
        model.addAttribute("ingresosRegistrados", pagoService.calcularIngresosTotales());
        model.addAttribute("ingresosMensuales", pagoService.calcularIngresosMesActual());
        model.addAttribute("ultimosUsuarios", usuarioService.listarRecientes());
        return "home/index";
    }
}
