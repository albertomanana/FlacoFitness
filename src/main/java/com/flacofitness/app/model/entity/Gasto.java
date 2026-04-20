package com.flacofitness.app.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.FrecuenciaGasto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gastos")
@Getter
@Setter
@NoArgsConstructor
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 160)
    private String concepto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaGasto categoria;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_gasto", nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Boolean recurrente;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FrecuenciaGasto frecuencia;

    @Column(nullable = false)
    private Boolean pagado;

    @Column(length = 120)
    private String proveedor;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_responsable_id")
    private StaffPerfil staffResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquina_id")
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false)
    private Boolean activo;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        if (recurrente == null) {
            recurrente = false;
        }
        if (pagado == null) {
            pagado = false;
        }
        if (activo == null) {
            activo = true;
        }
        if (categoria == null) {
            categoria = CategoriaGasto.OTROS;
        }
        if (!Boolean.TRUE.equals(recurrente)) {
            frecuencia = null;
        }
    }
}