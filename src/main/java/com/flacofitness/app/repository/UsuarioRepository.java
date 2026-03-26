package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    long countByActivoTrue();

    @EntityGraph(attributePaths = {"plan"})
    @Query("select usuario from Usuario usuario join usuario.plan plan " +
            "where usuario.activo = true and plan.activo = true and " +
            "(usuario.fechaProximoPago is null or usuario.fechaProximoPago <= :fechaReferencia)")
    List<Usuario> findUsuariosConPagoPendiente(@Param("fechaReferencia") LocalDate fechaReferencia);
}
