package com.flacofitness.app.model.dto;

import java.util.List;

public record GlobalSearchGroupItem(String label,
                                    List<GlobalSearchResultItem> items) {
}
