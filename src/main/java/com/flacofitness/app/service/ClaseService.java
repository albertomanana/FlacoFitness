package com.flacofitness.app.service;

import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Clase;
import com.flacofitness.app.repository.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClaseService {

    private final ClaseRepository claseRepository;

    public ClaseService(ClaseRepository claseRepository) {
        this.claseRepository = claseRepository;
    }

    public List<Clase> listarTodas() {
        return claseRepository.findAll();
    }

    public List<Clase> listarActivas() {
        return claseRepository.findByActivaTrue();
    }

    public long contarActivas() {
        return claseRepository.countByActivaTrue();
    }

    public Clase buscarPorId(Long id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada con id: " + id));
    }

    @Transactional
    public Clase guardar(Clase clase) {
        normalizar(clase);
        return claseRepository.save(clase);
    }

    @Transactional
    public Clase actualizar(Long id, Clase claseActualizada) {
        Clase clase = buscarPorId(id);
        clase.setNombre(claseActualizada.getNombre());
        clase.setDescripcion(claseActualizada.getDescripcion());
        clase.setCapacidadSugerida(claseActualizada.getCapacidadSugerida());
        clase.setActiva(claseActualizada.getActiva());
        clase.setObservaciones(claseActualizada.getObservaciones());
        normalizar(clase);
        return claseRepository.save(clase);
    }

    @Transactional
    public void desactivar(Long id) {
        Clase clase = buscarPorId(id);
        clase.setActiva(false);
        claseRepository.save(clase);
    }

    @Transactional
    public void activar(Long id) {
        Clase clase = buscarPorId(id);
        clase.setActiva(true);
        claseRepository.save(clase);
    }

    private void normalizar(Clase clase) {
        if (clase.getActiva() == null) {
            clase.setActiva(true);
        }
        if (clase.getCapacidadSugerida() != null && clase.getCapacidadSugerida() <= 0) {
            clase.setCapacidadSugerida(null);
        }
    }
}
