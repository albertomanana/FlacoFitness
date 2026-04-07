package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByActivoTrue();

    long countByActivoTrue();
}
