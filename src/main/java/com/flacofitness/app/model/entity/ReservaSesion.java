package com.flacofitness.app.model.entity;

import com.flacofitness.app.model.enums.EstadoReservaSesion;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservas_sesion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_clase_id", "usuario_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ReservaSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_clase_id", nullable = false)
    private SesionClase sesionClase;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReservaSesion estado;

    @CreationTimestamp
    @Column(name = "fecha_reserva", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaReserva;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    private void inicializarEstado() {
        if (estado == null) {
            estado = EstadoReservaSesion.RESERVADA;
        }
    }
}
