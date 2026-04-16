package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.dto.AsistenciaCheckInBatchResult;
import com.flacofitness.app.model.entity.Clase;
import com.flacofitness.app.model.entity.Rutina;
import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.enums.EstadoSesion;
import com.flacofitness.app.service.ClaseService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.SesionClaseService;
import com.flacofitness.app.service.StaffService;
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
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/sesiones")
public class SesionClaseController {

    private final SesionClaseService sesionClaseService;
    private final ClaseService claseService;
    private final StaffService staffService;
    private final RutinaService rutinaService;
    private final UsuarioService usuarioService;

    public SesionClaseController(SesionClaseService sesionClaseService,
                                 ClaseService claseService,
                                 StaffService staffService,
                                 RutinaService rutinaService,
                                 UsuarioService usuarioService) {
        this.sesionClaseService = sesionClaseService;
        this.claseService = claseService;
        this.staffService = staffService;
        this.rutinaService = rutinaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "fecha", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         Model model) {
        model.addAttribute("sesiones", fecha != null ? sesionClaseService.listarPorFecha(fecha) : sesionClaseService.listarTodas());
        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("sesionesHoy", sesionClaseService.contarSesionesHoy());
        model.addAttribute("sesionesProgramadas", sesionClaseService.contarProgramadas());
        return "sesiones/list";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        SesionClase sesionClase = new SesionClase();
        sesionClase.setClase(new Clase());
        sesionClase.setStaffResponsable(new StaffPerfil());
        sesionClase.setRutina(new Rutina());
        sesionClase.setFecha(LocalDate.now());
        sesionClase.setHoraInicio(LocalTime.now().withSecond(0).withNano(0));
        sesionClase.setAforo(12);
        sesionClase.setEstado(EstadoSesion.PROGRAMADA);
        cargarCatalogos(model);
        model.addAttribute("sesionClase", sesionClase);
        model.addAttribute("modoEdicion", false);
        return "sesiones/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("sesionClase") SesionClase sesionClase,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        normalizarRelaciones(sesionClase);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(sesionClase);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "sesiones/form";
        }

        try {
            sesionClaseService.guardar(sesionClase);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("sesionError", ex.getMessage());
            prepararRelaciones(sesionClase);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "sesiones/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Sesion creada correctamente.");
        return "redirect:/sesiones";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        SesionClase sesionClase = sesionClaseService.buscarPorId(id);
        model.addAttribute("sesionClase", sesionClase);
        model.addAttribute("reservas", sesionClaseService.listarReservas(id));
        model.addAttribute("asistencias", sesionClaseService.listarAsistencias(id));
        model.addAttribute("usuarios", usuarioService.listarActivos());
        model.addAttribute("reservasActivas", sesionClaseService.contarReservasActivas(id));
        return "sesiones/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        SesionClase sesionClase = sesionClaseService.buscarPorId(id);
        prepararRelaciones(sesionClase);
        cargarCatalogos(model);
        model.addAttribute("sesionClase", sesionClase);
        model.addAttribute("modoEdicion", true);
        return "sesiones/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("sesionClase") SesionClase sesionClase,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        normalizarRelaciones(sesionClase);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(sesionClase);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "sesiones/form";
        }

        try {
            sesionClaseService.actualizar(id, sesionClase);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("sesionError", ex.getMessage());
            prepararRelaciones(sesionClase);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "sesiones/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Sesion actualizada correctamente.");
        return "redirect:/sesiones";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        sesionClaseService.cancelar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Sesion cancelada.");
        return "redirect:/sesiones/" + id;
    }

    @PostMapping("/{id}/reservas")
    public String reservar(@PathVariable Long id,
                           @RequestParam(name = "usuarioIds", required = false) List<Long> usuarioIds,
                           RedirectAttributes redirectAttributes) {
        try {
            int creadas = 0;
            if (usuarioIds != null) {
                for (Long usuarioId : usuarioIds) {
                    if (usuarioId != null) {
                        sesionClaseService.reservarUsuario(id, usuarioId);
                        creadas++;
                    }
                }
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Reservas procesadas: " + creadas + ".");
        } catch (BusinessValidationException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/sesiones/" + id;
    }

    @PostMapping("/{id}/reservas/{usuarioId}/quitar")
    public String quitarReserva(@PathVariable Long id,
                                @PathVariable Long usuarioId,
                                RedirectAttributes redirectAttributes) {
        sesionClaseService.quitarReserva(id, usuarioId);
        redirectAttributes.addFlashAttribute("mensajeExito", "Reserva cancelada.");
        return "redirect:/sesiones/" + id;
    }

    @PostMapping("/{id}/asistencias")
    public String registrarAsistencia(@PathVariable Long id,
                                      @RequestParam(name = "usuarioIds", required = false) List<Long> usuarioIds,
                                      @RequestParam(name = "observaciones", required = false) String observaciones,
                                      RedirectAttributes redirectAttributes) {
        try {
            AsistenciaCheckInBatchResult resultado = sesionClaseService.registrarAsistenciaSesion(id, usuarioIds, observaciones);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Asistencias registradas: " + resultado.registrosCreados() +
                            ". Omitidas: " + resultado.registrosOmitidos() + ".");
        } catch (BusinessValidationException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/sesiones/" + id;
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("clases", claseService.listarActivas());
        model.addAttribute("staffActivos", staffService.listarActivosParaEntrenamiento());
        model.addAttribute("rutinas", rutinaService.listarTodas());
        model.addAttribute("estadosSesion", EstadoSesion.values());
    }

    private void prepararRelaciones(SesionClase sesionClase) {
        if (sesionClase.getClase() == null) {
            sesionClase.setClase(new Clase());
        }
        if (sesionClase.getStaffResponsable() == null) {
            sesionClase.setStaffResponsable(new StaffPerfil());
        }
        if (sesionClase.getRutina() == null) {
            sesionClase.setRutina(new Rutina());
        }
    }

    private void normalizarRelaciones(SesionClase sesionClase) {
        if (sesionClase.getStaffResponsable() != null && sesionClase.getStaffResponsable().getId() == null) {
            sesionClase.setStaffResponsable(null);
        }
        if (sesionClase.getRutina() != null && sesionClase.getRutina().getId() == null) {
            sesionClase.setRutina(null);
        }
    }
}
