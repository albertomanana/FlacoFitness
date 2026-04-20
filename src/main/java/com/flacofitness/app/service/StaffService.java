package com.flacofitness.app.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Rol;
import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoReservaSesion;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.repository.AsistenciaRepository;
import com.flacofitness.app.repository.ReservaSesionRepository;
import com.flacofitness.app.repository.RolRepository;
import com.flacofitness.app.repository.SesionClaseRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private static final String ROL_STAFF = "STAFF";

    private final StaffPerfilRepository staffPerfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SesionClaseRepository sesionClaseRepository;
    private final ReservaSesionRepository reservaSesionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final OperationalClockService operationalClockService;

    public StaffService(StaffPerfilRepository staffPerfilRepository,
                        UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        SesionClaseRepository sesionClaseRepository,
                        ReservaSesionRepository reservaSesionRepository,
                        AsistenciaRepository asistenciaRepository,
                        OperationalClockService operationalClockService) {
        this.staffPerfilRepository = staffPerfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sesionClaseRepository = sesionClaseRepository;
        this.reservaSesionRepository = reservaSesionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.operationalClockService = operationalClockService;
    }

    public List<StaffPerfil> listarTodos() {
        return staffPerfilRepository.findAll();
    }

    public List<StaffPerfil> listarActivos() {
        return staffPerfilRepository.findByActivoTrue();
    }

    public List<StaffPerfil> listarActivosParaEntrenamiento() {
        return staffPerfilRepository.findByActivoTrueAndPuedeImpartirClasesTrue();
    }

    public long contarActivos() {
        return staffPerfilRepository.countByActivoTrue();
    }

    public StaffPerfil buscarPorId(Long id) {
        return staffPerfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de staff no encontrado con id: " + id));
    }

    public Optional<StaffPerfil> buscarPorUsuario(Long usuarioId) {
        return staffPerfilRepository.findByUsuarioId(usuarioId);
    }

    public List<SesionClase> listarSesionesHoy(Long staffId) {
        return sesionClaseRepository.findByFechaAndStaffResponsableIdOrderByHoraInicioAscIdAsc(operationalClockService.today(), staffId);
    }

    public long contarMiembrosAsignadosHoy(Long staffId) {
        Set<Long> usuarioIds = new HashSet<>();
        List<SesionClase> sesionesHoy = listarSesionesHoy(staffId);
        for (SesionClase sesion : sesionesHoy) {
            reservaSesionRepository.findBySesionClaseIdOrderByFechaReservaDescIdDesc(sesion.getId()).stream()
                    .filter(reserva -> reserva.getEstado() == EstadoReservaSesion.RESERVADA
                            || reserva.getEstado() == EstadoReservaSesion.ASISTIO)
                    .map(reserva -> reserva.getUsuario().getId())
                    .forEach(usuarioIds::add);

            asistenciaRepository.findBySesionClaseIdOrderByHoraEntradaDescIdDesc(sesion.getId()).stream()
                    .map(asistencia -> asistencia.getUsuario().getId())
                    .forEach(usuarioIds::add);
        }
        return usuarioIds.size();
    }

    public List<Usuario> listarClientesInactivosAsignados(Long staffId, int diasSinActividad) {
        Set<Long> usuariosAsignados = new HashSet<>();
        List<SesionClase> sesiones = sesionClaseRepository.findByStaffResponsableIdOrderByFechaDescHoraInicioDescIdDesc(staffId);

        for (SesionClase sesion : sesiones) {
            reservaSesionRepository.findBySesionClaseIdOrderByFechaReservaDescIdDesc(sesion.getId()).stream()
                    .map(reserva -> reserva.getUsuario().getId())
                    .forEach(usuariosAsignados::add);
        }

        LocalDate hoy = operationalClockService.today();
        return usuariosAsignados.stream()
                .map(usuarioRepository::findById)
                .flatMap(Optional::stream)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .filter(usuario -> asistenciaRepository.findTopByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(usuario.getId())
                        .map(ultima -> ChronoUnit.DAYS.between(ultima.getFecha(), hoy) >= Math.max(1, diasSinActividad))
                        .orElse(true))
                .limit(8)
                .toList();
    }

    @Transactional
    public StaffPerfil guardar(StaffPerfil staffPerfil) {
        Usuario usuario = obtenerUsuarioValido(staffPerfil.getUsuario());

        if (staffPerfilRepository.existsByUsuarioId(usuario.getId())) {
            throw new DuplicateResourceException("El usuario seleccionado ya tiene perfil de staff");
        }

        staffPerfil.setUsuario(usuario);
        normalizarPerfil(staffPerfil);
        asegurarRolStaff(usuario);
        return staffPerfilRepository.save(staffPerfil);
    }

    @Transactional
    public StaffPerfil actualizar(Long id, StaffPerfil staffActualizado) {
        StaffPerfil staffExistente = buscarPorId(id);
        Usuario usuario = obtenerUsuarioValido(staffActualizado.getUsuario());

        staffPerfilRepository.findByUsuarioId(usuario.getId())
                .filter(perfil -> !perfil.getId().equals(id))
                .ifPresent(perfil -> {
                    throw new DuplicateResourceException("El usuario seleccionado ya tiene perfil de staff");
                });

        staffExistente.setUsuario(usuario);
        staffExistente.setEspecialidad(staffActualizado.getEspecialidad());
        staffExistente.setRolStaff(staffActualizado.getRolStaff());
        staffExistente.setActivo(staffActualizado.getActivo());
        staffExistente.setPuedeImpartirClases(staffActualizado.getPuedeImpartirClases());
        staffExistente.setFechaAlta(staffActualizado.getFechaAlta());
        staffExistente.setObservaciones(staffActualizado.getObservaciones());
        normalizarPerfil(staffExistente);
        asegurarRolStaff(usuario);
        return staffPerfilRepository.save(staffExistente);
    }

    @Transactional
    public void desactivar(Long id) {
        StaffPerfil staffPerfil = buscarPorId(id);
        staffPerfil.setActivo(false);
        staffPerfilRepository.save(staffPerfil);
    }

    @Transactional
    public void activar(Long id) {
        StaffPerfil staffPerfil = buscarPorId(id);
        staffPerfil.setActivo(true);
        staffPerfilRepository.save(staffPerfil);
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessValidationException("Debes seleccionar un usuario para el perfil de staff");
        }

        return usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuario.getId()));
    }

    private void normalizarPerfil(StaffPerfil staffPerfil) {
        if (staffPerfil.getFechaAlta() == null) {
            staffPerfil.setFechaAlta(operationalClockService.today());
        }
        if (staffPerfil.getActivo() == null) {
            staffPerfil.setActivo(true);
        }
        if (staffPerfil.getRolStaff() == null) {
            staffPerfil.setRolStaff(RolStaff.ENTRENADOR);
        }
        if (staffPerfil.getPuedeImpartirClases() == null) {
            staffPerfil.setPuedeImpartirClases(staffPerfil.getRolStaff() == RolStaff.ENTRENADOR);
        }
    }

    private void asegurarRolStaff(Usuario usuario) {
        Rol rolStaff = rolRepository.findByNombre(ROL_STAFF).orElse(null);
        if (rolStaff != null && (usuario.getRol() == null || !ROL_STAFF.equalsIgnoreCase(usuario.getRol().getNombre()))) {
            usuario.setRol(rolStaff);
            usuarioRepository.save(usuario);
        }
    }
}
