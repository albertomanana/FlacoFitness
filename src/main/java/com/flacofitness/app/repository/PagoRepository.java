package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
