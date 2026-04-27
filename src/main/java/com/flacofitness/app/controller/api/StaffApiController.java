package com.flacofitness.app.controller.api;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.AsistenciaService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/staff")
@AllArgsConstructor
public class StaffApiController {

    private final AccessSessionService accessSessionService;
    private final UsuarioRepository usuarioRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final AsistenciaService asistenciaService;
    private final com.flacofitness.app.service.OperationalClockService operationalClockService;

    /**
     * Registra asistencia rápida desde panel de recepción.
     * POST /api/staff/registrar-asistencia?usuarioId=123
     */
    @PostMapping("/registrar-asistencia")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarAsistencia(
            @RequestParam Long usuarioId,
            HttpSession session) {
        try {
            obtenerStaffAutenticado(session);
            
            // Validar que existe el usuario
            Usuario cliente = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            // Registrar check-in
            asistenciaService.registrarCheckInRapido(java.util.List.of(usuarioId), null);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Asistencia registrada: " + cliente.getNombre(),
                    "usuarioNombre", cliente.getNombre(),
                    "hora", operationalClockService.time().withSecond(0).withNano(0).toString()
            ));
        } catch (BusinessValidationException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "mensaje", "No se pudo registrar la asistencia."));
        }
    }

    /**
     * Obtiene lista de usuarios activos para dropdown de check-in rápido.
     * GET /api/staff/usuarios-activos
     */
    @GetMapping("/usuarios-activos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerUsuariosActivos(HttpSession session) {
        try {
            obtenerStaffAutenticado(session);
            
            var usuarios = usuarioRepository.findByActivoTrue()
                    .stream()
                    .map(u -> Map.of(
                            "id", u.getId(),
                            "nombre", u.getNombre() + " " + (u.getApellidos() != null ? u.getApellidos() : ""),
                            "email", u.getEmail()
                    ))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "usuarios", usuarios,
                    "count", usuarios.size()
            ));
        } catch (BusinessValidationException | ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "mensaje", "No se pudieron cargar los usuarios activos."));
        }
    }

    private Usuario obtenerStaffAutenticado(HttpSession session) {
        Usuario usuario = accessSessionService.getCurrentUser(session).orElse(null);
        if (usuario == null) {
            throw new BusinessValidationException("Usuario no autenticado");
        }
        
        // Validar que es staff
        StaffPerfil staffPerfil = staffPerfilRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new BusinessValidationException("No eres staff"));
        
        return usuario;
    }
}


