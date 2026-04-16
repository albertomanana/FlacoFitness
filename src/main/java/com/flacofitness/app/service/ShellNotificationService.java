package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.ShellNotificationItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShellNotificationService {

    private final PagoService pagoService;
    private final UsuarioService usuarioService;
    private final AsistenciaService asistenciaService;
    private final MembresiaService membresiaService;
    private final TrialService trialService;
    private final SesionClaseService sesionClaseService;
    private final GastoService gastoService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;

    public ShellNotificationService(PagoService pagoService,
                                    UsuarioService usuarioService,
                                    AsistenciaService asistenciaService,
                                    MembresiaService membresiaService,
                                    TrialService trialService,
                                    SesionClaseService sesionClaseService,
                                    GastoService gastoService,
                                    MaquinaService maquinaService,
                                    MaterialService materialService) {
        this.pagoService = pagoService;
        this.usuarioService = usuarioService;
        this.asistenciaService = asistenciaService;
        this.membresiaService = membresiaService;
        this.trialService = trialService;
        this.sesionClaseService = sesionClaseService;
        this.gastoService = gastoService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
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
                    "Revisar usuarios",
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

        long sesionesHoy = sesionClaseService.contarSesionesHoy();
        if (sesionesHoy > 0) {
            notifications.add(new ShellNotificationItem(
                    "Sesiones de hoy",
                    sesionesHoy + " sesion(es) programadas para la jornada.",
                    "Ver sesiones",
                    "/sesiones",
                    "success"
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
                    "/gastos",
                    "danger"
            ));
        }

        long recurrentesProximos = gastoService.contarRecurrentesProximos(7);
        if (recurrentesProximos > 0) {
            notifications.add(new ShellNotificationItem(
                    "Recurrentes proximos",
                    recurrentesProximos + " gasto(s) recurrentes vencen en los proximos 7 dias.",
                    "Abrir gastos",
                    "/gastos",
                    "warning"
            ));
        }

        long maquinasFueraServicio = maquinaService.contarFueraDeServicio();
        if (maquinasFueraServicio > 0) {
            notifications.add(new ShellNotificationItem(
                    "Maquinas fuera de servicio",
                    maquinasFueraServicio + " equipo(s) requieren accion de mantenimiento.",
                    "Ver maquinas",
                    "/maquinas",
                    "warning"
            ));
        }

        long materialesBajoStock = materialService.contarBajoStock();
        if (materialesBajoStock > 0) {
            notifications.add(new ShellNotificationItem(
                    "Material bajo stock",
                    materialesBajoStock + " item(s) necesitan reposicion.",
                    "Abrir materiales",
                    "/materiales",
                    "info"
            ));
        }

        return notifications;
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