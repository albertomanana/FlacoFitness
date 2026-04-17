-- FlacoFitness — data.sql (script de datos demo completo, sintaxis MySQL pura)
-- Ejecutar DESPUÉS de init.sql o sobre la BD ya inicializada por Hibernate.
-- Compatible: MySQL 8+ / MariaDB. Sin funciones H2.
-- Uso: mysql -u root flacofitness < docs/data.sql

SET FOREIGN_KEY_CHECKS = 0;

-- ─── ROLES ──────────────────────────────────────────────────────────────────
INSERT INTO roles (nombre) VALUES ('STAFF')   ON DUPLICATE KEY UPDATE nombre=nombre;
INSERT INTO roles (nombre) VALUES ('CLIENTE')  ON DUPLICATE KEY UPDATE nombre=nombre;

-- ─── PLANES ──────────────────────────────────────────────────────────────────
INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Basico','Plan mensual base para acceso general','MENSUAL','Acceso general, registro de asistencias y rutinas base.',29.90,30,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Basico');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Premium','Plan mensual completo con mayor cobertura de servicios','PREMIUM','Rutinas personalizadas, prioridad en clases y seguimiento ampliado.',49.90,30,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Premium');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Trimestral','Plan trimestral con descuento incluido','TRIMESTRAL','3 meses de acceso completo con descuento especial.',79.90,90,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Trimestral');

INSERT INTO planes (nombre, descripcion, tipo_membresia, beneficios, precio_mensual, duracion_dias, activo)
SELECT 'Estudiante','Plan especial para estudiantes universitarios','ESTUDIANTE','Acceso completo con descuento academico verificable.',19.90,30,1
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE nombre='Estudiante');

