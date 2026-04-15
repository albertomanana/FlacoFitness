package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.ShellNotificationItem;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.MembresiaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.SesionClaseService;
import com.flacofitness.app.service.TrialService;
import com.flacofitness.app.service.UsuarioService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice(assignableTypes = {
        ViewController.class,
        UsuarioController.class,
        PagoController.class,
        RutinaController.class,
        AsistenciaController.class,
        StaffController.class,
        MembresiaController.class,
        TrialController.class,
        ClaseController.class,
        SesionClaseController.class
})
public class ShellViewAdvice {

    private final PagoService pagoService;
    private final UsuarioService usuarioService;
    private final AsistenciaService asistenciaService;
    private final MembresiaService membresiaService;
    private final TrialService trialService;
    private final SesionClaseService sesionClaseService;

    public ShellViewAdvice(PagoService pagoService,
                           UsuarioService usuarioService,
                           AsistenciaService asistenciaService,
                           MembresiaService membresiaService,
                           TrialService trialService,
                           SesionClaseService sesionClaseService) {
        this.pagoService = pagoService;
        this.usuarioService = usuarioService;
        this.asistenciaService = asistenciaService;
        this.membresiaService = membresiaService;
        this.trialService = trialService;
        this.sesionClaseService = sesionClaseService;
    }

    @ModelAttribute("shellNotifications")
    public List<ShellNotificationItem> shellNotifications() {
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

        return notifications;
    }

    @ModelAttribute("shellNotificationCount")
    public int shellNotificationCount() {
        return shellNotifications().size();
    }
}
