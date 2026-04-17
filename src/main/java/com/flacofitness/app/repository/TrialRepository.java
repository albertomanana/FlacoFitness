package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.enums.EstadoTrial;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrialRepository extends JpaRepository<Trial, Long> {

    @Override
    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    List<Trial> findAll();

    @Override
    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    Optional<Trial> findById(Long id);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    List<Trial> findAllByOrderByFechaPruebaDescIdDesc();

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    List<Trial> findAllByEstadoOrderByFechaPruebaDescIdDesc(EstadoTrial estado);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    List<Trial> findByEstadoOrderByFechaPruebaAscIdAsc(EstadoTrial estado);

    long countByEstado(EstadoTrial estado);

    long countByFechaPrueba(LocalDate fechaPrueba);

    long countByFechaPruebaAndEstado(LocalDate fechaPrueba, EstadoTrial estado);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario"})
    List<Trial> findTop6ByFechaPruebaGreaterThanEqualOrderByFechaPruebaAscIdAsc(LocalDate fecha);
}
