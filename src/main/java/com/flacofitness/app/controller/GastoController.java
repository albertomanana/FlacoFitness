package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.EstadoGasto;
import com.flacofitness.app.model.enums.TipoGasto;
import com.flacofitness.app.service.FinancePdfService;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaquinaService;
import com.flacofitness.app.service.MaterialService;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.ControllerActivityLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/gastos")
public class GastoController {

    private final GastoService gastoService;
    private final StaffService staffService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final PagoService pagoService;
    private final FinancePdfService financePdfService;
    private final ControllerActivityLogger controllerActivityLogger;

    public GastoController(GastoService gastoService,
                           StaffService staffService,
                           MaquinaService maquinaService,
                           MaterialService materialService,
                           PagoService pagoService,
                           FinancePdfService financePdfService,
                           ControllerActivityLogger controllerActivityLogger) {
        this.gastoService = gastoService;
        this.staffService = staffService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.pagoService = pagoService;
        this.financePdfService = financePdfService;
        this.controllerActivityLogger = controllerActivityLogger;
    }

    @GetMapping
    public String listar(@RequestParam(name = "desde", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(name = "hasta", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(name = "categoria", required = false) CategoriaGasto categoria,
                         @RequestParam(name = "estado", required = false) EstadoGasto estado,
                         @RequestParam(name = "tipo", required = false) TipoGasto tipoGasto,
                         @RequestParam(name = "staffId", required = false) Long staffId,
                         @RequestParam(name = "maquinaId", required = false) Long maquinaId,
                         @RequestParam(name = "materialId", required = false) Long materialId,
                         @RequestParam(name = "recurrente", required = false) Boolean recurrente,
                         @RequestParam(name = "proveedor", required = false) String proveedor,
                         Model model) {
        model.addAttribute("gastos", gastoService.listarFiltrados(
                desde,
                hasta,
                categoria,
                estado,
                tipoGasto,
                staffId,
                maquinaId,
                materialId,
                recurrente,
                normalizarFiltroTexto(proveedor)));
        model.addAttribute("categoriasGasto", CategoriaGasto.values());
        model.addAttribute("estadosGasto", EstadoGasto.values());
        model.addAttribute("tiposGasto", TipoGasto.values());
        model.addAttribute("staffActivos", staffService.listarActivos());
        model.addAttribute("maquinasActivas", maquinaService.listarFiltradas(null, null));
        model.addAttribute("materialesActivos", materialService.listarFiltrados(null, null));
        model.addAttribute("desdeFiltro", desde);
        model.addAttribute("hastaFiltro", hasta);
        model.addAttribute("categoriaFiltro", categoria);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("tipoFiltro", tipoGasto);
        model.addAttribute("staffFiltro", staffId);
        model.addAttribute("maquinaFiltro", maquinaId);
        model.addAttribute("materialFiltro", materialId);
        model.addAttribute("recurrenteFiltro", recurrente);
        model.addAttribute("proveedorFiltro", proveedor);
        model.addAttribute("gastosTotales", gastoService.contarActivos());
        model.addAttribute("gastosCriticos", gastoService.contarCriticos());
        model.addAttribute("recurrentesProximos", gastoService.contarRecurrentesProximos(7));
        model.addAttribute("vencimientosProximos", gastoService.contarVencimientosProximos(7));
        BigDecimal gastoMes = gastoService.calcularGastoMesActual();
        BigDecimal ingresoMes = pagoService.calcularIngresosMesActual();
        model.addAttribute("gastoMesActual", gastoMes);
        model.addAttribute("gastoFijoMesActual", gastoService.calcularGastoFijoMesActual());
        model.addAttribute("gastoVariableMesActual", gastoService.calcularGastoVariableMesActual());
        model.addAttribute("ingresoMesActual", ingresoMes);
        model.addAttribute("balanceMesActual", ingresoMes.subtract(gastoMes));
        return "gastos/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Gasto gasto = new Gasto();
        prepararRelaciones(gasto);
        cargarCatalogos(model);
        model.addAttribute("gasto", gasto);
        model.addAttribute("modoEdicion", false);
        return "gastos/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("gasto") Gasto gasto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request,
                          HttpSession session) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "gastos/form";
        }

        try {
            Gasto guardado = gastoService.guardar(gasto);
            controllerActivityLogger.log(request, session,
                    "gastos", "gasto_creado", "gasto", guardado.getId(),
                    "Gasto registrado",
                    "Se registro un nuevo gasto operativo.");
            redirectAttributes.addFlashAttribute("mensajeExito", "Gasto registrado correctamente.");
            return "redirect:/gastos/" + guardado.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("gastoError", ex.getMessage());
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "gastos/form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("gasto", gastoService.buscarPorId(id));
        return "gastos/detail";
    }

    @PostMapping("/{id}/pagado")
    public String marcarPagado(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               @org.springframework.web.bind.annotation.RequestParam(name = "returnTo", required = false) String returnTo,
                               HttpServletRequest request,
                               HttpSession session) {
        gastoService.marcarPagado(id);
        controllerActivityLogger.log(request, session,
                "gastos", "gasto_pagado", "gasto", id,
                "Gasto pagado",
                "Se marco el gasto como pagado.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto marcado como pagado.");
        if ("list".equalsIgnoreCase(returnTo)) {
            return "redirect:/gastos";
        }
        return "redirect:/gastos/" + id;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Gasto gasto = gastoService.buscarPorId(id);
        prepararRelaciones(gasto);
        cargarCatalogos(model);
        model.addAttribute("gasto", gasto);
        model.addAttribute("modoEdicion", true);
        return "gastos/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("gasto") Gasto gasto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpSession session) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "gastos/form";
        }

        try {
            Gasto actualizado = gastoService.actualizar(id, gasto);
            controllerActivityLogger.log(request, session,
                    "gastos", "gasto_actualizado", "gasto", actualizado.getId(),
                    "Gasto actualizado",
                    "Se actualizo la ficha financiera del gasto.");
            redirectAttributes.addFlashAttribute("mensajeExito", "Gasto actualizado correctamente.");
            return "redirect:/gastos/" + actualizado.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("gastoError", ex.getMessage());
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "gastos/form";
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             @org.springframework.web.bind.annotation.RequestParam(name = "returnTo", required = false) String returnTo) {
        gastoService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto desactivado.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id,
                          RedirectAttributes redirectAttributes,
                          @org.springframework.web.bind.annotation.RequestParam(name = "returnTo", required = false) String returnTo) {
        gastoService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto activado.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(name = "desde", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam(name = "hasta", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                             @RequestParam(name = "categoria", required = false) CategoriaGasto categoria,
                                             @RequestParam(name = "estado", required = false) EstadoGasto estado,
                                             @RequestParam(name = "tipo", required = false) TipoGasto tipoGasto,
                                             @RequestParam(name = "staffId", required = false) Long staffId,
                                             @RequestParam(name = "maquinaId", required = false) Long maquinaId,
                                             @RequestParam(name = "materialId", required = false) Long materialId,
                                             @RequestParam(name = "recurrente", required = false) Boolean recurrente,
                                             @RequestParam(name = "proveedor", required = false) String proveedor) {
        var gastos = gastoService.listarFiltrados(
                desde,
                hasta,
                categoria,
                estado,
                tipoGasto,
                staffId,
                maquinaId,
                materialId,
                recurrente,
                normalizarFiltroTexto(proveedor));
        BigDecimal gastoMesActual = gastoService.calcularGastoMesActual();
        BigDecimal ingresoMesActual = pagoService.calcularIngresosMesActual();

        Map<String, Object> model = new HashMap<>();
        model.put("titulo", "Gastos operativos");
        model.put("subtitulo", "Listado filtrado de egresos, estados y relaciones operativas.");
        model.put("gastos", gastos);
        model.put("gastosTotales", gastoService.contarActivos());
        model.put("gastoMesActual", gastoMesActual);
        model.put("ingresoMesActual", ingresoMesActual);
        model.put("balanceMesActual", ingresoMesActual.subtract(gastoMesActual));
        byte[] pdf = financePdfService.render("reportes/gastos-listado", model);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=gastos-operativos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarDetallePdf(@PathVariable Long id) {
        Gasto gasto = gastoService.buscarPorId(id);
        Map<String, Object> model = new HashMap<>();
        model.put("titulo", "Detalle de gasto");
        model.put("subtitulo", "Ficha financiera individual para auditoria, revision y demo.");
        model.put("gasto", gasto);
        byte[] pdf = financePdfService.render("reportes/gasto-detalle", model);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=gasto-" + gasto.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("categoriasGasto", CategoriaGasto.values());
        model.addAttribute("staffActivos", staffService.listarActivos());
        model.addAttribute("maquinasActivas", maquinaService.listarFiltradas(null, null));
        model.addAttribute("materialesActivos", materialService.listarFiltrados(null, null));
    }

    private void prepararRelaciones(Gasto gasto) {
        if (gasto.getStaffResponsable() == null) {
            gasto.setStaffResponsable(new com.flacofitness.app.model.entity.StaffPerfil());
        }
        if (gasto.getMaquina() == null) {
            gasto.setMaquina(new com.flacofitness.app.model.entity.Maquina());
        }
        if (gasto.getMaterial() == null) {
            gasto.setMaterial(new com.flacofitness.app.model.entity.Material());
        }
    }

    private String normalizarFiltroTexto(String value) {
        if (value == null) {
            return null;
        }
        String normalizado = value.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private String resolveReturnPath(Long id, String returnTo) {
        if ("detail".equalsIgnoreCase(returnTo)) {
            return "/gastos/" + id;
        }
        return "/gastos";
    }
}
