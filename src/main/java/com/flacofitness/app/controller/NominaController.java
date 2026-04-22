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

    public NominaController(NominaService nominaService,
                            StaffService staffService,
                            FinancePdfService financePdfService,
                            OperationalClockService operationalClockService,
                            FinancialAutomationService financialAutomationService) {
        this.nominaService = nominaService;
        this.staffService = staffService;
        this.financePdfService = financePdfService;
        this.operationalClockService = operationalClockService;
        this.financialAutomationService = financialAutomationService;
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
    public String nueva(Model model) {
        Nomina nomina = new Nomina();
        nomina.setPeriodo(YearMonth.from(operationalClockService.today()).toString());
        prepararNomina(nomina);
        model.addAttribute("nomina", nomina);
        model.addAttribute("modoEdicion", false);
        cargarCatalogos(model);
        return "nominas/form";
    }

    @PostMapping
    public String generar(@Valid @ModelAttribute("nomina") Nomina nomina,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararNomina(nomina);
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "nominas/form";
        }

        try {
            Long staffPerfilId = nomina.getStaffPerfil() != null ? nomina.getStaffPerfil().getId() : null;
            if (staffPerfilId == null) {
                bindingResult.rejectValue("staffPerfil.id", "staffPerfil.id", "Debes seleccionar un staff para la nómina");
                prepararNomina(nomina);
                model.addAttribute("modoEdicion", false);
                cargarCatalogos(model);
                return "nominas/form";
            }

            nominaService.generar(
                    staffPerfilId,
                    nomina.getPeriodo(),
                    nomina.getSalarioBase(),
                    nomina.getBonus(),
                    nomina.getDeducciones());
        } catch (BusinessValidationException ex) {
            bindingResult.reject("nominaError", ex.getMessage());
            prepararNomina(nomina);
            model.addAttribute("modoEdicion", false);
            model.addAttribute("mensajeError", ex.getMessage());
            cargarCatalogos(model);
            return "nominas/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Nomina generada correctamente.");
        return "redirect:/nominas";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("nomina", nominaService.buscarPorId(id));
        return "nominas/detail";
    }

    @PostMapping("/{id}/pagada")
    public String marcarPagada(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        nominaService.marcarPagada(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Nomina marcada como pagada.");
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
