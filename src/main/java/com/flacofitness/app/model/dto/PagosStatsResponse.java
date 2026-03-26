package com.flacofitness.app.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record PagosStatsResponse(
        BigDecimal ingresosTotales,
        long pagosPendientes,
        List<IngresoMensualStatsItem> ingresosMensuales
) {
}
