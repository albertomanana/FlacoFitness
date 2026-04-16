package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.DashboardStatsResponse;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.MembresiaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.PlanService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.SesionClaseService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.TrialService;
import com.flacofitness.app.service.UsuarioService;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaquinaService;
import com.flacofitness.app.service.MaterialService;
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
    private final StaffService staffService;
    private final TrialService trialService;
    private final SesionClaseService sesionClaseService;
    private final MembresiaService membresiaService;
    private final GastoService gastoService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final ObjectMapper objectMapper;

    public ViewController(UsuarioService usuarioService,
                          PlanService planService,
                          RutinaService rutinaService,
                          PagoService pagoService,
                          AsistenciaService asistenciaService,
                          StaffService staffService,
                          TrialService trialService,
                          SesionClaseService sesionClaseService,
                          MembresiaService membresiaService,
                          GastoService gastoService,
                          MaquinaService maquinaService,
                          MaterialService materialService,
                          ObjectMapper objectMapper) {
        this.usuarioService = usuarioService;
        this.planService = planService;
        this.rutinaService = rutinaService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
        this.staffService = staffService;
        this.trialService = trialService;
        this.sesionClaseService = sesionClaseService;
        this.membresiaService = membresiaService;
        this.gastoService = gastoService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
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
        // NUEVAS MÉTRICAS PARA DASHBOARD
        model.addAttribute("usuariosAsistenciaActivos", asistenciaService.contarUsuariosActivos());
        model.addAttribute("usuariosAsistenciaInactivos", asistenciaService.contarUsuariosInactivos());
        model.addAttribute("usuariosFinancierosAlDia", pagoService.contarUsuariosAlDia());
        model.addAttribute("usuariosFinancierosConDeuda", pagoService.contarUsuariosConDeuda());
        model.addAttribute("usuariosFinancierosConVencidos", pagoService.contarUsuariosConPagosVencidos());
        model.addAttribute("staffActivos", staffService.contarActivos());
        model.addAttribute("trialsPendientes", trialService.contarPendientes());
        model.addAttribute("trialsHoy", trialService.contarHoy());
        model.addAttribute("sesionesHoy", sesionClaseService.contarSesionesHoy());
        model.addAttribute("membresiasActivas", membresiaService.contarActivas());
        model.addAttribute("membresiasVencidas", membresiaService.contarVencidas());
        model.addAttribute("gastoMesActual", gastoService.calcularGastoMesActual());
        model.addAttribute("gastosCriticos", gastoService.contarCriticos());
        model.addAttribute("gastosRecurrentesProximos", gastoService.contarRecurrentesProximos(7));
        model.addAttribute("maquinasFueraServicio", maquinaService.contarFueraDeServicio());
        model.addAttribute("materialesBajoStock", materialService.contarBajoStock());
        model.addAttribute("proximasSesiones", sesionClaseService.listarProximas());
        model.addAttribute("proximosTrials", trialService.listarProximos());
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
                usuarioService.obtenerAltasMensuales(),
                // NUEVAS MÉTRICAS
                asistenciaService.contarUsuariosActivos(),
                asistenciaService.contarUsuariosInactivos(),
                pagoService.contarUsuariosAlDia(),
                pagoService.contarUsuariosConDeuda(),
                pagoService.contarUsuariosConPagosVencidos(),
                staffService.contarActivos(),
                trialService.contarPendientes(),
                trialService.contarHoy(),
                sesionClaseService.contarSesionesHoy(),
                membresiaService.contarActivas(),
                membresiaService.contarVencidas()
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
