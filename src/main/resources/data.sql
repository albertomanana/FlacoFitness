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
SELECT 'Basico', 'Plan mensual base para acceso general', 29.00, 30, true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Basico'
);

INSERT INTO planes (nombre, descripcion, precio_mensual, duracion_dias, activo)
SELECT 'Estudiante', 'Plan mensual reducido para perfiles jovenes', 19.00, 30, true
WHERE NOT EXISTS (
    SELECT 1 FROM planes WHERE nombre = 'Estudiante'
);

INSERT INTO planes (nombre, descripcion, precio_mensual, duracion_dias, activo)
SELECT 'Premium', 'Plan historico completo mantenido solo para compatibilidad', 49.90, 30, false
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
INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
    DATE_SUB(CURDATE(), INTERVAL 1 MONTH),
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

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 1 MONTH),
    CURDATE(),
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

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 1 MONTH),
    49.90,
    'TARJETA',
    'PENDIENTE',
    'REF-C3',
    id,
    plan_id
FROM usuarios
WHERE email = 'carlos@demo.com'
  AND NOT EXISTS (
      SELECT 1 FROM pagos WHERE referencia = 'REF-C3'
  );

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
    DATE_SUB(CURDATE(), INTERVAL 1 MONTH),
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

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 1 MONTH),
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

INSERT INTO pagos (fecha_pago, fecha_vencimiento, monto, metodo_pago, estado, referencia, usuario_id, plan_id)
SELECT
    DATE_SUB(CURDATE(), INTERVAL 3 MONTH),
    DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
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

-- Staff visibles en /staff
INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'Antonio', 'Moreno Ibanez', '11111111A', 'antonio@demo.com', '600111111', 'Recepcion principal', true,
     DATE_SUB(NOW(), INTERVAL 8 MONTH), NULL,
     (SELECT id FROM roles WHERE nombre = 'STAFF' ORDER BY id ASC LIMIT 1),
     NULL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'antonio@demo.com');

INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'Claudia', 'Campos Torres', '22222222B', 'claudia@demo.com', '600222222', 'Sala de clases', true,
     DATE_SUB(NOW(), INTERVAL 7 MONTH), NULL,
     (SELECT id FROM roles WHERE nombre = 'STAFF' ORDER BY id ASC LIMIT 1),
     NULL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'claudia@demo.com');

INSERT INTO usuarios (nombre, apellidos, dni, email, telefono, direccion, activo, fecha_registro, fecha_proximo_pago, rol_id, plan_id)
SELECT 'Elena', 'Rivas Serrano', '33333333C', 'elena@demo.com', '600333333', 'Zona musculacion', true,
     DATE_SUB(NOW(), INTERVAL 6 MONTH), NULL,
     (SELECT id FROM roles WHERE nombre = 'STAFF' ORDER BY id ASC LIMIT 1),
     NULL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'elena@demo.com');

-- Rutinas extras visibles en la UI
INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Full Body Base', 'Plan general de adaptacion para comenzar la semana', 'FUERZA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 1 MONTH)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Full Body Base');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Cardio Salud', 'Rutina ligera para resistencia cardiovascular', 'MANTENIMIENTO', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 1 MONTH)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Cardio Salud');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Fuerza Funcional', 'Trabajo funcional para cuerpo completo', 'FUERZA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 25 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Fuerza Funcional');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'HIIT Metabolico', 'Sesiones intensas para elevar gasto calorico', 'PERDIDA_GRASA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 20 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'HIIT Metabolico');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Tren Superior', 'Rutina enfocada en torso y brazos', 'HIPERTROFIA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 18 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Tren Superior');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Tren Inferior', 'Rutina de piernas y cadena posterior', 'HIPERTROFIA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 16 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Tren Inferior');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Movilidad y Core', 'Movilidad articular y fortalecimiento central', 'MANTENIMIENTO', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 15 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Movilidad y Core');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Recuperacion Activa', 'Rutina suave para dias de descarga', 'MANTENIMIENTO', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 14 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Recuperacion Activa');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Circuito Energico', 'Circuito mixto de resistencia y fuerza', 'FUERZA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 13 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Circuito Energico');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Fuerza Mixta', 'Combinacion de cargas medias y control tecnico', 'HIPERTROFIA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 12 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Fuerza Mixta');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Gluteos y Core', 'Trabajo dirigido a gluteos y zona media', 'HIPERTROFIA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 11 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Gluteos y Core');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Espalda Fuerte', 'Rutina de traccion para dorsal y lumbar', 'FUERZA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 10 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Espalda Fuerte');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Pecho y Brazos', 'Rutina de empuje para tren superior', 'HIPERTROFIA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 9 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Pecho y Brazos');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Pierna Plus', 'Sesion avanzada para piernas y gluteos', 'HIPERTROFIA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 8 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Pierna Plus');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Movilidad Matutina', 'Rutina breve para activar el cuerpo', 'MANTENIMIENTO', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 7 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Movilidad Matutina');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Core Avanzado', 'Bloque exigente para abdomen y estabilidad', 'FUERZA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 6 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Core Avanzado');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Cardio Intermedio', 'Rutina intermedia para cardio sostenido', 'MANTENIMIENTO', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 5 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Cardio Intermedio');

