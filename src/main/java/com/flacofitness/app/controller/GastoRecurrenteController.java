package com.flacofitness.app.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.GastoRecurrente;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.FrecuenciaGasto;
import com.flacofitness.app.model.enums.TipoGasto;
import com.flacofitness.app.service.FinancePdfService;
import com.flacofitness.app.service.FinancialAutomationService;
import com.flacofitness.app.service.GastoRecurrenteService;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaquinaService;
import com.flacofitness.app.service.MaterialService;
import com.flacofitness.app.service.OperationalClockService;
import com.flacofitness.app.service.StaffService;
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

@Controller
@RequestMapping("/gastos/recurrentes")
public class GastoRecurrenteController {

    private final GastoRecurrenteService gastoRecurrenteService;
    private final GastoService gastoService;
    private final StaffService staffService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final FinancePdfService financePdfService;
    private final OperationalClockService operationalClockService;
    private final FinancialAutomationService financialAutomationService;

    public GastoRecurrenteController(GastoRecurrenteService gastoRecurrenteService,
                                     GastoService gastoService,
                                     StaffService staffService,
                                     MaquinaService maquinaService,
                                     MaterialService materialService,
                                     FinancePdfService financePdfService,
                                     OperationalClockService operationalClockService,
                                     FinancialAutomationService financialAutomationService) {
        this.gastoRecurrenteService = gastoRecurrenteService;
        this.gastoService = gastoService;
        this.staffService = staffService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.financePdfService = financePdfService;
        this.operationalClockService = operationalClockService;
        this.financialAutomationService = financialAutomationService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recurrentes", gastoRecurrenteService.listarActivos());
        model.addAttribute("recurrentesActivos", gastoRecurrenteService.contarActivos());
        model.addAttribute("recurrentesProximos", gastoRecurrenteService.contarProximos(7));
        model.addAttribute("gastosGeneradosHoy", gastoService.contarCriticos());
        return "gastos/recurrentes/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        GastoRecurrente recurrente = new GastoRecurrente();
        recurrente.setFrecuencia(FrecuenciaGasto.MENSUAL);
        recurrente.setCategoria(CategoriaGasto.OTROS);
        recurrente.setTipoGasto(TipoGasto.FIJO);
        recurrente.setFechaInicio(operationalClockService.today());
        recurrente.setFechaProximoCargo(operationalClockService.today());
        prepararRelaciones(recurrente);
        model.addAttribute("recurrente", recurrente);
        cargarCatalogos(model);
        model.addAttribute("modoEdicion", false);
        return "gastos/recurrentes/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("recurrente") GastoRecurrente recurrente,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(recurrente);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "gastos/recurrentes/form";
        }

        try {
            GastoRecurrente guardado = gastoRecurrenteService.guardar(recurrente);
            redirectAttributes.addFlashAttribute("mensajeExito", "Plantilla recurrente guardada correctamente.");
            return "redirect:/gastos/recurrentes/" + guardado.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("recurrenteError", ex.getMessage());
            prepararRelaciones(recurrente);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "gastos/recurrentes/form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("recurrente", gastoRecurrenteService.buscarPorId(id));
        return "gastos/recurrentes/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        GastoRecurrente recurrente = gastoRecurrenteService.buscarPorId(id);
        prepararRelaciones(recurrente);
        cargarCatalogos(model);
        model.addAttribute("recurrente", recurrente);
        model.addAttribute("modoEdicion", true);
        return "gastos/recurrentes/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("recurrente") GastoRecurrente recurrente,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(recurrente);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "gastos/recurrentes/form";
        }

        try {
            GastoRecurrente actualizado = gastoRecurrenteService.actualizar(id, recurrente);
            redirectAttributes.addFlashAttribute("mensajeExito", "Plantilla recurrente actualizada.");
            return "redirect:/gastos/recurrentes/" + actualizado.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("recurrenteError", ex.getMessage());
            prepararRelaciones(recurrente);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "gastos/recurrentes/form";
        }
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id,
                          RedirectAttributes redirectAttributes,
                          @RequestParam(name = "returnTo", required = false) String returnTo) {
        gastoRecurrenteService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Plantilla recurrente activada.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(name = "returnTo", required = false) String returnTo) {
        gastoRecurrenteService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Plantilla recurrente desactivada.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    @PostMapping("/procesar")
    public String procesar(RedirectAttributes redirectAttributes) {
        var result = financialAutomationService.runExpensesOnly("EXPENSES_MANUAL");
        redirectAttributes.addFlashAttribute(
                result.hasErrors() ? "mensajeError" : "mensajeExito",
                "Resultado de gastos recurrentes: " + result.toHumanSummary());
        return "redirect:/gastos/recurrentes";
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdf() {
        Map<String, Object> model = new HashMap<>();
        model.put("titulo", "Plantillas recurrentes");
        model.put("subtitulo", "Control de cargos automaticos y vencimientos.");
        model.put("recurrentes", gastoRecurrenteService.listarActivos());
        model.put("recurrentesActivos", gastoRecurrenteService.contarActivos());
        model.put("recurrentesProximos", gastoRecurrenteService.contarProximos(7));
        model.put("staffActivos", staffService.listarActivos());
        model.put("maquinasActivas", maquinaService.listarFiltradas(null, null));
        model.put("materialesActivos", materialService.listarFiltrados(null, null));

        byte[] pdf = financePdfService.render("reportes/recurrentes-listado", model);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recurrentes-financieros.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("categoriasGasto", CategoriaGasto.values());
        model.addAttribute("tiposGasto", TipoGasto.values());
        model.addAttribute("frecuenciasGasto", FrecuenciaGasto.values());
        model.addAttribute("staffActivos", staffService.listarActivos());
        model.addAttribute("maquinasActivas", maquinaService.listarFiltradas(null, null));
        model.addAttribute("materialesActivos", materialService.listarFiltrados(null, null));
    }

    private void prepararRelaciones(GastoRecurrente recurrente) {
        if (recurrente.getStaffResponsable() == null) {
            recurrente.setStaffResponsable(new com.flacofitness.app.model.entity.StaffPerfil());
        }
        if (recurrente.getMaquina() == null) {
            recurrente.setMaquina(new com.flacofitness.app.model.entity.Maquina());
        }
        if (recurrente.getMaterial() == null) {
            recurrente.setMaterial(new com.flacofitness.app.model.entity.Material());
        }
    }

    private String resolveReturnPath(Long id, String returnTo) {
        if ("detail".equalsIgnoreCase(returnTo)) {
            return "/gastos/recurrentes/" + id;
        }
        return "/gastos/recurrentes";
    }
}
