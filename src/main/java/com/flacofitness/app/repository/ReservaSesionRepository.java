package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.ReservaSesion;
import com.flacofitness.app.model.enums.EstadoReservaSesion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaSesionRepository extends JpaRepository<ReservaSesion, Long> {

    @EntityGraph(attributePaths = {"usuario", "sesionClase", "sesionClase.clase"})
    List<ReservaSesion> findBySesionClaseIdOrderByFechaReservaDescIdDesc(Long sesionClaseId);

    @EntityGraph(attributePaths = {"usuario", "sesionClase", "sesionClase.clase"})
    List<ReservaSesion> findByUsuarioIdOrderByFechaReservaDescIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "sesionClase", "sesionClase.clase"})
    Optional<ReservaSesion> findBySesionClaseIdAndUsuarioId(Long sesionClaseId, Long usuarioId);

    boolean existsBySesionClaseIdAndUsuarioId(Long sesionClaseId, Long usuarioId);

    long countBySesionClaseIdAndEstadoIn(Long sesionClaseId, Collection<EstadoReservaSesion> estados);

    long countBySesionClaseIdAndEstado(Long sesionClaseId, EstadoReservaSesion estado);
}
