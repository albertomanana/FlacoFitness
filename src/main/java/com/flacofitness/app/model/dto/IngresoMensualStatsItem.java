package com.flacofitness.app.model.dto;

import java.math.BigDecimal;

public record IngresoMensualStatsItem(
        String periodo,
        BigDecimal total
) {
}
