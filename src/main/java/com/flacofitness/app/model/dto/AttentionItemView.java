package com.flacofitness.app.model.dto;

public record AttentionItemView(String title,
                                String description,
                                String url,
                                String tone,
                                String icon,
                                long count) {
}
