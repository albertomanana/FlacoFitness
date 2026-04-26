package com.flacofitness.app.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.flacofitness.app.model.dto.ShellNotificationItem;

@Service
public class ShellNotificationService {

    private final PagoService pagoService;
    private final UsuarioService usuarioService;
    private final AsistenciaService asistenciaService;
    private final MembresiaService membresiaService;
    private final TrialService trialService;
    private final SesionClaseService sesionClaseService;
    private final GastoService gastoService;
    private final NominaService nominaService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final OperationalClockService operationalClockService;

    public ShellNotificationService(PagoService pagoService,
                                    UsuarioService usuarioService,
                                    AsistenciaService asistenciaService,
                                    MembresiaService membresiaService,
                                    TrialService trialService,
                                    SesionClaseService sesionClaseService,
                                    GastoService gastoService,
                                    NominaService nominaService,
                                    MaquinaService maquinaService,
                                    MaterialService materialService,
                                    OperationalClockService operationalClockService) {
        this.pagoService = pagoService;
        this.usuarioService = usuarioService;
        this.asistenciaService = asistenciaService;
        this.membresiaService = membresiaService;
        this.trialService = trialService;
        this.sesionClaseService = sesionClaseService;
        this.gastoService = gastoService;
        this.nominaService = nominaService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.operationalClockService = operationalClockService;
    }

