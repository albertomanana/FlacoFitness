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
import com.flacofitness.app.service.ControllerActivityLogger;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaterialService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/materiales")
public class MaterialController {

    private final MaterialService materialService;
    private final GastoService gastoService;
    private final ControllerActivityLogger controllerActivityLogger;

    public MaterialController(MaterialService materialService,
                              GastoService gastoService,
                              ControllerActivityLogger controllerActivityLogger) {
        this.materialService = materialService;
        this.gastoService = gastoService;
        this.controllerActivityLogger = controllerActivityLogger;
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
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request,
                          HttpSession session) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "materiales/form";
        }

        try {
            Material guardado = materialService.guardar(material);
            controllerActivityLogger.log(request, session,
                    "materiales", "material_creado", "material", guardado.getId(),
                    "Material creado",
                    "Se registro el material " + guardado.getNombre() + ".");
            redirectAttributes.addFlashAttribute("mensajeExito", "Material creado correctamente.");
            return "redirect:/materiales/" + guardado.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("materialError", ex.getMessage());
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "materiales/form";
        }
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
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpSession session) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "materiales/form";
        }

        try {
            Material actualizado = materialService.actualizar(id, material);
            controllerActivityLogger.log(request, session,
                    "materiales", "material_actualizado", "material", actualizado.getId(),
                    "Material actualizado",
                    "Se actualizo la ficha del material " + actualizado.getNombre() + ".");
            redirectAttributes.addFlashAttribute("mensajeExito", "Material actualizado correctamente.");
            return "redirect:/materiales/" + actualizado.getId();
        } catch (BusinessValidationException ex) {
            bindingResult.reject("materialError", ex.getMessage());
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "materiales/form";
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(name = "returnTo", required = false) String returnTo,
                             HttpServletRequest request,
                             HttpSession session) {
        materialService.desactivar(id);
        controllerActivityLogger.log(request, session,
                "materiales", "material_desactivado", "material", id,
                "Material desactivado",
                "Se retiro temporalmente un material del catalogo operativo.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Material desactivado.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id,
                          RedirectAttributes redirectAttributes,
                          @RequestParam(name = "returnTo", required = false) String returnTo,
                          HttpServletRequest request,
                          HttpSession session) {
        materialService.activar(id);
        controllerActivityLogger.log(request, session,
                "materiales", "material_activado", "material", id,
                "Material activado",
                "Se reactivo un material para volver a usarlo.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Material activado.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("estadosMaterial", EstadoMaterial.values());
        model.addAttribute("categoriasMaterial", CategoriaMaterial.values());
    }

    private String resolveReturnPath(Long id, String returnTo) {
        if ("detail".equalsIgnoreCase(returnTo)) {
            return "/materiales/" + id;
        }
        return "/materiales";
    }
}