INSERT INTO rutinas (nombre, descripcion, objetivo, tipo_rutina, activa, fecha_creacion)
SELECT 'Full Body Avanzada', 'Sesion avanzada de cuerpo completo', 'FUERZA', 'GENERAL', true,
     DATE_SUB(NOW(), INTERVAL 4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre = 'Full Body Avanzada');

-- Ejercicios y rutina_ejercicios
INSERT INTO ejercicios (nombre, grupo_muscular, descripcion, nivel_dificultad, activo)
SELECT 'Sentadilla', 'Piernas', 'Ejercicio base para fuerza y estabilidad', 'INTERMEDIO', true
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre = 'Sentadilla');

INSERT INTO ejercicios (nombre, grupo_muscular, descripcion, nivel_dificultad, activo)
SELECT 'Press de banca', 'Pecho', 'Trabajo principal de empuje de torso', 'INTERMEDIO', true
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre = 'Press de banca');

INSERT INTO ejercicios (nombre, grupo_muscular, descripcion, nivel_dificultad, activo)
SELECT 'Peso muerto', 'Espalda', 'Ejercicio compuesto para cadena posterior', 'AVANZADO', true
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre = 'Peso muerto');

INSERT INTO ejercicios (nombre, grupo_muscular, descripcion, nivel_dificultad, activo)
SELECT 'Burpees', 'Cardio', 'Ejercicio metabolico de alta intensidad', 'INTERMEDIO', true
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre = 'Burpees');

INSERT INTO ejercicios (nombre, grupo_muscular, descripcion, nivel_dificultad, activo)
SELECT 'Plancha', 'Core', 'Trabajo isometrico de abdomen y estabilizacion', 'PRINCIPIANTE', true
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre = 'Plancha');

INSERT INTO ejercicios (nombre, grupo_muscular, descripcion, nivel_dificultad, activo)
SELECT 'Remo con barra', 'Espalda', 'Movimiento de traccion horizontal', 'INTERMEDIO', true
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre = 'Remo con barra');

INSERT INTO rutina_ejercicios (rutina_id, ejercicio_id, series, repeticiones, descanso_segundos, orden)
SELECT (SELECT id FROM rutinas WHERE nombre = 'Full Body Principiante' ORDER BY id ASC LIMIT 1), (SELECT id FROM ejercicios WHERE nombre = 'Sentadilla' ORDER BY id ASC LIMIT 1), 4, 10, 90, 1
WHERE NOT EXISTS (
    SELECT 1 FROM rutina_ejercicios
    WHERE rutina_id = (SELECT id FROM rutinas WHERE nombre = 'Full Body Principiante' ORDER BY id ASC LIMIT 1)
    AND ejercicio_id = (SELECT id FROM ejercicios WHERE nombre = 'Sentadilla' ORDER BY id ASC LIMIT 1)
);

