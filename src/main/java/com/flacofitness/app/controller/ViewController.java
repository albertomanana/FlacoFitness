package com.flacofitness.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flacofitness.app.model.dto.DashboardStatsResponse;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.ActivityLogService;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaquinaService;
import com.flacofitness.app.service.MaterialService;
import com.flacofitness.app.service.MembresiaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.PlanService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.SesionClaseService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.TrialService;
import com.flacofitness.app.service.UsuarioService;
import com.flacofitness.app.service.ProductIntelligenceService;
import com.flacofitness.app.service.UxMemoryStateService;
import com.flacofitness.app.security.AccessProfile;
import com.flacofitness.app.security.AccessSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;

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
    private final ProductIntelligenceService productIntelligenceService;
    private final ActivityLogService activityLogService;
    private final UxMemoryStateService uxMemoryStateService;
    private final AccessSessionService accessSessionService;
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
                          ProductIntelligenceService productIntelligenceService,
                          ActivityLogService activityLogService,
                          UxMemoryStateService uxMemoryStateService,
                          AccessSessionService accessSessionService,
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
        this.productIntelligenceService = productIntelligenceService;
        this.activityLogService = activityLogService;
        this.uxMemoryStateService = uxMemoryStateService;
        this.accessSessionService = accessSessionService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String home(@RequestParam(name = "rangoDias", defaultValue = "30") int rangoDias,
                       Model model,
                       HttpSession session,
                       HttpServletRequest request,
                       HttpServletResponse response) {
        int rangoNormalizado = Math.max(7, Math.min(rangoDias, 365));
        AccessProfile profile = accessSessionService.getCurrentProfile(session);
        BigDecimal ingresosMesActual = pagoService.calcularIngresosMesActual();
        BigDecimal gastoMesActual = gastoService.calcularGastoMesActual();

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
        model.addAttribute("ingresosMensuales", ingresosMesActual);
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
        model.addAttribute("gastoMesActual", gastoMesActual);
        model.addAttribute("beneficioEstimado", ingresosMesActual.subtract(gastoMesActual));
        model.addAttribute("gastosCriticos", gastoService.contarCriticos());
        model.addAttribute("gastosRecurrentesProximos", gastoService.contarRecurrentesProximos(7));
        model.addAttribute("maquinasFueraServicio", maquinaService.contarFueraDeServicio());
        model.addAttribute("maquinasRevisionProxima", maquinaService.contarRevisionProxima(7));
        model.addAttribute("materialesBajoStock", materialService.contarBajoStock());
        model.addAttribute("trialsSemana", trialService.contarSemanaActual());
        model.addAttribute("proximasSesiones", sesionClaseService.listarProximas());
        model.addAttribute("proximosTrials", trialService.listarProximos());
        model.addAttribute("attentionItems", productIntelligenceService.buildAttentionItems());
        model.addAttribute("dashboardGuide", uxMemoryStateService.buildDashboardGuide(
                (String) request.getAttribute(com.flacofitness.app.service.BrowserTokenService.REQUEST_ATTR), profile));
        model.addAttribute("recentActivity", activityLogService.recentActivity());
        DashboardStatsResponse dashboardStats = construirDashboardStats(rangoNormalizado);
        model.addAttribute("dashboardStats", dashboardStats);
        model.addAttribute("dashboardStatsJson", serializarDashboardStats(dashboardStats));
        return "home/index";
    }

    private DashboardStatsResponse construirDashboardStats(int rangoDias) {
        BigDecimal ingresosMesActual = pagoService.calcularIngresosMesActual();
        BigDecimal gastoMesActual = gastoService.calcularGastoMesActual();

        return new DashboardStatsResponse(
                usuarioService.contarTotal(),
                usuarioService.contarActivos(),
                planService.contarActivos(),
                pagoService.contarPagosPendientes(),
                pagoService.contarPagosVencidos(),
                usuarioService.contarRenovacionesProximas(7),
                pagoService.calcularIngresosTotales(),
                ingresosMesActual,
                gastoMesActual,
                ingresosMesActual.subtract(gastoMesActual),
                asistenciaService.contarHoy(),
                rutinaService.contarActivas(),
                rangoDias,
                asistenciaService.obtenerAsistenciasUltimosDias(rangoDias),
                pagoService.obtenerIngresosMensuales(),
                gastoService.obtenerGastosMensuales(),
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
                trialService.contarSemanaActual(),
                sesionClaseService.contarSesionesHoy(),
                membresiaService.contarActivas(),
                membresiaService.contarVencidas(),
                maquinaService.contarFueraDeServicio(),
                maquinaService.contarRevisionProxima(7),
                materialService.contarBajoStock(),
                productIntelligenceService.countUsuariosEnRiesgo(),
                productIntelligenceService.countMembresiasPorCaducar(),
                productIntelligenceService.countGastosAnomalos(),
                productIntelligenceService.buildAttentionItems()
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
