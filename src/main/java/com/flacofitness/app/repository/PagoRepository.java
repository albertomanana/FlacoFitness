package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByUsuarioId(Long usuarioId);
}
