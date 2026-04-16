package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.ShellNotificationItem;
import com.flacofitness.app.service.ShellNotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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
        SesionClaseController.class,
        GastoController.class,
        MaquinaController.class,
        MaterialController.class
})
public class ShellViewAdvice {

    public static final String ATTR_DISMISSED_SIGNATURE = "ff.shell.notifications.dismissedSignature";

    private final ShellNotificationService shellNotificationService;

    public ShellViewAdvice(ShellNotificationService shellNotificationService) {
        this.shellNotificationService = shellNotificationService;
    }

    @ModelAttribute("shellNotifications")
    public List<ShellNotificationItem> shellNotifications(HttpSession session) {
        List<ShellNotificationItem> notifications = shellNotificationService.buildNotifications();
        String currentSignature = shellNotificationService.buildSignature(notifications);

        if (session == null) {
            return notifications;
        }

        Object dismissedValue = session.getAttribute(ATTR_DISMISSED_SIGNATURE);
        String dismissedSignature = dismissedValue instanceof String ? (String) dismissedValue : null;

        if (dismissedSignature != null && !dismissedSignature.equals(currentSignature)) {
            session.removeAttribute(ATTR_DISMISSED_SIGNATURE);
            return notifications;
        }

        if (dismissedSignature != null) {
            return List.of();
        }

        return notifications;
    }

    @ModelAttribute("shellNotificationCount")
    public int shellNotificationCount(HttpSession session) {
        return shellNotifications(session).size();
    }
}
