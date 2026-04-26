package com.flacofitness.app.config;

import com.flacofitness.app.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthBootstrapRunner.class);

    private final UsuarioService usuarioService;
    private final AccessSettings accessSettings;

    public AuthBootstrapRunner(UsuarioService usuarioService, AccessSettings accessSettings) {
        this.usuarioService = usuarioService;
        this.accessSettings = accessSettings;
    }

    @Override
    public void run(ApplicationArguments args) {
        int updated = usuarioService.bootstrapCredencialesFaltantes(accessSettings.getBootstrapPassword());
        if (updated > 0) {
            log.warn("Auth bootstrap actualizo {} cuenta(s) sin credenciales. Password temporal configurado desde app.auth.bootstrap-password.", updated);
        }
    }
}
