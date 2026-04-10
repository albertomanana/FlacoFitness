package com.flacofitness.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class DatabaseStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStartupLogger.class);

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final Environment environment;

    public DatabaseStartupLogger(ObjectProvider<DataSource> dataSourceProvider,
                                 Environment environment) {
        this.dataSourceProvider = dataSourceProvider;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DataSource dataSource = dataSourceProvider.getIfAvailable();

        if (dataSource == null) {
            return;
        }

        String[] activeProfiles = environment.getActiveProfiles();
        String profileSummary = activeProfiles.length == 0 ? "default" : String.join(", ", activeProfiles);
        String dataMode = environment.getProperty("app.data.mode", "no-definido");
        String sqlInitMode = environment.getProperty("spring.sql.init.mode", "no-definido");

        log.info("Perfil(es) activos: {}", profileSummary);
        log.info("Modo de datos declarado: {}", dataMode);
        log.info("Inicializacion SQL activa: {}", sqlInitMode);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Conexion a base de datos verificada correctamente.");
            log.info("Producto DB: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            log.info("Catalogo activo: {}", connection.getCatalog());
            log.info("Driver JDBC: {} {}", metaData.getDriverName(), metaData.getDriverVersion());
            log.info("Usuario conectado: {}", metaData.getUserName());
        }
    }
}
