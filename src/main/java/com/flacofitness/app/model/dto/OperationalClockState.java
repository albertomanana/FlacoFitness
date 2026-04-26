package com.flacofitness.app.model.dto;

public record OperationalClockState(
        String dateLabel,
        String timeLabel,
        String inputValue,
        String statusLabel
) {
}
