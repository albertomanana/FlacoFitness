package com.flacofitness.app.model.dto;

import java.util.List;

public record DashboardGuideView(boolean completed,
                                 boolean dismissed,
                                 List<DashboardGuideStepView> steps) {
}
