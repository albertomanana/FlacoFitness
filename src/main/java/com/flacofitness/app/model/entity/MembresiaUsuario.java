package com.flacofitness.app.model.entity;

import com.flacofitness.app.model.enums.EstadoMembresia;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
@Table(name = "membresias_usuario")
@Getter
@Setter
@NoArgsConstructor
public class MembresiaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMembresia estado;

    @Column(name = "precio_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioSnapshot;

    @Column(length = 80)
    private String origen;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @PrePersist
    @PreUpdate
    private void sincronizarDerivados() {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now();
        }
        if (estado == null) {
            estado = EstadoMembresia.ACTIVA;
        }
        if (plan != null) {
            if (precioSnapshot == null && plan.getPrecioMensual() != null) {
                precioSnapshot = plan.getPrecioMensual();
            }
            if (fechaFin == null && plan.getDuracionDias() != null) {
                fechaFin = fechaInicio.plusDays(Math.max(plan.getDuracionDias(), 1));
            }
        }
    }
}
