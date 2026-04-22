package com.flacofitness.app.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.EstadoGasto;
import com.flacofitness.app.model.enums.TipoGasto;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    @Override
    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material", "gastoRecurrente"})
    Optional<Gasto> findById(Long id);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material", "gastoRecurrente"})
    List<Gasto> findByActivoTrueOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material", "gastoRecurrente"})
    @Query("select g from Gasto g where g.activo = true " +
            "and (:desde is null or g.fecha >= :desde) " +
            "and (:hasta is null or g.fecha <= :hasta) " +
            "and (:categoria is null or g.categoria = :categoria) " +
            "and (:estado is null or g.estado = :estado) " +
            "and (:tipoGasto is null or g.tipoGasto = :tipoGasto) " +
            "and (:staffId is null or g.staffResponsable.id = :staffId) " +
            "and (:maquinaId is null or g.maquina.id = :maquinaId) " +
            "and (:materialId is null or g.material.id = :materialId) " +
            "and (:recurrente is null or (:recurrente = true and g.gastoRecurrente is not null) or (:recurrente = false and g.gastoRecurrente is null)) " +
            "and (:proveedor is null or lower(g.proveedor) like lower(concat('%', :proveedor, '%'))) " +
            "order by g.fecha desc, g.id desc")
    List<Gasto> buscarFiltrados(@Param("desde") LocalDate desde,
                                @Param("hasta") LocalDate hasta,
                                @Param("categoria") CategoriaGasto categoria,
                                @Param("estado") EstadoGasto estado,
                                @Param("tipoGasto") TipoGasto tipoGasto,
                                @Param("staffId") Long staffId,
                                @Param("maquinaId") Long maquinaId,
                                @Param("materialId") Long materialId,
                                @Param("recurrente") Boolean recurrente,
                                @Param("proveedor") String proveedor);

    @Query("select coalesce(sum(g.importe), 0) from Gasto g where g.activo = true and year(g.fecha) = :anio and month(g.fecha) = :mes")
    BigDecimal sumImporteByPeriodo(@Param("anio") int anio, @Param("mes") int mes);

    @Query("select coalesce(sum(g.importe), 0) from Gasto g where g.activo = true and g.tipoGasto = :tipoGasto and year(g.fecha) = :anio and month(g.fecha) = :mes")
    BigDecimal sumImporteByPeriodoAndTipo(@Param("anio") int anio, @Param("mes") int mes, @Param("tipoGasto") TipoGasto tipoGasto);

    @Query("select coalesce(sum(g.importe), 0) from Gasto g where g.activo = true and g.categoria = :categoria and year(g.fecha) = :anio and month(g.fecha) = :mes")
    BigDecimal sumImporteByPeriodoAndCategoria(@Param("anio") int anio, @Param("mes") int mes, @Param("categoria") CategoriaGasto categoria);

    @Query("select year(g.fecha) as anio, month(g.fecha) as mes, coalesce(sum(g.importe), 0) as total " +
            "from Gasto g where g.activo = true " +
            "group by year(g.fecha), month(g.fecha) " +
            "order by year(g.fecha), month(g.fecha)")
    List<GastoPorMesView> sumImporteGroupedByMes();

    long countByActivoTrue();

    long countByActivoTrueAndEstado(EstadoGasto estado);

    @Query("select count(g) from Gasto g where g.activo = true and g.estado in :estados and g.fechaVencimiento <= :fecha")
    long countCriticos(@Param("estados") List<EstadoGasto> estados, @Param("fecha") LocalDate fecha);

    @Query("select count(g) from Gasto g where g.activo = true and g.estado in :estados and g.fechaVencimiento between :desde and :hasta")
    long countVencimientosEntre(@Param("estados") List<EstadoGasto> estados, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material", "gastoRecurrente"})
    @Query("select g from Gasto g where g.activo = true and g.estado in :estados and g.fechaVencimiento between :desde and :hasta order by g.fechaVencimiento asc, g.id asc")
    List<Gasto> findProximosVencimientos(@Param("estados") List<EstadoGasto> estados, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material", "gastoRecurrente"})
    List<Gasto> findByActivoTrueAndStaffResponsableIdOrderByFechaDescIdDesc(Long staffResponsableId);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material", "gastoRecurrente"})
    List<Gasto> findByActivoTrueAndMaquinaIdOrderByFechaDescIdDesc(Long maquinaId);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "maquina", "material", "gastoRecurrente"})
    List<Gasto> findByActivoTrueAndMaterialIdOrderByFechaDescIdDesc(Long materialId);

    boolean existsByGastoRecurrenteIdAndFechaVencimiento(Long gastoRecurrenteId, LocalDate fechaVencimiento);

    @Query("select g.categoria as categoria, coalesce(sum(g.importe), 0) as total from Gasto g " +
            "where g.activo = true and year(g.fecha) = :anio and month(g.fecha) = :mes " +
            "group by g.categoria order by total desc")
    List<GastoPorCategoriaView> sumImporteGroupedByCategoria(@Param("anio") int anio, @Param("mes") int mes);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Gasto gasto set gasto.estado = :vencido " +
            "where gasto.activo = true and gasto.estado in :estadosAbiertos and gasto.fechaVencimiento < :fechaReferencia")
    int marcarVencidos(@Param("estadosAbiertos") List<EstadoGasto> estadosAbiertos,
                       @Param("vencido") EstadoGasto vencido,
                       @Param("fechaReferencia") LocalDate fechaReferencia);
}
