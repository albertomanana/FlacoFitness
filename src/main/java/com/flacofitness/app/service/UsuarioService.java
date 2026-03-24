package com.flacofitness.app.service;

import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        validarEmailDuplicado(usuario.getEmail(), null);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        Usuario usuarioExistente = buscarPorId(id);
        validarEmailDuplicado(usuarioActualizado.getEmail(), id);

        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setActivo(usuarioActualizado.getActivo());
        usuarioExistente.setRol(usuarioActualizado.getRol());
        usuarioExistente.setPlan(usuarioActualizado.getPlan());

        return usuarioRepository.save(usuarioExistente);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    private void validarEmailDuplicado(String email, Long usuarioIdActual) {
        usuarioRepository.findByEmail(email)
                .filter(usuario -> !usuario.getId().equals(usuarioIdActual))
                .ifPresent(usuario -> {
                    throw new DuplicateResourceException("Ya existe un usuario con email: " + email);
                });
    }
}
