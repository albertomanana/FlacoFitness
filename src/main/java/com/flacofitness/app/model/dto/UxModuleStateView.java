package com.flacofitness.app.model.dto;

public record UxModuleStateView(boolean tooltipSeen,
                                boolean emptyStateDismissed,
                                String guideStepState) {
}
