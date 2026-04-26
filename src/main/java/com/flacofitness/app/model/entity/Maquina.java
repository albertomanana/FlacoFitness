package com.flacofitness.app.model.entity;

import com.flacofitness.app.model.enums.CategoriaMaquina;
import com.flacofitness.app.model.enums.EstadoMaquina;
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
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maquinas")
@Getter
@Setter
@NoArgsConstructor
public class Maquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaMaquina categoria;

    @Column(length = 80)
    private String marca;

    @Column(length = 80)
    private String modelo;

    @Column(name = "numero_serie", unique = true, length = 120)
    private String numeroSerie;

    @Column(length = 140)
    private String ubicacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMaquina estado;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "coste_compra", precision = 10, scale = 2)
    private BigDecimal costeCompra;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "ultima_revision")
    private LocalDate ultimaRevision;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "proxima_revision")
    private LocalDate proximaRevision;

    @Column(name = "foto_path")
    private String fotoPath;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        if (activo == null) {
            activo = true;
        }
        if (estado == null) {
            estado = EstadoMaquina.OPERATIVA;
        }
        if (categoria == null) {
            categoria = CategoriaMaquina.FUERZA;
        }
    }
}
