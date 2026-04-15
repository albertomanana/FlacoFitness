package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.enums.CategoriaMaquina;
import com.flacofitness.app.model.enums.EstadoMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MaquinaRepository extends JpaRepository<Maquina, Long> {

    List<Maquina> findByActivoTrueOrderByNombreAsc();

    List<Maquina> findByActivoTrueAndEstadoOrderByNombreAsc(EstadoMaquina estado);

    List<Maquina> findByActivoTrueAndCategoriaOrderByNombreAsc(CategoriaMaquina categoria);

    List<Maquina> findByActivoTrueAndEstadoAndCategoriaOrderByNombreAsc(EstadoMaquina estado, CategoriaMaquina categoria);

    long countByActivoTrue();

    long countByActivoTrueAndEstadoIn(List<EstadoMaquina> estados);

    long countByActivoTrueAndProximaRevisionLessThanEqual(LocalDate fecha);

    boolean existsByNumeroSerie(String numeroSerie);

    boolean existsByNumeroSerieAndIdNot(String numeroSerie, Long id);
}