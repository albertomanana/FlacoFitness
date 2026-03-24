package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Rutina;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.RutinaRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Usuario usuario = obtenerUsuarioValido(rutina.getUsuario());
        rutina.setUsuario(usuario);
        return rutinaRepository.save(rutina);
    }

    @Transactional
    public Rutina actualizar(Long id, Rutina rutinaActualizada) {
        Rutina rutinaExistente = buscarPorId(id);
        Usuario usuario = obtenerUsuarioValido(rutinaActualizada.getUsuario());

        rutinaExistente.setNombre(rutinaActualizada.getNombre());
        rutinaExistente.setDescripcion(rutinaActualizada.getDescripcion());
        rutinaExistente.setObjetivo(rutinaActualizada.getObjetivo());
        rutinaExistente.setActiva(rutinaActualizada.getActiva());
        rutinaExistente.setUsuario(usuario);

        return rutinaRepository.save(rutinaExistente);
    }

    @Transactional
    public void desactivar(Long id) {
        Rutina rutina = buscarPorId(id);
        rutina.setActiva(false);
        rutinaRepository.save(rutina);
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessValidationException("La rutina debe estar asociada a un usuario válido");
        }

        return usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuario.getId()));
    }
}
