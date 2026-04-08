package com.flacofitness.app.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsResponse(
        long totalUsuarios,
        long usuariosActivos,
        long planesActivos,
        long pagosPendientes,
        long pagosVencidos,
        long renovacionesProximas,
        BigDecimal ingresosTotales,
        BigDecimal ingresosMensuales,
        long asistenciasHoy,
        long rutinasActivas,
        int rangoDias,
        List<AsistenciaDiariaStatsItem> asistenciasRecientes,
        List<IngresoMensualStatsItem> ingresosMensualesSerie,
        List<PlanDistribucionStatsItem> usuariosPorPlan,
        List<UsuarioAltaMensualStatsItem> altasRecientes
) {
}
