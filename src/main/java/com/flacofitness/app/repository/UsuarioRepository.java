package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Override
    @EntityGraph(attributePaths = {"rol", "plan"})
    List<Usuario> findAll();

    @Override
    @EntityGraph(attributePaths = {"rol", "plan"})
    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = {"rol", "plan"})
    List<Usuario> findByActivoTrue();

    @EntityGraph(attributePaths = {"rol", "plan"})
    List<Usuario> findTop8ByOrderByFechaRegistroDescIdDesc();

    @EntityGraph(attributePaths = {"rol", "plan"})
    @Query("select usuario from Usuario usuario " +
            "where usuario.activo = true and usuario.fechaProximoPago is not null and usuario.fechaProximoPago >= :fechaDesde " +
            "order by usuario.fechaProximoPago asc, usuario.id asc")
    List<Usuario> findRenovacionesProximas(@Param("fechaDesde") LocalDate fechaDesde);

    long countByActivoTrue();

    long countByActivoTrueAndFechaProximoPagoBetween(LocalDate fechaDesde, LocalDate fechaHasta);

    @Query("select coalesce(plan.nombre, 'Sin plan') as planNombre, count(usuario) as total " +
            "from Usuario usuario left join usuario.plan plan " +
            "group by plan.nombre order by count(usuario) desc, plan.nombre asc")
    List<UsuarioPorPlanView> countGroupedByPlan();

    @Query("select year(usuario.fechaRegistro) as anio, month(usuario.fechaRegistro) as mes, count(usuario) as total " +
            "from Usuario usuario " +
            "group by year(usuario.fechaRegistro), month(usuario.fechaRegistro) " +
            "order by year(usuario.fechaRegistro), month(usuario.fechaRegistro)")
    List<UsuarioAltaPorMesView> countAltasGroupedByMes();

    @EntityGraph(attributePaths = {"plan"})
    @Query("select usuario from Usuario usuario join usuario.plan plan " +
            "where usuario.activo = true and plan.activo = true and " +
            "(usuario.fechaProximoPago is null or usuario.fechaProximoPago <= :fechaReferencia)")
    List<Usuario> findUsuariosConPagoPendiente(@Param("fechaReferencia") LocalDate fechaReferencia);
}
