package com.flacofitness.app.model.dto;

import java.time.LocalDateTime;

public record UsuarioTimelineItem(
        String tipo,
        String titulo,
        String descripcion,
        LocalDateTime fechaHora) {
}
