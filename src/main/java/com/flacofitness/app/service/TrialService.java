package com.flacofitness.app.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Rol;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoTrial;
import com.flacofitness.app.repository.RolRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.TrialRepository;
import com.flacofitness.app.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class TrialService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final TrialRepository trialRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final OperationalClockService operationalClockService;

    public TrialService(TrialRepository trialRepository,
                        StaffPerfilRepository staffPerfilRepository,
                        UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        OperationalClockService operationalClockService) {
        this.trialRepository = trialRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.operationalClockService = operationalClockService;
    }

    public List<Trial> listarTodos() {
        return trialRepository.findAllByOrderByFechaPruebaDescIdDesc();
    }

    public List<Trial> listarFiltrados(LocalDate desde, LocalDate hasta, EstadoTrial estado) {
        if (desde != null && hasta != null) {
            if (estado != null) {
                return trialRepository.findAllByEstadoAndFechaPruebaBetweenOrderByFechaPruebaDescIdDesc(estado, desde, hasta);
            }
            return trialRepository.findAllByFechaPruebaBetweenOrderByFechaPruebaDescIdDesc(desde, hasta);
        }
        if (estado != null) {
            return trialRepository.findAllByEstadoOrderByFechaPruebaDescIdDesc(estado);
        }
        return listarTodos();
    }

    public List<Trial> listarProximos() {
        return trialRepository.findTop6ByFechaPruebaGreaterThanEqualOrderByFechaPruebaAscIdAsc(operationalClockService.today());
    }

    public Trial buscarPorId(Long id) {
        return trialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trial no encontrado con id: " + id));
    }

    public long contarPendientes() {
        return trialRepository.countByEstado(EstadoTrial.PENDIENTE);
    }

    public long contarHoy() {
        return trialRepository.countByFechaPruebaAndEstado(operationalClockService.today(), EstadoTrial.PENDIENTE);
    }

    public long contarSemanaActual() {
        LocalDate hoy = operationalClockService.today();
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate finSemana = inicioSemana.plusDays(6);
        return trialRepository.countByFechaPruebaBetween(inicioSemana, finSemana);
    }

    public long contarSinSeguimiento(int dias) {
        LocalDate limite = operationalClockService.today().minusDays(Math.max(1, dias));
        return trialRepository.countByEstadoAndFechaPruebaLessThanEqual(EstadoTrial.NO_ASISTIO, limite)
                + trialRepository.countByEstadoAndFechaPruebaLessThanEqual(EstadoTrial.PENDIENTE, limite);
    }

    public List<Trial> listarSinSeguimiento(int dias) {
        LocalDate limite = operationalClockService.today().minusDays(Math.max(1, dias));
        return trialRepository.findByEstadoAndFechaPruebaLessThanEqualOrderByFechaPruebaAscIdAsc(EstadoTrial.PENDIENTE, limite);
    }

    @Transactional
    public Trial guardar(Trial trial) {
        normalizar(trial);
        return trialRepository.save(trial);
    }

    @Transactional
    public Trial actualizar(Long id, Trial trialActualizado) {
        Trial trial = buscarPorId(id);
        trial.setNombre(trialActualizado.getNombre());
        trial.setApellidos(trialActualizado.getApellidos());
        trial.setTelefono(trialActualizado.getTelefono());
        trial.setEmail(trialActualizado.getEmail());
        trial.setOrigen(trialActualizado.getOrigen());
        trial.setFechaPrueba(trialActualizado.getFechaPrueba());
        trial.setEstado(trialActualizado.getEstado());
        trial.setStaffResponsable(trialActualizado.getStaffResponsable());
        trial.setObservaciones(trialActualizado.getObservaciones());
        normalizar(trial);
        return trialRepository.save(trial);
    }

    @Transactional
    public Trial actualizarEstado(Long id, EstadoTrial estado) {
        Trial trial = buscarPorId(id);
        trial.setEstado(estado != null ? estado : EstadoTrial.PENDIENTE);
        return trialRepository.save(trial);
    }

    @Transactional
    public Trial marcarAsistencia(Long id, boolean asistio) {
        Trial trial = buscarPorId(id);
        trial.setEstado(asistio ? EstadoTrial.ASISTIO : EstadoTrial.NO_ASISTIO);
        return trialRepository.save(trial);
    }

    @Transactional
    public Usuario convertirAUsuario(Long trialId) {
        Trial trial = buscarPorId(trialId);
        if (trial.getEmail() == null || trial.getEmail().isBlank()) {
            throw new BusinessValidationException("Para convertir un trial a usuario debe tener email");
        }

        Usuario usuario = usuarioRepository.findByEmail(trial.getEmail())
                .orElseGet(() -> crearUsuarioDesdeTrial(trial));

        trial.setUsuarioConvertido(usuario);
        trial.setEstado(EstadoTrial.CONVERTIDO);
        trialRepository.save(trial);
        return usuario;
    }

    private Usuario crearUsuarioDesdeTrial(Trial trial) {
        Usuario usuario = new Usuario();
        usuario.setNombre(trial.getNombre());
        usuario.setApellidos(trial.getApellidos());
        usuario.setEmail(trial.getEmail());
        usuario.setTelefono(trial.getTelefono());
        usuario.setActivo(true);
        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE).orElse(null);
        usuario.setRol(rolCliente);
        return usuarioRepository.save(usuario);
    }

    private void normalizar(Trial trial) {
        if (trial.getFechaPrueba() == null) {
            trial.setFechaPrueba(operationalClockService.today());
        }
        if (trial.getEstado() == null) {
            trial.setEstado(EstadoTrial.PENDIENTE);
        }
        if (trial.getStaffResponsable() != null && trial.getStaffResponsable().getId() != null) {
            StaffPerfil staff = staffPerfilRepository.findById(trial.getStaffResponsable().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Staff no encontrado con id: " + trial.getStaffResponsable().getId()));
            trial.setStaffResponsable(staff);
        } else {
            trial.setStaffResponsable(null);
        }
    }
}
