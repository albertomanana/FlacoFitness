-- ═══════════════════════════════════════════════════════════
-- FlacoFitness — Datos semilla (perfil local / H2 MODE=MySQL)
-- ═══════════════════════════════════════════════════════════

-- Roles
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

-- Planes
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

-- ──────────────────────────────────────────────
-- Usuarios demo (5 usuarios)
-- ──────────────────────────────────────────────
INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'Carlos', 'Martinez', '12345678A', 'carlos@demo.com', '600123123', 'Calle Principal 1', true,
       DATEADD('MONTH', -6, CURRENT_TIMESTAMP), DATEADD('DAY', 15, CURRENT_DATE),
       (SELECT id FROM roles WHERE nombre = 'CLIENTE'),
       (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'carlos@demo.com');

INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'Ana', 'Garcia', '23456789B', 'ana@demo.com', '600234234', 'Avenida Central 12', true,
       DATEADD('MONTH', -5, CURRENT_TIMESTAMP), DATEADD('DAY', 8, CURRENT_DATE),
       (SELECT id FROM roles WHERE nombre = 'CLIENTE'),
       (SELECT id FROM planes WHERE nombre = 'Basico')
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'ana@demo.com');

INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'David', 'Lopez', '34567890C', 'david@demo.com', '600345345', 'Plaza Mayor 3', true,
       DATEADD('MONTH', -4, CURRENT_TIMESTAMP), DATEADD('DAY', 22, CURRENT_DATE),
       (SELECT id FROM roles WHERE nombre = 'CLIENTE'),
       (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'david@demo.com');

INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'Maria', 'Sanchez', '45678901D', 'maria@demo.com', '600456456', 'Calle Luna 7', true,
       DATEADD('MONTH', -2, CURRENT_TIMESTAMP), DATEADD('DAY', 30, CURRENT_DATE),
       (SELECT id FROM roles WHERE nombre = 'CLIENTE'),
       (SELECT id FROM planes WHERE nombre = 'Basico')
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'maria@demo.com');

INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'Jorge', 'Perez', '56789012E', 'jorge@demo.com', '600567567', 'Paseo del Parque 22', false,
       DATEADD('MONTH', -1, CURRENT_TIMESTAMP), NULL,
       (SELECT id FROM roles WHERE nombre = 'CLIENTE'),
       (SELECT id FROM planes WHERE nombre = 'Basico')
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'jorge@demo.com');

-- ──────────────────────────────────────────────
-- Pagos demo
-- ──────────────────────────────────────────────
INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', -1, CURRENT_DATE), 49.90, 'TARJETA', 'PAGADO', 'REF-C1',
       (SELECT id FROM usuarios WHERE email = 'carlos@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-C1');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT DATEADD('MONTH', -1, CURRENT_DATE), CURRENT_DATE, 49.90, 'TARJETA', 'PAGADO', 'REF-C2',
       (SELECT id FROM usuarios WHERE email = 'carlos@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-C2');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT NULL, DATEADD('DAY', 15, CURRENT_DATE), 49.90, 'TARJETA', 'PENDIENTE', 'REF-C3',
       (SELECT id FROM usuarios WHERE email = 'carlos@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-C3');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', -1, CURRENT_DATE), 29.90, 'TRANSFERENCIA', 'PAGADO', 'REF-A1',
       (SELECT id FROM usuarios WHERE email = 'ana@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Basico')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-A1');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT DATEADD('MONTH', -1, CURRENT_DATE), CURRENT_DATE, 29.90, 'TRANSFERENCIA', 'PAGADO', 'REF-A2',
       (SELECT id FROM usuarios WHERE email = 'ana@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Basico')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-A2');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT NULL, DATEADD('DAY', 15, CURRENT_DATE), 29.90, 'TRANSFERENCIA', 'PENDIENTE', 'REF-A3',
       (SELECT id FROM usuarios WHERE email = 'ana@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Basico')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-A3');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT DATEADD('MONTH', -3, CURRENT_DATE), DATEADD('MONTH', -2, CURRENT_DATE), 49.90, 'EFECTIVO', 'PAGADO', 'REF-D1',
       (SELECT id FROM usuarios WHERE email = 'david@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-D1');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', -1, CURRENT_DATE), 49.90, 'EFECTIVO', 'PAGADO', 'REF-D2',
       (SELECT id FROM usuarios WHERE email = 'david@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Premium')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-D2');

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT DATEADD('DAY', -45, CURRENT_DATE), DATEADD('DAY', -40, CURRENT_DATE), 29.90, 'TARJETA', 'VENCIDO', 'REF-M1',
       (SELECT id FROM usuarios WHERE email = 'maria@demo.com'),
       (SELECT id FROM planes WHERE nombre = 'Basico')
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia = 'REF-M1');

