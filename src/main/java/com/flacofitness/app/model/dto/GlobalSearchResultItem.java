package com.flacofitness.app.model.dto;

public record GlobalSearchResultItem(String title,
                                     String subtitle,
                                     String url,
                                     String group,
                                     String iconKey,
                                     long score) {
}
