package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.ShellNotificationItem;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
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
        AsistenciaController.class
})
public class ShellViewAdvice {

    private final PagoService pagoService;
    private final UsuarioService usuarioService;
    private final AsistenciaService asistenciaService;

    public ShellViewAdvice(PagoService pagoService,
                           UsuarioService usuarioService,
                           AsistenciaService asistenciaService) {
        this.pagoService = pagoService;
        this.usuarioService = usuarioService;
        this.asistenciaService = asistenciaService;
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

        return notifications;
    }

    @ModelAttribute("shellNotificationCount")
    public int shellNotificationCount() {
        return shellNotifications().size();
    }
}