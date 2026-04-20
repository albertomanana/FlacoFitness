package com.flacofitness.app.controller;

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

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.enums.CategoriaMaterial;
import com.flacofitness.app.model.enums.EstadoMaterial;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaterialService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/materiales")
public class MaterialController {

    private final MaterialService materialService;
    private final GastoService gastoService;

    public MaterialController(MaterialService materialService,
                              GastoService gastoService) {
        this.materialService = materialService;
        this.gastoService = gastoService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "estado", required = false) EstadoMaterial estado,
                         @RequestParam(name = "categoria", required = false) CategoriaMaterial categoria,
                         Model model) {
        model.addAttribute("materiales", materialService.listarFiltrados(estado, categoria));
        model.addAttribute("estadosMaterial", EstadoMaterial.values());
        model.addAttribute("categoriasMaterial", CategoriaMaterial.values());
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("categoriaFiltro", categoria);
        model.addAttribute("materialesActivos", materialService.contarActivos());
        model.addAttribute("materialesBajoStock", materialService.contarBajoStock());
        return "materiales/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Material material = new Material();
        model.addAttribute("material", material);
        model.addAttribute("modoEdicion", false);
        cargarCatalogos(model);
        return "materiales/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("material") Material material,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "materiales/form";
        }

        try {
            materialService.guardar(material);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("materialError", ex.getMessage());
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "materiales/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Material creado correctamente.");
        return "redirect:/materiales";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("material", materialService.buscarPorId(id));
        model.addAttribute("gastosRelacionados", gastoService.listarPorMaterial(id).stream().limit(6).toList());
        return "materiales/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("material", materialService.buscarPorId(id));
        model.addAttribute("modoEdicion", true);
        cargarCatalogos(model);
        return "materiales/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("material") Material material,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "materiales/form";
        }

        try {
            materialService.actualizar(id, material);
        } catch (BusinessValidationException ex) {
            bindingResult.reject("materialError", ex.getMessage());
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "materiales/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Material actualizado correctamente.");
        return "redirect:/materiales";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        materialService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Material desactivado.");
        return "redirect:/materiales";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        materialService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Material activado.");
        return "redirect:/materiales";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("estadosMaterial", EstadoMaterial.values());
        model.addAttribute("categoriasMaterial", CategoriaMaterial.values());
    }
}