INSERT INTO rutina_ejercicios (rutina_id, ejercicio_id, series, repeticiones, descanso_segundos, orden)
SELECT (SELECT id FROM rutinas WHERE nombre = 'Full Body Principiante' ORDER BY id ASC LIMIT 1), (SELECT id FROM ejercicios WHERE nombre = 'Press de banca' ORDER BY id ASC LIMIT 1), 4, 8, 120, 2
WHERE NOT EXISTS (
    SELECT 1 FROM rutina_ejercicios
    WHERE rutina_id = (SELECT id FROM rutinas WHERE nombre = 'Full Body Principiante' ORDER BY id ASC LIMIT 1)
    AND ejercicio_id = (SELECT id FROM ejercicios WHERE nombre = 'Press de banca' ORDER BY id ASC LIMIT 1)
);

INSERT INTO rutina_ejercicios (rutina_id, ejercicio_id, series, repeticiones, descanso_segundos, orden)
SELECT (SELECT id FROM rutinas WHERE nombre = 'Plan Carlos Premium' ORDER BY id ASC LIMIT 1), (SELECT id FROM ejercicios WHERE nombre = 'Peso muerto' ORDER BY id ASC LIMIT 1), 5, 5, 150, 1
WHERE NOT EXISTS (
    SELECT 1 FROM rutina_ejercicios
    WHERE rutina_id = (SELECT id FROM rutinas WHERE nombre = 'Plan Carlos Premium' ORDER BY id ASC LIMIT 1)
    AND ejercicio_id = (SELECT id FROM ejercicios WHERE nombre = 'Peso muerto' ORDER BY id ASC LIMIT 1)
);

INSERT INTO rutina_ejercicios (rutina_id, ejercicio_id, series, repeticiones, descanso_segundos, orden)
SELECT (SELECT id FROM rutinas WHERE nombre = 'Plan Carlos Premium' ORDER BY id ASC LIMIT 1), (SELECT id FROM ejercicios WHERE nombre = 'Remo con barra' ORDER BY id ASC LIMIT 1), 4, 10, 90, 2
WHERE NOT EXISTS (
    SELECT 1 FROM rutina_ejercicios
    WHERE rutina_id = (SELECT id FROM rutinas WHERE nombre = 'Plan Carlos Premium' ORDER BY id ASC LIMIT 1)
    AND ejercicio_id = (SELECT id FROM ejercicios WHERE nombre = 'Remo con barra' ORDER BY id ASC LIMIT 1)
);

INSERT INTO rutina_ejercicios (rutina_id, ejercicio_id, series, repeticiones, descanso_segundos, orden)
SELECT (SELECT id FROM rutinas WHERE nombre = 'HIIT Cardio' ORDER BY id ASC LIMIT 1), (SELECT id FROM ejercicios WHERE nombre = 'Burpees' ORDER BY id ASC LIMIT 1), 6, 15, 45, 1
WHERE NOT EXISTS (
    SELECT 1 FROM rutina_ejercicios
    WHERE rutina_id = (SELECT id FROM rutinas WHERE nombre = 'HIIT Cardio' ORDER BY id ASC LIMIT 1)
    AND ejercicio_id = (SELECT id FROM ejercicios WHERE nombre = 'Burpees' ORDER BY id ASC LIMIT 1)
);

INSERT INTO rutina_ejercicios (rutina_id, ejercicio_id, series, repeticiones, descanso_segundos, orden)
SELECT (SELECT id FROM rutinas WHERE nombre = 'HIIT Cardio' ORDER BY id ASC LIMIT 1), (SELECT id FROM ejercicios WHERE nombre = 'Plancha' ORDER BY id ASC LIMIT 1), 4, 45, 30, 2
WHERE NOT EXISTS (
    SELECT 1 FROM rutina_ejercicios
    WHERE rutina_id = (SELECT id FROM rutinas WHERE nombre = 'HIIT Cardio' ORDER BY id ASC LIMIT 1)
    AND ejercicio_id = (SELECT id FROM ejercicios WHERE nombre = 'Plancha' ORDER BY id ASC LIMIT 1)
);

-- Materiales
INSERT INTO materiales (nombre, descripcion, activo)
SELECT 'Mancuernas ajustables', 'Set de mancuernas para trabajo de fuerza y accesorio', true
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre = 'Mancuernas ajustables');

INSERT INTO materiales (nombre, descripcion, activo)
SELECT 'Barras olimpicas', 'Barras para levantamientos y movimientos compuestos', true
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre = 'Barras olimpicas');

