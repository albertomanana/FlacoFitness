package com.flacofitness.app.model.dto;

import java.math.BigDecimal;

public record GastoCategoriaStatsItem(
        String categoria,
        BigDecimal total
) {
}
