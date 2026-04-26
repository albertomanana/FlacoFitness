package com.flacofitness.app.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flacofitness.app.model.entity.MembresiaUsuario;
import com.flacofitness.app.model.enums.EstadoMembresia;

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

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findByEstadoInOrderByFechaInicioDescIdDesc(Collection<EstadoMembresia> estados);

    long countByEstado(EstadoMembresia estado);

    long countByEstadoIn(Collection<EstadoMembresia> estados);

    long countByFechaFinBeforeAndEstadoIn(LocalDate fecha, Collection<EstadoMembresia> estados);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findByFechaFinBeforeAndEstadoIn(LocalDate fecha, Collection<EstadoMembresia> estados);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findByFechaFinBetweenAndEstadoInOrderByFechaFinAscIdAsc(LocalDate desde,
                                                                                     LocalDate hasta,
                                                                                     Collection<EstadoMembresia> estados);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findByFechaFinBeforeAndEstado(LocalDate fecha, EstadoMembresia estado);

    @EntityGraph(attributePaths = {"usuario", "plan"})
    List<MembresiaUsuario> findTop8ByOrderByFechaCreacionDescIdDesc();
}
