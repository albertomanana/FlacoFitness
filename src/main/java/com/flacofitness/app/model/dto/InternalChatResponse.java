package com.flacofitness.app.model.dto;

import java.util.List;

public record InternalChatResponse(String message, List<InternalChatAction> actions) {
}
