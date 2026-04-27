package com.flacofitness.app.controller;

import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.*;
import com.flacofitness.app.model.enums.EstadoReservaSesion;
import com.flacofitness.app.repository.ReservaSesionRepository;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/cliente")
public class ClientePortalController {

    private final AccessSessionService accessSessionService;
    private final UsuarioControlCenterService usuarioControlCenterService;
    private final PagoService pagoService;
    private final AsistenciaService asistenciaService;
    private final ReservaSesionRepository reservaSesionRepository;
    private final RutinaService rutinaService;
    private final SesionClaseService sesionClaseService;
    private final MembresiaService membresiaService;
    private final OperationalClockService operationalClockService;

    public ClientePortalController(AccessSessionService accessSessionService,
                                   UsuarioControlCenterService usuarioControlCenterService,
                                   PagoService pagoService,
                                   AsistenciaService asistenciaService,
                                   ReservaSesionRepository reservaSesionRepository,
                                   RutinaService rutinaService,
                                   SesionClaseService sesionClaseService,
                                   MembresiaService membresiaService,
                                   OperationalClockService operationalClockService) {
        this.accessSessionService = accessSessionService;
        this.usuarioControlCenterService = usuarioControlCenterService;
        this.pagoService = pagoService;
        this.asistenciaService = asistenciaService;
        this.reservaSesionRepository = reservaSesionRepository;
        this.rutinaService = rutinaService;
        this.sesionClaseService = sesionClaseService;
        this.membresiaService = membresiaService;
        this.operationalClockService = operationalClockService;
    }

    @GetMapping
    public String panel(Model model, HttpSession session) {
        return cargarPanel(model, session);
    }

    @GetMapping("/dashboard")
    public String panelDashboard(Model model, HttpSession session) {
        return cargarPanel(model, session);
    }

