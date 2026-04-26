package com.flacofitness.app.model.dto;

public record DashboardGuideStepView(String key,
                                     String title,
                                     String description,
                                     String url,
                                     boolean completed) {
}
