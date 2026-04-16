package com.flacofitness.app.model.dto;

public record AsistenciaCheckInBatchResult(
        int registrosCreados,
        int registrosOmitidos
) {
}
