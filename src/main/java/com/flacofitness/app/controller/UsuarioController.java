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
import com.flacofitness.app.service.ActivityLogService;
import com.flacofitness.app.service.ControllerActivityLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    private final ActivityLogService activityLogService;
    private final ControllerActivityLogger controllerActivityLogger;

    public UsuarioController(UsuarioService usuarioService,
                             UsuarioControlCenterService usuarioControlCenterService,
                             UserPhotoStorageService userPhotoStorageService,
                             RolRepository rolRepository,
                             PlanRepository planRepository,
                             RutinaService rutinaService,
                             PagoService pagoService,
                             ActivityLogService activityLogService,
                             ControllerActivityLogger controllerActivityLogger) {
        this.usuarioService = usuarioService;
        this.usuarioControlCenterService = usuarioControlCenterService;
        this.userPhotoStorageService = userPhotoStorageService;
        this.rolRepository = rolRepository;
        this.planRepository = planRepository;
        this.rutinaService = rutinaService;
        this.pagoService = pagoService;
        this.activityLogService = activityLogService;
        this.controllerActivityLogger = controllerActivityLogger;
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
                                 @RequestParam(name = "fotoFile", required = false) MultipartFile fotoFile,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request,
                                 HttpSession session) {
        asignarRelaciones(usuario);
        validarFotoEnFormulario(fotoFile, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            return "usuarios/form";
        }

        try {
            Usuario guardado = usuarioService.guardar(usuario);
            guardarFotoSiCorresponde(guardado, fotoFile);
            controllerActivityLogger.log(request, session,
                    "usuarios", "usuario_creado", "usuario", guardado.getId(),
                    "Usuario creado",
                    "Se dio de alta a " + construirNombreUsuario(guardado) + ".");
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Usuario creado correctamente. Credenciales activas para " + guardado.getUsername() + ".");
            return "redirect:/usuarios/" + guardado.getId();
        } catch (DuplicateResourceException ex) {
            rejectDuplicate(bindingResult, ex.getMessage());
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            model.addAttribute("mensajeError", ex.getMessage());
            return "usuarios/form";
        } catch (BusinessValidationException ex) {
            bindingResult.reject("usuarioError", ex.getMessage());
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", false);
            model.addAttribute("mensajeError", ex.getMessage());
            return "usuarios/form";
        }
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("controlCenter", usuarioControlCenterService.construirVista(usuario));
        model.addAttribute("deudaTotalUsuario", pagoService.calcularDeudaTotalPorUsuario(id));
        model.addAttribute("activityTimeline", activityLogService.recentByEntity("usuario", id));
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
                                    @RequestParam(name = "fotoFile", required = false) MultipartFile fotoFile,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request,
                                    HttpSession session) {
        asignarRelaciones(usuario);
        validarFotoEnFormulario(fotoFile, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            return "usuarios/form";
        }

        try {
            Usuario actualizado = usuarioService.actualizar(id, usuario);
            guardarFotoSiCorresponde(actualizado, fotoFile);
            controllerActivityLogger.log(request, session,
                    "usuarios", "usuario_actualizado", "usuario", actualizado.getId(),
                    "Usuario actualizado",
                    "Se actualizo la ficha de " + construirNombreUsuario(actualizado) + ".");
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
        } catch (DuplicateResourceException ex) {
            rejectDuplicate(bindingResult, ex.getMessage());
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("mensajeError", ex.getMessage());
            return "usuarios/form";
        } catch (BusinessValidationException ex) {
            bindingResult.reject("usuarioError", ex.getMessage());
            prepararRelaciones(usuario);
            cargarCatalogos(model);
            model.addAttribute("modoEdicion", true);
            model.addAttribute("mensajeError", ex.getMessage());
            return "usuarios/form";
        }

        return "redirect:/usuarios/" + id;
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
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request,
                                    HttpSession session) {
        rutinaService.sincronizarRutinasDeUsuario(id, rutinaIds);
        controllerActivityLogger.log(request, session,
                "rutinas", "rutina_asignada", "usuario", id,
                "Rutinas actualizadas",
                "Se sincronizaron las rutinas del usuario.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Rutinas del usuario actualizadas correctamente.");
        return "redirect:/usuarios/" + id;
    }

    @PostMapping("/{id}/desactivar")
    public String desactivarUsuario(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request,
                                    HttpSession session) {
        usuarioService.desactivar(id);
        controllerActivityLogger.log(request, session,
                "usuarios", "usuario_desactivado", "usuario", id,
                "Usuario desactivado",
                "Se desactivo la cuenta del usuario.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desactivado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/activar")
    public String activarUsuario(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request,
                                 HttpSession session) {
        usuarioService.activar(id);
        controllerActivityLogger.log(request, session,
                "usuarios", "usuario_activado", "usuario", id,
                "Usuario activado",
                "Se reactivo la cuenta del usuario.");
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario activado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request,
                                HttpSession session) {
        String temporal = usuarioService.resetPasswordTemporal(id);
        controllerActivityLogger.log(request, session,
                "usuarios", "password_reseteada", "usuario", id,
                "Password reseteada",
                "Se genero una contrasena temporal para la cuenta.");
        redirectAttributes.addFlashAttribute("mensajeExito",
                "Contrasena temporal generada: " + temporal + ". El usuario debera cambiarla al entrar.");
        return "redirect:/usuarios/" + id;
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

    private String construirNombreUsuario(Usuario usuario) {
        if (usuario == null) {
            return "usuario";
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            return usuario.getNombre();
        }
        return usuario.getNombre() + " " + usuario.getApellidos();
    }

    private void rejectDuplicate(BindingResult bindingResult, String message) {
        if (message != null && message.toLowerCase().contains("username")) {
            bindingResult.rejectValue("username", "duplicate", message);
            return;
        }
        bindingResult.rejectValue("email", "duplicate", message);
    }

    private void validarFotoEnFormulario(MultipartFile fotoFile, BindingResult bindingResult) {
        try {
            userPhotoStorageService.validarFotoUsuario(fotoFile);
        } catch (BusinessValidationException ex) {
            bindingResult.rejectValue("fotoPath", "fotoPath", ex.getMessage());
        }
    }

    private void guardarFotoSiCorresponde(Usuario usuario, MultipartFile fotoFile) {
        if (fotoFile == null || fotoFile.isEmpty()) {
            return;
        }
        String fotoPath = userPhotoStorageService.guardarFotoUsuario(usuario.getId(), fotoFile, usuario.getFotoPath());
        usuarioService.actualizarFotoPath(usuario.getId(), fotoPath);
        usuario.setFotoPath(fotoPath);
    }
}
