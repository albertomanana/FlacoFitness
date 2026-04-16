package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.enums.CategoriaMaterial;
import com.flacofitness.app.model.enums.EstadoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByActivoTrueOrderByNombreAsc();

    List<Material> findByActivoTrueAndEstadoOrderByNombreAsc(EstadoMaterial estado);

    List<Material> findByActivoTrueAndCategoriaOrderByNombreAsc(CategoriaMaterial categoria);

    List<Material> findByActivoTrueAndEstadoAndCategoriaOrderByNombreAsc(EstadoMaterial estado, CategoriaMaterial categoria);

    long countByActivoTrue();

    @Query("select count(m) from Material m where m.activo = true and m.stock <= m.stockMinimo")
    long countBajoStock();

    @Query("select m from Material m where m.activo = true and m.stock <= m.stockMinimo order by m.stock asc, m.nombre asc")
    List<Material> findBajoStock();
}