package com.flacofitness.app.controller;

import com.flacofitness.app.model.entity.Rutina;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.ObjetivoRutina;
import com.flacofitness.app.service.RutinaService;
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
@RequestMapping("/rutinas")
public class RutinaController {

    private final RutinaService rutinaService;
    private final UsuarioService usuarioService;

    public RutinaController(RutinaService rutinaService, UsuarioService usuarioService) {
        this.rutinaService = rutinaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarRutinas(Model model) {
        model.addAttribute("rutinas", rutinaService.listarTodas());
        return "rutinas/list";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        Rutina rutina = new Rutina();
        rutina.setActiva(true);
        prepararRelaciones(rutina);
        cargarCatalogos(model);
        model.addAttribute("rutina", rutina);
        model.addAttribute("modoEdicion", false);
        return "rutinas/form";
    }

    @PostMapping
    public String guardarRutina(@Valid @ModelAttribute("rutina") Rutina rutina,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        validarUsuarioSeleccionado(rutina, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(rutina);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "rutinas/form";
        }

        rutinaService.guardar(rutina);
        redirectAttributes.addFlashAttribute("mensajeExito", "Rutina creada correctamente.");
        return "redirect:/rutinas";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("rutina", rutinaService.buscarPorId(id));
        return "rutinas/detail";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Rutina rutina = rutinaService.buscarPorId(id);
        prepararRelaciones(rutina);
        cargarCatalogos(model);
        model.addAttribute("rutina", rutina);
        model.addAttribute("modoEdicion", true);
        return "rutinas/form";
    }

    @PostMapping("/{id}")
    public String actualizarRutina(@PathVariable Long id,
                                   @Valid @ModelAttribute("rutina") Rutina rutina,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        validarUsuarioSeleccionado(rutina, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(rutina);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "rutinas/form";
        }

        rutinaService.actualizar(id, rutina);
        redirectAttributes.addFlashAttribute("mensajeExito", "Rutina actualizada correctamente.");
        return "redirect:/rutinas";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivarRutina(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rutinaService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Rutina desactivada correctamente.");
        return "redirect:/rutinas";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("usuarios", usuarioService.listarActivos());
        model.addAttribute("objetivos", ObjetivoRutina.values());
    }

    private void prepararRelaciones(Rutina rutina) {
        if (rutina.getUsuario() == null) {
            rutina.setUsuario(new Usuario());
        }
    }

    private void validarUsuarioSeleccionado(Rutina rutina, BindingResult bindingResult) {
        if (rutina.getUsuario() == null || rutina.getUsuario().getId() == null) {
            bindingResult.rejectValue("usuario.id", "required", "Debes seleccionar un usuario.");
        }
    }
}
