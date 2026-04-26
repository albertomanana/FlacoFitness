package com.flacofitness.app.repository;

import com.flacofitness.app.model.dto.UsuarioAltaPorMesView;
import com.flacofitness.app.model.dto.UsuarioPorPlanView;
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

    @EntityGraph(attributePaths = {"rol", "plan"})
    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = {"rol", "plan"})
    Optional<Usuario> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"rol", "plan"})
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"rol", "plan"})
    Optional<Usuario> findFirstByActivoTrueAndRolNombreOrderByIdAsc(String rolNombre);

    @EntityGraph(attributePaths = {"rol", "plan"})
    List<Usuario> findByActivoTrue();

    @EntityGraph(attributePaths = {"rol", "plan"})
    List<Usuario> findTop8ByOrderByFechaRegistroDescIdDesc();

    @EntityGraph(attributePaths = {"rol", "plan"})
    @Query("""
            select usuario from Usuario usuario
            where lower(concat(
                coalesce(usuario.nombre, ''), ' ',
                coalesce(usuario.apellidos, ''), ' ',
                coalesce(usuario.email, ''), ' ',
                coalesce(usuario.username, '')
            )) like lower(concat('%', :query, '%'))
            order by usuario.activo desc, usuario.nombre asc, usuario.apellidos asc, usuario.id asc
            """)
    List<Usuario> searchTopForGlobal(@Param("query") String query, org.springframework.data.domain.Pageable pageable);

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
