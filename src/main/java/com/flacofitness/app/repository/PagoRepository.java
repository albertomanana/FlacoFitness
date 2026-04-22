package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

        @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
        List<Pago> findAllByOrderByFechaVencimientoDescIdDesc();

    @Override
    @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
    List<Pago> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
    Optional<Pago> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
    List<Pago> findByUsuarioId(Long usuarioId);

        @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
        List<Pago> findByUsuarioIdOrderByFechaVencimientoDescIdDesc(Long usuarioId);

        @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
        List<Pago> findByEstadoOrderByFechaVencimientoDescIdDesc(EstadoPago estado);

        @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
        List<Pago> findByUsuarioIdAndEstadoOrderByFechaVencimientoDescIdDesc(Long usuarioId, EstadoPago estado);

    @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
    List<Pago> findTop5ByUsuarioIdOrderByFechaVencimientoDescIdDesc(Long usuarioId);

    boolean existsByUsuarioIdAndFechaVencimiento(Long usuarioId, LocalDate fechaVencimiento);

    boolean existsByUsuarioIdAndFechaVencimientoAndIdNot(Long usuarioId, LocalDate fechaVencimiento, Long id);

    boolean existsByMembresiaUsuarioIdAndFechaVencimiento(Long membresiaUsuarioId, LocalDate fechaVencimiento);

    Optional<Pago> findTopByUsuarioIdOrderByFechaVencimientoDescIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "plan", "membresiaUsuario", "membresiaUsuario.plan"})
    List<Pago> findTop8ByOrderByFechaVencimientoDescIdDesc();

    long countByEstado(EstadoPago estado);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPago estado);

    @Query("select coalesce(sum(pago.monto), 0) from Pago pago where pago.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") EstadoPago estado);

    @Query("select coalesce(sum(pago.monto), 0) from Pago pago " +
            "where pago.estado = :estado and year(pago.fechaPago) = :anio and month(pago.fechaPago) = :mes")
    BigDecimal sumMontoByEstadoAndPeriodo(@Param("estado") EstadoPago estado,
                                          @Param("anio") int anio,
                                          @Param("mes") int mes);

    @Query("select year(pago.fechaPago) as anio, month(pago.fechaPago) as mes, coalesce(sum(pago.monto), 0) as total " +
            "from Pago pago where pago.estado = :estado and pago.fechaPago is not null " +
            "group by year(pago.fechaPago), month(pago.fechaPago) " +
            "order by year(pago.fechaPago), month(pago.fechaPago)")
    List<IngresoPorMesView> sumMontoGroupedByMes(@Param("estado") EstadoPago estado);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Pago pago set pago.estado = :vencido " +
            "where pago.estado in :estadosAbiertos and pago.fechaVencimiento < :fechaReferencia")
    int marcarVencidos(@Param("estadosAbiertos") List<EstadoPago> estadosAbiertos,
                       @Param("vencido") EstadoPago vencido,
                       @Param("fechaReferencia") LocalDate fechaReferencia);
}
