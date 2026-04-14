package com.flacofitness.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AccessSettings {

    private final String pin;
    private final int maxAttempts;
    private final Duration lockDuration;

    public AccessSettings(@Value("${app.access.pin:2468}") String pin,
                          @Value("${app.access.max-attempts:3}") int maxAttempts,
                          @Value("${app.access.lock-minutes:5}") long lockMinutes) {
        this.pin = pin == null || pin.isBlank() ? "2468" : pin.trim();
        this.maxAttempts = Math.max(1, maxAttempts);
        this.lockDuration = Duration.ofMinutes(Math.max(1L, lockMinutes));
    }

    public String getPin() {
        return pin;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getLockDuration() {
        return lockDuration;
    }
}