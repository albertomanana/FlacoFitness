package com.flacofitness.app.controller;

import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.enums.EstadoPago;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

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
        var usuarios = usuarioService.listarTodos();
        var rutinas = rutinaService.listarTodas();
        var pagos = pagoService.listarTodos();
        var asistencias = asistenciaService.listarTodas();

        model.addAttribute("usuariosTotales", usuarios.size());
        model.addAttribute("usuariosActivos", usuarioService.listarActivos().size());
        model.addAttribute("rutinasActivas", rutinas.stream()
                .filter(rutina -> Boolean.TRUE.equals(rutina.getActiva()))
                .count());
        model.addAttribute("pagosRegistrados", pagos.size());
        model.addAttribute("pagosPendientes", pagos.stream()
                .filter(pago -> pago.getEstado() == EstadoPago.PENDIENTE)
                .count());
        model.addAttribute("asistenciasRegistradas", asistencias.size());
        model.addAttribute("ingresosRegistrados", pagos.stream()
                .filter(pago -> pago.getEstado() == EstadoPago.PAGADO)
                .map(Pago::getMonto)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "home/index";
    }
}
