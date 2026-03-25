package com.flacofitness.app.model.dto;

import java.time.LocalDate;

public record AsistenciaDiariaStatsItem(
        LocalDate fecha,
        long total
) {
}
