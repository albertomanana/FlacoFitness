package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Rutina;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.RutinaRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RutinaService {

    private final RutinaRepository rutinaRepository;
    private final UsuarioRepository usuarioRepository;

    public RutinaService(RutinaRepository rutinaRepository, UsuarioRepository usuarioRepository) {
        this.rutinaRepository = rutinaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Rutina> listarTodas() {
        return rutinaRepository.findAll();
    }

    public List<Rutina> listarPorUsuario(Long usuarioId) {
        return rutinaRepository.findByUsuarioId(usuarioId);
    }

    public Rutina buscarPorId(Long id) {
        return rutinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada con id: " + id));
    }

    @Transactional
    public Rutina guardar(Rutina rutina) {
        rutina.setUsuarios(obtenerUsuariosValidos(rutina.getUsuarios()));
        return rutinaRepository.save(rutina);
    }

    @Transactional
    public Rutina actualizar(Long id, Rutina rutinaActualizada) {
        Rutina rutinaExistente = buscarPorId(id);
        Set<Usuario> usuarios = obtenerUsuariosValidos(rutinaActualizada.getUsuarios());

        rutinaExistente.setNombre(rutinaActualizada.getNombre());
        rutinaExistente.setDescripcion(rutinaActualizada.getDescripcion());
        rutinaExistente.setObjetivo(rutinaActualizada.getObjetivo());
        rutinaExistente.setTipoRutina(rutinaActualizada.getTipoRutina());
        rutinaExistente.setActiva(rutinaActualizada.getActiva());
        rutinaExistente.getUsuarios().clear();
        rutinaExistente.getUsuarios().addAll(usuarios);

        return rutinaRepository.save(rutinaExistente);
    }

    @Transactional
    public void desactivar(Long id) {
        Rutina rutina = buscarPorId(id);
        rutina.setActiva(false);
        rutinaRepository.save(rutina);
    }

    private Set<Usuario> obtenerUsuariosValidos(Set<Usuario> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            throw new BusinessValidationException("La rutina debe estar asociada al menos a un usuario valido");
        }

        Set<Long> usuarioIds = usuarios.stream()
                .map(Usuario::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (usuarioIds.isEmpty()) {
            throw new BusinessValidationException("La rutina debe estar asociada al menos a un usuario valido");
        }

        List<Usuario> usuariosValidados = usuarioRepository.findAllById(usuarioIds);

        if (usuariosValidados.size() != usuarioIds.size()) {
            Set<Long> idsEncontrados = usuariosValidados.stream()
                    .map(Usuario::getId)
                    .collect(Collectors.toSet());

            Long usuarioFaltante = usuarioIds.stream()
                    .filter(id -> !idsEncontrados.contains(id))
                    .findFirst()
                    .orElse(null);

            throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioFaltante);
        }

        return new LinkedHashSet<>(usuariosValidados);
    }
}
