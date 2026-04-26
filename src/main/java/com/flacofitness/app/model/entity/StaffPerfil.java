package com.flacofitness.app.model.entity;

import com.flacofitness.app.model.enums.RolStaff;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "staff_perfiles")
@Getter
@Setter
@NoArgsConstructor
public class StaffPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(length = 120)
    private String especialidad;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "rol_staff", nullable = false, length = 30)
    private RolStaff rolStaff;

    @NotNull
    @Column(nullable = false)
    private Boolean activo;

    @NotNull
    @Column(name = "puede_impartir_clases", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean puedeImpartirClases;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_alta", nullable = false)
    private LocalDate fechaAlta;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "salario_base_mensual", precision = 10, scale = 2)
    private BigDecimal salarioBaseMensual;

    @Column(name = "bonus_mensual", precision = 10, scale = 2)
    private BigDecimal bonusMensual;

    @Column(name = "deducciones_mensuales", precision = 10, scale = 2)
    private BigDecimal deduccionesMensuales;

    @Column(name = "nomina_automatica", nullable = false)
    private Boolean nominaAutomatica;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_proxima_nomina")
    private LocalDate fechaProximaNomina;

    @PrePersist
    private void inicializarValores() {
        if (activo == null) {
            activo = true;
        }
        if (rolStaff == null) {
            rolStaff = RolStaff.ENTRENADOR;
        }
        if (puedeImpartirClases == null) {
            puedeImpartirClases = rolStaff == RolStaff.ENTRENADOR;
        }
        if (nominaAutomatica == null) {
            nominaAutomatica = false;
        }
    }
}
