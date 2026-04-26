package com.flacofitness.app.model.dto;

public record ActivityLogItemView(String title,
                                  String description,
                                  String moduleKey,
                                  String actionKey,
                                  String route,
                                  String occurredAtLabel,
                                  String actorProfileLabel) {
}
