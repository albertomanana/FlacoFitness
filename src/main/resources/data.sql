INSERT INTO roles (nombre)
SELECT 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE nombre = 'ADMIN'
);

INSERT INTO roles (nombre)
SELECT 'CLIENTE'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE nombre = 'CLIENTE'
);

INSERT INTO planes (nombre, precio_mensual, activo)
SELECT 'Basico', 29.90, true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Basico'
);

INSERT INTO planes (nombre, precio_mensual, activo)
SELECT 'Premium', 49.90, true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Premium'
);
