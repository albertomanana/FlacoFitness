package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Rol;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.repository.RolRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private static final String ROL_STAFF = "STAFF";

    private final StaffPerfilRepository staffPerfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public StaffService(StaffPerfilRepository staffPerfilRepository,
                        UsuarioRepository usuarioRepository,
                        RolRepository rolRepository) {
        this.staffPerfilRepository = staffPerfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
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
            staffPerfil.setFechaAlta(LocalDate.now());
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
