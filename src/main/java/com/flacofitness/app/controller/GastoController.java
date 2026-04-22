package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.service.FinancePdfService;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaquinaService;
import com.flacofitness.app.service.MaterialService;
import com.flacofitness.app.service.PagoService;
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

    public GastoController(GastoService gastoService,
                           StaffService staffService,
                           MaquinaService maquinaService,
                           MaterialService materialService,
                           PagoService pagoService,
                           FinancePdfService financePdfService) {
        this.gastoService = gastoService;
        this.staffService = staffService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.pagoService = pagoService;
        this.financePdfService = financePdfService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "desde", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(name = "hasta", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(name = "categoria", required = false) CategoriaGasto categoria,
                         Model model) {
        model.addAttribute("gastos", gastoService.listarFiltrados(desde, hasta, categoria));
        model.addAttribute("categoriasGasto", CategoriaGasto.values());
        model.addAttribute("desdeFiltro", desde);
        model.addAttribute("hastaFiltro", hasta);
        model.addAttribute("categoriaFiltro", categoria);
        model.addAttribute("gastosTotales", gastoService.contarActivos());
        model.addAttribute("gastosCriticos", gastoService.contarCriticos());
        model.addAttribute("recurrentesProximos", gastoService.contarRecurrentesProximos(7));
        BigDecimal gastoMes = gastoService.calcularGastoMesActual();
        BigDecimal ingresoMes = pagoService.calcularIngresosMesActual();
        model.addAttribute("gastoMesActual", gastoMes);
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
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "gastos/form";
        }

        try {
            gastoService.guardar(gasto);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("gastoError", ex.getMessage());
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "gastos/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto registrado correctamente.");
        return "redirect:/gastos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("gasto", gastoService.buscarPorId(id));
        return "gastos/detail";
    }

    @PostMapping("/{id}/pagado")
    public String marcarPagado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        gastoService.marcarPagado(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto marcado como pagado.");
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
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "gastos/form";
        }

        try {
            gastoService.actualizar(id, gasto);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("gastoError", ex.getMessage());
            prepararRelaciones(gasto);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "gastos/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto actualizado correctamente.");
        return "redirect:/gastos";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        gastoService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto desactivado.");
        return "redirect:/gastos";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        gastoService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto activado.");
        return "redirect:/gastos";
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(name = "desde", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam(name = "hasta", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                             @RequestParam(name = "categoria", required = false) CategoriaGasto categoria) {
        var gastos = gastoService.listarFiltrados(desde, hasta, categoria);
        Map<String, Object> model = new HashMap<>();
        model.put("titulo", "Gastos operativos");
        model.put("subtitulo", "Listado filtrado de egresos y relaciones operativas.");
        model.put("gastos", gastos);
        model.put("gastosTotales", gastoService.contarActivos());
        model.put("gastoMesActual", gastoService.calcularGastoMesActual());
        model.put("ingresoMesActual", pagoService.calcularIngresosMesActual());
        model.put("balanceMesActual", pagoService.calcularIngresosMesActual().subtract(gastoService.calcularGastoMesActual()));
        byte[] pdf = financePdfService.render("reportes/gastos-listado", model);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=gastos-operativos.pdf")
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
}