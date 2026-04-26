package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.dto.TrialConversionResult;
import com.flacofitness.app.model.dto.TrialForm;
import com.flacofitness.app.model.enums.EstadoTrial;
import com.flacofitness.app.service.ControllerActivityLogger;
import com.flacofitness.app.service.OperationalClockService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.TrialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    private final ControllerActivityLogger controllerActivityLogger;

    public TrialController(TrialService trialService,
                           StaffService staffService,
                           OperationalClockService operationalClockService,
                           ControllerActivityLogger controllerActivityLogger) {
        this.trialService = trialService;
        this.staffService = staffService;
        this.operationalClockService = operationalClockService;
        this.controllerActivityLogger = controllerActivityLogger;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) LocalDate desde,
                         @RequestParam(required = false) LocalDate hasta,
                         @RequestParam(name = "estado", required = false) EstadoTrial estado,
                         Model model) {
        model.addAttribute("trials", trialService.listarFiltrados(desde, hasta, estado));
        model.addAttribute("desdeFiltro", desde);
        model.addAttribute("hastaFiltro", hasta);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estadosTrial", EstadoTrial.values());
        model.addAttribute("trialsPendientes", trialService.contarPendientes());
        model.addAttribute("trialsHoy", trialService.contarHoy());
        return "trials/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        TrialForm trialForm = new TrialForm();
        trialForm.setFechaPrueba(operationalClockService.today());
        trialForm.setEstado(EstadoTrial.PENDIENTE);
        cargarCatalogos(model);
        model.addAttribute("trialForm", trialForm);
        model.addAttribute("modoEdicion", false);
        return "trials/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("trialForm") TrialForm trialForm,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request,
                          HttpSession session) {
        if (bindingResult.hasErrors()) {
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "trials/form";
        }

        try {
            var guardado = trialService.guardar(trialForm);
            controllerActivityLogger.log(request, session,
                    "trials", "trial_creado", "trial", guardado.getId(),
                    "Trial creado",
                    "Se registro un nuevo dia de prueba para " + guardado.getNombre() + ".");
            redirectAttributes.addFlashAttribute("mensajeExito", "Trial creado correctamente.");
            return "redirect:/trials/" + guardado.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("trialError", ex.getMessage());
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            model.addAttribute("mensajeError", ex.getMessage());
            return "trials/form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("trial", trialService.buscarPorId(id));
        return "trials/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        cargarCatalogos(model);
        model.addAttribute("trialForm", TrialForm.from(trialService.buscarPorId(id)));
        model.addAttribute("modoEdicion", true);
        return "trials/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("trialForm") TrialForm trialForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpSession session) {
        trialForm.setId(id);
        if (bindingResult.hasErrors()) {
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "trials/form";
        }

        try {
            var actualizado = trialService.actualizar(id, trialForm);
            controllerActivityLogger.log(request, session,
                    "trials", "trial_actualizado", "trial", actualizado.getId(),
                    "Trial actualizado",
                    "Se actualizo el seguimiento comercial de " + actualizado.getNombre() + ".");
            redirectAttributes.addFlashAttribute("mensajeExito", "Trial actualizado correctamente.");
            return "redirect:/trials/" + id;
        } catch (BusinessValidationException ex) {
            bindingResult.reject("trialError", ex.getMessage());
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("mensajeError", ex.getMessage());
            return "trials/form";
        }
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam EstadoTrial estado,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request,
                                   HttpSession session) {
        trialService.actualizarEstado(id, estado);
        controllerActivityLogger.log(request, session,
                "trials", "trial_estado_actualizado", "trial", id,
                "Estado de trial actualizado",
                "El trial paso a " + estado.name().replace('_', ' ').toLowerCase() + ".");
        redirectAttributes.addFlashAttribute("mensajeExito", "Estado del trial actualizado.");
        return "redirect:/trials/" + id;
    }

    @PostMapping("/{id}/convertir")
    public String convertir(@PathVariable Long id,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest request,
                            HttpSession session) {
        try {
            TrialConversionResult conversion = trialService.convertirAUsuarioConCredenciales(id);
            Long usuarioId = conversion.usuario().getId();
            controllerActivityLogger.log(request, session,
                    "trials", "trial_convertido", "trial", id,
                    "Trial convertido",
                    "El lead se convirtio en un nuevo usuario.");
            if (conversion.nuevoUsuario()) {
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "Trial convertido a usuario. Password temporal: " + conversion.temporalPassword()
                                + ". El usuario debera cambiarla al entrar.");
            } else {
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "Trial vinculado a un usuario existente. Verifica el perfil y la membresia asignada.");
            }
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
}
