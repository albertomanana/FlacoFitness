package com.flacofitness.app.model.dto;

public record RecentVisitView(String title,
                              String url,
                              String iconKey,
                              String entityType,
                              String visitedAtLabel) {
}
