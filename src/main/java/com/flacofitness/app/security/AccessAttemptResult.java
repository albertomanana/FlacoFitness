package com.flacofitness.app.security;

import java.time.Instant;

public record AccessAttemptResult(boolean granted,
                                 boolean locked,
                                 int remainingAttempts,
                                 Instant lockedUntil,
                                 String message) {
}