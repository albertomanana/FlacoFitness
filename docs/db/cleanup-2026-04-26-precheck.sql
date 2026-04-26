-- FlacoFitness cleanup precheck - 2026-04-26
-- Run before docs/db/cleanup-2026-04-26.sql.
-- This script is read-only: it reports legacy tables, duplicate FKs and
-- rows that would block a safe cleanup.

USE flacofitness;

SELECT 'table_rows' AS check_name, 'app_clock_settings' AS object_name, COUNT(*) AS value FROM app_clock_settings
UNION ALL SELECT 'table_rows', 'staff', COUNT(*) FROM staff
UNION ALL SELECT 'table_rows', 'staff_perfiles', COUNT(*) FROM staff_perfiles
UNION ALL SELECT 'table_rows', 'sesiones', COUNT(*) FROM sesiones
UNION ALL SELECT 'table_rows', 'sesiones_clase', COUNT(*) FROM sesiones_clase
UNION ALL SELECT 'table_rows', 'ejercicios', COUNT(*) FROM ejercicios
UNION ALL SELECT 'table_rows', 'rutina_ejercicios', COUNT(*) FROM rutina_ejercicios
UNION ALL SELECT 'table_rows', 'reservas_sesion', COUNT(*) FROM reservas_sesion
UNION ALL SELECT 'table_rows', 'gastos', COUNT(*) FROM gastos
UNION ALL SELECT 'table_rows', 'trials', COUNT(*) FROM trials;

SELECT
    kcu.table_name,
    kcu.column_name,
    kcu.referenced_table_name,
    kcu.referenced_column_name,
    kcu.constraint_name
FROM information_schema.key_column_usage kcu
WHERE kcu.table_schema = DATABASE()
  AND kcu.referenced_table_name IS NOT NULL
  AND (
      kcu.table_name IN ('gastos', 'trials', 'reservas_sesion', 'sesiones', 'staff', 'rutina_ejercicios')
      OR kcu.referenced_table_name IN ('staff', 'sesiones', 'ejercicios', 'app_clock_settings')
  )
ORDER BY kcu.table_name, kcu.constraint_name, kcu.column_name;

SELECT
    'trials.staff_responsable_id missing staff_perfiles target' AS blocker,
    COUNT(*) AS blocker_count
FROM trials t
LEFT JOIN staff_perfiles sp ON sp.id = t.staff_responsable_id
WHERE t.staff_responsable_id IS NOT NULL
  AND sp.id IS NULL
UNION ALL
SELECT
    'gastos.staff_id missing staff_perfiles target',
    COUNT(*)
FROM gastos g
LEFT JOIN staff_perfiles sp ON sp.id = g.staff_id
WHERE g.staff_id IS NOT NULL
  AND sp.id IS NULL
UNION ALL
SELECT
    'reservas_sesion.sesion_id missing sesiones_clase target',
    COUNT(*)
FROM reservas_sesion r
LEFT JOIN sesiones_clase sc ON sc.id = r.sesion_id
WHERE sc.id IS NULL
UNION ALL
SELECT
    'reservas_sesion sesion_id != sesion_clase_id',
    COUNT(*)
FROM reservas_sesion r
WHERE r.sesion_clase_id IS NOT NULL
  AND r.sesion_id <> r.sesion_clase_id;

SELECT
    g.id,
    g.concepto,
    g.staff_id,
    g.staff_responsable_id,
    g.monto,
    g.importe,
    g.descripcion,
    g.observaciones,
    g.pagado + 0 AS pagado,
    g.recurrente + 0 AS recurrente,
    g.frecuencia,
    gr.id AS matching_recurrente_id
FROM gastos g
LEFT JOIN gastos_recurrentes gr ON gr.concepto = g.concepto
ORDER BY g.id;

SELECT
    id,
    nombre,
    tipo_membresia,
    precio_mensual,
    duracion_dias,
    activo
FROM planes
ORDER BY id;
