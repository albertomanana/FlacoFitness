package com.flacofitness.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flacofitness.app.model.dto.AsistenciasStatsResponse;
import com.flacofitness.app.model.dto.DashboardStatsResponse;
import com.flacofitness.app.model.dto.FinancialCenterStatsResponse;
import com.flacofitness.app.model.dto.GastosStatsResponse;
import com.flacofitness.app.model.dto.PagosStatsResponse;
import com.flacofitness.app.model.dto.RutinasStatsResponse;
import com.flacofitness.app.model.dto.UsuariosStatsResponse;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.FinancialCenterService;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaquinaService;
import com.flacofitness.app.service.MaterialService;
import com.flacofitness.app.service.MembresiaService;
import com.flacofitness.app.service.NominaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.PlanService;
import com.flacofitness.app.service.ProductIntelligenceService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.SesionClaseService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.TrialService;
import com.flacofitness.app.service.UsuarioService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final UsuarioService usuarioService;
    private final PlanService planService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final RutinaService rutinaService;
    private final StaffService staffService;
    private final TrialService trialService;
    private final SesionClaseService sesionClaseService;
    private final MembresiaService membresiaService;
    private final GastoService gastoService;
    private final NominaService nominaService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final ProductIntelligenceService productIntelligenceService;
    private final FinancialCenterService financialCenterService;

    public StatsController(UsuarioService usuarioService,
                           PlanService planService,
                           PagoService pagoService,
                           AsistenciaService asistenciaService,
                           RutinaService rutinaService,
                           StaffService staffService,
                           TrialService trialService,
                           SesionClaseService sesionClaseService,
                           MembresiaService membresiaService,
                           GastoService gastoService,
                           NominaService nominaService,
                           MaquinaService maquinaService,
                           MaterialService materialService,
                           ProductIntelligenceService productIntelligenceService,
                           FinancialCenterService financialCenterService) {
        this.usuarioService = usuarioService;
        this.planService = planService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
        this.rutinaService = rutinaService;
        this.staffService = staffService;
        this.trialService = trialService;
        this.sesionClaseService = sesionClaseService;
        this.membresiaService = membresiaService;
        this.gastoService = gastoService;
        this.nominaService = nominaService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.productIntelligenceService = productIntelligenceService;
        this.financialCenterService = financialCenterService;
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
                rangoNormalizado,
                asistenciaService.obtenerAsistenciasUltimosDias(rangoNormalizado),
                pagoService.obtenerIngresosMensuales(),
                gastoService.obtenerGastosMensuales(),
                usuarioService.obtenerDistribucionPorPlan(),
                usuarioService.obtenerAltasMensuales(),
                // Metricas operativas
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

    @GetMapping("/gastos")
    public GastosStatsResponse obtenerEstadisticasGastos() {
        BigDecimal gastoMesActual = gastoService.calcularGastoMesActual();
        BigDecimal ingresoMesActual = pagoService.calcularIngresosMesActual();

        return new GastosStatsResponse(
                gastoMesActual,
                gastoService.calcularGastoFijoMesActual(),
                gastoService.calcularGastoVariableMesActual(),
                ingresoMesActual,
                ingresoMesActual.subtract(gastoMesActual),
                gastoService.contarCriticos(),
                gastoService.contarVencimientosProximos(7),
                gastoService.contarRecurrentesProximos(7),
                nominaService.contarPendientes(),
                gastoService.obtenerGastosPorCategoriaMesActual(),
                gastoService.obtenerGastosMensuales()
        );
    }

    @GetMapping("/finanzas")
    public FinancialCenterStatsResponse obtenerEstadisticasFinanzas() {
        return financialCenterService.buildStats();
    }

    private int normalizarRango(int rangoDias) {
        return Math.max(7, Math.min(rangoDias, 365));
    }
}
