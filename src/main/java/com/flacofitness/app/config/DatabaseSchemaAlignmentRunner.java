package com.flacofitness.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Locale;

@Component
@Order(0)
public class DatabaseSchemaAlignmentRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaAlignmentRunner.class);

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    public DatabaseSchemaAlignmentRunner(ObjectProvider<DataSource> dataSourceProvider,
                                         ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.dataSourceProvider = dataSourceProvider;
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();

        if (dataSource == null || jdbcTemplate == null) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();

            if (!esMysqlOMariaDb(databaseProductName)) {
                return;
            }

            String schema = connection.getCatalog();

            if (schema == null || schema.isBlank()) {
                return;
            }

            if (!existeColumnaLegacyRutinaUsuario(schema)) {
                return;
            }

            log.warn("Se detecto el esquema legacy `rutinas.usuario_id`. Se iniciara la alineacion automatica con la relacion ManyToMany.");

            asegurarTablaIntermedia();

            int relacionesMigradas = jdbcTemplate.update("""
                    INSERT IGNORE INTO usuario_rutina (rutina_id, usuario_id)
                    SELECT id, usuario_id
                    FROM rutinas
                    WHERE usuario_id IS NOT NULL
                    """);

            List<String> clavesForaneas = jdbcTemplate.queryForList("""
                    SELECT CONSTRAINT_NAME
                    FROM information_schema.KEY_COLUMN_USAGE
                    WHERE TABLE_SCHEMA = ?
                      AND TABLE_NAME = 'rutinas'
                      AND COLUMN_NAME = 'usuario_id'
                      AND REFERENCED_TABLE_NAME IS NOT NULL
                    """, String.class, schema);

            for (String constraintName : clavesForaneas) {
                jdbcTemplate.execute("ALTER TABLE rutinas DROP FOREIGN KEY `" + constraintName + "`");
                log.info("Clave foranea legacy eliminada: {}", constraintName);
            }

            jdbcTemplate.execute("ALTER TABLE rutinas DROP COLUMN usuario_id");

            log.info("Esquema de rutinas alineado correctamente. Relaciones migradas a `usuario_rutina`: {}", relacionesMigradas);
        }
    }

    private boolean esMysqlOMariaDb(String databaseProductName) {
        if (databaseProductName == null) {
            return false;
        }

        String normalized = databaseProductName.toLowerCase(Locale.ROOT);
        return normalized.contains("mysql") || normalized.contains("mariadb");
    }

    private boolean existeColumnaLegacyRutinaUsuario(String schema) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();

        if (jdbcTemplate == null) {
            return false;
        }

        Integer total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = ?
                  AND TABLE_NAME = 'rutinas'
                  AND COLUMN_NAME = 'usuario_id'
                """, Integer.class, schema);

        return total != null && total > 0;
    }

    private void asegurarTablaIntermedia() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();

        if (jdbcTemplate == null) {
            return;
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS usuario_rutina (
                    rutina_id BIGINT NOT NULL,
                    usuario_id BIGINT NOT NULL,
                    PRIMARY KEY (rutina_id, usuario_id),
                    KEY idx_usuario_rutina_usuario (usuario_id),
                    CONSTRAINT fk_usuario_rutina_rutina
                        FOREIGN KEY (rutina_id) REFERENCES rutinas (id),
                    CONSTRAINT fk_usuario_rutina_usuario
                        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
                """);
    }
}
