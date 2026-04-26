package com.flacofitness.app.model.entity;

import com.flacofitness.app.model.enums.EstadoSesion;
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
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "sesiones_clase")
@Getter
@Setter
@NoArgsConstructor
public class SesionClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id", nullable = false)
    private Clase clase;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(nullable = false)
    private Integer aforo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesion estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id")
    private Rutina rutina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_responsable_id")
    private StaffPerfil staffResponsable;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    private void inicializarValores() {
        if (aforo == null || aforo <= 0) {
            aforo = clase != null && clase.getCapacidadSugerida() != null && clase.getCapacidadSugerida() > 0
                    ? clase.getCapacidadSugerida()
                    : 12;
        }
        if (estado == null) {
            estado = EstadoSesion.PROGRAMADA;
        }
    }
}
