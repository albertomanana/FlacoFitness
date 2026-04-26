package com.flacofitness.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AccessSettings {

    private final int maxAttempts;
    private final Duration lockDuration;
    private final String bootstrapPassword;

    public AccessSettings(@Value("${app.auth.max-attempts:5}") int maxAttempts,
                          @Value("${app.auth.lock-minutes:1}") long lockMinutes,
                          @Value("${app.auth.bootstrap-password:FlacoTemp2026!}") String bootstrapPassword) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.lockDuration = Duration.ofMinutes(Math.max(1L, lockMinutes));
        this.bootstrapPassword = bootstrapPassword == null || bootstrapPassword.isBlank()
                ? "FlacoTemp2026!"
                : bootstrapPassword.trim();
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getLockDuration() {
        return lockDuration;
    }

    public String getBootstrapPassword() {
        return bootstrapPassword;
    }
}
