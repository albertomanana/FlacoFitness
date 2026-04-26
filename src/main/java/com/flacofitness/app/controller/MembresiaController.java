package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.MembresiaUsuario;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoMembresia;
import com.flacofitness.app.model.enums.TipoMembresia;
import com.flacofitness.app.service.ControllerActivityLogger;
import com.flacofitness.app.service.MembresiaService;
import com.flacofitness.app.service.OperationalClockService;
import com.flacofitness.app.service.UsuarioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class MembresiaController {

    private final MembresiaService membresiaService;
    private final UsuarioService usuarioService;
    private final OperationalClockService operationalClockService;
    private final ControllerActivityLogger controllerActivityLogger;

    public MembresiaController(MembresiaService membresiaService,
                               UsuarioService usuarioService,
                               OperationalClockService operationalClockService,
                               ControllerActivityLogger controllerActivityLogger) {
        this.membresiaService = membresiaService;
        this.usuarioService = usuarioService;
        this.operationalClockService = operationalClockService;
        this.controllerActivityLogger = controllerActivityLogger;
    }

    @GetMapping("/membresias")
    public String listarCatalogo(Model model) {
        model.addAttribute("planes", membresiaService.listarCatalogo());
        model.addAttribute("membresiasActivas", membresiaService.contarActivas());
        model.addAttribute("membresiasVencidas", membresiaService.contarVencidas());
        return "membresias/list";
    }

    @GetMapping("/membresias/nueva")
    public String nuevoPlan(Model model) {
        Plan plan = new Plan();
        plan.setActivo(true);
        plan.setDuracionDias(30);
        plan.setTipoMembresia(TipoMembresia.MENSUAL);
        cargarCatalogosPlan(model);
        model.addAttribute("plan", plan);
        model.addAttribute("modoEdicion", false);
        return "membresias/form";
    }

    @PostMapping("/membresias")
    public String guardarPlan(@Valid @ModelAttribute("plan") Plan plan,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request,
                              HttpSession session) {
        if (bindingResult.hasErrors()) {
            cargarCatalogosPlan(model);
            model.addAttribute("modoEdicion", false);
            return "membresias/form";
        }

        Plan guardado = membresiaService.guardarPlan(plan);
        controllerActivityLogger.log(request, session,
                "membresias", "plan_creado", "plan", guardado.getId(),
                "Membresia creada",
                "Se dio de alta el plan " + guardado.getNombre() + ".");
        redirectAttributes.addFlashAttribute("mensajeExito", "Membresia creada correctamente.");
        return "redirect:/membresias/" + guardado.getId();
    }

    @GetMapping("/membresias/{id}")
    public String detallePlan(@PathVariable Long id, Model model) {
        model.addAttribute("plan", membresiaService.buscarPlan(id));
        model.addAttribute("contratosRecientes", membresiaService.listarRecientes());
        return "membresias/detail";
    }

    @GetMapping("/membresias/{id}/editar")
    public String editarPlan(@PathVariable Long id, Model model) {
        model.addAttribute("plan", membresiaService.buscarPlan(id));
        cargarCatalogosPlan(model);
        model.addAttribute("modoEdicion", true);
        return "membresias/form";
    }

    @PostMapping("/membresias/{id}")
    public String actualizarPlan(@PathVariable Long id,
                                 @Valid @ModelAttribute("plan") Plan plan,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request,
                                 HttpSession session) {
        if (bindingResult.hasErrors()) {
            cargarCatalogosPlan(model);
            model.addAttribute("modoEdicion", true);
            return "membresias/form";
        }

        Plan actualizado = membresiaService.actualizarPlan(id, plan);
        controllerActivityLogger.log(request, session,
                "membresias", "plan_actualizado", "plan", actualizado.getId(),
                "Membresia actualizada",
                "Se actualizo el catalogo del plan " + actualizado.getNombre() + ".");
        redirectAttributes.addFlashAttribute("mensajeExito", "Membresia actualizada correctamente.");
        return "redirect:/membresias/" + actualizado.getId();
    }

    @PostMapping("/membresias/{id}/desactivar")
    public String desactivarPlan(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(name = "returnTo", required = false) String returnTo,
                                 HttpServletRequest request,
                                 HttpSession session) {
        membresiaService.desactivarPlan(id);
        controllerActivityLogger.log(request, session,
                "membresias", "plan_desactivado", "plan", id,
                "Membresia desactivada",
                "Se desactivo temporalmente un plan del catalogo.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Membresia desactivada.");
        return "redirect:" + resolvePlanReturnPath(id, returnTo);
    }

    @PostMapping("/membresias/{id}/activar")
    public String activarPlan(@PathVariable Long id,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(name = "returnTo", required = false) String returnTo,
                              HttpServletRequest request,
                              HttpSession session) {
        membresiaService.activarPlan(id);
        controllerActivityLogger.log(request, session,
                "membresias", "plan_activado", "plan", id,
                "Membresia activada",
                "Se reactivo un plan del catalogo.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Membresia activada.");
        return "redirect:" + resolvePlanReturnPath(id, returnTo);
    }

    @GetMapping("/usuarios/{usuarioId}/membresias")
    public String membresiasUsuario(@PathVariable Long usuarioId, Model model) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        MembresiaUsuario contrato = new MembresiaUsuario();
        contrato.setUsuario(usuario);
        contrato.setFechaInicio(operationalClockService.today());
        contrato.setEstado(EstadoMembresia.ACTIVA);

        model.addAttribute("usuario", usuario);
        model.addAttribute("contratos", membresiaService.listarContratosPorUsuario(usuarioId));
        model.addAttribute("contrato", contrato);
        cargarCatalogosContrato(model);
        return "membresias/usuario";
    }

    @PostMapping("/usuarios/{usuarioId}/membresias")
    public String asignarMembresiaUsuario(@PathVariable Long usuarioId,
                                          @Valid @ModelAttribute("contrato") MembresiaUsuario contrato,
                                          BindingResult bindingResult,
                                          Model model,
                                          RedirectAttributes redirectAttributes,
                                          HttpServletRequest request,
                                          HttpSession session) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        contrato.setUsuario(usuario);

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("contratos", membresiaService.listarContratosPorUsuario(usuarioId));
            cargarCatalogosContrato(model);
            return "membresias/usuario";
        }

        try {
            MembresiaUsuario guardado = membresiaService.guardarContrato(contrato);
            controllerActivityLogger.log(request, session,
                    "membresias", "membresia_asignada", "usuario", usuarioId,
                    "Membresia asignada",
                    "Se asigno el plan " + (guardado.getPlan() != null ? guardado.getPlan().getNombre() : "sin plan")
                            + " al usuario " + usuario.getNombre() + ".");
        } catch (BusinessValidationException ex) {
            bindingResult.reject("membresiaError", ex.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("contratos", membresiaService.listarContratosPorUsuario(usuarioId));
            cargarCatalogosContrato(model);
            return "membresias/usuario";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Membresia asignada correctamente.");
        return "redirect:/usuarios/" + usuarioId + "/membresias";
    }

    @PostMapping("/usuarios/{usuarioId}/membresias/{contratoId}/cancelar")
    public String cancelarMembresiaUsuario(@PathVariable Long usuarioId,
                                           @PathVariable Long contratoId,
                                           RedirectAttributes redirectAttributes,
                                           HttpServletRequest request,
                                           HttpSession session) {
        membresiaService.cancelarContrato(contratoId);
        controllerActivityLogger.log(request, session,
                "membresias", "membresia_cancelada", "usuario", usuarioId,
                "Membresia cancelada",
                "Se cancelo un contrato de membresia del usuario.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Contrato de membresia cancelado.");
        return "redirect:/usuarios/" + usuarioId + "/membresias";
    }

    private void cargarCatalogosPlan(Model model) {
        model.addAttribute("tiposMembresia", TipoMembresia.values());
    }

    private void cargarCatalogosContrato(Model model) {
        model.addAttribute("planes", membresiaService.listarPlanesActivos());
        model.addAttribute("estadosMembresia", EstadoMembresia.values());
    }

    private String resolvePlanReturnPath(Long id, String returnTo) {
        if ("detail".equalsIgnoreCase(returnTo)) {
            return "/membresias/" + id;
        }
        return "/membresias";
    }
}
