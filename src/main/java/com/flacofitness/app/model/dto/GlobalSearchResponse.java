package com.flacofitness.app.model.dto;

import java.util.List;

public record GlobalSearchResponse(String query,
                                   List<GlobalSearchGroupItem> groups,
                                   int totalResults) {
}
