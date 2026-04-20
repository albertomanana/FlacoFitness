package com.flacofitness.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_clock_settings")
@Getter
@Setter
@NoArgsConstructor
public class AppClockSetting {

    @Id
    private Long id;

    @Column(name = "fecha_hora_operativa")
    private LocalDateTime fechaHoraOperativa;

    @Column(nullable = false)
    private Boolean simulado = false;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @PreUpdate
    private void marcarActualizacion() {
        actualizadoEn = LocalDateTime.now();
    }
}
