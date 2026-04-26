package com.flacofitness.app.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialCenterStatsResponse(
        String periodoActual,
        BigDecimal ingresosMes,
        BigDecimal gastosMes,
        BigDecimal beneficioEstimado,
        BigDecimal deudaTotal,
        BigDecimal nominasPendientesTotal,
        long pagosProgramados,
        long pagosPendientes,
        long pagosVencidos,
        long pagosPagados,
        long nominasBorrador,
        long nominasPendientes,
        long nominasPagadas,
        long nominasCanceladas,
        long gastosCriticos,
        long gastosProximos,
        long recurrentesProximos,
        List<IngresoMensualStatsItem> ingresosMensuales,
        List<IngresoMensualStatsItem> gastosMensuales,
        List<GastoCategoriaStatsItem> gastosPorCategoria,
        List<GastoCategoriaStatsItem> pagosPorEstado,
        List<GastoCategoriaStatsItem> nominasPorEstado
) {
}
