package com.flacofitness.app.model.dto;

import com.flacofitness.app.model.entity.Usuario;

public record TrialConversionResult(
        Usuario usuario,
        String temporalPassword,
        boolean nuevoUsuario
) {
}
