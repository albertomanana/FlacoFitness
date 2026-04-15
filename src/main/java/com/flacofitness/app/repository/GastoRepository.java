package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.enums.CategoriaGasto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    @Override
    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material"})
    Optional<Gasto> findById(Long id);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material"})
    List<Gasto> findByActivoTrueOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material"})
    @Query("select g from Gasto g where g.activo = true " +
            "and (:desde is null or g.fecha >= :desde) " +
            "and (:hasta is null or g.fecha <= :hasta) " +
            "and (:categoria is null or g.categoria = :categoria) " +
            "order by g.fecha desc, g.id desc")
    List<Gasto> buscarFiltrados(@Param("desde") LocalDate desde,
                                @Param("hasta") LocalDate hasta,
                                @Param("categoria") CategoriaGasto categoria);

    @Query("select coalesce(sum(g.importe), 0) from Gasto g where g.activo = true and year(g.fecha) = :anio and month(g.fecha) = :mes")
    BigDecimal sumImporteByPeriodo(@Param("anio") int anio, @Param("mes") int mes);

    @Query("select year(g.fecha) as anio, month(g.fecha) as mes, coalesce(sum(g.importe), 0) as total " +
            "from Gasto g where g.activo = true " +
            "group by year(g.fecha), month(g.fecha) " +
            "order by year(g.fecha), month(g.fecha)")
    List<GastoPorMesView> sumImporteGroupedByMes();

    long countByActivoTrue();

    long countByActivoTrueAndPagadoFalseAndFechaLessThanEqual(LocalDate fecha);

    long countByActivoTrueAndRecurrenteTrueAndFechaLessThanEqual(LocalDate fecha);

    long countByActivoTrueAndRecurrenteTrueAndPagadoFalseAndFechaBetween(LocalDate desde, LocalDate hasta);
}