-- ──────────────────────────────────────────────
-- Asistencias demo
-- ──────────────────────────────────────────────
INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT CURRENT_DATE, '07:45:00',
       (SELECT id FROM usuarios WHERE email = 'carlos@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'carlos@demo.com' AND a.fecha = CURRENT_DATE
);

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT DATEADD('DAY', -1, CURRENT_DATE), '09:00:00',
       (SELECT id FROM usuarios WHERE email = 'carlos@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'carlos@demo.com' AND a.fecha = DATEADD('DAY', -1, CURRENT_DATE)
);

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT DATEADD('DAY', -2, CURRENT_DATE), '09:30:00',
       (SELECT id FROM usuarios WHERE email = 'carlos@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'carlos@demo.com' AND a.fecha = DATEADD('DAY', -2, CURRENT_DATE)
);

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT DATEADD('DAY', -1, CURRENT_DATE), '18:15:00',
       (SELECT id FROM usuarios WHERE email = 'ana@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'ana@demo.com' AND a.fecha = DATEADD('DAY', -1, CURRENT_DATE)
);

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT DATEADD('DAY', -3, CURRENT_DATE), '18:00:00',
       (SELECT id FROM usuarios WHERE email = 'ana@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'ana@demo.com' AND a.fecha = DATEADD('DAY', -3, CURRENT_DATE)
);

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT CURRENT_DATE, '08:00:00',
       (SELECT id FROM usuarios WHERE email = 'david@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'david@demo.com' AND a.fecha = CURRENT_DATE
);

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT DATEADD('DAY', -5, CURRENT_DATE), '08:00:00',
       (SELECT id FROM usuarios WHERE email = 'david@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'david@demo.com' AND a.fecha = DATEADD('DAY', -5, CURRENT_DATE)
);

INSERT INTO asistencias (fecha, hora_entrada, usuario_id)
SELECT DATEADD('DAY', -1, CURRENT_DATE), '17:30:00',
       (SELECT id FROM usuarios WHERE email = 'maria@demo.com')
WHERE NOT EXISTS (
    SELECT 1 FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id
    WHERE u.email = 'maria@demo.com' AND a.fecha = DATEADD('DAY', -1, CURRENT_DATE)
);

-- ──────────────────────────────────────────────
-- Rutinas demo
-- ──────────────────────────────────────────────
INSERT INTO rutinas (nombre, descripcion, tipo_rutina, activa, fecha_creacion)
SELECT 'Full Body Principiante', 'Rutina de cuerpo completo para usuarios que inician en el gimnasio', 'GENERAL', true,
       DATEADD('MONTH', -3, CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Full Body Principiante');

INSERT INTO rutinas (nombre, descripcion, tipo_rutina, activa, fecha_creacion)
SELECT 'HIIT Cardio', 'Entrenamiento intervalico de alta intensidad para quemar grasa', 'GENERAL', true,
       DATEADD('MONTH', -2, CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'HIIT Cardio');

INSERT INTO rutinas (nombre, descripcion, tipo_rutina, activa, fecha_creacion)
SELECT 'Plan Carlos Premium', 'Rutina personalizada para Carlos', 'PERSONALIZADA', true,
       DATEADD('MONTH', -1, CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Plan Carlos Premium');

-- Assign Carlos to his personalized routine
INSERT INTO usuario_rutina (usuario_id, rutina_id)
SELECT
    (SELECT id FROM usuarios WHERE email = 'carlos@demo.com'),
    (SELECT id FROM rutinas WHERE nombre = 'Plan Carlos Premium')
WHERE NOT EXISTS (
    SELECT 1 FROM usuario_rutina
    WHERE usuario_id = (SELECT id FROM usuarios WHERE email = 'carlos@demo.com')
      AND rutina_id = (SELECT id FROM rutinas WHERE nombre = 'Plan Carlos Premium')
);

-- Assign Ana and David to the general routines
INSERT INTO usuario_rutina (usuario_id, rutina_id)
SELECT
    (SELECT id FROM usuarios WHERE email = 'ana@demo.com'),
    (SELECT id FROM rutinas WHERE nombre = 'HIIT Cardio')
WHERE NOT EXISTS (
    SELECT 1 FROM usuario_rutina
    WHERE usuario_id = (SELECT id FROM usuarios WHERE email = 'ana@demo.com')
      AND rutina_id = (SELECT id FROM rutinas WHERE nombre = 'HIIT Cardio')
);

INSERT INTO usuario_rutina (usuario_id, rutina_id)
SELECT
    (SELECT id FROM usuarios WHERE email = 'david@demo.com'),
    (SELECT id FROM rutinas WHERE nombre = 'Full Body Principiante')
WHERE NOT EXISTS (
    SELECT 1 FROM usuario_rutina
    WHERE usuario_id = (SELECT id FROM usuarios WHERE email = 'david@demo.com')
      AND rutina_id = (SELECT id FROM rutinas WHERE nombre = 'Full Body Principiante')
);
