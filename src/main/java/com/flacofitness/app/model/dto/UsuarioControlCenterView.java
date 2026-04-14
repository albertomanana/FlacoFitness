package com.flacofitness.app.model.dto;

import com.flacofitness.app.model.entity.Asistencia;
import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.entity.Rutina;

import java.time.LocalDate;
import java.util.List;

public record UsuarioControlCenterView(
        long totalAsistencias,
        LocalDate ultimaAsistencia,
        Long diasSinAsistencia,
        boolean actividadReciente,
        String estadoActividad,
        int rachaActual,
        boolean enRiesgo,
        long pagosPendientes,
        long pagosVencidos,
        boolean pagosAlDia,
        String estadoPago,
        int score,
        String scoreCategoria,
        String segmento,
        String resumenInteligente,
        List<Rutina> rutinasAsignadas,
        List<Asistencia> asistenciasRecientes,
        List<Pago> pagosRecientes,
        List<UsuarioTimelineItem> timeline) {
}
