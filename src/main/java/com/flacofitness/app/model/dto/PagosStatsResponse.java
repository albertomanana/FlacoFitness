package com.flacofitness.app.model.dto;

import java.math.BigDecimal;

public record PagosStatsResponse(
        BigDecimal ingresosTotales,
        long pagosPendientes
) {
}
