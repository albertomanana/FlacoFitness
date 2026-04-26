-- FlacoFitness - referencia de datos semilla
-- En MySQL real `spring.sql.init.mode=never`; este archivo es referencia.
-- Para una demo controlada se puede activar `DemoDataSeeder` con
-- `app.demo-seeder.enabled=true`.

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
       29.00,
       30,
       true
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre = 'Basico');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Estudiante',
       'Cuota reducida para estudiantes',
       'ESTUDIANTE',
       'Acceso general con precio reducido.',
       19.00,
       30,
       true
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre = 'Estudiante');

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
