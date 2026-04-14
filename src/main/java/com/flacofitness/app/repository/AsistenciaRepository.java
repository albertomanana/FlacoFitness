package com.flacofitness.app.repository;

import com.flacofitness.app.model.dto.UsuarioAsistenciaCountDto;
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

    @EntityGraph(attributePaths = {"usuario"})
    @Query("select asistencia from Asistencia asistencia " +
            "order by asistencia.fecha desc, asistencia.horaEntrada desc, asistencia.id desc")
    List<Asistencia> findAllOrdered();

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Asistencia> findById(Long id);

    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findByFechaOrderByHoraEntradaDescIdDesc(LocalDate fecha);

    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findByFechaAndUsuarioIdOrderByHoraEntradaDescIdDesc(LocalDate fecha, Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Asistencia> findTopByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    List<Asistencia> findTop5ByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(Long usuarioId);

    long countByFecha(LocalDate fecha);

    boolean existsByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);

    @Query("select asistencia.fecha as fecha, count(asistencia) as total from Asistencia asistencia " +
            "group by asistencia.fecha order by asistencia.fecha")
    List<AsistenciaPorDiaView> countGroupedByFecha();

    @Query("select asistencia.fecha as fecha, count(asistencia) as total from Asistencia asistencia " +
            "where asistencia.fecha >= :fechaDesde " +
            "group by asistencia.fecha order by asistencia.fecha")
    List<AsistenciaPorDiaView> countGroupedByFechaDesde(@Param("fechaDesde") LocalDate fechaDesde);

    @Query("select asistencia.fecha as fecha, count(asistencia) as total from Asistencia asistencia " +
            "where asistencia.fecha between :fechaDesde and :fechaHasta " +
            "group by asistencia.fecha order by asistencia.fecha")
    List<AsistenciaPorDiaView> countGroupedByFechaBetween(@Param("fechaDesde") LocalDate fechaDesde,
                                                          @Param("fechaHasta") LocalDate fechaHasta);

    @Query("select year(asistencia.fecha) as anio, month(asistencia.fecha) as mes, count(asistencia) as total " +
            "from Asistencia asistencia group by year(asistencia.fecha), month(asistencia.fecha) " +
            "order by year(asistencia.fecha), month(asistencia.fecha)")
    List<AsistenciaPorMesView> countGroupedByMes();

    long countByUsuarioIdAndFechaBetween(Long usuarioId, LocalDate fechaDesde, LocalDate fechaHasta);

    @Query("select count(distinct asistencia.usuario.id) from Asistencia asistencia where asistencia.usuario.activo = true and asistencia.fecha >= :fechaDesde")
    long countDistinctUsuariosActivosDesde(@Param("fechaDesde") LocalDate fechaDesde);

    @Query("select new com.flacofitness.app.model.dto.UsuarioAsistenciaCountDto(asistencia.usuario.id, count(asistencia)) " +
            "from Asistencia asistencia " +
            "where asistencia.fecha >= :fechaDesde " +
            "group by asistencia.usuario.id " +
            "order by count(asistencia) desc")
    List<UsuarioAsistenciaCountDto> findTopUsuariosByAsistenciasDesde(@Param("fechaDesde") LocalDate fechaDesde);

    // Interface for the projection
    interface UsuarioAsistenciaCountView {
        Long getUsuarioId();
        Long getTotalAsistencias();
    }
}
