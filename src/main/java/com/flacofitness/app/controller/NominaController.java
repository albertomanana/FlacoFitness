package com.flacofitness.app.controller;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Nomina;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.service.FinancePdfService;
import com.flacofitness.app.service.FinancialAutomationService;
import com.flacofitness.app.service.NominaService;
import com.flacofitness.app.service.OperationalClockService;
import com.flacofitness.app.service.StaffService;
import com.flacofitness.app.service.ControllerActivityLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/nominas")
public class NominaController {

    private final NominaService nominaService;
    private final StaffService staffService;
    private final FinancePdfService financePdfService;
    private final OperationalClockService operationalClockService;
    private final FinancialAutomationService financialAutomationService;
    private final ControllerActivityLogger controllerActivityLogger;

    public NominaController(NominaService nominaService,
                            StaffService staffService,
                            FinancePdfService financePdfService,
                            OperationalClockService operationalClockService,
                            FinancialAutomationService financialAutomationService,
                            ControllerActivityLogger controllerActivityLogger) {
        this.nominaService = nominaService;
        this.staffService = staffService;
        this.financePdfService = financePdfService;
        this.operationalClockService = operationalClockService;
        this.financialAutomationService = financialAutomationService;
        this.controllerActivityLogger = controllerActivityLogger;
    }

