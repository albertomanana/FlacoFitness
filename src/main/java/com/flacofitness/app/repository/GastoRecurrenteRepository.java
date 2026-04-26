package com.flacofitness.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flacofitness.app.model.entity.GastoRecurrente;

public interface GastoRecurrenteRepository extends JpaRepository<GastoRecurrente, Long> {

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material"})
    List<GastoRecurrente> findByActivoTrueOrderByFechaProximoCargoAscIdAsc();

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material"})
    List<GastoRecurrente> findByActivoTrueAndFechaProximoCargoLessThanEqualOrderByFechaProximoCargoAscIdAsc(LocalDate fecha);

    long countByActivoTrue();

    long countByActivoTrueAndFechaProximoCargoBetween(LocalDate desde, LocalDate hasta);
}
