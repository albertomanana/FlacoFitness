package com.flacofitness.app.model.dto;

public record OperationalClockState(
        boolean simulated,
        String dateLabel,
        String timeLabel,
        String inputValue,
        String statusLabel
) {
}
