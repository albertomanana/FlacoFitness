package com.flacofitness.app.controller;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Rol;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.PlanRepository;
import com.flacofitness.app.repository.RolRepository;
import com.flacofitness.app.service.UserPhotoStorageService;
import com.flacofitness.app.service.UsuarioControlCenterService;
import com.flacofitness.app.service.UsuarioService;
import com.flacofitness.app.service.RutinaService;
import com.flacofitness.app.service.PagoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioControlCenterService usuarioControlCenterService;
    private final UserPhotoStorageService userPhotoStorageService;
    private final RolRepository rolRepository;
    private final PlanRepository planRepository;
    private final RutinaService rutinaService;
    private final PagoService pagoService;

    public UsuarioController(UsuarioService usuarioService,
                             UsuarioControlCenterService usuarioControlCenterService,
                             UserPhotoStorageService userPhotoStorageService,
                             RolRepository rolRepository,
                             PlanRepository planRepository,
                             RutinaService rutinaService,
                             PagoService pagoService) {
        this.usuarioService = usuarioService;
        this.usuarioControlCenterService = usuarioControlCenterService;
        this.userPhotoStorageService = userPhotoStorageService;
        this.rolRepository = rolRepository;
        this.planRepository = planRepository;
        this.rutinaService = rutinaService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Usuario usuario = new Usuario();
        usuario.setActivo(true);
        prepararRelaciones(usuario);
        cargarCatalogos(model);
        model.addAttribute("usuario", usuario);
        model.addAttribute("modoEdicion", false);
        return "usuarios/form";
    }

    @PostMapping
    public String guardarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        asignarRelaciones(usuario);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "usuarios/form";
        }

        try {
            usuarioService.guardar(usuario);
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "usuarios/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario creado correctamente.");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("controlCenter", usuarioControlCenterService.construirVista(usuario));
        model.addAttribute("deudaTotalUsuario", pagoService.calcularDeudaTotalPorUsuario(id));
        return "usuarios/detail";
    }

    @PostMapping("/{id}/foto")
    public String subirFoto(@PathVariable Long id,
                            @RequestParam("foto") MultipartFile foto,
                            @RequestParam(value = "redirectTo", defaultValue = "detail") String redirectTo,
                            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.buscarPorId(id);

        try {
            String fotoPath = userPhotoStorageService.guardarFotoUsuario(id, foto, usuario.getFotoPath());
            usuarioService.actualizarFotoPath(id, fotoPath);
            redirectAttributes.addFlashAttribute("mensajeExito", "Foto actualizada correctamente.");
        } catch (BusinessValidationException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        if ("editar".equalsIgnoreCase(redirectTo)) {
            return "redirect:/usuarios/" + id + "/editar#foto-panel";
        }

        return "redirect:/usuarios/" + id;
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        prepararRelaciones(usuario);
        cargarCatalogos(model);
        model.addAttribute("usuario", usuario);
        model.addAttribute("modoEdicion", true);
        return "usuarios/form";
    }

    @PostMapping("/{id}")
    public String actualizarUsuario(@PathVariable Long id,
                                    @Valid @ModelAttribute("usuario") Usuario usuario,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        asignarRelaciones(usuario);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "usuarios/form";
        }

        try {
            usuarioService.actualizar(id, usuario);
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "usuarios/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/rutinas")
    public String mostrarFormularioRutinas(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        // We get all routines to allow assigning
        model.addAttribute("todasLasRutinas", rutinaService.listarTodas());
        model.addAttribute("rutinasAsignadas", rutinaService.listarPorUsuario(id));
        return "usuarios/rutinas-form";
    }

    @PostMapping("/{id}/rutinas")
    public String actualizarRutinas(@PathVariable Long id,
                                    @RequestParam(name = "rutinaIds", required = false) List<Long> rutinaIds,
                                    RedirectAttributes redirectAttributes) {
        rutinaService.sincronizarRutinasDeUsuario(id, rutinaIds);
        redirectAttributes.addFlashAttribute("mensajeExito", "Rutinas del usuario actualizadas correctamente.");
        return "redirect:/usuarios/" + id;
    }

    @PostMapping("/{id}/desactivar")
    public String desactivarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desactivado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/activar")
    public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.activar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario activado correctamente.");
        return "redirect:/usuarios";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("roles", rolRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre")));
        model.addAttribute("planes", planRepository.findByActivoTrue());
    }

    private void prepararRelaciones(Usuario usuario) {
        if (usuario.getRol() == null) {
            usuario.setRol(new Rol());
        }
        if (usuario.getPlan() == null) {
            usuario.setPlan(new Plan());
        }
    }

    private void asignarRelaciones(Usuario usuario) {
        if (usuario.getRol() != null && usuario.getRol().getId() != null) {
            Rol rol = rolRepository.findById(usuario.getRol().getId()).orElse(null);
            usuario.setRol(rol);
        } else {
            usuario.setRol(null);
        }

        if (usuario.getPlan() != null && usuario.getPlan().getId() != null) {
            Plan plan = planRepository.findById(usuario.getPlan().getId()).orElse(null);
            usuario.setPlan(plan);
        } else {
            usuario.setPlan(null);
        }
    }
}
