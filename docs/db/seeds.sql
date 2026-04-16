-- FlacoFitness - referencia de datos semilla
-- Para desarrollo real se recomienda el perfil `local`, que ejecuta
-- `DemoDataSeeder` y genera una demo completa sin tocar MySQL.

INSERT INTO roles (nombre)
SELECT 'STAFF'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'STAFF');

INSERT INTO roles (nombre)
SELECT 'CLIENTE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'CLIENTE');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Basico',
       'Plan mensual base para acceso general',
       'MENSUAL',
       'Acceso general, registro de asistencias y rutinas base.',
       29.90,
       30,
       true
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre = 'Basico');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Premium',
       'Plan mensual completo con mayor cobertura de servicios',
       'PREMIUM',
       'Rutinas personalizadas, prioridad en clases y seguimiento ampliado.',
       49.90,
       30,
       true
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre = 'Premium');

-- La demo rica incluye:
-- - usuarios clientes y staff
-- - pagos y contratos de membresia
-- - asistencias libres y asistencias a sesiones
-- - clases, sesiones y reservas
-- - trials comerciales
-- - rutinas asignadas y staff responsable
--
-- Esa carga vive en:
-- src/main/java/com/flacofitness/app/config/DemoDataSeeder.java