-- ─── USUARIOS (80 usuarios: 3 staff + 77 clientes) ──────────────────────────
-- Los primeros 3 son del staff vinculados a staff_perfiles
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id)
SELECT 'Antonio','Moreno Ibanez','antonio@demo.com','11111111A','600111111',1,DATE_SUB(NOW(),INTERVAL 24 MONTH),(SELECT id FROM roles WHERE nombre='STAFF'),NULL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='antonio@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id)
SELECT 'Claudia','Campos Torres','claudia@demo.com','22222222B','600222222',1,DATE_SUB(NOW(),INTERVAL 24 MONTH),(SELECT id FROM roles WHERE nombre='STAFF'),NULL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='claudia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id)
SELECT 'Elena','Rivas Serrano','elena@demo.com','33333333C','600333333',1,DATE_SUB(NOW(),INTERVAL 18 MONTH),(SELECT id FROM roles WHERE nombre='STAFF'),NULL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='elena@demo.com');
-- Clientes 1-77
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Carlos','Martinez','carlos@demo.com','12345678A','600100001',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 15 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='carlos@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Ana','Garcia','ana@demo.com','23456789B','600100002',1,DATE_SUB(NOW(),INTERVAL 5 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 8 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='ana@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'David','Lopez','david@demo.com','34567890C','600100003',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 22 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='david@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Maria','Sanchez','maria@demo.com','45678901D','600100004',1,DATE_SUB(NOW(),INTERVAL 2 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 30 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='maria@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Jorge','Perez','jorge@demo.com','56789012E','600100005',0,DATE_SUB(NOW(),INTERVAL 1 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='jorge@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Laura','Fernandez Ruiz','laura@demo.com','61111111F','600100006',1,DATE_SUB(NOW(),INTERVAL 8 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 5 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='laura@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Miguel','Ortega Vega','miguel@demo.com','62222222G','600100007',1,DATE_SUB(NOW(),INTERVAL 7 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 12 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='miguel@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Sofia','Blanco Molina','sofia@demo.com','63333333H','600100008',1,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 45 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='sofia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Pablo','Jimenez Castro','pablo@demo.com','64444444I','600100009',1,DATE_SUB(NOW(),INTERVAL 10 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 3 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='pablo@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Isabel','Torres Bravo','isabel@demo.com','65555555J','600100010',1,DATE_SUB(NOW(),INTERVAL 9 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 20 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='isabel@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Alejandro','Muñoz Pardo','alejandro@demo.com','66666666K','600100011',1,DATE_SUB(NOW(),INTERVAL 11 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),DATE_ADD(CURDATE(),INTERVAL 18 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='alejandro@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Carmen','Herrera Luna','carmen@demo.com','67777777L','600100012',1,DATE_SUB(NOW(),INTERVAL 5 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 9 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='carmen@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Roberto','Gil Serrano','roberto@demo.com','68888888M','600100013',0,DATE_SUB(NOW(),INTERVAL 13 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='roberto@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Pilar','Navarro Cruz','pilar@demo.com','69999999N','600100014',1,DATE_SUB(NOW(),INTERVAL 2 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 27 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='pilar@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Fernando','Llorente Peña','fernando@demo.com','70000000O','600100015',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 60 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='fernando@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Rosa','Mendez Alonso','rosa@demo.com','71111111P','600100016',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 4 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='rosa@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Javier','Santos Reyes','javier@demo.com','72222222Q','600100017',1,DATE_SUB(NOW(),INTERVAL 7 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 16 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='javier@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Nuria','Vidal Fuente','nuria@demo.com','73333333R','600100018',1,DATE_SUB(NOW(),INTERVAL 1 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),DATE_ADD(CURDATE(),INTERVAL 25 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='nuria@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Andres','Romero Mora','andres@demo.com','74444444S','600100019',1,DATE_SUB(NOW(),INTERVAL 8 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 6 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='andres@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Lucia','Aguilar Vera','lucia@demo.com','75555555T','600100020',0,DATE_SUB(NOW(),INTERVAL 15 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='lucia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Victor','Cano Pinto','victor@demo.com','76666666U','600100021',1,DATE_SUB(NOW(),INTERVAL 9 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 11 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='victor@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Marta','Iglesias Ramos','marta@demo.com','77777777V','600100022',1,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 55 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='marta@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Hugo','Rubio Sanz','hugo@demo.com','78888888W','600100023',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 2 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='hugo@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Cristina','Pascual Moya','cristina@demo.com','79999999X','600100024',1,DATE_SUB(NOW(),INTERVAL 12 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 28 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='cristina@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Oscar','Leon Marin','oscar@demo.com','80000000Y','600100025',0,DATE_SUB(NOW(),INTERVAL 2 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='oscar@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Patricia','Flores Diaz','patricia@demo.com','81111111Z','600100026',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 19 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='patricia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Raul','Medina Guerrero','raul@demo.com','82222222A','600100027',1,DATE_SUB(NOW(),INTERVAL 10 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 7 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='raul@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Silvia','Nieto Cabello','silvia@demo.com','83333333B','600100028',1,DATE_SUB(NOW(),INTERVAL 5 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 23 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='silvia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Tomas','Vargas Espejo','tomas@demo.com','84444444C','600100029',1,DATE_SUB(NOW(),INTERVAL 7 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 40 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='tomas@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Yolanda','Soler Vega','yolanda@demo.com','85555555D','600100030',1,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 14 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='yolanda@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Alberto','Crespo Toro','alb.c@demo.com','86666666E','600100031',1,DATE_SUB(NOW(),INTERVAL 8 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 10 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='alb.c@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Beatriz','Prieto Haro','beatriz@demo.com','87777777F','600100032',0,DATE_SUB(NOW(),INTERVAL 16 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='beatriz@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Diego','Fuentes Campo','diego@demo.com','88888888G','600100033',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),DATE_ADD(CURDATE(),INTERVAL 17 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='diego@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Eva','Molina Roca','eva@demo.com','89999999H','600100034',1,DATE_SUB(NOW(),INTERVAL 11 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 1 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='eva@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Francisco','Lara Blanco','francisco@demo.com','90000000I','600100035',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 21 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='francisco@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Gloria','Pena Marcos','gloria@demo.com','91111111J','600100036',1,DATE_SUB(NOW(),INTERVAL 2 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 29 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='gloria@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Hector','Delgado Font','hector@demo.com','92222222K','600100037',1,DATE_SUB(NOW(),INTERVAL 9 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 50 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='hector@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Irene','Castillo Rojo','irene@demo.com','93333333L','600100038',1,DATE_SUB(NOW(),INTERVAL 5 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 13 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='irene@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Julian','Soto Peral','julian@demo.com','94444444M','600100039',0,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='julian@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Katia','Ruiz Abad','katia@demo.com','95555555N','600100040',1,DATE_SUB(NOW(),INTERVAL 7 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 8 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='katia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Luis','Campos Vidal','luis.cv@demo.com','96666666O','600100041',1,DATE_SUB(NOW(),INTERVAL 10 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 24 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='luis.cv@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Monica','Gallego Rios','monica@demo.com','97777777P','600100042',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 26 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='monica@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Nicolas','Bravo Mena','nicolas@demo.com','98888888Q','600100043',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 15 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='nicolas@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Olga','Vera Camacho','olga@demo.com','99999999R','600100044',0,DATE_SUB(NOW(),INTERVAL 14 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='olga@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Pedro','Cabrera Nunez','pedro@demo.com','10000001S','600100045',1,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 75 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='pedro@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Quintina','Hidalgo Parra','quintina@demo.com','10000002T','600100046',1,DATE_SUB(NOW(),INTERVAL 2 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 16 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='quintina@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Ricardo','Exposito Mora','ricardo@demo.com','10000003U','600100047',1,DATE_SUB(NOW(),INTERVAL 8 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 4 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='ricardo@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Sara','Montero Ayala','sara@demo.com','10000004V','600100048',1,DATE_SUB(NOW(),INTERVAL 5 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),DATE_ADD(CURDATE(),INTERVAL 22 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='sara@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Teresa','Iborra Falcon','teresa@demo.com','10000005W','600100049',1,DATE_SUB(NOW(),INTERVAL 11 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 9 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='teresa@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Ulises','Barbero Mata','ulises@demo.com','10000006X','600100050',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 31 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='ulises@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Vanesa','Carrasco Tena','vanesa@demo.com','10000007Y','600100051',0,DATE_SUB(NOW(),INTERVAL 18 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='vanesa@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Walter','Serrano Leal','walter@demo.com','10000008Z','600100052',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 5 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='walter@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Ximena','Mora Aguilera','ximena@demo.com','10000009A','600100053',1,DATE_SUB(NOW(),INTERVAL 7 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 12 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='ximena@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Yago','Bernal Acosta','yago@demo.com','10000010B','600100054',1,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 65 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='yago@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Zoe','Palomares Ruiz','zoe@demo.com','10000011C','600100055',1,DATE_SUB(NOW(),INTERVAL 1 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 29 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='zoe@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Adrian','Gallardo Moya','adrian.g@demo.com','10000012D','600100056',1,DATE_SUB(NOW(),INTERVAL 9 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 3 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='adrian.g@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Belen','Cano Zurita','belen@demo.com','10000013E','600100057',1,DATE_SUB(NOW(),INTERVAL 5 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),DATE_ADD(CURDATE(),INTERVAL 20 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='belen@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Cesar','Ponce Mateo','cesar@demo.com','10000014F','600100058',0,DATE_SUB(NOW(),INTERVAL 20 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='cesar@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Diana','Saiz Rivero','diana@demo.com','10000015G','600100059',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 6 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='diana@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Emilio','Vera Lozano','emilio@demo.com','10000016H','600100060',1,DATE_SUB(NOW(),INTERVAL 10 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 14 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='emilio@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Fatima','Exposito Gil','fatima@demo.com','10000017I','600100061',1,DATE_SUB(NOW(),INTERVAL 2 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 28 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='fatima@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Gonzalo','Ibañez Vela','gonzalo@demo.com','10000018J','600100062',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 11 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='gonzalo@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Hortensia','Medrano Cruz','hortensia@demo.com','10000019K','600100063',1,DATE_SUB(NOW(),INTERVAL 8 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 48 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='hortensia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Ivan','Pascual Moya','ivan@demo.com','10000020L','600100064',0,DATE_SUB(NOW(),INTERVAL 17 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='ivan@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Julia','Reyes Mora','julia@demo.com','10000021M','600100065',1,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 18 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='julia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Kevin','Santos Rubio','kevin@demo.com','10000022N','600100066',1,DATE_SUB(NOW(),INTERVAL 7 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 7 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='kevin@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Leyre','Fuentes Baron','leyre@demo.com','10000023O','600100067',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Estudiante'),DATE_ADD(CURDATE(),INTERVAL 21 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='leyre@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Manuel','Duran Arenas','manuel@demo.com','10000024P','600100068',1,DATE_SUB(NOW(),INTERVAL 12 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 2 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='manuel@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Natalia','Guerrero Polo','natalia@demo.com','10000025Q','600100069',1,DATE_SUB(NOW(),INTERVAL 5 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 26 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='natalia@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Ovidio','Parra Leal','ovidio@demo.com','10000026R','600100070',0,DATE_SUB(NOW(),INTERVAL 22 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),NULL WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='ovidio@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Paloma','Salas Cano','paloma@demo.com','10000027S','600100071',1,DATE_SUB(NOW(),INTERVAL 1 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 30 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='paloma@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Quique','Moya Heredia','quique@demo.com','10000028T','600100072',1,DATE_SUB(NOW(),INTERVAL 9 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 13 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='quique@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Rebeca','Ureña Vidal','rebeca@demo.com','10000029U','600100073',1,DATE_SUB(NOW(),INTERVAL 6 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 19 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='rebeca@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Sergio','Pinto Moya','sergio@demo.com','10000030V','600100074',1,DATE_SUB(NOW(),INTERVAL 3 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Trimestral'),DATE_ADD(CURDATE(),INTERVAL 70 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='sergio@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Tamara','Vega Solis','tamara@demo.com','10000031W','600100075',1,DATE_SUB(NOW(),INTERVAL 4 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 23 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='tamara@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Unai','Ceballos Ramos','unai@demo.com','10000032X','600100076',1,DATE_SUB(NOW(),INTERVAL 8 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Premium'),DATE_ADD(CURDATE(),INTERVAL 8 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='unai@demo.com');
INSERT INTO usuarios (nombre,apellidos,email,dni,telefono,activo,fecha_registro,rol_id,plan_id,fecha_proximo_pago)
SELECT 'Vera','Montoya Bravo','vera@demo.com','10000033Y','600100077',1,DATE_SUB(NOW(),INTERVAL 2 MONTH),(SELECT id FROM roles WHERE nombre='CLIENTE'),(SELECT id FROM planes WHERE nombre='Basico'),DATE_ADD(CURDATE(),INTERVAL 27 DAY) WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email='vera@demo.com');

-- ─── STAFF (tabla legacy) ───────────────────────────────────────────────────
INSERT INTO staff (nombre,apellidos,email,telefono,cargo,activo)
SELECT 'Antonio','Moreno Ibanez','staff.antonio@demo.com','600111111','Recepcion',1
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email='staff.antonio@demo.com');
INSERT INTO staff (nombre,apellidos,email,telefono,cargo,activo)
SELECT 'Claudia','Campos Torres','staff.claudia@demo.com','600222222','Entrenadora',1
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email='staff.claudia@demo.com');
INSERT INTO staff (nombre,apellidos,email,telefono,cargo,activo)
SELECT 'Elena','Rivas Serrano','staff.elena@demo.com','600333333','Fisioterapeuta',1
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email='staff.elena@demo.com');

-- ─── STAFF PERFILES (vinculados a usuarios del sistema) ─────────────────────
INSERT INTO staff_perfiles (usuario_id, rol_staff, especialidad, activo, fecha_alta)
SELECT (SELECT id FROM usuarios WHERE email='antonio@demo.com'),'RECEPCION','Atencion al cliente y gestión de accesos',1,DATE_SUB(CURDATE(),INTERVAL 24 MONTH)
WHERE NOT EXISTS (SELECT 1 FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='antonio@demo.com'));
INSERT INTO staff_perfiles (usuario_id, rol_staff, especialidad, activo, fecha_alta)
SELECT (SELECT id FROM usuarios WHERE email='claudia@demo.com'),'ENTRENADOR','Fuerza, hipertrofia y entrenamiento funcional',1,DATE_SUB(CURDATE(),INTERVAL 24 MONTH)
WHERE NOT EXISTS (SELECT 1 FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com'));
INSERT INTO staff_perfiles (usuario_id, rol_staff, especialidad, activo, fecha_alta)
SELECT (SELECT id FROM usuarios WHERE email='elena@demo.com'),'GERENTE','Gestion economica y coordinacion de instalaciones',1,DATE_SUB(CURDATE(),INTERVAL 18 MONTH)
WHERE NOT EXISTS (SELECT 1 FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='elena@demo.com'));

-- ─── EJERCICIOS ─────────────────────────────────────────────────────────────
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Sentadilla','Piernas','Ejercicio base para fuerza y estabilidad','INTERMEDIO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Sentadilla');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Press de banca','Pecho','Trabajo principal de empuje de torso','INTERMEDIO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Press de banca');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Peso muerto','Espalda','Ejercicio compuesto para cadena posterior','AVANZADO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Peso muerto');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Burpees','Cardio','Ejercicio metabolico de alta intensidad','INTERMEDIO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Burpees');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Plancha','Core','Trabajo isometrico de abdomen y estabilizacion','PRINCIPIANTE',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Plancha');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Remo con barra','Espalda','Movimiento de traccion horizontal','INTERMEDIO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Remo con barra');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Zancadas','Piernas','Trabajo de cuadriceps y gluteos con desplazamiento','PRINCIPIANTE',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Zancadas');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Dominadas','Espalda','Traccion vertical con peso corporal','AVANZADO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Dominadas');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Press militar','Hombros','Empuje vertical para deltoides','INTERMEDIO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Press militar');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Curl de biceps','Brazos','Aislamiento de biceps con mancuernas','PRINCIPIANTE',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Curl de biceps');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Triceps en polea','Brazos','Extension de triceps en maquina de cable','PRINCIPIANTE',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Triceps en polea');
INSERT INTO ejercicios (nombre,grupo_muscular,descripcion,nivel_dificultad,activo)
SELECT 'Hip thrust','Gluteos','Empuje de cadera para gluteo maximo','INTERMEDIO',1
WHERE NOT EXISTS (SELECT 1 FROM ejercicios WHERE nombre='Hip thrust');

-- ─── CLASES ─────────────────────────────────────────────────────────────────
INSERT INTO clases (nombre,descripcion,capacidad_sugerida,activa)
SELECT 'Cycling','Clase de bicicleta estatica de alta intensidad',20,1
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Cycling');
INSERT INTO clases (nombre,descripcion,capacidad_sugerida,activa)
SELECT 'Yoga','Sesion de yoga para flexibilidad y equilibrio mental',15,1
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Yoga');
INSERT INTO clases (nombre,descripcion,capacidad_sugerida,activa)
SELECT 'Box Fit','Entrenamiento de boxeo adaptado sin contacto',12,1
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Box Fit');
INSERT INTO clases (nombre,descripcion,capacidad_sugerida,activa)
SELECT 'HIIT Grupal','Entrenamiento interválico de alta intensidad en grupo',18,1
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='HIIT Grupal');
INSERT INTO clases (nombre,descripcion,capacidad_sugerida,activa)
SELECT 'Pilates','Trabajo de core, postura y movilidad',12,1
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Pilates');
INSERT INTO clases (nombre,descripcion,capacidad_sugerida,activa)
SELECT 'Functional Training','Circuito funcional con material libre',16,1
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Functional Training');


-- ─── RUTINAS ────────────────────────────────────────────────────────────────
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Full Body Principiante','Rutina de cuerpo completo para usuarios que inician','GENERAL','MANTENIMIENTO',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Full Body Principiante');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'HIIT Cardio','Entrenamiento intervalico de alta intensidad para quemar grasa','GENERAL','PERDIDA_GRASA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='HIIT Cardio');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Fuerza Funcional','Circuito de fuerza con movimientos compuestos','GENERAL','FUERZA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Fuerza Funcional');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Hipertrofia 5x5','Programa de hipertrofia con series de 5 repeticiones','GENERAL','HIPERTROFIA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Hipertrofia 5x5');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Cardio Salud','Rutina de cardio suave para salud cardiovascular','GENERAL','MANTENIMIENTO',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Cardio Salud');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Movilidad y Core','Trabajo de movilidad articular y estabilizacion central','GENERAL','MANTENIMIENTO',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Movilidad y Core');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Tren Superior','Enfasis en pecho, espalda y brazos','GENERAL','HIPERTROFIA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Tren Superior');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Tren Inferior','Enfasis en cuadriceps, gluteos e isquiotibiales','GENERAL','HIPERTROFIA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Tren Inferior');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Plan Carlos Premium','Rutina personalizada para Carlos (intensidad alta)','PERSONALIZADA','FUERZA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Plan Carlos Premium');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Plan Ana Cardio','Rutina personalizada cardio-funcional para Ana','PERSONALIZADA','PERDIDA_GRASA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Plan Ana Cardio');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Recuperacion Activa','Rutina suave post-entrenamiento o lesion','GENERAL','MANTENIMIENTO',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Recuperacion Activa');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Circuito Energico','Circuito de alta activacion metabolica','GENERAL','PERDIDA_GRASA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Circuito Energico');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Gluteos y Core','Especifico de gluteo medio, maximo y transverso abdominal','GENERAL','HIPERTROFIA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Gluteos y Core');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Espalda Fuerte','Trabajo de espalda completa con remos y dominadas','GENERAL','FUERZA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Espalda Fuerte');
INSERT INTO rutinas (nombre,descripcion,tipo_rutina,objetivo,activa)
SELECT 'Pecho y Brazos','Sesion enfocada en empuje horizontal y aislamiento de brazos','GENERAL','HIPERTROFIA',1
WHERE NOT EXISTS (SELECT 1 FROM rutinas WHERE nombre='Pecho y Brazos');

-- ─── RUTINA_EJERCICIOS ───────────────────────────────────────────────────────
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Full Body Principiante'),(SELECT id FROM ejercicios WHERE nombre='Sentadilla'),3,12,90,1
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Full Body Principiante') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Sentadilla'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Full Body Principiante'),(SELECT id FROM ejercicios WHERE nombre='Press de banca'),3,10,90,2
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Full Body Principiante') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Press de banca'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Full Body Principiante'),(SELECT id FROM ejercicios WHERE nombre='Plancha'),3,45,60,3
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Full Body Principiante') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Plancha'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='HIIT Cardio'),(SELECT id FROM ejercicios WHERE nombre='Burpees'),6,15,45,1
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='HIIT Cardio') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Burpees'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='HIIT Cardio'),(SELECT id FROM ejercicios WHERE nombre='Zancadas'),4,20,30,2
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='HIIT Cardio') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Zancadas'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Plan Carlos Premium'),(SELECT id FROM ejercicios WHERE nombre='Peso muerto'),5,5,150,1
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Plan Carlos Premium') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Peso muerto'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Plan Carlos Premium'),(SELECT id FROM ejercicios WHERE nombre='Dominadas'),5,6,120,2
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Plan Carlos Premium') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Dominadas'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Tren Superior'),(SELECT id FROM ejercicios WHERE nombre='Press de banca'),4,8,120,1
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Tren Superior') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Press de banca'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Tren Superior'),(SELECT id FROM ejercicios WHERE nombre='Remo con barra'),4,10,90,2
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Tren Superior') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Remo con barra'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Gluteos y Core'),(SELECT id FROM ejercicios WHERE nombre='Hip thrust'),4,12,90,1
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Gluteos y Core') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Hip thrust'));
INSERT INTO rutina_ejercicios (rutina_id,ejercicio_id,series,repeticiones,descanso_segundos,orden)
SELECT (SELECT id FROM rutinas WHERE nombre='Gluteos y Core'),(SELECT id FROM ejercicios WHERE nombre='Plancha'),3,60,60,2
WHERE NOT EXISTS (SELECT 1 FROM rutina_ejercicios WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Gluteos y Core') AND ejercicio_id=(SELECT id FROM ejercicios WHERE nombre='Plancha'));


-- ─── USUARIO_RUTINA (asignaciones) ──────────────────────────────────────────
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Plan Carlos Premium'),(SELECT id FROM usuarios WHERE email='carlos@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Plan Carlos Premium') AND usuario_id=(SELECT id FROM usuarios WHERE email='carlos@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Plan Ana Cardio'),(SELECT id FROM usuarios WHERE email='ana@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Plan Ana Cardio') AND usuario_id=(SELECT id FROM usuarios WHERE email='ana@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Full Body Principiante'),(SELECT id FROM usuarios WHERE email='david@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Full Body Principiante') AND usuario_id=(SELECT id FROM usuarios WHERE email='david@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Gluteos y Core'),(SELECT id FROM usuarios WHERE email='maria@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Gluteos y Core') AND usuario_id=(SELECT id FROM usuarios WHERE email='maria@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='HIIT Cardio'),(SELECT id FROM usuarios WHERE email='laura@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='HIIT Cardio') AND usuario_id=(SELECT id FROM usuarios WHERE email='laura@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Fuerza Funcional'),(SELECT id FROM usuarios WHERE email='miguel@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Fuerza Funcional') AND usuario_id=(SELECT id FROM usuarios WHERE email='miguel@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Tren Superior'),(SELECT id FROM usuarios WHERE email='pablo@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Tren Superior') AND usuario_id=(SELECT id FROM usuarios WHERE email='pablo@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Cardio Salud'),(SELECT id FROM usuarios WHERE email='isabel@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Cardio Salud') AND usuario_id=(SELECT id FROM usuarios WHERE email='isabel@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Movilidad y Core'),(SELECT id FROM usuarios WHERE email='sofia@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Movilidad y Core') AND usuario_id=(SELECT id FROM usuarios WHERE email='sofia@demo.com'));
INSERT INTO usuario_rutina (rutina_id,usuario_id)
SELECT (SELECT id FROM rutinas WHERE nombre='Hipertrofia 5x5'),(SELECT id FROM usuarios WHERE email='javier@demo.com')
WHERE NOT EXISTS (SELECT 1 FROM usuario_rutina WHERE rutina_id=(SELECT id FROM rutinas WHERE nombre='Hipertrofia 5x5') AND usuario_id=(SELECT id FROM usuarios WHERE email='javier@demo.com'));

-- ─── SESIONES_CLASE ──────────────────────────────────────────────────────────
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='Cycling'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_ADD(CURDATE(),INTERVAL 1 DAY),'09:00:00','10:00:00',20,'PROGRAMADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='Cycling');
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='HIIT Grupal'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_ADD(CURDATE(),INTERVAL 1 DAY),'18:00:00','18:45:00',18,'PROGRAMADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='HIIT Grupal');
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='Yoga'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_ADD(CURDATE(),INTERVAL 2 DAY),'10:00:00','11:00:00',15,'PROGRAMADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='Yoga');
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='Pilates'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_ADD(CURDATE(),INTERVAL 2 DAY),'19:00:00','20:00:00',12,'PROGRAMADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='Pilates');
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='Box Fit'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_ADD(CURDATE(),INTERVAL 3 DAY),'09:00:00','10:00:00',12,'PROGRAMADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='Box Fit');
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='Functional Training'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_ADD(CURDATE(),INTERVAL 3 DAY),'17:00:00','18:00:00',16,'PROGRAMADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='Functional Training');
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='Cycling'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_SUB(CURDATE(),INTERVAL 2 DAY),'09:00:00','10:00:00',20,'FINALIZADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='Cycling');
INSERT INTO sesiones_clase (clase_id,staff_responsable_id,fecha,hora_inicio,hora_fin,aforo,estado)
SELECT (SELECT id FROM clases WHERE nombre='HIIT Grupal'),(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='claudia@demo.com')),DATE_SUB(CURDATE(),INTERVAL 3 DAY),'18:00:00','18:45:00',18,'FINALIZADA'
WHERE EXISTS (SELECT 1 FROM clases WHERE nombre='HIIT Grupal');


-- ─── PAGOS ──────────────────────────────────────────────────────────────────
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-C1',49.90,'TARJETA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 2 MONTH),DATE_SUB(CURDATE(),INTERVAL 1 MONTH) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-C1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-C2',49.90,'TARJETA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 1 MONTH),CURDATE() WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-C2');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-C3',49.90,'TARJETA','PENDIENTE',NULL,DATE_ADD(CURDATE(),INTERVAL 15 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-C3');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='ana@demo.com'),(SELECT id FROM planes WHERE nombre='Basico'),'REF-A1',29.90,'TRANSFERENCIA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 2 MONTH),DATE_SUB(CURDATE(),INTERVAL 1 MONTH) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-A1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='ana@demo.com'),(SELECT id FROM planes WHERE nombre='Basico'),'REF-A2',29.90,'TRANSFERENCIA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 1 MONTH),CURDATE() WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-A2');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='ana@demo.com'),(SELECT id FROM planes WHERE nombre='Basico'),'REF-A3',29.90,'TRANSFERENCIA','PENDIENTE',NULL,DATE_ADD(CURDATE(),INTERVAL 15 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-A3');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='david@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-D1',49.90,'EFECTIVO','PAGADO',DATE_SUB(CURDATE(),INTERVAL 3 MONTH),DATE_SUB(CURDATE(),INTERVAL 2 MONTH) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-D1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='david@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-D2',49.90,'EFECTIVO','PAGADO',DATE_SUB(CURDATE(),INTERVAL 2 MONTH),DATE_SUB(CURDATE(),INTERVAL 1 MONTH) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-D2');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='maria@demo.com'),(SELECT id FROM planes WHERE nombre='Basico'),'REF-M1',29.90,'TARJETA','VENCIDO',DATE_SUB(CURDATE(),INTERVAL 45 DAY),DATE_SUB(CURDATE(),INTERVAL 15 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-M1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='laura@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-L1',49.90,'TARJETA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 1 MONTH),DATE_ADD(CURDATE(),INTERVAL 5 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-L1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='javier@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-J1',49.90,'EFECTIVO','PAGADO',DATE_SUB(CURDATE(),INTERVAL 1 MONTH),DATE_ADD(CURDATE(),INTERVAL 16 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-J1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='raul@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-R1',49.90,'TARJETA','PENDIENTE',NULL,DATE_ADD(CURDATE(),INTERVAL 7 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-R1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='pablo@demo.com'),(SELECT id FROM planes WHERE nombre='Premium'),'REF-P1',49.90,'TRANSFERENCIA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 27 DAY),DATE_ADD(CURDATE(),INTERVAL 3 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-P1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='sofia@demo.com'),(SELECT id FROM planes WHERE nombre='Trimestral'),'REF-S1',79.90,'TARJETA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 45 DAY),DATE_ADD(CURDATE(),INTERVAL 45 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-S1');
INSERT INTO pagos (usuario_id,plan_id,referencia,monto,metodo_pago,estado,fecha_pago,fecha_vencimiento)
SELECT (SELECT id FROM usuarios WHERE email='hector@demo.com'),(SELECT id FROM planes WHERE nombre='Trimestral'),'REF-H1',79.90,'TARJETA','PAGADO',DATE_SUB(CURDATE(),INTERVAL 40 DAY),DATE_ADD(CURDATE(),INTERVAL 50 DAY) WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE referencia='REF-H1');

-- ─── ASISTENCIAS ─────────────────────────────────────────────────────────────
INSERT INTO asistencias (usuario_id,fecha,hora_entrada,observaciones)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),CURDATE(),'07:45:00','Entrenamiento funcional' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='carlos@demo.com') AND fecha=CURDATE());
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),DATE_SUB(CURDATE(),INTERVAL 1 DAY),'09:00:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='carlos@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 1 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),DATE_SUB(CURDATE(),INTERVAL 2 DAY),'09:30:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='carlos@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 2 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),DATE_SUB(CURDATE(),INTERVAL 3 DAY),'08:15:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='carlos@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 3 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='carlos@demo.com'),DATE_SUB(CURDATE(),INTERVAL 4 DAY),'08:00:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='carlos@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 4 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada,observaciones)
SELECT (SELECT id FROM usuarios WHERE email='ana@demo.com'),DATE_SUB(CURDATE(),INTERVAL 1 DAY),'18:15:00','Clase de cardio' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='ana@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 1 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='ana@demo.com'),DATE_SUB(CURDATE(),INTERVAL 3 DAY),'18:00:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='ana@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 3 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='ana@demo.com'),DATE_SUB(CURDATE(),INTERVAL 5 DAY),'17:45:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='ana@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 5 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada,observaciones)
SELECT (SELECT id FROM usuarios WHERE email='david@demo.com'),CURDATE(),'08:00:00','Trabajo de pierna' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='david@demo.com') AND fecha=CURDATE());
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='david@demo.com'),DATE_SUB(CURDATE(),INTERVAL 5 DAY),'08:00:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='david@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 5 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada,observaciones)
SELECT (SELECT id FROM usuarios WHERE email='maria@demo.com'),DATE_SUB(CURDATE(),INTERVAL 1 DAY),'17:30:00','Tonificacion general' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='maria@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 1 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='laura@demo.com'),CURDATE(),'06:45:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='laura@demo.com') AND fecha=CURDATE());
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='laura@demo.com'),DATE_SUB(CURDATE(),INTERVAL 1 DAY),'06:50:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='laura@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 1 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='miguel@demo.com'),DATE_SUB(CURDATE(),INTERVAL 2 DAY),'10:00:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='miguel@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 2 DAY));
INSERT INTO asistencias (usuario_id,fecha,hora_entrada)
SELECT (SELECT id FROM usuarios WHERE email='pablo@demo.com'),DATE_SUB(CURDATE(),INTERVAL 1 DAY),'08:30:00' WHERE NOT EXISTS (SELECT 1 FROM asistencias WHERE usuario_id=(SELECT id FROM usuarios WHERE email='pablo@demo.com') AND fecha=DATE_SUB(CURDATE(),INTERVAL 1 DAY));


