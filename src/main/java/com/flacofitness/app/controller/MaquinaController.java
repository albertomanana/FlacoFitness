package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.enums.CategoriaMaquina;
import com.flacofitness.app.model.enums.EstadoMaquina;
import com.flacofitness.app.service.MaquinaPhotoStorageService;
import com.flacofitness.app.service.MaquinaService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/maquinas")
public class MaquinaController {

    private final MaquinaService maquinaService;
    private final MaquinaPhotoStorageService maquinaPhotoStorageService;

    public MaquinaController(MaquinaService maquinaService,
                             MaquinaPhotoStorageService maquinaPhotoStorageService) {
        this.maquinaService = maquinaService;
        this.maquinaPhotoStorageService = maquinaPhotoStorageService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "estado", required = false) EstadoMaquina estado,
                         @RequestParam(name = "categoria", required = false) CategoriaMaquina categoria,
                         Model model) {
        model.addAttribute("maquinas", maquinaService.listarFiltradas(estado, categoria));
        model.addAttribute("estadosMaquina", EstadoMaquina.values());
        model.addAttribute("categoriasMaquina", CategoriaMaquina.values());
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("categoriaFiltro", categoria);
        model.addAttribute("maquinasActivas", maquinaService.contarActivas());
        model.addAttribute("maquinasFueraServicio", maquinaService.contarFueraDeServicio());
        model.addAttribute("revisionesProximas", maquinaService.contarRevisionProxima(7));
        return "maquinas/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Maquina maquina = new Maquina();
        model.addAttribute("maquina", maquina);
        model.addAttribute("modoEdicion", false);
        cargarCatalogos(model);
        return "maquinas/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("maquina") Maquina maquina,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "maquinas/form";
        }

        try {
            maquinaService.guardar(maquina);
        } catch (BusinessValidationException | com.flacofitness.app.exception.DuplicateResourceException ex) {
            bindingResult.reject("maquinaError", ex.getMessage());
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "maquinas/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Maquina creada correctamente.");
        return "redirect:/maquinas";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("maquina", maquinaService.buscarPorId(id));
        return "maquinas/detail";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("maquina", maquinaService.buscarPorId(id));
        model.addAttribute("modoEdicion", true);
        cargarCatalogos(model);
        return "maquinas/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("maquina") Maquina maquina,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "maquinas/form";
        }

        try {
            maquinaService.actualizar(id, maquina);
        } catch (BusinessValidationException | com.flacofitness.app.exception.DuplicateResourceException ex) {
            bindingResult.reject("maquinaError", ex.getMessage());
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "maquinas/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Maquina actualizada correctamente.");
        return "redirect:/maquinas";
    }

    @PostMapping("/{id}/foto")
    public String subirFoto(@PathVariable Long id,
                            @RequestParam("foto") MultipartFile foto,
                            @RequestParam(value = "redirectTo", defaultValue = "detail") String redirectTo,
                            RedirectAttributes redirectAttributes) {
        Maquina maquina = maquinaService.buscarPorId(id);

        try {
            String fotoPath = maquinaPhotoStorageService.guardarFotoMaquina(id, foto, maquina.getFotoPath());
            maquinaService.actualizarFotoPath(id, fotoPath);
            redirectAttributes.addFlashAttribute("mensajeExito", "Foto de la maquina actualizada correctamente.");
        } catch (BusinessValidationException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        if ("editar".equalsIgnoreCase(redirectTo)) {
            return "redirect:/maquinas/" + id + "/editar#foto-panel";
        }

        return "redirect:/maquinas/" + id;
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        maquinaService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Maquina desactivada.");
        return "redirect:/maquinas";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        maquinaService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Maquina activada.");
        return "redirect:/maquinas";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("estadosMaquina", EstadoMaquina.values());
        model.addAttribute("categoriasMaquina", CategoriaMaquina.values());
    }
}