    private String cargarPanel(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        LocalDate hoy = operationalClockService.today();

        // Datos personales y control center
        model.addAttribute("usuario", usuario);
        model.addAttribute("controlCenter", usuarioControlCenterService.construirVista(usuario));

        // Membresía: buscar MembresiaUsuario activa o usar plan legacy
        MembresiaUsuario membresiaActiva = membresiaService.buscarContratoActivoPorUsuario(usuario.getId())
                .orElse(null);
        model.addAttribute("membresiaActiva", membresiaActiva);

        // Pagos propios: próximos, vencidos, pagados
        List<Pago> pagosRecientes = pagoService.listarPorUsuario(usuario.getId())
                .stream()
                .sorted(Comparator.comparing(Pago::getFechaPago, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
        model.addAttribute("pagosRecientes", pagosRecientes);
        model.addAttribute("deudaTotal", pagoService.calcularDeudaTotalPorUsuario(usuario.getId()));

        // Rutinas asignadas
        List<Rutina> rutinasAsignadas = rutinaService.listarPorUsuario(usuario.getId())
                .stream()
                .sorted(Comparator.comparing(Rutina::getActiva).reversed())
                .limit(4)
                .toList();
        model.addAttribute("rutinasAsignadas", rutinasAsignadas);

        // Clases/sesiones próximas en las que podría inscribirse
        List<SesionClase> sesionesProximas = sesionClaseService.listarPorFecha(hoy)
                .stream()
                .filter(s -> s.getFecha().isAfter(hoy) || s.getFecha().isEqual(hoy))
                .limit(5)
                .toList();
        model.addAttribute("sesionesProximas", sesionesProximas);

        // Reservas activas
        List<ReservaSesion> reservasActivas = reservaSesionRepository
                .findByUsuarioIdOrderByFechaReservaDescIdDesc(usuario.getId())
                .stream()
                .filter(r -> r.getEstado() == EstadoReservaSesion.RESERVADA)
                .limit(5)
                .toList();
        model.addAttribute("reservasActivas", reservasActivas);

        // Asistencias recientes
        List<Asistencia> asistenciasRecientes = asistenciaService.listarRecientesPorUsuario(usuario.getId())
                .stream()
                .limit(3)
                .toList();
        model.addAttribute("asistenciasRecientes", asistenciasRecientes);
        model.addAttribute("totalAsistencias", asistenciaService.contarPorUsuario(usuario.getId()));

        return "cliente/panel";
    }

    @GetMapping("/rutinas")
    public String misRutinas(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        List<Rutina> rutinas = rutinaService.listarPorUsuario(usuario.getId())
                .stream()
                .sorted(Comparator.comparing(Rutina::getActiva).reversed()
                        .thenComparing(Rutina::getNombre))
                .toList();
        model.addAttribute("usuario", usuario);
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("tituloListado", "Mis rutinas");
        model.addAttribute("subtituloListado", "Entrenamiento asignado.");
        return "cliente/rutinas";
    }

    @GetMapping("/rutinas/{id}")
    public String detalleRutinaPropia(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        Rutina rutina = rutinaService.buscarPorId(id);
        boolean asignada = rutina.getUsuarios().stream().anyMatch(u -> Objects.equals(u.getId(), usuario.getId()));
        if (!asignada) {
            throw new ResourceNotFoundException("Rutina no disponible para este cliente");
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("rutina", rutina);
        return "cliente/rutina-detail";
    }

    @GetMapping("/clases")
    public String clasesDisponibles(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        LocalDate hoy = operationalClockService.today();
        List<SesionClase> sesionesProximas = sesionClaseService.listarPorFecha(hoy)
                .stream()
                .filter(s -> s.getFecha().isAfter(hoy.minusDays(1)))
                .sorted(Comparator.comparing(SesionClase::getFecha)
                        .thenComparing(SesionClase::getHoraInicio))
                .toList();
        model.addAttribute("usuario", usuario);
        model.addAttribute("sesionesProximas", sesionesProximas);
        model.addAttribute("tituloListado", "Clases disponibles");
        model.addAttribute("subtituloListado", "Próximas sesiones para inscribirse.");
        return "cliente/clases";
    }

    @GetMapping("/membresia")
    public String miMembresia(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        MembresiaUsuario membresia = membresiaService.buscarContratoActivoPorUsuario(usuario.getId())
                .orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("membresia", membresia);
        model.addAttribute("tituloListado", "Mi membresía");
        model.addAttribute("subtituloListado", "Estado de suscripción y renovación.");
        return "cliente/membresia";
    }

    @GetMapping("/pagos")
    public String misPagos(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        List<Pago> pagos = pagoService.listarPorUsuario(usuario.getId())
                .stream()
                .sorted(Comparator.comparing(Pago::getFechaPago, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("usuario", usuario);
        model.addAttribute("pagos", pagos);
        model.addAttribute("deudaTotal", pagoService.calcularDeudaTotalPorUsuario(usuario.getId()));
        model.addAttribute("tituloListado", "Mis pagos");
        model.addAttribute("subtituloListado", "Historial de transacciones y deuda.");
        return "cliente/pagos";
    }

    @GetMapping("/pagos/{id}")
    public String detallePagoPropio(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        Pago pago = pagoService.buscarPorId(id);
        if (pago.getUsuario() == null || !Objects.equals(pago.getUsuario().getId(), usuario.getId())) {
            throw new ResourceNotFoundException("Pago no disponible para este cliente");
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("pago", pago);
        return "cliente/pago-detail";
    }

    @GetMapping("/asistencias")
    public String misAsistencias(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        List<Asistencia> asistencias = asistenciaService.listarPorUsuario(usuario.getId())
                .stream()
                .sorted(Comparator.comparing(Asistencia::getFecha, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("usuario", usuario);
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("totalAsistencias", asistencias.size());
        model.addAttribute("tituloListado", "Mis asistencias");
        model.addAttribute("subtituloListado", "Historial de check-in y participación.");
        return "cliente/asistencias";
    }

    @PostMapping("/checkin")
    public String checkIn(RedirectAttributes redirectAttributes, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        try {
            asistenciaService.registrarCheckInRapido(List.of(usuario.getId()), "Check-in manual desde portal cliente");
            redirectAttributes.addFlashAttribute("mensajeExito", "Check-in registrado correctamente. Buen entrenamiento.");
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

