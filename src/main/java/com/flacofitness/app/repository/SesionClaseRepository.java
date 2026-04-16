package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.enums.EstadoSesion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SesionClaseRepository extends JpaRepository<SesionClase, Long> {

    @Override
    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    List<SesionClase> findAll();

    @Override
    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    Optional<SesionClase> findById(Long id);

    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    @Query("select sesion from SesionClase sesion order by sesion.fecha desc, sesion.horaInicio desc, sesion.id desc")
    List<SesionClase> findAllOrdered();

    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    List<SesionClase> findByFechaOrderByHoraInicioAscIdAsc(LocalDate fecha);

    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    List<SesionClase> findTop8ByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraInicioAscIdAsc(LocalDate fecha, EstadoSesion estado);

    long countByFechaAndEstado(LocalDate fecha, EstadoSesion estado);

    @Query("select count(sesion) from SesionClase sesion where sesion.fecha >= :fecha and sesion.estado = :estado")
    long countProgramadasDesde(@Param("fecha") LocalDate fecha, @Param("estado") EstadoSesion estado);
}
