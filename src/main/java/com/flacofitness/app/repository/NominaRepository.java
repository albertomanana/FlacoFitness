package com.flacofitness.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flacofitness.app.model.entity.Nomina;
import com.flacofitness.app.model.enums.EstadoNomina;

public interface NominaRepository extends JpaRepository<Nomina, Long> {

    @Override
    @EntityGraph(attributePaths = {"staffPerfil", "staffPerfil.usuario", "gasto"})
    Optional<Nomina> findById(Long id);

    @EntityGraph(attributePaths = {"staffPerfil", "staffPerfil.usuario", "gasto"})
    List<Nomina> findByOrderByPeriodoDescIdDesc();

    @EntityGraph(attributePaths = {"staffPerfil", "staffPerfil.usuario", "gasto"})
    List<Nomina> findByStaffPerfilIdOrderByPeriodoDescIdDesc(Long staffPerfilId);

    boolean existsByStaffPerfilIdAndPeriodo(Long staffPerfilId, String periodo);

    long countByEstado(EstadoNomina estado);
}
