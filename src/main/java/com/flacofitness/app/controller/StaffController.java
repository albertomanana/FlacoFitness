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
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;
    private final UsuarioService usuarioService;

    public StaffController(StaffService staffService,
                           UsuarioService usuarioService) {
        this.staffService = staffService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarStaff(Model model) {
        model.addAttribute("staff", staffService.listarTodos());
        model.addAttribute("staffList", staffService.listarTodos());
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
                               RedirectAttributes redirectAttributes) {
        normalizarRelaciones(staff);

        if (bindingResult.hasErrors()) {
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "staff/form";
        }

        try {
            staffService.guardar(staff);
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("staffError", ex.getMessage());
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "staff/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Staff creado correctamente.");
        return "redirect:/staff";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("staff", staffService.buscarPorId(id));
        model.addAttribute("sesionesHoy", staffService.listarSesionesHoy(id));
        model.addAttribute("miembrosAsignadosHoy", staffService.contarMiembrosAsignadosHoy(id));
        model.addAttribute("clientesInactivos", staffService.listarClientesInactivosAsignados(id, 14));
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
                                  RedirectAttributes redirectAttributes) {
        normalizarRelaciones(staff);

        if (bindingResult.hasErrors()) {
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "staff/form";
        }

        try {
            staffService.actualizar(id, staff);
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("staffError", ex.getMessage());
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "staff/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Staff actualizado correctamente.");
        return "redirect:/staff";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivarStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        staffService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Staff desactivado correctamente.");
        return "redirect:/staff";
    }

    @PostMapping("/{id}/activar")
    public String activarStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        staffService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Staff activado correctamente.");
        return "redirect:/staff";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("usuariosActivos", usuarioService.listarActivos());
        model.addAttribute("rolesStaff", RolStaff.values());
    }

    private void normalizarRelaciones(StaffPerfil staff) {
        if (staff.getUsuario() != null && staff.getUsuario().getId() == null) {
            staff.setUsuario(null);
        }
    }
}