-- ─── TRIALS ──────────────────────────────────────────────────────────────────
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones,staff_responsable_id)
SELECT 'Marco','Torres Peñas','marco.torres@gmail.com','620111001','Instagram','PENDIENTE',DATE_ADD(CURDATE(),INTERVAL 2 DAY),'Interesado en plan Premium',
(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='antonio@demo.com'))
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='marco.torres@gmail.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones,staff_responsable_id)
SELECT 'Laia','Puig Rovira','laia.puig@hotmail.com','620111002','Referido amigo','PENDIENTE',DATE_ADD(CURDATE(),INTERVAL 5 DAY),'Le gustaria clases de yoga',
(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='antonio@demo.com'))
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='laia.puig@hotmail.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones,staff_responsable_id)
SELECT 'Carlos','Esteve Mas','c.esteve@gmail.com','620111003','Google','ASISTIO',DATE_SUB(CURDATE(),INTERVAL 3 DAY),'Muy motivado, pregunto por membresía trimestral',
(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='antonio@demo.com'))
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='c.esteve@gmail.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones)
SELECT 'Miriam','Sanz Cuenca','m.sanz@outlook.com','620111004','Pasando por la zona','NO_ASISTIO',DATE_SUB(CURDATE(),INTERVAL 7 DAY),'No aparecio, llamar para reagendar'
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='m.sanz@outlook.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones,staff_responsable_id)
SELECT 'Ruben','Canovas Gil','ruben.c@gmail.com','620111005','Facebook','PENDIENTE',DATE_ADD(CURDATE(),INTERVAL 1 DAY),'Consulto precio de plan basico',
(SELECT id FROM staff_perfiles WHERE usuario_id=(SELECT id FROM usuarios WHERE email='antonio@demo.com'))
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='ruben.c@gmail.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones)
SELECT 'Ainara','Zabala Etxea','ainara.z@gmail.com','620111006','Instagram','PENDIENTE',DATE_ADD(CURDATE(),INTERVAL 8 DAY),'Interesada en clases de pilates'
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='ainara.z@gmail.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba)
SELECT 'Jaume','Valls Pont','jaume.v@gmail.com','620111007','Web','CANCELADO',DATE_SUB(CURDATE(),INTERVAL 5 DAY)
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='jaume.v@gmail.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones)
SELECT 'Noa','Rivas Montes','noa.r@demo.com','620111008','Referido','CONVERTIDO',DATE_SUB(CURDATE(),INTERVAL 10 DAY),'Convertida a usuaria - plan Premium' WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='noa.r@demo.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba)
SELECT 'Ariadna','Colomer Serra','ariadna.c@gmail.com','620111009','Instagram','PENDIENTE',DATE_ADD(CURDATE(),INTERVAL 4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='ariadna.c@gmail.com');
INSERT INTO trials (nombre,apellidos,email,telefono,origen,estado,fecha_prueba,observaciones)
SELECT 'Oriol','Franch Vila','oriol.f@gmail.com','620111010','Google','ASISTIO',DATE_SUB(CURDATE(),INTERVAL 1 DAY),'Muy interesado en entrenos matinales'
WHERE NOT EXISTS (SELECT 1 FROM trials WHERE email='oriol.f@gmail.com');

