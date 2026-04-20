package com.flacofitness.app.model.entity;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.flacofitness.app.model.enums.EstadoTrial;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trials")
@Getter
@Setter
@NoArgsConstructor
public class Trial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 150)
    private String apellidos;

    @Email
    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_prueba", nullable = false)
    private LocalDate fechaPrueba;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTrial estado;

    @Column(length = 120)
    private String origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_responsable_id")
    private StaffPerfil staffResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_convertido_id")
    private Usuario usuarioConvertido;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    private void inicializarValores() {
        if (fechaPrueba == null) {
            fechaPrueba = LocalDate.now();
        }
        if (estado == null) {
            estado = EstadoTrial.PENDIENTE;
        }
    }
}
