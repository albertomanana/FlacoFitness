-- Data seed script for FlacoFitness
-- Created: 2026-04-28
-- Purpose: Populate database with essential demo data

SET FOREIGN_KEY_CHECKS=0;

-- Insert roles
INSERT INTO roles (nombre) VALUES 
  ('CLIENTE'),
  ('ADMIN'),
  ('STAFF'),
  ('RECEPCION'),
  ('ENTRENADOR'),
  ('GERENTE');

-- Insert planes
INSERT INTO planes (nombre, descripcion, precio_mensual, duracion_dias, activo, tipo_membresia, beneficios) VALUES
  ('Plan Básico', 'Acceso básico al centro', 29.00, 30, 1, 'MENSUAL', 'Acceso ilimitado al gimnasio, horario 6am-10pm'),
  ('Plan Premium', 'Acceso completo + entrenamientos personalizados', 59.00, 30, 1, 'PREMIUM', 'Acceso 24/7, 2 sesiones de entrenador personal/mes'),
  ('Plan Trimestral', 'Acceso trimestral con descuento', 75.00, 90, 1, 'TRIMESTRAL', 'Acceso ilimitado, 3 meses por 75€'),
  ('Plan Estudiante', 'Descuento especial para estudiantes', 19.00, 30, 1, 'ESTUDIANTE', 'Acceso básico con carnet estudiante válido'),
  ('Plan Prueba', 'Prueba gratuita 7 días', 0.00, 7, 1, 'PRUEBA', 'Acceso completo durante 7 días');

-- Insert admin user (password: admin123)
INSERT INTO usuarios (nombre, apellidos, email, username, password_hash, activo, rol_id, debe_cambiar_password) VALUES
  ('Administrador', 'Sistema', 'admin@flacofitness.com', 'admin', '$2a$10$gfgQEEyWSTaOZg/3OIp8TevQA3YeYrPfB8urvDNnUO3BkStUrWBEe', 1, 2, 0);

-- Insert sample clients
INSERT INTO usuarios (nombre, apellidos, email, username, password_hash, activo, rol_id, plan_id, fecha_proximo_pago) VALUES
  ('Juan', 'Pérez García', 'juan.perez@example.com', 'juan.perez', '$2a$10$gfgQEEyWSTaOZg/3OIp8TevQA3YeYrPfB8urvDNnUO3BkStUrWBEe', 1, 1, 1, DATE_ADD(NOW(), INTERVAL 30 DAY)),
  ('María', 'López Martínez', 'maria.lopez@example.com', 'maria.lopez', '$2a$10$gfgQEEyWSTaOZg/3OIp8TevQA3YeYrPfB8urvDNnUO3BkStUrWBEe', 1, 1, 2, DATE_ADD(NOW(), INTERVAL 30 DAY)),
  ('Carlos', 'Rodríguez Sánchez', 'carlos.rodriguez@example.com', 'carlos.rodriguez', '$2a$10$gfgQEEyWSTaOZg/3OIp8TevQA3YeYrPfB8urvDNnUO3BkStUrWBEe', 1, 1, 1, DATE_ADD(NOW(), INTERVAL 30 DAY));

-- Insert staff
INSERT INTO usuarios (nombre, apellidos, email, username, password_hash, activo, rol_id) VALUES
  ('Ana', 'García Fernández', 'ana.garcia@flacofitness.com', 'ana.garcia', '$2a$10$gfgQEEyWSTaOZg/3OIp8TevQA3YeYrPfB8urvDNnUO3BkStUrWBEe', 1, 5),
  ('Luis', 'Martínez López', 'luis.martinez@flacofitness.com', 'luis.martinez', '$2a$10$gfgQEEyWSTaOZg/3OIp8TevQA3YeYrPfB8urvDNnUO3BkStUrWBEe', 1, 4);

SET FOREIGN_KEY_CHECKS=1;
