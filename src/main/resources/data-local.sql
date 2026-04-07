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

INSERT INTO usuarios (
    nombre,
    apellidos,
    dni,
    email,
    telefono,
    direccion,
    activo,
    fecha_registro,
    fecha_proximo_pago,
    rol_id,
    plan_id
)
SELECT
    'Carlos',
    'Martinez',
    '12345678A',
    'carlos@demo.com',
    '600123123',
    'Calle Principal 1',
    true,
    DATEADD('MONTH', -2, CURRENT_TIMESTAMP),
    DATEADD('DAY', 15, CURRENT_DATE),
    (SELECT id FROM roles WHERE nombre = 'CLIENTE'),
    (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'carlos@demo.com'
);
