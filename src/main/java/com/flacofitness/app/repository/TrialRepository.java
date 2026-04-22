package com.flacofitness.app.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.enums.EstadoTrial;

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
    List<Trial> findAllByFechaPruebaBetweenOrderByFechaPruebaDescIdDesc(LocalDate desde, LocalDate hasta);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    List<Trial> findAllByEstadoAndFechaPruebaBetweenOrderByFechaPruebaDescIdDesc(EstadoTrial estado, LocalDate desde, LocalDate hasta);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    List<Trial> findByEstadoOrderByFechaPruebaAscIdAsc(EstadoTrial estado);

    List<Trial> findByEmailAndEstadoNot(String email, EstadoTrial estado);

    long countByEstado(EstadoTrial estado);

    long countByFechaPrueba(LocalDate fechaPrueba);

    long countByFechaPruebaBetween(LocalDate desde, LocalDate hasta);

    long countByFechaPruebaAndEstado(LocalDate fechaPrueba, EstadoTrial estado);

    long countByEstadoAndFechaPruebaLessThanEqual(EstadoTrial estado, LocalDate fechaPrueba);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario", "usuarioConvertido"})
    List<Trial> findByEstadoAndFechaPruebaLessThanEqualOrderByFechaPruebaAscIdAsc(EstadoTrial estado, LocalDate fechaPrueba);

    @EntityGraph(attributePaths = {"staffResponsable", "staffResponsable.usuario"})
    List<Trial> findTop6ByFechaPruebaGreaterThanEqualOrderByFechaPruebaAscIdAsc(LocalDate fecha);
}
