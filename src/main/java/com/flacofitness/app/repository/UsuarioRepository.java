package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Override
    @EntityGraph(attributePaths = {"rol", "plan"})
    List<Usuario> findAll();

    @Override
    @EntityGraph(attributePaths = {"rol", "plan"})
    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = {"rol", "plan"})
    List<Usuario> findByActivoTrue();
}
