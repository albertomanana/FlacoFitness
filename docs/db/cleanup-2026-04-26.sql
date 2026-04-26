-- FlacoFitness definitive cleanup - 2026-04-26
--
-- Required before running:
--   mysqldump -uflaco_user -pflaco_pass flacofitness \
--     app_clock_settings staff sesiones ejercicios rutina_ejercicios \
--     gastos reservas_sesion trials > tmp/db-backups/flacofitness-cleanup.sql
--
-- This script removes legacy MySQL artifacts after data-preserving migrations.

USE flacofitness;

DROP PROCEDURE IF EXISTS ff_drop_fk_if_exists;
DROP PROCEDURE IF EXISTS ff_drop_index_if_exists;
DROP PROCEDURE IF EXISTS ff_drop_column_if_exists;
DROP PROCEDURE IF EXISTS ff_exec_if_column_exists;

DELIMITER //

CREATE PROCEDURE ff_drop_fk_if_exists(IN p_table_name VARCHAR(128), IN p_constraint_name VARCHAR(128))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_constraint_name
          AND constraint_type = 'FOREIGN KEY'
    ) THEN
        SET @ff_sql = CONCAT('ALTER TABLE `', p_table_name, '` DROP FOREIGN KEY `', p_constraint_name, '`');
        PREPARE ff_stmt FROM @ff_sql;
        EXECUTE ff_stmt;
        DEALLOCATE PREPARE ff_stmt;
    END IF;
END//

CREATE PROCEDURE ff_drop_index_if_exists(IN p_table_name VARCHAR(128), IN p_index_name VARCHAR(128))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ff_sql = CONCAT('ALTER TABLE `', p_table_name, '` DROP INDEX `', p_index_name, '`');
        PREPARE ff_stmt FROM @ff_sql;
        EXECUTE ff_stmt;
        DEALLOCATE PREPARE ff_stmt;
    END IF;
END//

CREATE PROCEDURE ff_drop_column_if_exists(IN p_table_name VARCHAR(128), IN p_column_name VARCHAR(128))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ff_sql = CONCAT('ALTER TABLE `', p_table_name, '` DROP COLUMN `', p_column_name, '`');
        PREPARE ff_stmt FROM @ff_sql;
        EXECUTE ff_stmt;
        DEALLOCATE PREPARE ff_stmt;
    END IF;
END//

CREATE PROCEDURE ff_exec_if_column_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_sql TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ff_sql = p_sql;
        PREPARE ff_stmt FROM @ff_sql;
        EXECUTE ff_stmt;
        DEALLOCATE PREPARE ff_stmt;
    END IF;
END//

DELIMITER ;

-- Preserve useful values from legacy columns before dropping them.
CALL ff_exec_if_column_exists(
    'gastos',
    'monto',
    'UPDATE gastos SET importe = monto WHERE monto IS NOT NULL AND (importe IS NULL OR importe = 0)'
);

CALL ff_exec_if_column_exists(
    'gastos',
    'descripcion',
    'UPDATE gastos SET observaciones = descripcion WHERE descripcion IS NOT NULL AND descripcion <> '''' AND (observaciones IS NULL OR observaciones = '''')'
);

CALL ff_exec_if_column_exists(
    'gastos',
    'pagado',
    'UPDATE gastos SET estado = ''PAGADO'' WHERE pagado = b''1'' AND estado <> ''PAGADO'''
);

CALL ff_exec_if_column_exists(
    'gastos',
    'staff_id',
    'UPDATE gastos g JOIN staff_perfiles sp ON sp.id = g.staff_id SET g.staff_responsable_id = sp.id WHERE g.staff_id IS NOT NULL AND g.staff_responsable_id IS NULL'
);

CALL ff_exec_if_column_exists(
    'gastos',
    'recurrente',
    'UPDATE gastos g JOIN gastos_recurrentes gr ON gr.concepto = g.concepto SET g.gasto_recurrente_id = gr.id WHERE g.gasto_recurrente_id IS NULL AND (g.recurrente = b''1'' OR g.frecuencia IS NOT NULL)'
);

