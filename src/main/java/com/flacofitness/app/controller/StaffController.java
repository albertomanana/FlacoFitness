package com.flacofitness.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.NominaService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.UsuarioService;
import com.flacofitness.app.service.ActivityLogService;
import com.flacofitness.app.service.ControllerActivityLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;
    private final UsuarioService usuarioService;
    private final GastoService gastoService;
    private final NominaService nominaService;
    private final ActivityLogService activityLogService;
    private final ControllerActivityLogger controllerActivityLogger;

    public StaffController(StaffService staffService,
                           UsuarioService usuarioService,
                           GastoService gastoService,
                           NominaService nominaService,
                           ActivityLogService activityLogService,
                           ControllerActivityLogger controllerActivityLogger) {
        this.staffService = staffService;
        this.usuarioService = usuarioService;
        this.gastoService = gastoService;
        this.nominaService = nominaService;
        this.activityLogService = activityLogService;
        this.controllerActivityLogger = controllerActivityLogger;
    }

    @GetMapping
    public String listarStaff(Model model) {
        var staff = staffService.listarTodos();
        model.addAttribute("staff", staff);
        model.addAttribute("staffList", staff);
        return "staff/list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        StaffPerfil staff = new StaffPerfil();
        staff.setActivo(true);
        staff.setPuedeImpartirClases(true);
        staff.setRolStaff(RolStaff.ENTRENADOR);
        staff.setUsuario(new Usuario());
        cargarCatalogos(model);
        model.addAttribute("staff", staff);
        model.addAttribute("modoEdicion", false);
        return "staff/form";
    }

    @PostMapping
    public String guardarStaff(@Valid @ModelAttribute("staff") StaffPerfil staff,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request,
                               HttpSession session) {
        normalizarRelaciones(staff);

        if (bindingResult.hasErrors()) {
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "staff/form";
        }

        try {
            StaffPerfil guardado = staffService.guardar(staff);
            controllerActivityLogger.log(request, session,
                    "staff", "staff_creado", "staff", guardado.getId(),
                    "Perfil de staff creado",
                    "Se creo el perfil interno del equipo.");
            redirectAttributes.addFlashAttribute("mensajeExito", "Staff creado correctamente.");
            return "redirect:/staff/" + guardado.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("staffError", ex.getMessage());
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "staff/form";
        }
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("staff", staffService.buscarPorId(id));
        model.addAttribute("sesionesHoy", staffService.listarSesionesHoy(id));
        model.addAttribute("miembrosAsignadosHoy", staffService.contarMiembrosAsignadosHoy(id));
        model.addAttribute("clientesInactivos", staffService.listarClientesInactivosAsignados(id, 14));
        model.addAttribute("gastosRelacionados", gastoService.listarPorStaff(id).stream().limit(6).toList());
        model.addAttribute("nominasRecientes", nominaService.listarFiltradas(id).stream().limit(6).toList());
        model.addAttribute("activityTimeline", activityLogService.recentByEntity("staff", id));
        return "staff/detail";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("staff", staffService.buscarPorId(id));
        cargarCatalogos(model);
        model.addAttribute("modoEdicion", true);
        return "staff/form";
    }

    @PostMapping("/{id}")
    public String actualizarStaff(@PathVariable Long id,
                                  @Valid @ModelAttribute("staff") StaffPerfil staff,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request,
                                  HttpSession session) {
        normalizarRelaciones(staff);

        if (bindingResult.hasErrors()) {
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "staff/form";
        }

        try {
            StaffPerfil actualizado = staffService.actualizar(id, staff);
            controllerActivityLogger.log(request, session,
                    "staff", "staff_actualizado", "staff", actualizado.getId(),
                    "Perfil de staff actualizado",
                    "Se actualizo la ficha interna del staff.");
            redirectAttributes.addFlashAttribute("mensajeExito", "Staff actualizado correctamente.");
            return "redirect:/staff/" + actualizado.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("staffError", ex.getMessage());
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "staff/form";
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivarStaff(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes,
                                  @org.springframework.web.bind.annotation.RequestParam(name = "returnTo", required = false) String returnTo,
                                  HttpServletRequest request,
                                  HttpSession session) {
        staffService.desactivar(id);
        controllerActivityLogger.log(request, session,
                "staff", "staff_desactivado", "staff", id,
                "Staff desactivado",
                "Se desactivo el perfil interno del staff.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Staff desactivado correctamente.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    @PostMapping("/{id}/activar")
    public String activarStaff(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               @org.springframework.web.bind.annotation.RequestParam(name = "returnTo", required = false) String returnTo,
                               HttpServletRequest request,
                               HttpSession session) {
        staffService.activar(id);
        controllerActivityLogger.log(request, session,
                "staff", "staff_activado", "staff", id,
                "Staff activado",
                "Se reactivo el perfil interno del staff.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Staff activado correctamente.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("usuariosActivos", usuarioService.listarTodos());
        model.addAttribute("rolesStaff", RolStaff.values());
    }

    private void normalizarRelaciones(StaffPerfil staff) {
        if (staff.getUsuario() != null && staff.getUsuario().getId() == null) {
            staff.setUsuario(null);
        }
    }

    private String resolveReturnPath(Long id, String returnTo) {
        if ("detail".equalsIgnoreCase(returnTo)) {
            return "/staff/" + id;
        }
        return "/staff";
    }
}