INSERT INTO materiales (nombre, descripcion, activo)
SELECT 'Kettlebells', 'Piezas de peso libre para potencia y acondicionamiento', true
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre = 'Kettlebells');

INSERT INTO materiales (nombre, descripcion, activo)
SELECT 'Bandas elasticas', 'Bandas de resistencia para movilidad y apoyo', true
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre = 'Bandas elasticas');

-- Staff para modulo /staff
INSERT INTO staff (nombre, apellidos, email, telefono, cargo, activo)
SELECT 'Antonio', 'Moreno Ibanez', 'staff.antonio@demo.com', '600111111', 'Recepcion', true
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'staff.antonio@demo.com');

INSERT INTO staff (nombre, apellidos, email, telefono, cargo, activo)
SELECT 'Claudia', 'Campos Torres', 'staff.claudia@demo.com', '600222222', 'Entrenadora', true
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'staff.claudia@demo.com');

INSERT INTO staff (nombre, apellidos, email, telefono, cargo, activo)
SELECT 'Elena', 'Rivas Serrano', 'staff.elena@demo.com', '600333333', 'Fisoterapeuta', true
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'staff.elena@demo.com');

-- Maquinas para modulo /maquinas
INSERT INTO maquinas (nombre, descripcion, estado, activo)
SELECT 'Cinta de correr X1', 'Cinta principal de cardio de uso continuo', 'ACTIVA', true
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre = 'Cinta de correr X1');

INSERT INTO maquinas (nombre, descripcion, estado, activo)
SELECT 'Bicicleta estatica B-Pro', 'Bicicleta de alta resistencia para spinning libre', 'ACTIVA', true
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre = 'Bicicleta estatica B-Pro');

INSERT INTO maquinas (nombre, descripcion, estado, activo)
SELECT 'Polea dual D500', 'Maquina de poleas para trabajo de espalda y brazos', 'MANTENIMIENTO', true
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre = 'Polea dual D500');

INSERT INTO maquinas (nombre, descripcion, estado, activo)
SELECT 'Prensa de piernas P900', 'Maquina de fuerza para tren inferior', 'ACTIVA', true
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre = 'Prensa de piernas P900');

-- Gastos para modulo /gastos
INSERT INTO gastos (concepto, descripcion, monto, fecha_gasto, estado, activo, staff_id, maquina_id, material_id)
SELECT
    'Mantenimiento cinta principal',
    'Cambio de banda y ajuste de motor en cinta principal',
    185.00,
    DATE_SUB(CURDATE(), INTERVAL 6 DAY),
    'APROBADO',
    true,
    (SELECT id FROM staff WHERE email = 'staff.antonio@demo.com' ORDER BY id ASC LIMIT 1),
    (SELECT id FROM maquinas WHERE nombre = 'Cinta de correr X1' ORDER BY id ASC LIMIT 1),
    NULL
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto = 'Mantenimiento cinta principal');

INSERT INTO gastos (concepto, descripcion, monto, fecha_gasto, estado, activo, staff_id, maquina_id, material_id)
SELECT
    'Reposicion bandas elasticas',
    'Compra de nuevo set de bandas para clases grupales',
    76.40,
    DATE_SUB(CURDATE(), INTERVAL 4 DAY),
    'APROBADO',
    true,
    (SELECT id FROM staff WHERE email = 'staff.claudia@demo.com' ORDER BY id ASC LIMIT 1),
    NULL,
    (SELECT id FROM materiales WHERE nombre = 'Bandas elasticas' ORDER BY id ASC LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto = 'Reposicion bandas elasticas');

INSERT INTO gastos (concepto, descripcion, monto, fecha_gasto, estado, activo, staff_id, maquina_id, material_id)
SELECT
    'Lubricacion polea dual',
    'Servicio preventivo de lubricacion y tension de cables',
    49.99,
    DATE_SUB(CURDATE(), INTERVAL 2 DAY),
    'REGISTRADO',
    true,
    (SELECT id FROM staff WHERE email = 'staff.elena@demo.com' ORDER BY id ASC LIMIT 1),
    (SELECT id FROM maquinas WHERE nombre = 'Polea dual D500' ORDER BY id ASC LIMIT 1),
    NULL
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto = 'Lubricacion polea dual');
