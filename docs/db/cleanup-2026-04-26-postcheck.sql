-- FlacoFitness cleanup postcheck - 2026-04-26
-- Run after docs/db/cleanup-2026-04-26.sql.

USE flacofitness;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
ORDER BY table_name;

SELECT
    'legacy_tables_remaining' AS check_name,
    COUNT(*) AS value
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('app_clock_settings', 'staff', 'sesiones', 'ejercicios', 'rutina_ejercicios');

SELECT
    'legacy_fk_remaining' AS check_name,
    COUNT(*) AS value
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND referenced_table_name IN ('staff', 'sesiones', 'ejercicios', 'app_clock_settings');

SELECT
    'gastos_legacy_columns_remaining' AS check_name,
    COUNT(*) AS value
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'gastos'
  AND column_name IN ('monto', 'descripcion', 'pagado', 'recurrente', 'frecuencia', 'staff_id');

SELECT
    'reservas_legacy_columns_remaining' AS check_name,
    COUNT(*) AS value
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'reservas_sesion'
  AND column_name IN ('sesion_clase_id', 'asistio', 'observaciones');

SELECT
    id,
    nombre,
    tipo_membresia,
    precio_mensual,
    duracion_dias,
    activo
FROM planes
ORDER BY id;

SELECT
    id,
    concepto,
    importe,
    estado,
    tipo_gasto,
    staff_responsable_id,
    gasto_recurrente_id
FROM gastos
ORDER BY id;
