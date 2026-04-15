-- FlacoFitness - semilla segura para MySQL/MariaDB si se habilita spring.sql.init.mode.
-- No crea datos demo operativos para evitar contaminar una base real.

INSERT INTO roles (nombre)
SELECT 'STAFF'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE nombre = 'STAFF'
);

INSERT INTO roles (nombre)
SELECT 'CLIENTE'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE nombre = 'CLIENTE'
);

UPDATE usuarios
SET rol_id = (
    SELECT id FROM roles WHERE nombre = 'STAFF' ORDER BY id ASC LIMIT 1
)
WHERE rol_id IN (
    SELECT id FROM roles WHERE nombre = 'ADMIN'
);

DELETE FROM roles
WHERE nombre = 'ADMIN';

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Basico',
       'Plan mensual base para acceso general',
       'MENSUAL',
       'Acceso general, registro de asistencias y rutinas base.',
       29.90,
       30,
       true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Basico'
);

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Premium',
       'Plan mensual completo con mayor cobertura de servicios',
       'PREMIUM',
       'Rutinas personalizadas, prioridad en clases y seguimiento ampliado.',
       49.90,
       30,
       true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Premium'
);
