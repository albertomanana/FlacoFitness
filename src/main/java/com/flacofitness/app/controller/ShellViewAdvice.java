package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.ShellNotificationItem;
import com.flacofitness.app.model.dto.UxModuleStateView;
import com.flacofitness.app.model.dto.OperationalClockState;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.security.AccessProfile;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.BrowserTokenService;
import com.flacofitness.app.service.OperationalClockService;
import com.flacofitness.app.service.RecentVisitService;
import com.flacofitness.app.service.ShellNotificationService;
import com.flacofitness.app.service.UxMemoryStateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Set;

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
        GastoRecurrenteController.class,
        FinanzasController.class,
        NominaController.class,
        MaquinaController.class,
        MaterialController.class,
        ClientePortalController.class,
        BusquedaController.class,
        CuentaController.class
})
public class ShellViewAdvice {

    public static final String ATTR_DISMISSED_SIGNATURE = "ff.shell.notifications.dismissedSignature";

    private final ShellNotificationService shellNotificationService;
    private final AccessSessionService accessSessionService;
    private final OperationalClockService operationalClockService;
    private final BrowserTokenService browserTokenService;
    private final RecentVisitService recentVisitService;
    private final UxMemoryStateService uxMemoryStateService;

    public ShellViewAdvice(ShellNotificationService shellNotificationService,
                           AccessSessionService accessSessionService,
                           OperationalClockService operationalClockService,
                           BrowserTokenService browserTokenService,
                           RecentVisitService recentVisitService,
                           UxMemoryStateService uxMemoryStateService) {
        this.shellNotificationService = shellNotificationService;
        this.accessSessionService = accessSessionService;
        this.operationalClockService = operationalClockService;
        this.browserTokenService = browserTokenService;
        this.recentVisitService = recentVisitService;
        this.uxMemoryStateService = uxMemoryStateService;
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

    @ModelAttribute("currentUser")
    public Usuario currentUser(HttpSession session) {
        return accessSessionService.getCurrentUser(session).orElse(null);
    }

    @ModelAttribute("operationalClock")
    public OperationalClockState operationalClock() {
        return operationalClockService.state();
    }

    @ModelAttribute("browserToken")
    public String browserToken(HttpServletRequest request, HttpServletResponse response) {
        return browserTokenService.resolveOrCreate(request, response);
    }

    @ModelAttribute("recentVisits")
    public List<?> recentVisits(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        return recentVisitService.listFor(
                browserTokenService.resolveOrCreate(request, response),
                accessSessionService.getCurrentProfile(session));
    }

    @ModelAttribute("dashboardUxState")
    public UxModuleStateView dashboardUxState(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        return uxMemoryStateService.resolveModuleState(
                browserTokenService.resolveOrCreate(request, response),
                accessSessionService.getCurrentProfile(session),
                "dashboard");
    }

    @ModelAttribute("currentModuleKey")
    public String currentModuleKey(HttpServletRequest request) {
        return resolveModuleKey(request.getRequestURI());
    }

    @ModelAttribute("currentModuleUxState")
    public UxModuleStateView currentModuleUxState(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  HttpSession session) {
        String moduleKey = resolveModuleKey(request.getRequestURI());
        if (moduleKey == null) {
            return new UxModuleStateView(true, false, null);
        }
        return uxMemoryStateService.resolveModuleState(
                browserTokenService.resolveOrCreate(request, response),
                accessSessionService.getCurrentProfile(session),
                moduleKey);
    }

    @ModelAttribute("currentRequestUri")
    public String currentRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return query == null || query.isBlank() ? uri : uri + "?" + query;
    }

    private String resolveModuleKey(String uri) {
        if (uri == null || uri.isBlank()) {
            return null;
        }
        if ("/".equals(uri)) {
            return "dashboard";
        }

        Set<String> supportedModules = Set.of(
                "usuarios",
                "pagos",
                "membresias",
                "sesiones",
                "finanzas",
                "gastos",
                "staff",
                "maquinas",
                "materiales",
                "trials");

        return supportedModules.stream()
                .filter(module -> uri.equals("/" + module) || uri.startsWith("/" + module + "/"))
                .findFirst()
                .orElse(null);
    }
}
