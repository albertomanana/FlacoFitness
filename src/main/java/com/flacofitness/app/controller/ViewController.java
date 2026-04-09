package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.DashboardStatsResponse;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.PlanService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.UsuarioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    private final UsuarioService usuarioService;
    private final PlanService planService;
    private final RutinaService rutinaService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final ObjectMapper objectMapper;

    public ViewController(UsuarioService usuarioService,
                          PlanService planService,
                          RutinaService rutinaService,
                          PagoService pagoService,
                          AsistenciaService asistenciaService,
                          ObjectMapper objectMapper) {
        this.usuarioService = usuarioService;
        this.planService = planService;
        this.rutinaService = rutinaService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String home(@RequestParam(name = "rangoDias", defaultValue = "30") int rangoDias,
                       Model model) {
        int rangoNormalizado = Math.max(7, Math.min(rangoDias, 365));

        model.addAttribute("usuariosTotales", usuarioService.contarTotal());
        model.addAttribute("usuariosActivos", usuarioService.contarActivos());
        model.addAttribute("planesActivos", planService.contarActivos());
        model.addAttribute("rutinasActivas", rutinaService.contarActivas());
        model.addAttribute("pagosRegistrados", pagoService.contarTodos());
        model.addAttribute("pagosPendientes", pagoService.contarPagosPendientes());
        model.addAttribute("pagosVencidos", pagoService.contarPagosVencidos());
        model.addAttribute("asistenciasRegistradas", asistenciaService.contarTodas());
        model.addAttribute("asistenciasHoy", asistenciaService.contarHoy());
        model.addAttribute("ingresosRegistrados", pagoService.calcularIngresosTotales());
        model.addAttribute("ingresosMensuales", pagoService.calcularIngresosMesActual());
        model.addAttribute("renovacionesProximas", usuarioService.contarRenovacionesProximas(7));
        model.addAttribute("dashboardRangoDias", rangoNormalizado);
        model.addAttribute("ultimosUsuarios", usuarioService.listarRecientes());
        model.addAttribute("rutinasDestacadas", rutinaService.listarActivasDestacadas());
        model.addAttribute("ultimosPagos", pagoService.listarRecientes());
        model.addAttribute("proximosCobros", usuarioService.listarRenovacionesProximas());
        DashboardStatsResponse dashboardStats = construirDashboardStats(rangoNormalizado);
        model.addAttribute("dashboardStats", dashboardStats);
        model.addAttribute("dashboardStatsJson", serializarDashboardStats(dashboardStats));
        return "home/index";
    }

    private DashboardStatsResponse construirDashboardStats(int rangoDias) {
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
                rangoDias,
                asistenciaService.obtenerAsistenciasUltimosDias(rangoDias),
                pagoService.obtenerIngresosMensuales(),
                usuarioService.obtenerDistribucionPorPlan(),
                usuarioService.obtenerAltasMensuales()
        );
    }

    private String serializarDashboardStats(DashboardStatsResponse dashboardStats) {
        try {
            return objectMapper.writeValueAsString(dashboardStats);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el estado inicial del dashboard", ex);
        }
    }
}
