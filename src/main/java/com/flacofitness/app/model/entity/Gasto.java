package com.flacofitness.app.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.EstadoGasto;
import com.flacofitness.app.model.enums.TipoGasto;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gasto", nullable = false, length = 20)
    private TipoGasto tipoGasto;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_gasto", nullable = false)
    private LocalDate fecha;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoGasto estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_recurrente_id")
    private GastoRecurrente gastoRecurrente;

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

    public boolean estaPagado() {
        return estado == EstadoGasto.PAGADO;
    }

    public boolean isPagado() {
        return estaPagado();
    }

    public boolean isRecurrente() {
        return gastoRecurrente != null;
    }

    public boolean estaVencido(LocalDate fechaReferencia) {
        return estado != EstadoGasto.PAGADO
                && estado != EstadoGasto.CANCELADO
                && fechaVencimiento != null
                && fechaReferencia != null
                && fechaVencimiento.isBefore(fechaReferencia);
    }

    @PrePersist
    private void prePersist() {
        if (activo == null) {
            activo = true;
        }
        if (categoria == null) {
            categoria = CategoriaGasto.OTROS;
        }
        if (tipoGasto == null) {
            tipoGasto = TipoGasto.VARIABLE;
        }
        if (fechaVencimiento == null) {
            fechaVencimiento = fecha;
        }
        if (estado == null) {
            estado = EstadoGasto.PENDIENTE;
        }
    }
}
