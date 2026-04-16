package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.MembresiaUsuario;
import com.flacofitness.app.model.enums.EstadoMembresia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MembresiaUsuarioRepository extends JpaRepository<MembresiaUsuario, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario", "plan"})
    Optional<MembresiaUsuario> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findByUsuarioIdOrderByFechaInicioDescIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    Optional<MembresiaUsuario> findTopByUsuarioIdAndEstadoOrderByFechaInicioDescIdDesc(Long usuarioId, EstadoMembresia estado);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    Optional<MembresiaUsuario> findTopByUsuarioIdAndEstadoInOrderByFechaInicioDescIdDesc(Long usuarioId, Collection<EstadoMembresia> estados);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findByEstadoOrderByFechaFinAscIdAsc(EstadoMembresia estado);

    long countByEstado(EstadoMembresia estado);

    long countByEstadoIn(Collection<EstadoMembresia> estados);

    long countByFechaFinBeforeAndEstadoIn(LocalDate fecha, Collection<EstadoMembresia> estados);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findTop8ByOrderByFechaCreacionDescIdDesc();
}
