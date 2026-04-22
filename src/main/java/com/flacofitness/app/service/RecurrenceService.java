package com.flacofitness.app.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.flacofitness.app.model.enums.FrecuenciaGasto;

@Service
public class RecurrenceService {

    public LocalDate nextByPlanDuration(LocalDate base, Integer durationDays) {
        if (base == null) {
            return null;
        }
        return base.plusDays(Math.max(durationDays != null ? durationDays : 1, 1));
    }

    public LocalDate nextByFrequency(LocalDate base, FrecuenciaGasto frequency) {
        if (base == null || frequency == null) {
            return null;
        }

        return switch (frequency) {
            case SEMANAL -> base.plusWeeks(1);
            case QUINCENAL -> base.plusDays(15);
            case MENSUAL -> base.plusMonths(1);
            case TRIMESTRAL -> base.plusMonths(3);
            case ANUAL -> base.plusYears(1);
        };
    }
}
