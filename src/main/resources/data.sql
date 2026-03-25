INSERT INTO roles (nombre)
SELECT 'STAFF'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE nombre = 'STAFF'
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

INSERT INTO roles (nombre)
SELECT 'CLIENTE'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE nombre = 'CLIENTE'
);

INSERT INTO planes (nombre, descripcion, precio_mensual, duracion_dias, activo)
SELECT 'Basico', 'Plan mensual base para acceso general', 29.90, 30, true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Basico'
);

INSERT INTO planes (nombre, descripcion, precio_mensual, duracion_dias, activo)
SELECT 'Premium', 'Plan mensual completo con mayor cobertura de servicios', 49.90, 30, true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Premium'
);
