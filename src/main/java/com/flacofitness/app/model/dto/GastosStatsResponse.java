package com.flacofitness.app.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record GastosStatsResponse(
        BigDecimal gastoMes,
        BigDecimal gastoFijoMes,
        BigDecimal gastoVariableMes,
        BigDecimal ingresoMes,
        BigDecimal beneficioEstimado,
        long gastosVencidos,
        long proximosVencimientos,
        long recurrentesActivos,
        long nominasPendientes,
        List<GastoCategoriaStatsItem> gastosPorCategoria,
        List<IngresoMensualStatsItem> gastosMensuales
) {
}
