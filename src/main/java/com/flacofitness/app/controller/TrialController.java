package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.enums.EstadoTrial;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.OperationalClockService;
import com.flacofitness.app.service.TrialService;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/trials")
public class TrialController {

    private final TrialService trialService;
    private final StaffService staffService;
    private final OperationalClockService operationalClockService;

    public TrialController(TrialService trialService,
                           StaffService staffService,
                           OperationalClockService operationalClockService) {
        this.trialService = trialService;
        this.staffService = staffService;
        this.operationalClockService = operationalClockService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "estado", required = false) EstadoTrial estado, Model model) {
        model.addAttribute("trials", trialService.listarFiltrados(estado));
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estadosTrial", EstadoTrial.values());
        model.addAttribute("trialsPendientes", trialService.contarPendientes());
        model.addAttribute("trialsHoy", trialService.contarHoy());
        return "trials/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Trial trial = new Trial();
        trial.setFechaPrueba(operationalClockService.today());
        trial.setEstado(EstadoTrial.PENDIENTE);
        trial.setStaffResponsable(new StaffPerfil());
        cargarCatalogos(model);
        model.addAttribute("trial", trial);
        model.addAttribute("modoEdicion", false);
        return "trials/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("trial") Trial trial,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        normalizarRelaciones(trial);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(trial);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "trials/form";
        }

        trialService.guardar(trial);
        redirectAttributes.addFlashAttribute("mensajeExito", "Trial creado correctamente.");
        return "redirect:/trials";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("trial", trialService.buscarPorId(id));
        return "trials/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Trial trial = trialService.buscarPorId(id);
        prepararRelaciones(trial);
        cargarCatalogos(model);
        model.addAttribute("trial", trial);
        model.addAttribute("modoEdicion", true);
        return "trials/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("trial") Trial trial,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        normalizarRelaciones(trial);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(trial);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "trials/form";
        }

        trialService.actualizar(id, trial);
        redirectAttributes.addFlashAttribute("mensajeExito", "Trial actualizado correctamente.");
        return "redirect:/trials";
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam EstadoTrial estado,
                                   RedirectAttributes redirectAttributes) {
        trialService.actualizarEstado(id, estado);
        redirectAttributes.addFlashAttribute("mensajeExito", "Estado del trial actualizado.");
        return "redirect:/trials/" + id;
    }

    @PostMapping("/{id}/convertir")
    public String convertir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Long usuarioId = trialService.convertirAUsuario(id).getId();
            redirectAttributes.addFlashAttribute("mensajeExito", "Trial convertido a usuario.");
            return "redirect:/usuarios/" + usuarioId;
        } catch (BusinessValidationException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/trials/" + id;
        }
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("staffActivos", staffService.listarActivos());
        model.addAttribute("estadosTrial", EstadoTrial.values());
    }

    private void prepararRelaciones(Trial trial) {
        if (trial.getStaffResponsable() == null) {
            trial.setStaffResponsable(new StaffPerfil());
        }
    }

    private void normalizarRelaciones(Trial trial) {
        if (trial.getStaffResponsable() != null && trial.getStaffResponsable().getId() == null) {
            trial.setStaffResponsable(null);
        }
    }
}
