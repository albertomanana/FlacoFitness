package com.flacofitness.app.model.dto;

import java.time.LocalDate;

public record AsistenciaCalendarDayView(
        LocalDate fecha,
        int numeroDia,
        boolean perteneceAlMesActivo,
        boolean esHoy,
        boolean estaSeleccionado,
        long totalAsistencias
) {
}
