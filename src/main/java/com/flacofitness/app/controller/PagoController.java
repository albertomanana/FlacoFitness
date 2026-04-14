package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoPago;
import com.flacofitness.app.model.enums.MetodoPago;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.PlanService;
import com.flacofitness.app.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final UsuarioService usuarioService;
    private final PlanService planService;

    public PagoController(PagoService pagoService,
                          UsuarioService usuarioService,
                          PlanService planService) {
        this.pagoService = pagoService;
        this.usuarioService = usuarioService;
        this.planService = planService;
    }

    @GetMapping
    public String listarPagos(@RequestParam(name = "usuarioId", required = false) Long usuarioId,
                              @RequestParam(name = "estado", required = false) EstadoPago estado,
                              Model model) {
        model.addAttribute("pagos", pagoService.listarFiltrados(usuarioId, estado));
        cargarResumenFinanciero(model);
        model.addAttribute("usuariosFiltro", usuarioService.listarTodos());
        model.addAttribute("estadosFiltro", EstadoPago.values());
        model.addAttribute("usuarioFiltroId", usuarioId);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("tituloListado", "Pagos");
        model.addAttribute("subtituloListado", "Gestión general de pagos registrados en el sistema.");
        return "pagos/list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Pago pago = new Pago();
        pago.setUsuario(new Usuario());
        pago.setPlan(new Plan());
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaVencimiento(java.time.LocalDate.now().plusDays(30));
        cargarCatalogos(model);
        model.addAttribute("pago", pago);
        model.addAttribute("modoEdicion", false);
        return "pagos/form";
    }

    @PostMapping
    public String guardarPago(@Valid @ModelAttribute("pago") Pago pago,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        normalizarRelaciones(pago);
        validarUsuarioSeleccionado(pago, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(pago);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "pagos/form";
        }

        try {
            pagoService.guardar(pago);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("businessError", ex.getMessage());
            prepararRelaciones(pago);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "pagos/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Pago creado correctamente.");
        return "redirect:/pagos";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("pago", pagoService.buscarPorId(id));
        return "pagos/detail";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Pago pago = pagoService.buscarPorId(id);
        prepararRelaciones(pago);
        cargarCatalogos(model);
        model.addAttribute("pago", pago);
        model.addAttribute("modoEdicion", true);
        return "pagos/form";
    }

    @PostMapping("/{id}")
    public String actualizarPago(@PathVariable Long id,
                                 @Valid @ModelAttribute("pago") Pago pago,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        normalizarRelaciones(pago);
        validarUsuarioSeleccionado(pago, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(pago);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "pagos/form";
        }

        try {
            pagoService.actualizar(id, pago);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("businessError", ex.getMessage());
            prepararRelaciones(pago);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "pagos/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Pago actualizado correctamente.");
        return "redirect:/pagos";
    }

    @PostMapping("/{id}/marcar-pagado")
    public String marcarComoPagado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pagoService.marcarComoPagado(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "El cobro se ha registrado exitosamente. Estado actualizado a PAGADO.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar el pago: " + e.getMessage());
        }
        return "redirect:/pagos";
    }

    @GetMapping("/usuario/{usuarioId}")
    public String listarPagosPorUsuario(@PathVariable Long usuarioId, Model model) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        model.addAttribute("pagos", pagoService.listarFiltrados(usuarioId, null));
        cargarResumenFinanciero(model);
        model.addAttribute("usuariosFiltro", usuarioService.listarTodos());
        model.addAttribute("estadosFiltro", EstadoPago.values());
        model.addAttribute("usuarioFiltroId", usuarioId);
        model.addAttribute("estadoFiltro", null);
        model.addAttribute("tituloListado", "Pagos del usuario");
        model.addAttribute("subtituloListado", "Historial de pagos de " + construirNombreUsuario(usuario) + ".");
        return "pagos/list";
    }

    private void cargarResumenFinanciero(Model model) {
        model.addAttribute("ingresosTotales", pagoService.calcularIngresosTotales());
        model.addAttribute("ingresosMesActual", pagoService.calcularIngresosMesActual());
        model.addAttribute("pagosPendientes", pagoService.contarPagosPendientes());
        model.addAttribute("pagosVencidos", pagoService.contarPagosVencidos());
        model.addAttribute("usuariosAlDia", pagoService.contarUsuariosAlDia());
        model.addAttribute("usuariosConDeuda", pagoService.contarUsuariosConDeuda());
        model.addAttribute("usuariosConVencidos", pagoService.contarUsuariosConPagosVencidos());
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("usuarios", usuarioService.listarActivos());
        model.addAttribute("planes", planService.listarActivos());
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("estadosPago", EstadoPago.values());
    }

    private void prepararRelaciones(Pago pago) {
        if (pago.getUsuario() == null) {
            pago.setUsuario(new Usuario());
        }
        if (pago.getPlan() == null) {
            pago.setPlan(new Plan());
        }
    }

    private void normalizarRelaciones(Pago pago) {
        if (pago.getUsuario() != null && pago.getUsuario().getId() == null) {
            pago.setUsuario(null);
        }
        if (pago.getPlan() != null && pago.getPlan().getId() == null) {
            pago.setPlan(null);
        }
    }

    private void validarUsuarioSeleccionado(Pago pago, BindingResult bindingResult) {
        if (pago.getUsuario() == null || pago.getUsuario().getId() == null) {
            bindingResult.rejectValue("usuario.id", "required", "Debes seleccionar un usuario.");
        }
    }

    private String construirNombreUsuario(Usuario usuario) {
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            return usuario.getNombre();
        }
        return usuario.getNombre() + " " + usuario.getApellidos();
    }
}
