package com.flacofitness.app.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsResponse(
        long totalUsuarios,
        long usuariosActivos,
        long planesActivos,
        long pagosPendientes,
        BigDecimal ingresosMensuales,
        long asistenciasHoy,
        long rutinasActivas,
        List<AsistenciaDiariaStatsItem> asistenciasRecientes,
        List<IngresoMensualStatsItem> ingresosMensualesSerie,
        List<PlanDistribucionStatsItem> usuariosPorPlan,
        List<UsuarioAltaMensualStatsItem> altasRecientes
) {
}
