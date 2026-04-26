package com.flacofitness.app.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "reservas_sesion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_id", "usuario_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ReservaSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionClase sesionClase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReservaSesion estado;

    @CreationTimestamp
    @Column(name = "fecha_reserva", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaReserva;

    @PrePersist
    private void inicializarEstado() {
        if (estado == null) {
            estado = EstadoReservaSesion.RESERVADA;
        }
    }
}
