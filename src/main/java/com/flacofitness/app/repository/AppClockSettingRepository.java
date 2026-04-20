package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.AppClockSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppClockSettingRepository extends JpaRepository<AppClockSetting, Long> {
}
