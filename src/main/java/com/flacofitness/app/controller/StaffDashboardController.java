package com.flacofitness.app.controller;

import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Nomina;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

    private final AccessSessionService accessSessionService;
    private final StaffPerfilRepository staffPerfilRepository;
    private final SesionClaseService sesionClaseService;
    private final AsistenciaService asistenciaService;
    private final UsuarioService usuarioService;
    private final NominaService nominaService;
    private final ClaseService claseService;
    private final OperationalClockService operationalClockService;
    private final FinancePdfService financePdfService;

    public StaffDashboardController(AccessSessionService accessSessionService,
                                   StaffPerfilRepository staffPerfilRepository,
                                   SesionClaseService sesionClaseService,
                                   AsistenciaService asistenciaService,
                                   UsuarioService usuarioService,
                                   NominaService nominaService,
                                   ClaseService claseService,
                                   OperationalClockService operationalClockService,
                                   FinancePdfService financePdfService) {
        this.accessSessionService = accessSessionService;
        this.staffPerfilRepository = staffPerfilRepository;
        this.sesionClaseService = sesionClaseService;
        this.asistenciaService = asistenciaService;
        this.usuarioService = usuarioService;
        this.nominaService = nominaService;
        this.claseService = claseService;
        this.operationalClockService = operationalClockService;
        this.financePdfService = financePdfService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Usuario staffUser = obtenerStaffAutenticado(session);
        StaffPerfil staffPerfil = staffPerfilRepository.findByUsuarioId(staffUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No hay perfil staff para este usuario"));

        RolStaff rolStaff = staffPerfil.getRolStaff();
        LocalDate hoy = operationalClockService.today();

        // Datos comunes
        model.addAttribute("staffPerfil", staffPerfil);
        model.addAttribute("usuario", staffUser);
        model.addAttribute("hoy", hoy);

        // Datos específicos por rol
        if (rolStaff == RolStaff.ENTRENADOR) {
            return dashboardEntrenador(staffPerfil, hoy, model);
        } else if (rolStaff == RolStaff.RECEPCION) {
            return dashboardRecepcion(staffPerfil, hoy, model);
        } else if (rolStaff == RolStaff.GERENTE) {
            return dashboardGerente(staffPerfil, hoy, model);
        }

        throw new ResourceNotFoundException("Rol staff desconocido: " + rolStaff);
    }

    @GetMapping("/nominas")
    public String nominasPropias(Model model, HttpSession session) {
        StaffPerfil staffPerfil = obtenerStaffPerfilAutenticado(session);
        List<Nomina> nominas = nominaService.listarFiltradas(staffPerfil.getId());
        model.addAttribute("staffPerfil", staffPerfil);
        model.addAttribute("nominas", nominas);
        return "staff/nominas";
    }

    @GetMapping("/nominas/{id}")
    public String detalleNominaPropia(@PathVariable Long id, Model model, HttpSession session) {
        StaffPerfil staffPerfil = obtenerStaffPerfilAutenticado(session);
        Nomina nomina = nominaService.buscarPorId(id);
        validarNominaPropia(nomina, staffPerfil);
        model.addAttribute("staffPerfil", staffPerfil);
        model.addAttribute("nomina", nomina);
        return "staff/nomina-detail";
    }

    @GetMapping(value = "/nominas/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfNominaPropia(@PathVariable Long id, HttpSession session) {
        StaffPerfil staffPerfil = obtenerStaffPerfilAutenticado(session);
        Nomina nomina = nominaService.buscarPorId(id);
        validarNominaPropia(nomina, staffPerfil);
        Map<String, Object> model = new HashMap<>();
        model.put("titulo", "Nomina " + nomina.getPeriodo());
        model.put("subtitulo", "Detalle individual para consulta del staff.");
        model.put("nomina", nomina);
        byte[] pdf = financePdfService.render("reportes/nomina-detalle", model);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nomina-" + nomina.getReferencia() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String dashboardEntrenador(StaffPerfil staffPerfil, LocalDate hoy, Model model) {
        // Sesiones del día donde este usuario es responsable
        var sesionesHoy = sesionClaseService.listarPorFecha(hoy)
                .stream()
                .filter(s -> s.getStaffResponsable() != null && s.getStaffResponsable().getId().equals(staffPerfil.getId()))
                .toList();

        // Próximas sesiones (próximos 7 días)
        var proximasSesiones = sesionClaseService.listarProximas()
                .stream()
                .filter(s -> s.getStaffResponsable() != null && s.getStaffResponsable().getId().equals(staffPerfil.getId()))
                .limit(5)
                .toList();

        // Asistencias de las sesiones del día
        long asistenciasHoy = asistenciaService.contarPorFecha(hoy);

        // Nóminas propias (últimas 3)
        var nominasPropias = nominaService.listarFiltradas(staffPerfil.getId())
                .stream()
                .limit(3)
                .toList();

        model.addAttribute("sesionesHoy", sesionesHoy);
        model.addAttribute("proximasSesiones", proximasSesiones);
        model.addAttribute("asistenciasHoy", asistenciasHoy);
        model.addAttribute("nominasPropias", nominasPropias);
        model.addAttribute("puedeImpartir", staffPerfil.getPuedeImpartirClases());

        return "staff/dashboard-entrenador";
    }

    private String dashboardRecepcion(StaffPerfil staffPerfil, LocalDate hoy, Model model) {
        // Sesiones del día (cualquier responsable, para saber horario)
        var sesionesHoy = sesionClaseService.listarPorFecha(hoy);

        // Asistencias registradas hoy
        var asistenciasHoy = asistenciaService.listarPorFecha(hoy);

        // Usuarios activos (para buscar/check-in)
        var usuariosActivos = usuarioService.listarActivos();

        // Nuevos clientes (últimos trials no convertidos)
        var trialsRecientes = usuarioService.contarRenovacionesProximas(1);

        model.addAttribute("sesionesHoy", sesionesHoy);
        model.addAttribute("asistenciasHoy", asistenciasHoy);
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("trialsRecientes", trialsRecientes);

        return "staff/dashboard-recepcion";
    }

    private String dashboardGerente(StaffPerfil staffPerfil, LocalDate hoy, Model model) {
        // Resumen operativo: usuarios activos, asistencias hoy, clases hoy
        long usuariosActivos = usuarioService.contarActivos();
        long asistenciasHoy = asistenciaService.contarPorFecha(hoy);
        var clasesHoy = claseService.listarTodas();
        long sesionesHoy = sesionClaseService.listarPorFecha(hoy).stream().count();

        var staffActivo = staffPerfilRepository.findByActivoTrue()
                .stream()
                .limit(10)
                .toList();

        // Ultimos pagos (para alertas)
        var ultimosPagos = usuarioService.listarRenovacionesProximas()
                .stream()
                .limit(5)
                .toList();

        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("asistenciasHoy", asistenciasHoy);
        model.addAttribute("clasesHoy", clasesHoy);
        model.addAttribute("sesionesHoy", sesionesHoy);
        model.addAttribute("staffActivo", staffActivo);
        model.addAttribute("ultimosPagos", ultimosPagos);

        return "staff/dashboard-gerente";
    }

    private Usuario obtenerStaffAutenticado(HttpSession session) {
        return accessSessionService.getCurrentUser(session)
                .orElseThrow(() -> new ResourceNotFoundException("No hay un usuario staff autenticado"));
    }

    private StaffPerfil obtenerStaffPerfilAutenticado(HttpSession session) {
        Usuario staffUser = obtenerStaffAutenticado(session);
        return staffPerfilRepository.findByUsuarioId(staffUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No hay perfil staff para este usuario"));
    }

    private void validarNominaPropia(Nomina nomina, StaffPerfil staffPerfil) {
        if (nomina.getStaffPerfil() == null || !Objects.equals(nomina.getStaffPerfil().getId(), staffPerfil.getId())) {
            throw new ResourceNotFoundException("Nomina no disponible para este staff");
        }
    }
}

