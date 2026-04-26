package com.flacofitness.app.controller;

import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.ReservaSesion;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoReservaSesion;
import com.flacofitness.app.repository.ReservaSesionRepository;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.UsuarioControlCenterService;
import jakarta.servlet.http.HttpSession;
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

    private final AccessSessionService accessSessionService;
    private final UsuarioControlCenterService usuarioControlCenterService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final ReservaSesionRepository reservaSesionRepository;

    public ClientePortalController(AccessSessionService accessSessionService,
                                   UsuarioControlCenterService usuarioControlCenterService,
                                   PagoService pagoService,
                                   AsistenciaService asistenciaService,
                                   ReservaSesionRepository reservaSesionRepository) {
        this.accessSessionService = accessSessionService;
        this.usuarioControlCenterService = usuarioControlCenterService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
        this.reservaSesionRepository = reservaSesionRepository;
    }

    @GetMapping
    public String panel(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        List<ReservaSesion> reservasActivas = reservaSesionRepository
                .findByUsuarioIdOrderByFechaReservaDescIdDesc(usuario.getId())
                .stream()
                .filter(r -> r.getEstado() == EstadoReservaSesion.RESERVADA)
                .limit(10)
                .toList();
        model.addAttribute("usuario", usuario);
        model.addAttribute("controlCenter", usuarioControlCenterService.construirVista(usuario));
        model.addAttribute("deudaTotalUsuario", pagoService.calcularDeudaTotalPorUsuario(usuario.getId()));
        model.addAttribute("historialAsistencias", asistenciaService.listarRecientesPorUsuario(usuario.getId()));
        model.addAttribute("reservasActivas", reservasActivas);
        return "cliente/panel";
    }

    @PostMapping("/checkin")
    public String checkIn(RedirectAttributes redirectAttributes, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        try {
            asistenciaService.registrarCheckInRapido(List.of(usuario.getId()), "Check-in manual desde portal cliente");
            redirectAttributes.addFlashAttribute("mensajeExito", "Check-in registrado correctamente. ¡Buen entrenamiento!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo registrar el check-in: " + e.getMessage());
        }
        return "redirect:/cliente";
    }

    private Usuario obtenerUsuarioAutenticado(HttpSession session) {
        return accessSessionService.getCurrentUser(session)
                .orElseThrow(() -> new ResourceNotFoundException("No hay un usuario cliente autenticado para el panel"));
    }
}
