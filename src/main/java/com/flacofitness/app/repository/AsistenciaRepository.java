package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByUsuarioId(Long usuarioId);
}
