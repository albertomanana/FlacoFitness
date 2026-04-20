package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.OperationalClockState;
import com.flacofitness.app.model.entity.AppClockSetting;
import com.flacofitness.app.repository.AppClockSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class OperationalClockService {

    private static final Long SINGLETON_ID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter TIME_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final AppClockSettingRepository appClockSettingRepository;

    public OperationalClockService(AppClockSettingRepository appClockSettingRepository) {
        this.appClockSettingRepository = appClockSettingRepository;
    }

    public LocalDateTime now() {
        AppClockSetting setting = getSetting();
        if (Boolean.TRUE.equals(setting.getSimulado()) && setting.getFechaHoraOperativa() != null) {
            return setting.getFechaHoraOperativa();
        }
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
        AppClockSetting setting = getSetting();
        boolean simulated = Boolean.TRUE.equals(setting.getSimulado()) && setting.getFechaHoraOperativa() != null;
        return new OperationalClockState(
                simulated,
                capitalize(current.format(DATE_LABEL_FORMAT)),
                current.format(TIME_LABEL_FORMAT),
                current.format(INPUT_FORMAT),
                simulated ? "Simulada" : "Sistema"
        );
    }

    @Transactional
    public void setFixed(LocalDateTime fechaHora) {
        AppClockSetting setting = getSettingForUpdate();
        setting.setFechaHoraOperativa(fechaHora.withSecond(0).withNano(0));
        setting.setSimulado(true);
        setting.setActualizadoEn(LocalDateTime.now());
        appClockSettingRepository.save(setting);
    }

    @Transactional
    public void shiftMonths(int months) {
        setFixed(now().plusMonths(months));
    }

    @Transactional
    public void resetToSystem() {
        AppClockSetting setting = getSettingForUpdate();
        setting.setFechaHoraOperativa(null);
        setting.setSimulado(false);
        setting.setActualizadoEn(LocalDateTime.now());
        appClockSettingRepository.save(setting);
    }

    private AppClockSetting getSetting() {
        return appClockSettingRepository.findById(SINGLETON_ID)
                .orElseGet(this::newDefaultSetting);
    }

    private AppClockSetting getSettingForUpdate() {
        return appClockSettingRepository.findById(SINGLETON_ID)
                .orElseGet(this::newDefaultSetting);
    }

    private AppClockSetting newDefaultSetting() {
        AppClockSetting setting = new AppClockSetting();
        setting.setId(SINGLETON_ID);
        setting.setFechaHoraOperativa(null);
        setting.setSimulado(false);
        return setting;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
