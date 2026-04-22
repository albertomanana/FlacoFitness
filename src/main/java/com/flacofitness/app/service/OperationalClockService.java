package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.OperationalClockState;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class OperationalClockService {

    private static final DateTimeFormatter DATE_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter TIME_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    public LocalDate today() {
        return now().toLocalDate();
    }

    public LocalTime time() {
        return now().toLocalTime();
    }

    public YearMonth currentYearMonth() {
        return YearMonth.from(today());
    }

    public OperationalClockState state() {
        LocalDateTime current = now().withSecond(0).withNano(0);
        return new OperationalClockState(
                false,
                capitalize(current.format(DATE_LABEL_FORMAT)),
                current.format(TIME_LABEL_FORMAT),
                current.format(INPUT_FORMAT),
                "Sistema"
        );
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
