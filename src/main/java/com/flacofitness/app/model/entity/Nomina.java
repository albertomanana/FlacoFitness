package com.flacofitness.app.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.flacofitness.app.model.enums.EstadoNomina;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nominas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_nomina_staff_periodo", columnNames = {"staff_perfil_id", "periodo"})
})
@Getter
@Setter
@NoArgsConstructor
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_perfil_id", nullable = false)
    private StaffPerfil staffPerfil;

    @NotBlank
    @Column(nullable = false, length = 7)
    private String periodo;

    @NotNull
    @Column(name = "salario_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bonus;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deducciones;

    @NotNull
    @Column(name = "salario_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioNeto;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNomina estado;

    @Column(nullable = false, unique = true, length = 80)
    private String referencia;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_id")
    private Gasto gasto;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        if (bonus == null) {
            bonus = BigDecimal.ZERO;
        }
        if (deducciones == null) {
            deducciones = BigDecimal.ZERO;
        }
        if (salarioNeto == null && salarioBase != null) {
            salarioNeto = salarioBase.add(bonus).subtract(deducciones);
        }
        if (estado == null) {
            estado = EstadoNomina.EMITIDA;
        }
    }
}
