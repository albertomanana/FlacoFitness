package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.dto.PasswordChangeForm;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cuenta")
public class CuentaController {

    private final AccessSessionService accessSessionService;
    private final UsuarioService usuarioService;

    public CuentaController(AccessSessionService accessSessionService,
                            UsuarioService usuarioService) {
        this.accessSessionService = accessSessionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/password")
    public String mostrarCambioPassword(HttpSession session, Model model) {
        Usuario usuario = accessSessionService.getCurrentUser(session).orElse(null);
        if (usuario == null) {
            return "redirect:/acceso";
        }
        model.addAttribute("passwordForm", new PasswordChangeForm());
        model.addAttribute("mustChangePassword", Boolean.TRUE.equals(usuario.getMustChangePassword()));
        return "auth/password";
    }

    @PostMapping("/password")
    public String cambiarPassword(@ModelAttribute("passwordForm") PasswordChangeForm form,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuario = accessSessionService.getCurrentUser(session).orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debes iniciar sesion para cambiar la contrasena.");
            return "redirect:/acceso";
        }
        try {
            usuarioService.cambiarPassword(usuario.getId(),
                    form.getCurrentPassword(),
                    form.getNewPassword(),
                    form.getConfirmPassword());
            Usuario refreshed = usuarioService.buscarPorId(usuario.getId());
            accessSessionService.refreshSession(session, refreshed);
            redirectAttributes.addFlashAttribute("mensajeExito", "Contrasena actualizada correctamente.");
            return "redirect:" + accessSessionService.getCurrentProfile(session).entryPoint();
        } catch (BusinessValidationException ex) {
            model.addAttribute("mustChangePassword", Boolean.TRUE.equals(usuario.getMustChangePassword()));
            model.addAttribute("mensajeError", ex.getMessage());
            return "auth/password";
        }
    }
}
