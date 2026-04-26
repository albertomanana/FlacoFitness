package com.flacofitness.app.service;

import com.flacofitness.app.config.ProductIntelligenceSettings;
import com.flacofitness.app.model.dto.AttentionItemView;
import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.MembresiaUsuario;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.EstadoMembresia;
import com.flacofitness.app.model.enums.EstadoPago;
import com.flacofitness.app.model.enums.TipoMembresia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ProductIntelligenceService {

    private static final List<EstadoMembresia> OPERATIVE_MEMBERSHIPS = List.of(
            EstadoMembresia.ACTIVA,
            EstadoMembresia.PENDIENTE,
            EstadoMembresia.PRUEBA
    );

    private final ProductIntelligenceSettings settings;
    private final UsuarioService usuarioService;
    private final UsuarioControlCenterService usuarioControlCenterService;
    private final MembresiaService membresiaService;
    private final GastoService gastoService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final PagoService pagoService;
    private final OperationalClockService operationalClockService;

    public ProductIntelligenceService(ProductIntelligenceSettings settings,
                                      UsuarioService usuarioService,
                                      UsuarioControlCenterService usuarioControlCenterService,
                                      MembresiaService membresiaService,
                                      GastoService gastoService,
                                      MaquinaService maquinaService,
                                      MaterialService materialService,
                                      PagoService pagoService,
                                      OperationalClockService operationalClockService) {
        this.settings = settings;
        this.usuarioService = usuarioService;
        this.usuarioControlCenterService = usuarioControlCenterService;
        this.membresiaService = membresiaService;
        this.gastoService = gastoService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.pagoService = pagoService;
        this.operationalClockService = operationalClockService;
    }

    public long countUsuariosEnRiesgo() {
        return usuarioService.listarActivos().stream()
                .filter(usuario -> classifyUsuario(usuario) == UserHealth.EN_RIESGO)
                .count();
    }

    public long countMembresiasPorCaducar() {
        return membresiaService.listarRenovacionesProximas(settings.getMembershipExpiringDays()).size();
    }

    public long countGastosAnomalos() {
        return anomalousExpenses().size();
    }

    public List<AttentionItemView> buildAttentionItems() {
        long usuariosEnRiesgo = countUsuariosEnRiesgo();
        long pagosVencidos = pagoService.contarPagosVencidos();
        long membresiasPorCaducar = countMembresiasPorCaducar();
        long stockBajo = materialService.contarBajoStock();
        long maquinasRevision = maquinaService.contarRevisionProxima(settings.getMachineReviewDays());
        long gastosAnomalos = countGastosAnomalos();

        return List.of(
                new AttentionItemView("Usuarios en riesgo",
                        "Miembros con menor actividad o friccion de cobro reciente.",
                        "/usuarios",
                        usuariosEnRiesgo > 0 ? "warning" : "neutral",
                        "fa-user-clock",
                        usuariosEnRiesgo),
                new AttentionItemView("Pagos vencidos",
                        "Cobros que ya pasaron a vencido y requieren seguimiento.",
                        "/pagos?estado=VENCIDO",
                        pagosVencidos > 0 ? "danger" : "neutral",
                        "fa-money-bill-wave",
                        pagosVencidos),
                new AttentionItemView("Membresias por caducar",
                        "Contratos que vencen dentro de la ventana operativa prioritaria.",
                        "/membresias",
                        membresiasPorCaducar > 0 ? "warning" : "neutral",
                        "fa-id-card",
                        membresiasPorCaducar),
                new AttentionItemView("Material con stock bajo",
                        "Reposiciones que conviene resolver antes de afectar clases y sesiones.",
                        "/materiales",
                        stockBajo > 0 ? "warning" : "neutral",
                        "fa-box-open",
                        stockBajo),
                new AttentionItemView("Maquinas con revision proxima",
                        "Activos que entran en ventana de revision o mantenimiento.",
                        "/maquinas",
                        maquinasRevision > 0 ? "warning" : "neutral",
                        "fa-screwdriver-wrench",
                        maquinasRevision),
                new AttentionItemView("Gastos anómalos",
                        "Desviaciones sobre el historico operativo reciente.",
                        "/gastos",
                        gastosAnomalos > 0 ? "danger" : "neutral",
                        "fa-chart-line",
                        gastosAnomalos)
        );
    }

    public UserHealth classifyUsuario(Usuario usuario) {
        if (usuario == null) {
            return UserHealth.INACTIVO;
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return UserHealth.INACTIVO;
        }

        var controlCenter = usuarioControlCenterService.construirVista(usuario);
        if (controlCenter.diasSinAsistencia() == null) {
            return UserHealth.EN_RIESGO;
        }
        if (controlCenter.diasSinAsistencia() > settings.getInactiveDays()) {
            return UserHealth.INACTIVO;
        }
        if (controlCenter.pagosVencidos() > 0) {
            return UserHealth.EN_RIESGO;
        }

        boolean membershipExpiringSoon = membresiaService.buscarContratoActivoPorUsuario(usuario.getId())
                .map(contrato -> contrato.getFechaFin() != null
                        && !contrato.getFechaFin().isBefore(operationalClockService.today())
                        && ChronoUnit.DAYS.between(operationalClockService.today(), contrato.getFechaFin()) <= settings.getMembershipExpiringDays())
                .orElse(false);

        if (controlCenter.diasSinAsistencia() <= settings.getRiskDays()
                && controlCenter.pagosPendientes() == 0
                && !membershipExpiringSoon) {
            return UserHealth.ACTIVO;
        }
        return UserHealth.EN_RIESGO;
    }

    public ClientTier classifyCliente(Usuario usuario) {
        return membresiaService.buscarContratoActivoPorUsuario(usuario.getId())
                .map(MembresiaUsuario::getPlan)
                .map(plan -> plan.getTipoMembresia() == TipoMembresia.PREMIUM ? ClientTier.PREMIUM : ClientTier.BASICO)
                .orElse(ClientTier.BASICO);
    }

    public ExpenseSignal classifyGasto(Gasto gasto, Map<CategoriaGasto, ExpenseBaseline> baselines) {
        if (gasto == null || gasto.getCategoria() == null || gasto.getImporte() == null) {
            return ExpenseSignal.NORMAL;
        }

        ExpenseBaseline baseline = baselines.get(gasto.getCategoria());
        BigDecimal threshold = baseline != null && baseline.average().compareTo(BigDecimal.ZERO) > 0
                ? baseline.average().multiply(settings.getExpenseAnomalyFactor())
                : settings.fallbackThresholdFor(gasto.getCategoria());

        return gasto.getImporte().compareTo(threshold) > 0 ? ExpenseSignal.ALTO : ExpenseSignal.NORMAL;
    }

    public Map<CategoriaGasto, ExpenseBaseline> buildExpenseBaselines() {
        LocalDate today = operationalClockService.today();
        List<Gasto> recentExpenses = gastoService.listarFiltrados(today.minusDays(90), today, null);
        Map<CategoriaGasto, BigDecimal> sums = new EnumMap<>(CategoriaGasto.class);
        Map<CategoriaGasto, Long> counts = new EnumMap<>(CategoriaGasto.class);

        for (Gasto gasto : recentExpenses) {
            if (gasto.getCategoria() == null || gasto.getImporte() == null) {
                continue;
            }
            sums.merge(gasto.getCategoria(), gasto.getImporte(), BigDecimal::add);
            counts.merge(gasto.getCategoria(), 1L, Long::sum);
        }

        Map<CategoriaGasto, ExpenseBaseline> baselines = new EnumMap<>(CategoriaGasto.class);
        for (Map.Entry<CategoriaGasto, Long> entry : counts.entrySet()) {
            BigDecimal average = sums.get(entry.getKey())
                    .divide(BigDecimal.valueOf(entry.getValue()), 2, RoundingMode.HALF_UP);
            baselines.put(entry.getKey(), new ExpenseBaseline(average, entry.getValue()));
        }
        return baselines;
    }

    private List<Gasto> anomalousExpenses() {
        Map<CategoriaGasto, ExpenseBaseline> baselines = buildExpenseBaselines();
        LocalDate today = operationalClockService.today();
        return gastoService.listarFiltrados(today.minusDays(90), today, null).stream()
                .filter(gasto -> classifyGasto(gasto, baselines) == ExpenseSignal.ALTO)
                .toList();
    }

    public record ExpenseBaseline(BigDecimal average, long samples) {
    }

    public enum UserHealth {
        ACTIVO, EN_RIESGO, INACTIVO
    }

    public enum ClientTier {
        PREMIUM, BASICO
    }

    public enum ExpenseSignal {
        NORMAL, ALTO
    }
}
