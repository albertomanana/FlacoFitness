package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Asistencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    long countByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Asistencia> findTopByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findTop5ByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(Long usuarioId);

    long countByFecha(LocalDate fecha);

    @Query("select asistencia.fecha as fecha, count(asistencia) as total from Asistencia asistencia " +
            "group by asistencia.fecha order by asistencia.fecha")
    List<AsistenciaPorDiaView> countGroupedByFecha();

    @Query("select asistencia.fecha as fecha, count(asistencia) as total from Asistencia asistencia " +
            "where asistencia.fecha >= :fechaDesde " +
            "group by asistencia.fecha order by asistencia.fecha")
    List<AsistenciaPorDiaView> countGroupedByFechaDesde(@Param("fechaDesde") LocalDate fechaDesde);

    @Query("select year(asistencia.fecha) as anio, month(asistencia.fecha) as mes, count(asistencia) as total " +
            "from Asistencia asistencia group by year(asistencia.fecha), month(asistencia.fecha) " +
            "order by year(asistencia.fecha), month(asistencia.fecha)")
    List<AsistenciaPorMesView> countGroupedByMes();
}