-- ─── MAQUINAS ────────────────────────────────────────────────────────────────
INSERT INTO maquinas (nombre,descripcion,categoria,estado,marca,modelo,numero_serie,ubicacion,activo)
SELECT 'Cinta de correr X1','Cinta principal de cardio de uso continuo','CARDIO','ACTIVA','TechnoGym','Run 1000','SN-CX1-001','Sala Cardio - Zona A',1
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre='Cinta de correr X1');
INSERT INTO maquinas (nombre,descripcion,categoria,estado,marca,modelo,numero_serie,ubicacion,activo)
SELECT 'Bicicleta estatica B-Pro','Bicicleta de alta resistencia para spinning libre','CARDIO','ACTIVA','Kettler','Racer S','SN-BPR-002','Sala Cardio - Zona A',1
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre='Bicicleta estatica B-Pro');
INSERT INTO maquinas (nombre,descripcion,categoria,estado,marca,modelo,numero_serie,ubicacion,activo)
SELECT 'Polea dual D500','Maquina de poleas para trabajo de espalda y brazos','FUERZA','MANTENIMIENTO','BH Fitness','D500 Pro','SN-PD5-003','Sala Musculacion - Zona B',1
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre='Polea dual D500');
INSERT INTO maquinas (nombre,descripcion,categoria,estado,marca,modelo,numero_serie,ubicacion,activo)
SELECT 'Prensa de piernas P900','Maquina de fuerza para tren inferior','FUERZA','ACTIVA','Life Fitness','P900 Max','SN-PP9-004','Sala Musculacion - Zona B',1
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre='Prensa de piernas P900');
INSERT INTO maquinas (nombre,descripcion,categoria,estado,marca,modelo,numero_serie,ubicacion,activo)
SELECT 'Eliptica EF800','Eliptica de bajo impacto para cardio suave','CARDIO','ACTIVA','Precor','EFX 800','SN-EF8-005','Sala Cardio - Zona A',1
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre='Eliptica EF800');
INSERT INTO maquinas (nombre,descripcion,categoria,estado,marca,modelo,numero_serie,ubicacion,activo)
SELECT 'Rack de sentadillas','Rack multifuncion para sentadilla y press','FUERZA','ACTIVA','Rogue','Monster Rack','SN-RS1-006','Sala Musculacion - Zona C',1
WHERE NOT EXISTS (SELECT 1 FROM maquinas WHERE nombre='Rack de sentadillas');

