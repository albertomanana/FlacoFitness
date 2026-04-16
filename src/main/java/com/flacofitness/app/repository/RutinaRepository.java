package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Rutina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuarios", "staffResponsable", "staffResponsable.usuario"})
    List<Rutina> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuarios", "staffResponsable", "staffResponsable.usuario"})
    Optional<Rutina> findById(Long id);

    @EntityGraph(attributePaths = {"usuarios", "staffResponsable", "staffResponsable.usuario"})
    @Query("select distinct rutina from Rutina rutina join rutina.usuarios usuario where usuario.id = :usuarioId")
    List<Rutina> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @EntityGraph(attributePaths = {"usuarios", "staffResponsable", "staffResponsable.usuario"})
    List<Rutina> findByActivaTrue();

    long countByActivaTrue();
}
