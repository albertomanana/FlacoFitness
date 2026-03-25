package com.flacofitness.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
@ConditionalOnBean(DataSource.class)
public class DatabaseStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStartupLogger.class);

    private final DataSource dataSource;

    public DatabaseStartupLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Conexion MySQL verificada correctamente.");
            log.info("Base activa: {}", connection.getCatalog());
            log.info("Driver JDBC: {} {}", metaData.getDriverName(), metaData.getDriverVersion());
            log.info("Usuario conectado: {}", metaData.getUserName());
        }
    }
}