-- ─── MATERIALES ──────────────────────────────────────────────────────────────
INSERT INTO materiales (nombre,descripcion,categoria,estado,stock,stock_minimo,coste_unitario,proveedor,ubicacion,activo)
SELECT 'Mancuernas ajustables','Set de mancuernas para trabajo de fuerza','ENTRENAMIENTO','DISPONIBLE',12,4,35.00,'Sport Supply','Almacen - Estanteria A',1
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre='Mancuernas ajustables');
INSERT INTO materiales (nombre,descripcion,categoria,estado,stock,stock_minimo,coste_unitario,proveedor,ubicacion,activo)
SELECT 'Barras olimpicas','Barras para levantamientos y movimientos compuestos','ENTRENAMIENTO','DISPONIBLE',6,2,120.00,'Sport Supply','Almacen - Estanteria B',1
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre='Barras olimpicas');
INSERT INTO materiales (nombre,descripcion,categoria,estado,stock,stock_minimo,coste_unitario,proveedor,ubicacion,activo)
SELECT 'Kettlebells','Piezas de peso libre para potencia y acondicionamiento','ENTRENAMIENTO','BAJO_STOCK',3,5,28.00,'Fitness Pro','Almacen - Estanteria A',1
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre='Kettlebells');
INSERT INTO materiales (nombre,descripcion,categoria,estado,stock,stock_minimo,coste_unitario,proveedor,ubicacion,activo)
SELECT 'Bandas elasticas','Bandas de resistencia para movilidad y apoyo','ENTRENAMIENTO','DISPONIBLE',25,10,6.50,'Fitness Pro','Almacen - Estanteria C',1
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre='Bandas elasticas');
INSERT INTO materiales (nombre,descripcion,categoria,estado,stock,stock_minimo,coste_unitario,proveedor,ubicacion,activo)
SELECT 'Esterillas yoga','Esterillas antideslizantes para clases de yoga y pilates','ENTRENAMIENTO','DISPONIBLE',18,8,15.00,'Decathlon B2B','Sala Yoga',1
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre='Esterillas yoga');
INSERT INTO materiales (nombre,descripcion,categoria,estado,stock,stock_minimo,coste_unitario,proveedor,ubicacion,activo)
SELECT 'Desinfectante superficies','Spray desinfectante para maquinas y superficies','LIMPIEZA','DISPONIBLE',8,3,4.20,'CleanPro','Almacen - Limpieza',1
WHERE NOT EXISTS (SELECT 1 FROM materiales WHERE nombre='Desinfectante superficies');

