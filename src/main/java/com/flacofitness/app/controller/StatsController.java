package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.AsistenciasStatsResponse;
import com.flacofitness.app.model.dto.DashboardStatsResponse;
import com.flacofitness.app.model.dto.PagosStatsResponse;
import com.flacofitness.app.model.dto.RutinasStatsResponse;
import com.flacofitness.app.model.dto.UsuariosStatsResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.flacofitness.app.service.PlanService;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.UsuarioService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final UsuarioService usuarioService;
    private final PlanService planService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final RutinaService rutinaService;

    public StatsController(UsuarioService usuarioService,
                           PlanService planService,
                           PagoService pagoService,
                           AsistenciaService asistenciaService,
                           RutinaService rutinaService) {
        this.usuarioService = usuarioService;
        this.planService = planService;
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

    @GetMapping("/dashboard")
    public DashboardStatsResponse obtenerEstadisticasDashboard(
            @RequestParam(name = "rangoDias", defaultValue = "30")
            @Min(7) @Max(365) int rangoDias) {
        int rangoNormalizado = normalizarRango(rangoDias);

        return new DashboardStatsResponse(
                usuarioService.contarTotal(),
                usuarioService.contarActivos(),
                planService.contarActivos(),
                pagoService.contarPagosPendientes(),
                pagoService.contarPagosVencidos(),
                usuarioService.contarRenovacionesProximas(7),
                pagoService.calcularIngresosTotales(),
                pagoService.calcularIngresosMesActual(),
                asistenciaService.contarHoy(),
                rutinaService.contarActivas(),
                rangoNormalizado,
                asistenciaService.obtenerAsistenciasUltimosDias(rangoNormalizado),
                pagoService.obtenerIngresosMensuales(),
                usuarioService.obtenerDistribucionPorPlan(),
                usuarioService.obtenerAltasMensuales(),
                // NUEVAS MÉTRICAS
                asistenciaService.contarUsuariosActivos(),
                asistenciaService.contarUsuariosInactivos(),
                pagoService.contarUsuariosAlDia(),
                pagoService.contarUsuariosConDeuda(),
                pagoService.contarUsuariosConPagosVencidos()
        );
    }

    private int normalizarRango(int rangoDias) {
        return Math.max(7, Math.min(rangoDias, 365));
    }
}
