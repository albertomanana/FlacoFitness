package com.flacofitness.app.model.dto;

import java.util.List;

public record AsistenciaCalendarView(
        String mes,
        String etiquetaMes,
        String mesAnterior,
        String mesSiguiente,
        List<String> diasSemana,
        List<AsistenciaCalendarDayView> dias
) {
}
