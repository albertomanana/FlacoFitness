package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<Pago> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario", "plan"})
    Optional<Pago> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<Pago> findByUsuarioId(Long usuarioId);

    long countByEstado(EstadoPago estado);

    @Query("select coalesce(sum(pago.monto), 0) from Pago pago where pago.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") EstadoPago estado);
}
