package com.flacofitness.app.model.dto;

import java.util.List;

public record AsistenciasStatsResponse(
        List<AsistenciaDiariaStatsItem> asistenciasPorDia,
        List<AsistenciaMensualStatsItem> asistenciasMensuales
) {
}
