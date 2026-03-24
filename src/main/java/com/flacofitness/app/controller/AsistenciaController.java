package com.flacofitness.app.controller;

import com.flacofitness.app.model.entity.Asistencia;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.service.AsistenciaService;
import com.flacofitness.app.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final UsuarioService usuarioService;
    private final SmartValidator validator;

    public AsistenciaController(AsistenciaService asistenciaService,
                                UsuarioService usuarioService,
                                SmartValidator validator) {
        this.asistenciaService = asistenciaService;
        this.usuarioService = usuarioService;
        this.validator = validator;
    }

    @GetMapping
    public String listarAsistencias(Model model) {
        model.addAttribute("asistencias", asistenciaService.listarTodas());
        model.addAttribute("tituloListado", "Asistencias");
        model.addAttribute("subtituloListado", "Registro general de asistencias del gimnasio.");
        return "asistencias/list";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(new Usuario());
        model.addAttribute("asistencia", asistencia);
        model.addAttribute("usuarios", usuarioService.listarActivos());
        return "asistencias/form";
    }

    @PostMapping
    public String guardarAsistencia(@Valid @ModelAttribute("asistencia") Asistencia asistencia,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        BindingResult currentResult = ajustarValidacionFechaPorDefecto(asistencia, bindingResult);
        validarUsuarioSeleccionado(asistencia, currentResult);

        if (currentResult.hasErrors()) {
            prepararRelaciones(asistencia);
            model.addAttribute("usuarios", usuarioService.listarActivos());
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "asistencia", currentResult);
            return "asistencias/form";
        }

        asistenciaService.registrar(asistencia);
        redirectAttributes.addFlashAttribute("mensajeExito", "Asistencia registrada correctamente.");
        return "redirect:/asistencias";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("asistencia", asistenciaService.buscarPorId(id));
        return "asistencias/detail";
    }

    @GetMapping("/usuario/{usuarioId}")
    public String listarPorUsuario(@PathVariable Long usuarioId, Model model) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        model.addAttribute("asistencias", asistenciaService.listarPorUsuario(usuarioId));
        model.addAttribute("tituloListado", "Asistencias del usuario");
        model.addAttribute("subtituloListado", "Historial de asistencias de " + construirNombreUsuario(usuario) + ".");
        return "asistencias/list";
    }

    private BindingResult ajustarValidacionFechaPorDefecto(Asistencia asistencia, BindingResult bindingResult) {
        if (asistencia.getFecha() != null) {
            return bindingResult;
        }

        asistencia.setFecha(LocalDate.now());
        BeanPropertyBindingResult revalidatedResult = new BeanPropertyBindingResult(asistencia, "asistencia");
        validator.validate(asistencia, revalidatedResult);
        return revalidatedResult;
    }

    private void prepararRelaciones(Asistencia asistencia) {
        if (asistencia.getUsuario() == null) {
            asistencia.setUsuario(new Usuario());
        }
    }

    private void validarUsuarioSeleccionado(Asistencia asistencia, BindingResult bindingResult) {
        if (asistencia.getUsuario() == null || asistencia.getUsuario().getId() == null) {
            bindingResult.rejectValue("usuario.id", "required", "Debes seleccionar un usuario.");
        }
    }

    private String construirNombreUsuario(Usuario usuario) {
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            return usuario.getNombre();
        }
        return usuario.getNombre() + " " + usuario.getApellidos();
    }
}
