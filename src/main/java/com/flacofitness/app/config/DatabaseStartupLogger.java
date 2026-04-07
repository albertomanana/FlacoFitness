package com.flacofitness.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class DatabaseStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStartupLogger.class);

    private final ObjectProvider<DataSource> dataSourceProvider;

    public DatabaseStartupLogger(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DataSource dataSource = dataSourceProvider.getIfAvailable();

        if (dataSource == null) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Conexion MySQL verificada correctamente.");
            log.info("Base activa: {}", connection.getCatalog());
            log.info("Driver JDBC: {} {}", metaData.getDriverName(), metaData.getDriverVersion());
            log.info("Usuario conectado: {}", metaData.getUserName());
        }
    }
}
