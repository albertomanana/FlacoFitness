package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.StaffPerfil;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffPerfilRepository extends JpaRepository<StaffPerfil, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    Optional<StaffPerfil> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findByActivoTrue();

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findByActivoTrueAndPuedeImpartirClasesTrue();

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findByActivoTrueAndNominaAutomaticaTrue();

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    Optional<StaffPerfil> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    long countByActivoTrue();
}
