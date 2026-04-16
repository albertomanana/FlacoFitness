package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Clase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

    List<Clase> findByActivaTrue();

    long countByActivaTrue();
}
