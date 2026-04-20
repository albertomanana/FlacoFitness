package com.flacofitness.app.controller;

import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.UsuarioRepository;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.UsuarioControlCenterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClientePortalController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioControlCenterService usuarioControlCenterService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;

    public ClientePortalController(UsuarioRepository usuarioRepository,
                                  UsuarioControlCenterService usuarioControlCenterService,
                                  PagoService pagoService,
                                  AsistenciaService asistenciaService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioControlCenterService = usuarioControlCenterService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    public String panel(Model model) {
        Usuario usuario = obtenerUsuarioDemo();
        model.addAttribute("usuario", usuario);
        model.addAttribute("controlCenter", usuarioControlCenterService.construirVista(usuario));
        model.addAttribute("deudaTotalUsuario", pagoService.calcularDeudaTotalPorUsuario(usuario.getId()));
        return "cliente/panel";
    }

    @PostMapping("/checkin")
    public String checkIn(RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioDemo();
        try {
            asistenciaService.registrarCheckInRapido(List.of(usuario.getId()), "Check-in manual desde portal cliente");
            redirectAttributes.addFlashAttribute("mensajeExito", "Check-in registrado correctamente. ¡Buen entrenamiento!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo registrar el check-in: " + e.getMessage());
        }
        return "redirect:/cliente";
    }

    private Usuario obtenerUsuarioDemo() {
        return usuarioRepository.findFirstByActivoTrueAndRolNombreOrderByIdAsc("CLIENTE")
                .or(() -> usuarioRepository.findByActivoTrue().stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("No hay un usuario cliente disponible para el panel"));
    }
}
