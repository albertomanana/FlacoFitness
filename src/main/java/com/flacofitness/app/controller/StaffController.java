package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.service.SesionClaseService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;
    private final UsuarioService usuarioService;
    private final SesionClaseService sesionClaseService;

    public StaffController(StaffService staffService,
                           UsuarioService usuarioService,
                           SesionClaseService sesionClaseService) {
        this.staffService = staffService;
        this.usuarioService = usuarioService;
        this.sesionClaseService = sesionClaseService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("staff", staffService.listarTodos());
        return "staff/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        StaffPerfil staffPerfil = new StaffPerfil();
        staffPerfil.setUsuario(new Usuario());
        staffPerfil.setActivo(true);
        cargarCatalogos(model);
        model.addAttribute("staffPerfil", staffPerfil);
        model.addAttribute("modoEdicion", false);
        return "staff/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("staffPerfil") StaffPerfil staffPerfil,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(staffPerfil);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "staff/form";
        }

        try {
            staffService.guardar(staffPerfil);
        } catch (DuplicateResourceException | BusinessValidationException ex) {
            bindingResult.reject("staffError", ex.getMessage());
            prepararRelaciones(staffPerfil);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "staff/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Perfil de staff creado correctamente.");
        return "redirect:/staff";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("staffPerfil", staffService.buscarPorId(id));
        model.addAttribute("sesionesProximas", sesionClaseService.listarProximas());
        return "staff/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        StaffPerfil staffPerfil = staffService.buscarPorId(id);
        prepararRelaciones(staffPerfil);
        cargarCatalogos(model);
        model.addAttribute("staffPerfil", staffPerfil);
        model.addAttribute("modoEdicion", true);
        return "staff/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("staffPerfil") StaffPerfil staffPerfil,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(staffPerfil);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "staff/form";
        }

        try {
            staffService.actualizar(id, staffPerfil);
        } catch (DuplicateResourceException | BusinessValidationException ex) {
            bindingResult.reject("staffError", ex.getMessage());
            prepararRelaciones(staffPerfil);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "staff/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Perfil de staff actualizado correctamente.");
        return "redirect:/staff";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        staffService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Perfil de staff desactivado.");
        return "redirect:/staff";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        staffService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Perfil de staff activado.");
        return "redirect:/staff";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("rolesStaff", RolStaff.values());
    }

    private void prepararRelaciones(StaffPerfil staffPerfil) {
        if (staffPerfil.getUsuario() == null) {
            staffPerfil.setUsuario(new Usuario());
        }
    }
}
