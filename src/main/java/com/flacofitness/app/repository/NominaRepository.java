package com.flacofitness.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flacofitness.app.model.entity.Nomina;
import com.flacofitness.app.model.enums.EstadoNomina;

import java.math.BigDecimal;

public interface NominaRepository extends JpaRepository<Nomina, Long> {

    @Override
    @EntityGraph(attributePaths = {"staffPerfil", "staffPerfil.usuario", "gasto"})
    Optional<Nomina> findById(Long id);

    @EntityGraph(attributePaths = {"staffPerfil", "staffPerfil.usuario", "gasto"})
    List<Nomina> findByOrderByPeriodoDescIdDesc();

    @EntityGraph(attributePaths = {"staffPerfil", "staffPerfil.usuario", "gasto"})
    List<Nomina> findByStaffPerfilIdOrderByPeriodoDescIdDesc(Long staffPerfilId);

    boolean existsByStaffPerfilIdAndPeriodo(Long staffPerfilId, String periodo);

    boolean existsByStaffPerfilIdAndPeriodoAndIdNot(Long staffPerfilId, String periodo, Long id);

    long countByEstado(EstadoNomina estado);

    @Query("select coalesce(sum(n.salarioNeto), 0) from Nomina n where n.estado = :estado")
    BigDecimal sumSalarioNetoByEstado(@Param("estado") EstadoNomina estado);
}
