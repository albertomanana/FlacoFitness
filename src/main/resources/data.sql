-- FlacoFitness — semilla segura para MySQL (se ejecuta en AMBOS perfiles via spring.sql.init)
-- Todos los inserts son idempotentes (WHERE NOT EXISTS / ON DUPLICATE KEY).
-- Sin funciones H2. Compatible con MySQL 8+ y MariaDB.

-- ─── ROLES ───────────────────────────────────────────────────────────────────
INSERT INTO roles (nombre) VALUES ('STAFF')   ON DUPLICATE KEY UPDATE nombre=nombre;
INSERT INTO roles (nombre) VALUES ('CLIENTE')  ON DUPLICATE KEY UPDATE nombre=nombre;

-- ─── PLANES BASE ─────────────────────────────────────────────────────────────
INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Basico','Plan mensual base para acceso general','MENSUAL',
       'Acceso general, registro de asistencias y rutinas base.',29.90,30,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Basico');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Premium','Plan mensual completo con mayor cobertura de servicios','PREMIUM',
       'Rutinas personalizadas, prioridad en clases y seguimiento ampliado.',49.90,30,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Premium');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Trimestral','Plan trimestral con descuento incluido','TRIMESTRAL',
       '3 meses de acceso completo con descuento especial.',79.90,90,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Trimestral');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Estudiante','Plan especial para estudiantes universitarios','ESTUDIANTE',
       'Acceso completo con descuento academico verificable.',19.90,30,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Estudiante');
