package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.ShellNotificationItem;
import com.flacofitness.app.model.dto.OperationalClockState;
import com.flacofitness.app.security.AccessProfile;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.OperationalClockService;
import com.flacofitness.app.service.ShellNotificationService;
import jakarta.servlet.http.HttpServletRequest;
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
        MaterialController.class,
        ClientePortalController.class
})
public class ShellViewAdvice {

    public static final String ATTR_DISMISSED_SIGNATURE = "ff.shell.notifications.dismissedSignature";

    private final ShellNotificationService shellNotificationService;
    private final AccessSessionService accessSessionService;
    private final OperationalClockService operationalClockService;

    public ShellViewAdvice(ShellNotificationService shellNotificationService,
                           AccessSessionService accessSessionService,
                           OperationalClockService operationalClockService) {
        this.shellNotificationService = shellNotificationService;
        this.accessSessionService = accessSessionService;
        this.operationalClockService = operationalClockService;
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

    @ModelAttribute("accessProfile")
    public AccessProfile accessProfile(HttpSession session) {
        return accessSessionService.getCurrentProfile(session);
    }

    @ModelAttribute("operationalClock")
    public OperationalClockState operationalClock() {
        return operationalClockService.state();
    }

    @ModelAttribute("currentRequestUri")
    public String currentRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return query == null || query.isBlank() ? uri : uri + "?" + query;
    }
}
