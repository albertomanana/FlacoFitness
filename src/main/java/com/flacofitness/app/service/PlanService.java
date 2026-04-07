package com.flacofitness.app.service;

import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<Plan> listarTodos() {
        return planRepository.findAll();
    }

    public List<Plan> listarActivos() {
        return planRepository.findByActivoTrue();
    }

    public long contarActivos() {
        return planRepository.countByActivoTrue();
    }

    public Plan buscarPorId(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con id: " + id));
    }

    @Transactional
    public Plan guardar(Plan plan) {
        return planRepository.save(plan);
    }
}
