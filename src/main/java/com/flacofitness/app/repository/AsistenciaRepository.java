package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Asistencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Asistencia> findById(Long id);

    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findByUsuarioId(Long usuarioId);
}
