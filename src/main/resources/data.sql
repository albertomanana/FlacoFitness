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

-- Datos demo de usuarios
INSERT INTO usuarios (nombre, apellidos, email, activo, fecha_registro, rol_id, plan_id)
SELECT
    'Carlos',
    'Martinez',
    'carlos@demo.com',
    true,
    DATE_SUB(CURDATE(), INTERVAL 6 MONTH),
    (SELECT id FROM roles WHERE nombre = 'CLIENTE' ORDER BY id ASC LIMIT 1),
    (SELECT id FROM planes WHERE nombre = 'Premium' ORDER BY id ASC LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'carlos@demo.com'
);

INSERT INTO usuarios (nombre, apellidos, email, activo, fecha_registro, rol_id, plan_id)
SELECT
    'Ana',
    'Garcia',
    'ana@demo.com',
    true,
    DATE_SUB(CURDATE(), INTERVAL 5 MONTH),
    (SELECT id FROM roles WHERE nombre = 'CLIENTE' ORDER BY id ASC LIMIT 1),
    (SELECT id FROM planes WHERE nombre = 'Basico' ORDER BY id ASC LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'ana@demo.com'
);

INSERT INTO usuarios (nombre, apellidos, email, activo, fecha_registro, rol_id, plan_id)
SELECT
    'David',
    'Lopez',
    'david@demo.com',
    true,
    DATE_SUB(CURDATE(), INTERVAL 4 MONTH),
    (SELECT id FROM roles WHERE nombre = 'CLIENTE' ORDER BY id ASC LIMIT 1),
    (SELECT id FROM planes WHERE nombre = 'Premium' ORDER BY id ASC LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'david@demo.com'
);

INSERT INTO usuarios (nombre, apellidos, email, activo, fecha_registro, rol_id, plan_id)
SELECT
    'Maria',
    'Sanchez',
    'maria@demo.com',
    true,
    DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
    (SELECT id FROM roles WHERE nombre = 'CLIENTE' ORDER BY id ASC LIMIT 1),
    (SELECT id FROM planes WHERE nombre = 'Basico' ORDER BY id ASC LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'maria@demo.com'
);

INSERT INTO usuarios (nombre, apellidos, email, activo, fecha_registro, rol_id, plan_id)
SELECT
    'Jorge',
    'Perez',
    'jorge@demo.com',
    false,
    DATE_SUB(CURDATE(), INTERVAL 1 MONTH),
    (SELECT id FROM roles WHERE nombre = 'CLIENTE' ORDER BY id ASC LIMIT 1),
    (SELECT id FROM planes WHERE nombre = 'Basico' ORDER BY id ASC LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'jorge@demo.com'
);

-- Datos demo de pagos
INSERT INTO pagos (fecha_pago, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
    49.90,
    'TARJETA',
    'PAGADO',
    'REF-C1',
    id,
    plan_id
FROM usuarios
WHERE email = 'carlos@demo.com'
  AND NOT EXISTS (
      SELECT 1 FROM pagos WHERE referencia = 'REF-C1'
  );

INSERT INTO pagos (fecha_pago, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 1 MONTH),
    49.90,
    'TARJETA',
    'PAGADO',
    'REF-C2',
    id,
    plan_id
FROM usuarios
WHERE email = 'carlos@demo.com'
  AND NOT EXISTS (
      SELECT 1 FROM pagos WHERE referencia = 'REF-C2'
  );

INSERT INTO pagos (fecha_pago, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
    29.90,
    'TRANSFERENCIA',
    'PAGADO',
    'REF-A1',
    id,
    plan_id
FROM usuarios
WHERE email = 'ana@demo.com'
  AND NOT EXISTS (
      SELECT 1 FROM pagos WHERE referencia = 'REF-A1'
  );

INSERT INTO pagos (fecha_pago, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    CURDATE(),
    29.90,
    'TRANSFERENCIA',
    'PENDIENTE',
    'REF-A2',
    id,
    plan_id
FROM usuarios
WHERE email = 'ana@demo.com'
  AND NOT EXISTS (
      SELECT 1 FROM pagos WHERE referencia = 'REF-A2'
  );

INSERT INTO pagos (fecha_pago, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 3 MONTH),
    49.90,
    'EFECTIVO',
    'PAGADO',
    'REF-D1',
    id,
    plan_id
FROM usuarios
WHERE email = 'david@demo.com'
  AND NOT EXISTS (
      SELECT 1 FROM pagos WHERE referencia = 'REF-D1'
  );

-- Datos demo de asistencias
INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 1 DAY),
    '09:00:00',
    id
FROM usuarios
WHERE email = 'carlos@demo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM asistencias a
      JOIN usuarios u ON a.usuario_id = u.id
      WHERE u.email = 'carlos@demo.com'
        AND a.fecha = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
  );

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 2 DAY),
    '09:30:00',
    id
FROM usuarios
WHERE email = 'carlos@demo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM asistencias a
      JOIN usuarios u ON a.usuario_id = u.id
      WHERE u.email = 'carlos@demo.com'
        AND a.fecha = DATE_SUB(CURDATE(), INTERVAL 2 DAY)
  );

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 1 DAY),
    '18:15:00',
    id
FROM usuarios
WHERE email = 'ana@demo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM asistencias a
      JOIN usuarios u ON a.usuario_id = u.id
      WHERE u.email = 'ana@demo.com'
        AND a.fecha = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
  );

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 3 DAY),
    '18:00:00',
    id
FROM usuarios
WHERE email = 'ana@demo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM asistencias a
      JOIN usuarios u ON a.usuario_id = u.id
      WHERE u.email = 'ana@demo.com'
        AND a.fecha = DATE_SUB(CURDATE(), INTERVAL 3 DAY)
  );

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT
    CURDATE(),
    '07:45:00',
    id
FROM usuarios
WHERE email = 'david@demo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM asistencias a
      JOIN usuarios u ON a.usuario_id = u.id
      WHERE u.email = 'david@demo.com'
        AND a.fecha = CURDATE()
  );

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 5 DAY),
    '08:00:00',
    id
FROM usuarios
WHERE email = 'david@demo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM asistencias a
      JOIN usuarios u ON a.usuario_id = u.id
      WHERE u.email = 'david@demo.com'
        AND a.fecha = DATE_SUB(CURDATE(), INTERVAL 5 DAY)
  );
