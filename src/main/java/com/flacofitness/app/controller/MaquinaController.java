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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.enums.CategoriaMaquina;
import com.flacofitness.app.model.enums.EstadoMaquina;
import com.flacofitness.app.service.ControllerActivityLogger;
import com.flacofitness.app.service.GastoService;
import com.flacofitness.app.service.MaquinaPhotoStorageService;
import com.flacofitness.app.service.MaquinaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/maquinas")
public class MaquinaController {

    private final MaquinaService maquinaService;
    private final MaquinaPhotoStorageService maquinaPhotoStorageService;
    private final GastoService gastoService;
    private final ControllerActivityLogger controllerActivityLogger;

    public MaquinaController(MaquinaService maquinaService,
                             MaquinaPhotoStorageService maquinaPhotoStorageService,
                             GastoService gastoService,
                             ControllerActivityLogger controllerActivityLogger) {
        this.maquinaService = maquinaService;
        this.maquinaPhotoStorageService = maquinaPhotoStorageService;
        this.gastoService = gastoService;
        this.controllerActivityLogger = controllerActivityLogger;
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
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request,
                          HttpSession session) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "maquinas/form";
        }

        try {
            Maquina guardada = maquinaService.guardar(maquina);
            controllerActivityLogger.log(request, session,
                    "maquinas", "maquina_creada", "maquina", guardada.getId(),
                    "Maquina creada",
                    "Se registro la maquina " + guardada.getNombre() + ".");
            redirectAttributes.addFlashAttribute("mensajeExito", "Maquina creada correctamente.");
            return "redirect:/maquinas/" + guardada.getId();
        } catch (BusinessValidationException | com.flacofitness.app.exception.DuplicateResourceException ex) {
            bindingResult.reject("maquinaError", ex.getMessage());
            model.addAttribute("modoEdicion", false);
            cargarCatalogos(model);
            return "maquinas/form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("maquina", maquinaService.buscarPorId(id));
        model.addAttribute("gastosRelacionados", gastoService.listarPorMaquina(id).stream().limit(6).toList());
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
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpSession session) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "maquinas/form";
        }

        try {
            Maquina actualizada = maquinaService.actualizar(id, maquina);
            controllerActivityLogger.log(request, session,
                    "maquinas", "maquina_actualizada", "maquina", actualizada.getId(),
                    "Maquina actualizada",
                    "Se actualizo la ficha de la maquina " + actualizada.getNombre() + ".");
            redirectAttributes.addFlashAttribute("mensajeExito", "Maquina actualizada correctamente.");
            return "redirect:/maquinas/" + actualizada.getId();
        } catch (BusinessValidationException | com.flacofitness.app.exception.DuplicateResourceException ex) {
            bindingResult.reject("maquinaError", ex.getMessage());
            model.addAttribute("modoEdicion", true);
            cargarCatalogos(model);
            return "maquinas/form";
        }
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
    public String desactivar(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(name = "returnTo", required = false) String returnTo,
                             HttpServletRequest request,
                             HttpSession session) {
        maquinaService.desactivar(id);
        controllerActivityLogger.log(request, session,
                "maquinas", "maquina_desactivada", "maquina", id,
                "Maquina desactivada",
                "Se saco temporalmente una maquina de servicio.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Maquina desactivada.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id,
                          RedirectAttributes redirectAttributes,
                          @RequestParam(name = "returnTo", required = false) String returnTo,
                          HttpServletRequest request,
                          HttpSession session) {
        maquinaService.activar(id);
        controllerActivityLogger.log(request, session,
                "maquinas", "maquina_activada", "maquina", id,
                "Maquina activada",
                "Se devolvio una maquina al inventario operativo.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Maquina activada.");
        return "redirect:" + resolveReturnPath(id, returnTo);
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("estadosMaquina", EstadoMaquina.values());
        model.addAttribute("categoriasMaquina", CategoriaMaquina.values());
    }

    private String resolveReturnPath(Long id, String returnTo) {
        if ("detail".equalsIgnoreCase(returnTo)) {
            return "/maquinas/" + id;
        }
        return "/maquinas";
    }
}