    @GetMapping
    public String listar(@RequestParam(name = "staffId", required = false) Long staffId, Model model) {
        var nominas = nominaService.listarFiltradas(staffId);
        model.addAttribute("nominas", nominas);
        model.addAttribute("nominasPendientes", nominaService.contarPendientes());
        model.addAttribute("staffActivos", staffService.listarActivos());
        model.addAttribute("staffFiltro", staffId);
        model.addAttribute("totalNeto", nominas.stream()
                .map(Nomina::getSalarioNeto)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "nominas/list";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(name = "staffId", required = false) Long staffId, Model model) {
        Nomina nomina = new Nomina();
        nomina.setPeriodo(YearMonth.from(operationalClockService.today()).toString());
        if (staffId != null) {
            StaffPerfil staffPerfil = staffService.buscarPorId(staffId);
            nomina.setStaffPerfil(staffPerfil);
            nomina.setSalarioBase(staffPerfil.getSalarioBaseMensual());
            nomina.setBonus(staffPerfil.getBonusMensual());
            nomina.setDeducciones(staffPerfil.getDeduccionesMensuales());
        }
        prepararNomina(nomina);
        model.addAttribute("nomina", nomina);
        model.addAttribute("modoEdicion", false);
        cargarCatalogos(model);
        return "nominas/form";
    }

    @PostMapping
    public String generar(@Valid @ModelAttribute("nomina") Nomina nomina,
                          @RequestParam(name = "action", defaultValue = "emitir") String action,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request,
                          HttpSession session) {
        if (bindingResult.hasErrors()) {
            prepararNomina(nomina);
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "nominas/form";
        }

        try {
            Long staffPerfilId = nomina.getStaffPerfil() != null ? nomina.getStaffPerfil().getId() : null;
            if (staffPerfilId == null) {
                bindingResult.rejectValue("staffPerfil.id", "staffPerfil.id", "Debes seleccionar un staff para la nomina");
                prepararNomina(nomina);
                model.addAttribute("modoEdicion", false);
                cargarCatalogos(model);
                return "nominas/form";
            }

            Nomina generada = "borrador".equalsIgnoreCase(action)
                    ? nominaService.guardarBorrador(
                    staffPerfilId,
                    nomina.getPeriodo(),
                    nomina.getSalarioBase(),
                    nomina.getBonus(),
                    nomina.getDeducciones())
                    : nominaService.generar(
                    staffPerfilId,
                    nomina.getPeriodo(),
                    nomina.getSalarioBase(),
                    nomina.getBonus(),
                    nomina.getDeducciones());
            controllerActivityLogger.log(request, session,
                    "nominas", "nomina_generada", "nomina", generada.getId(),
                    "Nomina generada",
                    "Se genero la nomina del periodo " + generada.getPeriodo() + ".");
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "borrador".equalsIgnoreCase(action)
                            ? "Nomina guardada como borrador."
                            : "Nomina emitida correctamente.");
            return "redirect:/nominas/" + generada.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("nominaError", ex.getMessage());
            prepararNomina(nomina);
            model.addAttribute("modoEdicion", false);
            model.addAttribute("mensajeError", ex.getMessage());
            cargarCatalogos(model);
            return "nominas/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        Nomina nomina = nominaService.buscarPorId(id);
        if (nomina.getEstado() != null && nomina.getEstado().name().equals("BORRADOR")) {
            prepararNomina(nomina);
            model.addAttribute("nomina", nomina);
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "nominas/form";
        }
        redirectAttributes.addFlashAttribute("mensajeError", "Solo puedes editar nominas en borrador.");
        return "redirect:/nominas/" + id;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("nomina") Nomina nomina,
                             @RequestParam(name = "action", defaultValue = "borrador") String action,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpSession session) {
        if (bindingResult.hasErrors()) {
            prepararNomina(nomina);
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "nominas/form";
        }

        try {
            Long staffPerfilId = nomina.getStaffPerfil() != null ? nomina.getStaffPerfil().getId() : null;
            if (staffPerfilId == null) {
                bindingResult.rejectValue("staffPerfil.id", "staffPerfil.id", "Debes seleccionar un staff para la nomina");
                prepararNomina(nomina);
                model.addAttribute("modoEdicion", true);
                cargarCatalogos(model);
                return "nominas/form";
            }

            Nomina actualizada = nominaService.actualizarBorrador(
                    id,
                    staffPerfilId,
                    nomina.getPeriodo(),
                    nomina.getSalarioBase(),
                    nomina.getBonus(),
                    nomina.getDeducciones());

            if ("emitir".equalsIgnoreCase(action)) {
                actualizada = nominaService.emitir(actualizada.getId());
                controllerActivityLogger.log(request, session,
                        "nominas", "nomina_emitida", "nomina", actualizada.getId(),
                        "Nomina emitida",
                        "Se emitio la nomina del periodo " + actualizada.getPeriodo() + ".");
                redirectAttributes.addFlashAttribute("mensajeExito", "Nomina emitida correctamente.");
            } else {
                controllerActivityLogger.log(request, session,
                        "nominas", "nomina_actualizada", "nomina", actualizada.getId(),
                        "Nomina actualizada",
                        "Se actualizo el borrador de la nomina del periodo " + actualizada.getPeriodo() + ".");
                redirectAttributes.addFlashAttribute("mensajeExito", "Nomina actualizada correctamente.");
            }

            return "redirect:/nominas/" + actualizada.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("nominaError", ex.getMessage());
            prepararNomina(nomina);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("mensajeError", ex.getMessage());
            cargarCatalogos(model);
            return "nominas/form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("nomina", nominaService.buscarPorId(id));
        return "nominas/detail";
    }

    @PostMapping("/{id}/emitir")
    public String emitir(@PathVariable Long id,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest request,
                         HttpSession session) {
        Nomina nomina = nominaService.emitir(id);
        controllerActivityLogger.log(request, session,
                "nominas", "nomina_emitida", "nomina", id,
                "Nomina emitida",
                "Se emitio la nomina del periodo " + nomina.getPeriodo() + ".");
        redirectAttributes.addFlashAttribute("mensajeExito", "Nomina emitida correctamente.");
        return "redirect:/nominas/" + id;
    }

    @PostMapping("/{id}/pagada")
    public String marcarPagada(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request,
                               HttpSession session) {
        nominaService.marcarPagada(id);
        controllerActivityLogger.log(request, session,
                "nominas", "nomina_pagada", "nomina", id,
                "Nomina pagada",
                "Se marco la nomina como pagada.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Nomina marcada como pagada.");
        return "redirect:/nominas/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request,
                           HttpSession session) {
        Nomina nomina = nominaService.cancelar(id);
        controllerActivityLogger.log(request, session,
                "nominas", "nomina_cancelada", "nomina", id,
                "Nomina cancelada",
                "Se cancelo la nomina del periodo " + nomina.getPeriodo() + ".");
        redirectAttributes.addFlashAttribute("mensajeExito", "Nomina cancelada.");
        return "redirect:/nominas/" + id;
    }

    @PostMapping("/generar-automaticas")
    public String generarAutomatica(RedirectAttributes redirectAttributes) {
        var result = financialAutomationService.runPayrollsOnly("PAYROLLS_MANUAL");
        redirectAttributes.addFlashAttribute(
                result.hasErrors() ? "mensajeError" : "mensajeExito",
                "Resultado de nominas automaticas: " + result.toHumanSummary());
        return "redirect:/nominas";
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(name = "staffId", required = false) Long staffId) {
        var nominas = nominaService.listarFiltradas(staffId);
        Map<String, Object> model = new HashMap<>();
        model.put("titulo", "Nominas internas");
        model.put("subtitulo", "Seguimiento de salarios experimentales y cierres.");
        model.put("nominas", nominas);
        model.put("nominasPendientes", nominaService.contarPendientes());
        model.put("totalNeto", nominas.stream()
                .map(Nomina::getSalarioNeto)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        byte[] pdf = financePdfService.render("reportes/nominas-listado", model);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nominas-financieras.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdfDetalle(@PathVariable Long id) {
        Nomina nomina = nominaService.buscarPorId(id);
        Map<String, Object> model = new HashMap<>();
        model.put("titulo", "Nomina " + nomina.getPeriodo());
        model.put("subtitulo", "Detalle individual para archivo y auditoria interna.");
        model.put("nomina", nomina);
        byte[] pdf = financePdfService.render("reportes/nomina-detalle", model);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nomina-" + nomina.getReferencia() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("staffActivos", staffService.listarActivos());
    }

    private void prepararNomina(Nomina nomina) {
        if (nomina.getStaffPerfil() == null) {
            nomina.setStaffPerfil(new StaffPerfil());
        }
    }
}
