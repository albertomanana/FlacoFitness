package com.flacofitness.app.config;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.flacofitness.app.model.enums.FrecuenciaGasto;
import com.flacofitness.app.service.OperationalClockService;

@Component
@Order(1)
@ConditionalOnBean(JdbcTemplate.class)
public class FinancialSchemaRepairRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinancialSchemaRepairRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final OperationalClockService operationalClockService;

    public FinancialSchemaRepairRunner(JdbcTemplate jdbcTemplate,
                                       OperationalClockService operationalClockService) {
        this.jdbcTemplate = jdbcTemplate;
        this.operationalClockService = operationalClockService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("gastos")) {
            return;
        }

        repairLegacyColumns();
        normalizeLegacyData();
        migrateRecurringTemplates();
    }

    private void repairLegacyColumns() {
        executeIgnoring("ALTER TABLE gastos MODIFY COLUMN categoria VARCHAR(40) NOT NULL");
        executeIgnoring("ALTER TABLE gastos MODIFY COLUMN estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE'");
        executeIgnoring("ALTER TABLE gastos MODIFY COLUMN monto DECIMAL(10,2) NULL DEFAULT NULL");
        executeIgnoring("ALTER TABLE gastos MODIFY COLUMN pagado BIT(1) NULL DEFAULT b'0'");

        // Compatibilidad legacy: la columna recurrente puede seguir existiendo en MySQL
        // aunque el modelo actual use gasto_recurrente_id. Debe tener default para no bloquear inserts.
        addColumnIfMissing("gastos", "recurrente", "ALTER TABLE gastos ADD COLUMN recurrente BIT(1) NOT NULL DEFAULT b'0'");
        executeIgnoring("ALTER TABLE gastos MODIFY COLUMN recurrente BIT(1) NOT NULL DEFAULT b'0'");
        executeIgnoring("UPDATE gastos SET recurrente = b'0' WHERE recurrente IS NULL");

        addColumnIfMissing("gastos", "tipo_gasto", "ALTER TABLE gastos ADD COLUMN tipo_gasto VARCHAR(20) NOT NULL DEFAULT 'VARIABLE'");
        addColumnIfMissing("gastos", "fecha_vencimiento", "ALTER TABLE gastos ADD COLUMN fecha_vencimiento DATE NULL");
        addColumnIfMissing("gastos", "gasto_recurrente_id", "ALTER TABLE gastos ADD COLUMN gasto_recurrente_id BIGINT NULL");

        executeIgnoring("CREATE INDEX idx_gastos_fecha_vencimiento ON gastos(fecha_vencimiento)");
        executeIgnoring("CREATE INDEX idx_gastos_estado ON gastos(estado)");
        executeIgnoring("CREATE UNIQUE INDEX uk_gastos_recurrente_vencimiento ON gastos(gasto_recurrente_id, fecha_vencimiento)");
        executeIgnoring("ALTER TABLE gastos ADD CONSTRAINT fk_gastos_recurrente FOREIGN KEY (gasto_recurrente_id) REFERENCES gastos_recurrentes(id)");
    }

    private void normalizeLegacyData() {
        executeIgnoring("UPDATE gastos SET importe = monto WHERE (importe IS NULL OR importe = 0) AND monto IS NOT NULL AND monto > 0");
        executeIgnoring("UPDATE gastos SET fecha_vencimiento = fecha_gasto WHERE fecha_vencimiento IS NULL");
        executeIgnoring("UPDATE gastos SET categoria = 'MATERIAL' WHERE categoria = 'COMPRA_MATERIAL'");
        executeIgnoring("UPDATE gastos SET categoria = 'LUZ' WHERE categoria = 'SUMINISTROS' AND LOWER(concepto) LIKE '%luz%'");
        executeIgnoring("UPDATE gastos SET categoria = 'AGUA' WHERE categoria = 'SUMINISTROS' AND LOWER(concepto) LIKE '%agua%'");
        executeIgnoring("UPDATE gastos SET categoria = 'INTERNET' WHERE categoria = 'SUMINISTROS' AND LOWER(concepto) LIKE '%internet%'");
        executeIgnoring("UPDATE gastos SET categoria = 'OTROS' WHERE categoria = 'SUMINISTROS'");
        executeIgnoring("UPDATE gastos SET categoria = 'MAQUINA' WHERE categoria = 'MANTENIMIENTO' AND maquina_id IS NOT NULL");

        executeIgnoring("""
                UPDATE gastos
                SET tipo_gasto = CASE
                    WHEN recurrente = b'1' OR categoria IN ('ALQUILER','LUZ','AGUA','INTERNET','NOMINA','SOFTWARE','LIMPIEZA') THEN 'FIJO'
                    ELSE 'VARIABLE'
                END
                WHERE tipo_gasto IS NULL OR tipo_gasto = ''
                """);

        updateIgnoring("""
                UPDATE gastos
                SET estado = CASE
                    WHEN estado IN ('CANCELADO','RECHAZADO') THEN 'CANCELADO'
                    WHEN pagado = b'1' THEN 'PAGADO'
                    WHEN fecha_vencimiento < ? THEN 'VENCIDO'
                    WHEN fecha_vencimiento > ? THEN 'PROGRAMADO'
                    ELSE 'PENDIENTE'
                END
                WHERE estado IN ('APROBADO','REGISTRADO','RECHAZADO','CANCELADO')
                   OR estado IS NULL
                   OR estado = ''
                """, Date.valueOf(operationalClockService.today()), Date.valueOf(operationalClockService.today()));
    }

    private void migrateRecurringTemplates() {
        if (!tableExists("gastos_recurrentes")) {
            return;
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT concepto,
                       categoria,
                       COALESCE(tipo_gasto, 'FIJO') AS tipo_gasto,
                       COALESCE(MAX(importe), 0) AS importe,
                       COALESCE(frecuencia, 'MENSUAL') AS frecuencia,
                       MIN(fecha_gasto) AS fecha_inicio,
                       MAX(fecha_gasto) AS ultima_fecha,
                       MAX(proveedor) AS proveedor,
                       MAX(observaciones) AS observaciones,
                       MAX(staff_responsable_id) AS staff_responsable_id,
                       MAX(maquina_id) AS maquina_id,
                       MAX(material_id) AS material_id
                FROM gastos
                WHERE recurrente = b'1'
                GROUP BY concepto, categoria, frecuencia
                """);

        for (Map<String, Object> row : rows) {
            String concepto = stringValue(row.get("concepto"));
            String categoria = stringValue(row.get("categoria"));
            String frecuencia = stringValue(row.get("frecuencia"));
            if (concepto == null || categoria == null) {
                continue;
            }

            Long templateId = findRecurringTemplate(concepto, categoria);
            if (templateId == null) {
                LocalDate fechaInicio = toLocalDate(row.get("fecha_inicio"), operationalClockService.today());
                LocalDate ultimaFecha = toLocalDate(row.get("ultima_fecha"), fechaInicio);
                LocalDate proximoCargo = siguienteFecha(ultimaFecha, frecuencia);

                jdbcTemplate.update("""
                        INSERT INTO gastos_recurrentes
                            (concepto, categoria, tipo_gasto, importe, frecuencia, fecha_inicio, fecha_proximo_cargo,
                             proveedor, observaciones, staff_responsable_id, maquina_id, material_id, activo, fecha_creacion)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP)
                        """,
                        concepto,
                        categoria,
                        stringValue(row.get("tipo_gasto")),
                        row.get("importe") instanceof BigDecimal amount ? amount : BigDecimal.ZERO,
                        frecuencia,
                        Date.valueOf(fechaInicio),
                        Date.valueOf(proximoCargo),
                        stringValue(row.get("proveedor")),
                        stringValue(row.get("observaciones")),
                        row.get("staff_responsable_id"),
                        row.get("maquina_id"),
                        row.get("material_id"));
                templateId = findRecurringTemplate(concepto, categoria);
            }

            if (templateId != null) {
                jdbcTemplate.update("""
                        UPDATE gastos
                        SET gasto_recurrente_id = ?
                        WHERE gasto_recurrente_id IS NULL
                          AND concepto = ?
                          AND categoria = ?
                          AND recurrente = b'1'
                        """, templateId, concepto, categoria);
            }
        }
    }

    private Long findRecurringTemplate(String concepto, String categoria) {
        List<Long> ids = jdbcTemplate.queryForList("""
                SELECT id
                FROM gastos_recurrentes
                WHERE concepto = ? AND categoria = ?
                ORDER BY id ASC
                LIMIT 1
                """, Long.class, concepto, categoria);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private LocalDate siguienteFecha(LocalDate fecha, String frecuencia) {
        FrecuenciaGasto normalized = FrecuenciaGasto.valueOf(frecuencia.toUpperCase(Locale.ROOT));
        return switch (normalized) {
            case SEMANAL -> fecha.plusWeeks(1);
            case QUINCENAL -> fecha.plusDays(15);
            case MENSUAL -> fecha.plusMonths(1);
            case TRIMESTRAL -> fecha.plusMonths(3);
            case ANUAL -> fecha.plusYears(1);
        };
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    private void addColumnIfMissing(String tableName, String columnName, String sql) {
        if (!columnExists(tableName, columnName)) {
            executeIgnoring(sql);
        }
    }

    private void executeIgnoring(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {
            LOGGER.debug("Migracion financiera omitida o ya aplicada: {}", sql, ex);
        }
    }

    private void updateIgnoring(String sql, Object... args) {
        try {
            jdbcTemplate.update(sql, args);
        } catch (DataAccessException ex) {
            LOGGER.debug("Migracion financiera omitida o ya aplicada: {}", sql, ex);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDate toLocalDate(Object value, LocalDate fallback) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return fallback;
    }
}
