package com.flacofitness.app.controller.api;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.ReservaSesion;
import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoReservaSesion;
import com.flacofitness.app.repository.ReservaSesionRepository;
import com.flacofitness.app.repository.SesionClaseRepository;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.AsistenciaService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/cliente")
@AllArgsConstructor
public class ClienteApiController {

    private final AccessSessionService accessSessionService;
    private final SesionClaseRepository sesionClaseRepository;
    private final ReservaSesionRepository reservaSesionRepository;
    private final AsistenciaService asistenciaService;

    @PostMapping("/reservar-sesion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reservarSesion(
            @RequestParam Long sesionId,
            HttpSession session) {
        try {
            Usuario usuario = obtenerUsuarioAutenticado(session);
            SesionClase sesion = sesionClaseRepository.findById(sesionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

            // Validar que no esté ya reservado
            boolean yaReservado = reservaSesionRepository
                    .findBySesionClaseIdAndUsuarioId(sesionId, usuario.getId())
                    .filter(r -> r.getEstado() == EstadoReservaSesion.RESERVADA)
                    .isPresent();

            if (yaReservado) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "mensaje", "Ya tienes una reserva en esta sesión"));
            }

            // Validar cupo disponible
            long reservasActivas = reservaSesionRepository.countBySesionClaseIdAndEstado(sesionId, EstadoReservaSesion.RESERVADA);
            if (reservasActivas >= sesion.getAforo()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "mensaje", "No hay cupo disponible en esta sesión"));
            }

            // Crear reserva
            ReservaSesion reserva = new ReservaSesion();
            reserva.setUsuario(usuario);
            reserva.setSesionClase(sesion);
            reserva.setEstado(EstadoReservaSesion.RESERVADA);
            reservaSesionRepository.save(reserva);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Reserva confirmada",
                    "sesionNombre", sesion.getClase().getNombre(),
                    "fecha", sesion.getFecha().toString()
            ));
        } catch (BusinessValidationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "mensaje", "No se pudo completar la reserva."));
        }
    }

    @DeleteMapping("/cancelar-reserva/{reservaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelarReserva(
            @PathVariable Long reservaId,
            HttpSession session) {
        try {
            Usuario usuario = obtenerUsuarioAutenticado(session);
            ReservaSesion reserva = reservaSesionRepository.findById(reservaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

            // Validar que pertenezca al usuario
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "mensaje", "No puedes cancelar esta reserva"));
            }

            reserva.setEstado(EstadoReservaSesion.CANCELADA);
            reservaSesionRepository.save(reserva);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Reserva cancelada"
            ));
        } catch (BusinessValidationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "mensaje", "No se pudo cancelar la reserva."));
        }
    }

    @PostMapping("/check-in")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarCheckIn(
            @RequestParam(required = false) Long sesionId,
            HttpSession session) {
        try {
            Usuario usuario = obtenerUsuarioAutenticado(session);

            if (sesionId == null) {
                asistenciaService.registrarCheckInRapido(java.util.List.of(usuario.getId()), null);
            } else {
                sesionClaseRepository.findById(sesionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));
                asistenciaService.registrarCheckInRapido(java.util.List.of(usuario.getId()), null);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Check-in registrado correctamente"
            ));
        } catch (BusinessValidationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "mensaje", "No se pudo registrar el check-in."));
        }
    }

    private Usuario obtenerUsuarioAutenticado(HttpSession session) {
        Usuario usuario = accessSessionService.getCurrentUser(session).orElse(null);
        if (usuario == null) {
            throw new BusinessValidationException("Usuario no autenticado");
        }
        return usuario;
    }
}


