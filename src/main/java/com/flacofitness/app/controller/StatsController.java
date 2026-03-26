package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.AsistenciasStatsResponse;
import com.flacofitness.app.model.dto.PagosStatsResponse;
import com.flacofitness.app.model.dto.RutinasStatsResponse;
import com.flacofitness.app.model.dto.UsuariosStatsResponse;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final UsuarioService usuarioService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final RutinaService rutinaService;

    public StatsController(UsuarioService usuarioService,
                           PagoService pagoService,
                           AsistenciaService asistenciaService,
                           RutinaService rutinaService) {
        this.usuarioService = usuarioService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
        this.rutinaService = rutinaService;
    }

    @GetMapping("/usuarios")
    public UsuariosStatsResponse obtenerEstadisticasUsuarios() {
        return new UsuariosStatsResponse(
                usuarioService.contarTotal(),
                usuarioService.contarActivos()
        );
    }

    @GetMapping("/pagos")
    public PagosStatsResponse obtenerEstadisticasPagos() {
        return new PagosStatsResponse(
                pagoService.calcularIngresosTotales(),
                pagoService.contarPagosPendientes(),
                pagoService.obtenerIngresosMensuales()
        );
    }

    @GetMapping("/asistencias")
    public AsistenciasStatsResponse obtenerEstadisticasAsistencias() {
        return new AsistenciasStatsResponse(
                asistenciaService.obtenerAsistenciasPorDia(),
                asistenciaService.obtenerAsistenciasMensuales()
        );
    }

    @GetMapping("/rutinas")
    public RutinasStatsResponse obtenerEstadisticasRutinas() {
        return new RutinasStatsResponse(rutinaService.contarActivas());
    }
}
