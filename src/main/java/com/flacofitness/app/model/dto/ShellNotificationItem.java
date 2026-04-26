package com.flacofitness.app.model.dto;

public record ShellNotificationItem(String title,
                                   String description,
                                   String actionLabel,
                                   String actionUrl,
                                   String tone) {
}