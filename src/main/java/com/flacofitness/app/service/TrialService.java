package com.flacofitness.app.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrialService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final TrialRepository trialRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public TrialService(TrialRepository trialRepository,
                        StaffPerfilRepository staffPerfilRepository,
                        UsuarioRepository usuarioRepository,
                        RolRepository rolRepository) {
        this.trialRepository = trialRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    public List<Trial> listarTodos() {
        return trialRepository.findAllByOrderByFechaPruebaDescIdDesc();
    }

    public List<Trial> listarFiltrados(EstadoTrial estado) {
        if (estado == null) {
            return listarTodos();
        }
        return trialRepository.findAllByEstadoOrderByFechaPruebaDescIdDesc(estado);
    }

    public List<Trial> listarProximos() {
        return trialRepository.findTop6ByFechaPruebaGreaterThanEqualOrderByFechaPruebaAscIdAsc(LocalDate.now());
    }

    public Trial buscarPorId(Long id) {
        return trialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trial no encontrado con id: " + id));
    }

    public long contarPendientes() {
        return trialRepository.countByEstado(EstadoTrial.PENDIENTE);
    }

    public long contarHoy() {
        return trialRepository.countByFechaPruebaAndEstado(LocalDate.now(), EstadoTrial.PENDIENTE);
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
            trial.setFechaPrueba(LocalDate.now());
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
