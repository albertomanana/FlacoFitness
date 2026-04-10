package com.flacofitness.app.controller;

import com.flacofitness.app.model.entity.Rutina;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.ObjetivoRutina;
import com.flacofitness.app.model.enums.TipoRutina;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        rutina.setTipoRutina(TipoRutina.GENERAL);
        prepararRelaciones(rutina);
        cargarCatalogos(model);
        model.addAttribute("rutina", rutina);
        model.addAttribute("usuariosSeleccionados", obtenerUsuariosSeleccionados(rutina));
        model.addAttribute("modoEdicion", false);
        return "rutinas/form";
    }

    @PostMapping
    public String guardarRutina(@Valid @ModelAttribute("rutina") Rutina rutina,
                                BindingResult bindingResult,
                                @RequestParam(name = "usuarioIds", required = false) List<Long> usuarioIds,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        aplicarUsuariosSeleccionados(rutina, usuarioIds);
        validarUsuariosSeleccionados(rutina, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(rutina);
            cargarCatalogos(model);
            model.addAttribute("usuariosSeleccionados", obtenerUsuariosSeleccionados(rutina));
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
        model.addAttribute("usuariosSeleccionados", obtenerUsuariosSeleccionados(rutina));
        model.addAttribute("modoEdicion", true);
        return "rutinas/form";
    }

    @PostMapping("/{id}")
    public String actualizarRutina(@PathVariable Long id,
                                   @Valid @ModelAttribute("rutina") Rutina rutina,
                                   BindingResult bindingResult,
                                   @RequestParam(name = "usuarioIds", required = false) List<Long> usuarioIds,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        aplicarUsuariosSeleccionados(rutina, usuarioIds);
        validarUsuariosSeleccionados(rutina, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(rutina);
            cargarCatalogos(model);
            model.addAttribute("usuariosSeleccionados", obtenerUsuariosSeleccionados(rutina));
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

    @PostMapping("/{id}/activar")
    public String activarRutina(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rutinaService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Rutina activada correctamente.");
        return "redirect:/rutinas";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("usuarios", usuarioService.listarActivos());
        model.addAttribute("objetivos", ObjetivoRutina.values());
        model.addAttribute("tiposRutina", TipoRutina.values());
    }

    private void prepararRelaciones(Rutina rutina) {
        if (rutina.getUsuarios() == null) {
            rutina.setUsuarios(new LinkedHashSet<>());
        }
    }

    private void aplicarUsuariosSeleccionados(Rutina rutina, List<Long> usuarioIds) {
        LinkedHashSet<Usuario> usuarios = new LinkedHashSet<>();

        if (usuarioIds != null) {
            for (Long usuarioId : usuarioIds) {
                if (usuarioId == null) {
                    continue;
                }

                Usuario usuario = new Usuario();
                usuario.setId(usuarioId);
                usuarios.add(usuario);
            }
        }

        rutina.setUsuarios(usuarios);
    }

    private void validarUsuariosSeleccionados(Rutina rutina, BindingResult bindingResult) {
        if (rutina.getUsuarios() == null || rutina.getUsuarios().isEmpty()) {
            bindingResult.rejectValue("usuarios", "required", "Debes seleccionar al menos un usuario.");
        }
    }

    private List<Long> obtenerUsuariosSeleccionados(Rutina rutina) {
        return rutina.getUsuarios().stream()
                .map(Usuario::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
