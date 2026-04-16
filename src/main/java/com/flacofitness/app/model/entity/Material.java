package com.flacofitness.app.model.entity;

import com.flacofitness.app.model.enums.CategoriaMaterial;
import com.flacofitness.app.model.enums.EstadoMaterial;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "materiales")
@Getter
@Setter
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaMaterial categoria;

    @NotNull
    @Column(nullable = false)
    private Integer stock;

    @NotNull
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(length = 140)
    private String ubicacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMaterial estado;

    @Column(name = "coste_unitario", precision = 10, scale = 2)
    private BigDecimal costeUnitario;

    @Column(length = 120)
    private String proveedor;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        if (stock == null || stock < 0) {
            stock = 0;
        }
        if (stockMinimo == null || stockMinimo < 0) {
            stockMinimo = 0;
        }
        if (estado == null) {
            estado = EstadoMaterial.DISPONIBLE;
        }
        if (categoria == null) {
            categoria = CategoriaMaterial.ENTRENAMIENTO;
        }
        if (activo == null) {
            activo = true;
        }
    }
}