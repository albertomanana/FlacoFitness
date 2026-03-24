package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Rutina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Rutina> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Rutina> findById(Long id);

    @EntityGraph(attributePaths = {"usuario"})
    List<Rutina> findByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    List<Rutina> findByActivaTrue();
}
