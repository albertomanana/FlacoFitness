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
        BigDecimal gastoMesActual,
        BigDecimal beneficioEstimado,
        long asistenciasHoy,
        long rutinasActivas,
        int rangoDias,
        List<AsistenciaDiariaStatsItem> asistenciasRecientes,
        List<IngresoMensualStatsItem> ingresosMensualesSerie,
        List<IngresoMensualStatsItem> gastosMensualesSerie,
        List<PlanDistribucionStatsItem> usuariosPorPlan,
        List<UsuarioAltaMensualStatsItem> altasRecientes,
        // NUEVAS MÉTRICAS PARA ANÁLISIS
        long usuariosAsistenciaActivos,
        long usuariosAsistenciaInactivos,
        long usuariosFinancierosAlDia,
        long usuariosFinancierosConDeuda,
        long usuariosFinancierosConVencidos,
        long staffActivos,
        long trialsPendientes,
        long trialsHoy,
        long trialsSemana,
        long sesionesHoy,
        long membresiasActivas,
        long membresiasVencidas,
        long maquinasFueraServicio,
        long maquinasRevisionProxima,
        long materialesBajoStock,
        long usuariosEnRiesgo,
        long membresiasPorCaducar,
        long gastosAnomalos,
        List<AttentionItemView> attentionItems
) {
}