CALL ff_exec_if_column_exists(
    'gastos',
    'recurrente',
    'UPDATE gastos SET tipo_gasto = ''FIJO'' WHERE recurrente = b''1'' OR frecuencia IS NOT NULL'
);

CALL ff_exec_if_column_exists(
    'reservas_sesion',
    'sesion_clase_id',
    'UPDATE reservas_sesion SET sesion_id = sesion_clase_id WHERE sesion_clase_id IS NOT NULL AND (sesion_id IS NULL OR sesion_id <> sesion_clase_id)'
);

CALL ff_exec_if_column_exists(
    'reservas_sesion',
    'asistio',
    'UPDATE reservas_sesion SET estado = ''ASISTIO'' WHERE asistio = b''1'' AND estado = ''RESERVADA'''
);

-- Keep only the current model foreign keys.
CALL ff_drop_fk_if_exists('trials', 'FKrypafjh4htvumn6642nm6uttg');
CALL ff_drop_fk_if_exists('gastos', 'FKfuremlnhdul0vvt76sivol70o');
CALL ff_drop_fk_if_exists('gastos', 'fk_gastos_recurrente');
CALL ff_drop_fk_if_exists('reservas_sesion', 'FKajkkueqxo85qjywc80dkwuhem');
CALL ff_drop_fk_if_exists('reservas_sesion', 'FK8yrvoqmo90xcxvpwrm7a32kqe');

CALL ff_drop_index_if_exists('reservas_sesion', 'UKqa3ldd4hxtbb1cxcab7hpr6qe');

-- Remove columns that no current JPA entity maps.
CALL ff_drop_column_if_exists('gastos', 'monto');
CALL ff_drop_column_if_exists('gastos', 'descripcion');
CALL ff_drop_column_if_exists('gastos', 'pagado');
CALL ff_drop_column_if_exists('gastos', 'recurrente');
CALL ff_drop_column_if_exists('gastos', 'frecuencia');
CALL ff_drop_column_if_exists('gastos', 'staff_id');
CALL ff_drop_column_if_exists('reservas_sesion', 'sesion_clase_id');
CALL ff_drop_column_if_exists('reservas_sesion', 'asistio');
CALL ff_drop_column_if_exists('reservas_sesion', 'observaciones');

-- Normalize the active commercial catalogue without deleting history.
UPDATE planes
SET precio_mensual = 29.00,
    duracion_dias = 30,
    activo = 1,
    tipo_membresia = 'MENSUAL'
WHERE nombre = 'Basico';

UPDATE planes
SET precio_mensual = 19.00,
    duracion_dias = 30,
    activo = 1,
    tipo_membresia = 'ESTUDIANTE'
WHERE nombre = 'Estudiante';

UPDATE planes
SET activo = 0
WHERE nombre IN ('Premium', 'Plus', 'Trimestral')
   OR tipo_membresia IN ('PREMIUM', 'TRIMESTRAL', 'PRUEBA');

-- Drop legacy tables once their references are removed.
DROP TABLE IF EXISTS app_clock_settings;
DROP TABLE IF EXISTS rutina_ejercicios;
DROP TABLE IF EXISTS ejercicios;
DROP TABLE IF EXISTS sesiones;
DROP TABLE IF EXISTS staff;

DROP PROCEDURE IF EXISTS ff_drop_fk_if_exists;
DROP PROCEDURE IF EXISTS ff_drop_index_if_exists;
DROP PROCEDURE IF EXISTS ff_drop_column_if_exists;
DROP PROCEDURE IF EXISTS ff_exec_if_column_exists;

-- Post-cleanup summary.
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
ORDER BY table_name;

SELECT
    kcu.table_name,
    kcu.column_name,
    kcu.referenced_table_name,
    kcu.constraint_name
FROM information_schema.key_column_usage kcu
WHERE kcu.table_schema = DATABASE()
  AND kcu.referenced_table_name IS NOT NULL
ORDER BY kcu.table_name, kcu.constraint_name, kcu.column_name;
