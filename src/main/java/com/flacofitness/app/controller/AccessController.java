package com.flacofitness.app.controller;

import com.flacofitness.app.config.AccessSettings;
import com.flacofitness.app.security.AccessAttemptResult;
import com.flacofitness.app.security.AccessSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping
public class AccessController {

    private final AccessSessionService accessSessionService;
    private final AccessSettings accessSettings;

    public AccessController(AccessSessionService accessSessionService,
                            AccessSettings accessSettings) {
        this.accessSessionService = accessSessionService;
        this.accessSettings = accessSettings;
    }

    @GetMapping("/acceso")
    public String mostrarAcceso(HttpSession session, Model model) {
        if (accessSessionService.isGranted(session)) {
            boolean mustChangePassword = accessSessionService.getCurrentUser(session)
                    .map(usuario -> Boolean.TRUE.equals(usuario.getMustChangePassword()))
                    .orElse(false);
            if (mustChangePassword) {
                return "redirect:/cuenta/password";
            }
            return "redirect:" + accessSessionService.resolveTarget(session);
        }

        model.addAttribute("remainingAttempts", accessSessionService.getRemainingAttempts(session));
        model.addAttribute("isLocked", accessSessionService.isLocked(session));
        model.addAttribute("lockedUntil", accessSessionService.getLockedUntil(session));
        model.addAttribute("lockedUntilText", formatLockedUntil(accessSessionService.getLockedUntil(session)));
        model.addAttribute("targetPath", accessSessionService.resolveTarget(session));
        model.addAttribute("maxAttempts", accessSettings.getMaxAttempts());
        model.addAttribute("lockMinutes", accessSettings.getLockDuration().toMinutes());
        return "auth/acceso";
    }

    @PostMapping("/acceso")
    public String validarAcceso(@RequestParam(name = "login", required = false) String login,
                                @RequestParam(name = "password", required = false) String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        AccessAttemptResult result = accessSessionService.authenticate(session, login, password);
        if (result.granted()) {
            String target = accessSessionService.resolveTarget(session);
            accessSessionService.clearTarget(session);
            var profile = accessSessionService.getCurrentProfile(session);
            if (!profile.canAccess(target, "GET")) {
                target = profile.entryPoint();
            }

            boolean mustChangePassword = accessSessionService.getCurrentUser(session)
                    .map(usuario -> Boolean.TRUE.equals(usuario.getMustChangePassword()))
                    .orElse(false);
            redirectAttributes.addFlashAttribute("mensajeExito", mustChangePassword
                    ? "Acceso concedido. Debes cambiar tu contrasena temporal antes de continuar trabajando."
                    : "Acceso concedido correctamente.");
            return mustChangePassword ? "redirect:/cuenta/password" : "redirect:" + target;
        }

        redirectAttributes.addFlashAttribute("mensajeError", result.message());
        return "redirect:/acceso";
    }

    @PostMapping("/salir")
    public String cerrarAcceso(HttpSession session, RedirectAttributes redirectAttributes) {
        accessSessionService.clear(session);
        redirectAttributes.addFlashAttribute("mensajeExito", "La sesion se cerro correctamente.");
        return "redirect:/acceso";
    }

    private String formatLockedUntil(java.time.Instant lockedUntil) {
        if (lockedUntil == null) {
            return null;
        }

        return DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(lockedUntil);
    }
}
