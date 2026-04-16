package com.flacofitness.app.controller;

import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.UsuarioRepository;
import com.flacofitness.app.service.PagoService;
import com.flacofitness.app.service.UsuarioControlCenterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class ClientePortalController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioControlCenterService usuarioControlCenterService;
    private final PagoService pagoService;

    public ClientePortalController(UsuarioRepository usuarioRepository,
                                  UsuarioControlCenterService usuarioControlCenterService,
                                  PagoService pagoService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioControlCenterService = usuarioControlCenterService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public String panel(Model model) {
        Usuario usuario = usuarioRepository.findFirstByActivoTrueAndRolNombreOrderByIdAsc("CLIENTE")
                .or(() -> usuarioRepository.findByActivoTrue().stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("No hay un usuario cliente disponible para el panel"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("controlCenter", usuarioControlCenterService.construirVista(usuario));
        model.addAttribute("deudaTotalUsuario", pagoService.calcularDeudaTotalPorUsuario(usuario.getId()));
        return "cliente/panel";
    }
}