-- ─── GASTOS ──────────────────────────────────────────────────────────────────
INSERT INTO gastos (concepto,descripcion,categoria,monto,importe,estado,fecha_gasto,pagado,recurrente,proveedor,activo,maquina_id)
SELECT 'Mantenimiento cinta principal','Cambio de banda y ajuste de motor en cinta principal','MANTENIMIENTO',185.00,185.00,'APROBADO',DATE_SUB(CURDATE(),INTERVAL 7 DAY),0,0,'TechnoGym Service',1,
(SELECT id FROM maquinas WHERE nombre='Cinta de correr X1' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto='Mantenimiento cinta principal');
INSERT INTO gastos (concepto,descripcion,categoria,monto,importe,estado,fecha_gasto,pagado,recurrente,proveedor,activo,material_id)
SELECT 'Reposicion bandas elasticas','Compra de nuevo set de bandas para clases grupales','COMPRA_MATERIAL',76.40,76.40,'APROBADO',DATE_SUB(CURDATE(),INTERVAL 5 DAY),0,0,'Fitness Pro',1,
(SELECT id FROM materiales WHERE nombre='Bandas elasticas' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto='Reposicion bandas elasticas');
INSERT INTO gastos (concepto,descripcion,categoria,monto,importe,estado,fecha_gasto,pagado,recurrente,frecuencia,proveedor,activo)
SELECT 'Alquiler local','Cuota mensual de alquiler de las instalaciones','ALQUILER',1200.00,1200.00,'APROBADO',DATE_SUB(CURDATE(),INTERVAL 1 DAY),1,1,'MENSUAL','Inmobiliaria Centro',1
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto='Alquiler local');
INSERT INTO gastos (concepto,descripcion,categoria,monto,importe,estado,fecha_gasto,pagado,recurrente,frecuencia,proveedor,activo)
SELECT 'Nomina Antonio (Recepcion)','Salario mensual recepcionista','NOMINA',1350.00,1350.00,'APROBADO',DATE_SUB(CURDATE(),INTERVAL 1 DAY),1,1,'MENSUAL','RRHH',1
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto='Nomina Antonio (Recepcion)');
INSERT INTO gastos (concepto,descripcion,categoria,monto,importe,estado,fecha_gasto,pagado,recurrente,frecuencia,proveedor,activo)
SELECT 'Nomina Claudia (Entrenadora)','Salario mensual entrenadora','NOMINA',1600.00,1600.00,'APROBADO',DATE_SUB(CURDATE(),INTERVAL 1 DAY),1,1,'MENSUAL','RRHH',1
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto='Nomina Claudia (Entrenadora)');
INSERT INTO gastos (concepto,descripcion,categoria,monto,importe,estado,fecha_gasto,pagado,recurrente,proveedor,activo)
SELECT 'Marketing redes sociales','Promocion de campaña de captacion en Instagram y Facebook','MARKETING',80.00,80.00,'REGISTRADO',CURDATE(),0,0,'Agencia Social',1
WHERE NOT EXISTS (SELECT 1 FROM gastos WHERE concepto='Marketing redes sociales');

SET FOREIGN_KEY_CHECKS = 1;
-- ═══════════════════════════════════════════════════════
-- FIN de data.sql — FlacoFitness datos demo completos
-- ═══════════════════════════════════════════════════════

