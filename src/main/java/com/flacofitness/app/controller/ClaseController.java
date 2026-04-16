package com.flacofitness.app.controller;

import com.flacofitness.app.model.entity.Clase;
import com.flacofitness.app.service.ClaseService;
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
@RequestMapping("/clases")
public class ClaseController {

    private final ClaseService claseService;

    public ClaseController(ClaseService claseService) {
        this.claseService = claseService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clases", claseService.listarTodas());
        return "clases/list";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        Clase clase = new Clase();
        clase.setActiva(true);
        clase.setCapacidadSugerida(12);
        model.addAttribute("clase", clase);
        model.addAttribute("modoEdicion", false);
        return "clases/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("clase") Clase clase,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", false);
            return "clases/form";
        }

        claseService.guardar(clase);
        redirectAttributes.addFlashAttribute("mensajeExito", "Clase creada correctamente.");
        return "redirect:/clases";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("clase", claseService.buscarPorId(id));
        return "clases/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("clase", claseService.buscarPorId(id));
        model.addAttribute("modoEdicion", true);
        return "clases/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("clase") Clase clase,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            return "clases/form";
        }

        claseService.actualizar(id, clase);
        redirectAttributes.addFlashAttribute("mensajeExito", "Clase actualizada correctamente.");
        return "redirect:/clases";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        claseService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Clase desactivada.");
        return "redirect:/clases";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        claseService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Clase activada.");
        return "redirect:/clases";
    }
}
