package com.flacofitness.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Locale;

public class DatabaseConnectionFailureListener implements ApplicationListener<ApplicationFailedEvent> {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionFailureListener.class);

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        Throwable rootCause = findRootCause(event.getException());
        String message = rootCause != null && rootCause.getMessage() != null
                ? rootCause.getMessage().toLowerCase(Locale.ROOT)
                : "";

        if (!isDatabaseFailure(message)) {
            return;
        }

        log.error("No se pudo iniciar FlacoFitness por un problema de conexion con MySQL.");
        log.error("Verifica spring.datasource.url, spring.datasource.username y spring.datasource.password.");
        log.error("Comprueba que MySQL este levantado en localhost:3306 y que exista la base `flacofitness`.");

        if (message.contains("access denied")) {
            log.error("Pista: el usuario o la password de MySQL son incorrectos.");
        } else if (message.contains("unknown database")) {
            log.error("Pista: la base `flacofitness` no existe. Ejecuta `docs/db/create-database.sql`.");
        } else if (message.contains("communications link failure")
                || message.contains("connection refused")
                || message.contains("connect timed out")) {
            log.error("Pista: MySQL no responde en `localhost:3306` o el servicio esta detenido.");
        }
    }

    private boolean isDatabaseFailure(String message) {
        return message.contains("access denied")
                || message.contains("unknown database")
                || message.contains("communications link failure")
                || message.contains("connection refused")
                || message.contains("connect timed out")
                || message.contains("jdbc");
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