    public List<ShellNotificationItem> buildNotifications() {
        List<ShellNotificationItem> notifications = new ArrayList<>();

        long pagosVencidos = pagoService.contarPagosVencidos();
        if (pagosVencidos > 0) {
            notifications.add(new ShellNotificationItem(
                    "Pagos vencidos",
                    pagosVencidos + " cobro(s) requieren seguimiento inmediato.",
                    "Abrir pagos",
                    "/pagos?estado=VENCIDO",
                    "danger"
            ));
        }

        long pagosPendientes = pagoService.contarPagosPendientes();
        if (pagosPendientes > 0) {
            notifications.add(new ShellNotificationItem(
                    "Pagos pendientes",
                    pagosPendientes + " cobro(s) siguen en estado pendiente.",
                    "Ver pendientes",
                    "/pagos?estado=PENDIENTE",
                    "warning"
            ));
        }

        long renovaciones = usuarioService.contarRenovacionesProximas(7);
        if (renovaciones > 0) {
            notifications.add(new ShellNotificationItem(
                    "Renovaciones proximas",
                    renovaciones + " socio(s) renuevan en los proximos 7 dias.",
                    "Abrir usuarios",
                    "/usuarios",
                    "info"
            ));
        }

        long asistenciasHoy = asistenciaService.contarHoy();
        if (asistenciasHoy > 0) {
            notifications.add(new ShellNotificationItem(
                    "Actividad de hoy",
                    asistenciasHoy + " asistencia(s) registradas durante la jornada.",
                    "Abrir asistencias",
                    "/asistencias",
                    "success"
            ));
        }

        long usuariosInactivos = asistenciaService.contarUsuariosInactivos();
        if (usuariosInactivos > 0) {
            notifications.add(new ShellNotificationItem(
                    "Usuarios inactivos",
                    usuariosInactivos + " socio(s) llevan mas de 14 dias sin check-in.",
                    "Abrir usuarios",
                    "/usuarios",
                    "warning"
            ));
        }

        long trialsHoy = trialService.contarHoy();
        if (trialsHoy > 0) {
            notifications.add(new ShellNotificationItem(
                    "Trials de hoy",
                    trialsHoy + " prueba(s) necesitan seguimiento comercial.",
                    "Abrir trials",
                    "/trials",
                    "info"
            ));
        }

        long trialsSinSeguimiento = trialService.contarSinSeguimiento(3);
        if (trialsSinSeguimiento > 0) {
            notifications.add(new ShellNotificationItem(
                    "Trials sin seguimiento",
                    trialsSinSeguimiento + " lead(s) requieren contacto comercial.",
                    "Gestionar trials",
                    "/trials?estado=PENDIENTE",
                    "warning"
            ));
        }

        long sesionesHoy = sesionClaseService.contarSesionesHoy();
        if (sesionesHoy > 0) {
            notifications.add(new ShellNotificationItem(
                    "Sesiones de hoy",
                    sesionesHoy + " sesion(es) programadas para la jornada.",
                    "Ver sesiones",
                    "/sesiones?fecha=" + operationalClockService.today(),
                    "success"
            ));
        }

        if (!sesionClaseService.listarProximas().isEmpty()) {
            notifications.add(new ShellNotificationItem(
                    "Clase proxima a iniciar",
                    "Tienes sesiones proximas en agenda para hoy. Revisa aforo y asistencia.",
                    "Abrir agenda",
                    "/sesiones",
                    "info"
            ));
        }

        long membresiasVencidas = membresiaService.contarVencidas();
        if (membresiasVencidas > 0) {
            notifications.add(new ShellNotificationItem(
                    "Membresias vencidas",
                    membresiasVencidas + " contrato(s) requieren renovacion o revision.",
                    "Gestionar membresias",
                    "/membresias",
                    "warning"
            ));
        }

        long gastosCriticos = gastoService.contarCriticos();
        if (gastosCriticos > 0) {
            notifications.add(new ShellNotificationItem(
                    "Gastos criticos",
                    gastosCriticos + " gasto(s) pendientes vencidos requieren seguimiento financiero.",
                    "Revisar gastos",
                    "/gastos?estado=VENCIDO",
                    "danger"
            ));
        }

        long recurrentesProximos = gastoService.contarRecurrentesProximos(7);
        if (recurrentesProximos > 0) {
            notifications.add(new ShellNotificationItem(
                    "Recurrentes proximos",
                    recurrentesProximos + " gasto(s) recurrentes vencen en los proximos 7 dias.",
                    "Abrir gastos",
                    "/gastos?recurrente=true",
                    "warning"
            ));
        }

        long nominasPendientes = nominaService.contarPendientes();
        if (nominasPendientes > 0) {
            notifications.add(new ShellNotificationItem(
                "Nominas pendientes",
                nominasPendientes + " nomina(s) siguen emitidas sin cierre.",
                "Abrir nominas",
                "/nominas",
                "warning"
            ));
        }

        long maquinasFueraServicio = maquinaService.contarFueraDeServicio();
        if (maquinasFueraServicio > 0) {
            notifications.add(new ShellNotificationItem(
                    "Maquinas fuera de servicio",
                    maquinasFueraServicio + " equipo(s) requieren accion de mantenimiento.",
                    "Ver maquinas",
                    "/maquinas?estado=AVERIADA",
                    "warning"
            ));
        }

        long maquinasRevision = maquinaService.contarRevisionProxima(7);
        if (maquinasRevision > 0) {
            notifications.add(new ShellNotificationItem(
                    "Revision de maquinaria",
                    maquinasRevision + " equipo(s) deben revisarse en los proximos 7 dias.",
                    "Programar revisiones",
                    "/maquinas",
                    "info"
            ));
        }

        long materialesBajoStock = materialService.contarBajoStock();
        if (materialesBajoStock > 0) {
            notifications.add(new ShellNotificationItem(
                    "Material bajo stock",
                    materialesBajoStock + " item(s) necesitan reposicion.",
                    "Abrir materiales",
                    "/materiales?estado=BAJO_STOCK",
                    "info"
            ));
        }

        notifications.sort(Comparator.comparingInt(item -> priorityForTone(item.tone())));
        return notifications;
    }

    private int priorityForTone(String tone) {
        return switch (tone) {
            case "danger" -> 0;
            case "warning" -> 1;
            case "info" -> 2;
            case "success" -> 3;
            default -> 4;
        };
    }

    public String buildSignature(List<ShellNotificationItem> notifications) {
        StringBuilder rawSignature = new StringBuilder();
        for (ShellNotificationItem item : notifications) {
            rawSignature.append(item.title())
                    .append('|')
                    .append(item.description())
                    .append('|')
                    .append(item.actionUrl())
                    .append(';');
        }
        return Integer.toHexString(rawSignature.toString().hashCode());
    }
}
