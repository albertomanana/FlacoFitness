package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.enums.CategoriaMaterial;
import com.flacofitness.app.model.enums.EstadoMaterial;
import com.flacofitness.app.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<Material> listarFiltrados(EstadoMaterial estado, CategoriaMaterial categoria) {
        if (estado != null && categoria != null) {
            return materialRepository.findByActivoTrueAndEstadoAndCategoriaOrderByNombreAsc(estado, categoria);
        }

        if (estado != null) {
            return materialRepository.findByActivoTrueAndEstadoOrderByNombreAsc(estado);
        }

        if (categoria != null) {
            return materialRepository.findByActivoTrueAndCategoriaOrderByNombreAsc(categoria);
        }

        return materialRepository.findByActivoTrueOrderByNombreAsc();
    }

    public Material buscarPorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con id: " + id));
    }

    public long contarActivos() {
        return materialRepository.countByActivoTrue();
    }

    public long contarBajoStock() {
        return materialRepository.countBajoStock();
    }

    public List<Material> listarBajoStock() {
        return materialRepository.findBajoStock();
    }

    @Transactional
    public Material guardar(Material material) {
        normalizar(material);
        return materialRepository.save(material);
    }

    @Transactional
    public Material actualizar(Long id, Material materialActualizado) {
        Material material = buscarPorId(id);
        material.setNombre(materialActualizado.getNombre());
        material.setCategoria(materialActualizado.getCategoria());
        material.setStock(materialActualizado.getStock());
        material.setStockMinimo(materialActualizado.getStockMinimo());
        material.setUbicacion(materialActualizado.getUbicacion());
        material.setEstado(materialActualizado.getEstado());
        material.setCosteUnitario(materialActualizado.getCosteUnitario());
        material.setProveedor(materialActualizado.getProveedor());
        material.setObservaciones(materialActualizado.getObservaciones());
        material.setActivo(materialActualizado.getActivo());
        normalizar(material);
        return materialRepository.save(material);
    }

    @Transactional
    public void desactivar(Long id) {
        Material material = buscarPorId(id);
        material.setActivo(false);
        materialRepository.save(material);
    }

    @Transactional
    public void activar(Long id) {
        Material material = buscarPorId(id);
        material.setActivo(true);
        if (material.getEstado() == EstadoMaterial.INACTIVO) {
            material.setEstado(EstadoMaterial.DISPONIBLE);
        }
        materialRepository.save(material);
    }

    private void normalizar(Material material) {
        if (!StringUtils.hasText(material.getNombre())) {
            throw new BusinessValidationException("Debes indicar el nombre del material");
        }
        if (material.getCategoria() == null) {
            material.setCategoria(CategoriaMaterial.ENTRENAMIENTO);
        }
        if (material.getStock() == null || material.getStock() < 0) {
            throw new BusinessValidationException("El stock no puede ser negativo");
        }
        if (material.getStockMinimo() == null || material.getStockMinimo() < 0) {
            throw new BusinessValidationException("El stock minimo no puede ser negativo");
        }
        if (material.getCosteUnitario() != null && material.getCosteUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("El coste unitario no puede ser negativo");
        }
        if (material.getActivo() == null) {
            material.setActivo(true);
        }

        if (!material.getActivo()) {
            material.setEstado(EstadoMaterial.INACTIVO);
            return;
        }

        if (material.getStock() <= 0) {
            material.setEstado(EstadoMaterial.AGOTADO);
        } else if (material.getStock() <= material.getStockMinimo()) {
            material.setEstado(EstadoMaterial.BAJO_STOCK);
        } else if (material.getEstado() == null || material.getEstado() == EstadoMaterial.INACTIVO) {
            material.setEstado(EstadoMaterial.DISPONIBLE);
        }
    }
}