package com.flacofitness.app.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.enums.EstadoSesion;

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
    List<SesionClase> findByFechaAndStaffResponsableIdOrderByHoraInicioAscIdAsc(LocalDate fecha, Long staffResponsableId);

    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    List<SesionClase> findByStaffResponsableIdOrderByFechaDescHoraInicioDescIdDesc(Long staffResponsableId);

    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    @Query("select sesion from SesionClase sesion where (:estado is null or sesion.estado = :estado) and (:fecha is null or sesion.fecha = :fecha) order by sesion.fecha desc, sesion.horaInicio desc, sesion.id desc")
    List<SesionClase> findByFiltros(@Param("fecha") LocalDate fecha, @Param("estado") EstadoSesion estado);

    @EntityGraph(attributePaths = {"clase", "staffResponsable", "staffResponsable.usuario", "rutina"})
    List<SesionClase> findTop8ByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraInicioAscIdAsc(LocalDate fecha, EstadoSesion estado);

    long countByFechaAndEstado(LocalDate fecha, EstadoSesion estado);

    @Query("select count(sesion) from SesionClase sesion where sesion.fecha >= :fecha and sesion.estado = :estado")
    long countProgramadasDesde(@Param("fecha") LocalDate fecha, @Param("estado") EstadoSesion estado);

    List<SesionClase> findByFechaBeforeAndEstado(LocalDate fecha, EstadoSesion estado);
}
