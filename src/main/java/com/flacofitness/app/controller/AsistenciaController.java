package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.AsistenciaCheckInBatchResult;
import com.flacofitness.app.model.entity.Asistencia;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final UsuarioService usuarioService;

    public AsistenciaController(AsistenciaService asistenciaService,
                                UsuarioService usuarioService) {
        this.asistenciaService = asistenciaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarAsistencias(@RequestParam(name = "fecha", required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                    @RequestParam(name = "usuarioId", required = false) Long usuarioId,
                                    @RequestParam(name = "mes", required = false) String mes,
                                    Model model) {
        return renderListado(fecha, usuarioId, mes, model);
    }

    @PostMapping("/rapido")
    public String checkInRapido(@RequestParam(name = "usuarioIds", required = false) List<String> usuarioIdsRaw,
                                @RequestParam(name = "usuarioId", required = false) Long usuarioId,
                                @RequestParam(name = "observaciones", required = false) String observaciones,
                                RedirectAttributes redirectAttributes) {
        try {
            List<Long> ids = parsearUsuarioIds(usuarioIdsRaw);
            if (usuarioId != null) {
                ids.add(usuarioId);
            }

            AsistenciaCheckInBatchResult resultado = asistenciaService.registrarCheckInRapido(ids, observaciones);

            if (resultado.registrosCreados() > 0 && resultado.registrosOmitidos() > 0) {
                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "Check-in registrado para " + resultado.registrosCreados() +
                                " usuario(s). " + resultado.registrosOmitidos() +
                                " ya tenia(n) entrada hoy o no estaba(n) disponible(s).");
            } else if (resultado.registrosCreados() > 0) {
                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "Check-in registrado para " + resultado.registrosCreados() + " usuario(s).");
            } else {
                redirectAttributes.addFlashAttribute(
                        "mensajeError",
                        "No se creo ningun check-in nuevo. Los usuarios seleccionados ya tenian entrada hoy.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar el check-in: " + ex.getMessage());
        }

        return "redirect:/asistencias";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(new Usuario());
        asistencia.setHoraEntrada(java.time.LocalTime.now().withSecond(0).withNano(0));
        model.addAttribute("asistencia", asistencia);
        model.addAttribute("usuarios", usuarioService.listarActivos());
        return "asistencias/form";
    }

    @PostMapping
    public String guardarAsistencia(@Valid @ModelAttribute("asistencia") Asistencia asistencia,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        validarUsuarioSeleccionado(asistencia, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(asistencia);
            model.addAttribute("usuarios", usuarioService.listarActivos());
            return "asistencias/form";
        }

        asistenciaService.registrar(asistencia);
        redirectAttributes.addFlashAttribute("mensajeExito", "Asistencia registrada correctamente.");
        return "redirect:/asistencias";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("asistencia", asistenciaService.buscarPorId(id));
        return "asistencias/detail";
    }

    @GetMapping("/usuario/{usuarioId}")
    public String listarPorUsuario(@PathVariable Long usuarioId,
                                   @RequestParam(name = "fecha", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                   @RequestParam(name = "mes", required = false) String mes,
                                   Model model) {
        return renderListado(fecha, usuarioId, mes, model);
    }

    private String renderListado(LocalDate fecha, Long usuarioId, String mes, Model model) {
        Usuario usuarioFiltro = usuarioId != null ? usuarioService.buscarPorId(usuarioId) : null;
        YearMonth mesSeleccionado = resolverMes(mes, fecha);
        List<Asistencia> asistencias = obtenerAsistenciasFiltradas(fecha, usuarioId);

        model.addAttribute("asistencias", asistencias);
        model.addAttribute("usuariosCheckIn", usuarioService.listarActivos());
        model.addAttribute("usuariosFiltro", usuarioService.listarTodos());
        model.addAttribute("usuarioFiltroId", usuarioId);
        model.addAttribute("usuarioFiltro", usuarioFiltro);
        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("mesFiltro", mesSeleccionado.toString());
        model.addAttribute("calendarView", asistenciaService.construirCalendarioMensual(mesSeleccionado, fecha));
        model.addAttribute("checkInsHoy", asistenciaService.contarHoy());
        model.addAttribute("usuariosAsistenciaActivos", asistenciaService.contarUsuariosActivos());
        model.addAttribute("usuariosAsistenciaInactivos", asistenciaService.contarUsuariosInactivos());
        model.addAttribute("tituloListado", construirTituloListado(fecha, usuarioFiltro));
        model.addAttribute("subtituloListado", construirSubtituloListado(fecha, usuarioFiltro));
        return "asistencias/list";
    }

    private List<Asistencia> obtenerAsistenciasFiltradas(LocalDate fecha, Long usuarioId) {
        if (fecha != null && usuarioId != null) {
            return asistenciaService.listarPorFechaYUsuario(fecha, usuarioId);
        }

        if (fecha != null) {
            return asistenciaService.listarPorFecha(fecha);
        }

        if (usuarioId != null) {
            return asistenciaService.listarPorUsuario(usuarioId);
        }

        return asistenciaService.listarTodas();
    }

    private YearMonth resolverMes(String mes, LocalDate fecha) {
        if (mes != null && !mes.isBlank()) {
            try {
                return YearMonth.parse(mes);
            } catch (DateTimeParseException ignored) {
                // Se usa el fallback por fecha o mes actual.
            }
        }

        if (fecha != null) {
            return YearMonth.from(fecha);
        }

        return YearMonth.now();
    }

    private String construirTituloListado(LocalDate fecha, Usuario usuarioFiltro) {
        if (fecha != null && usuarioFiltro != null) {
            return "Asistencias de " + construirNombreUsuario(usuarioFiltro) + " el " + fecha;
        }

        if (fecha != null) {
            return "Asistencias del " + fecha;
        }

        if (usuarioFiltro != null) {
            return "Asistencias de " + construirNombreUsuario(usuarioFiltro);
        }

        return "Asistencias";
    }

    private String construirSubtituloListado(LocalDate fecha, Usuario usuarioFiltro) {
        if (fecha != null && usuarioFiltro != null) {
            return "Detalle filtrado por dia y usuario.";
        }

        if (fecha != null) {
            return "Registro diario filtrado desde el calendario operativo.";
        }

        if (usuarioFiltro != null) {
            return "Historial completo del usuario dentro del control de accesos.";
        }

        return "Registro general de asistencias del gimnasio.";
    }

    private void prepararRelaciones(Asistencia asistencia) {
        if (asistencia.getUsuario() == null) {
            asistencia.setUsuario(new Usuario());
        }
    }

    private void validarUsuarioSeleccionado(Asistencia asistencia, BindingResult bindingResult) {
        if (asistencia.getUsuario() == null || asistencia.getUsuario().getId() == null) {
            bindingResult.rejectValue("usuario.id", "required", "Debes seleccionar un usuario.");
        }
    }

    private String construirNombreUsuario(Usuario usuario) {
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            return usuario.getNombre();
        }
        return usuario.getNombre() + " " + usuario.getApellidos();
    }

    private List<Long> parsearUsuarioIds(List<String> usuarioIdsRaw) {
        List<Long> ids = new ArrayList<>();
        if (usuarioIdsRaw == null) {
            return ids;
        }

        for (String valor : usuarioIdsRaw) {
            if (valor == null || valor.isBlank()) {
                continue;
            }

            String[] tokens = valor.split("[^0-9]+");
            for (String token : tokens) {
                if (token == null || token.isBlank()) {
                    continue;
                }
                ids.add(Long.parseLong(token));
            }
        }

        return ids;
    }
}
