-- Database initialization script for FlacoFitness
-- Created: 2026-04-28
-- Purpose: Initialize base schema and structure

-- Set session parameters
SET FOREIGN_KEY_CHECKS=0;
SET AUTOCOMMIT=0;

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS flacofitness 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE flacofitness;

-- Table: roles
CREATE TABLE IF NOT EXISTS roles (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  nombre varchar(100) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY nombre (nombre)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table: planes
CREATE TABLE IF NOT EXISTS planes (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  nombre varchar(100) NOT NULL,
  descripcion text DEFAULT NULL,
  precio_mensual decimal(10,2) NOT NULL,
  duracion_dias int(11) NOT NULL DEFAULT 30,
  activo tinyint(1) DEFAULT 1,
  beneficios text DEFAULT NULL,
  tipo_membresia enum('ESTUDIANTE','MENSUAL','PREMIUM','PRUEBA','TRIMESTRAL') DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
  activo bit(1) NOT NULL,
  fecha_nacimiento date DEFAULT NULL,
  fecha_proximo_pago date DEFAULT NULL,
  fecha_registro timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  id bigint(20) NOT NULL AUTO_INCREMENT,
  plan_id bigint(20) DEFAULT NULL,
  rol_id bigint(20) DEFAULT NULL,
  dni varchar(20) DEFAULT NULL,
  telefono varchar(20) DEFAULT NULL,
  nombre varchar(100) NOT NULL,
  apellidos varchar(150) DEFAULT NULL,
  email varchar(150) NOT NULL,
  direccion varchar(255) DEFAULT NULL,
  foto_path varchar(255) DEFAULT NULL,
  must_change_password bit(1) NOT NULL,
  password_hash varchar(120) DEFAULT NULL,
  username varchar(60) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKkfsp0s1tflm1cwlj8idhqsad0 (email),
  UNIQUE KEY UKm2dvbwfge291euvmk6vkkocao (username),
  KEY FKqn9tvfdrn5cocyuk109pone9j (plan_id),
  KEY FKqf5elo4jcq7qrt83oi0qmenjo (rol_id),
  CONSTRAINT FKqf5elo4jcq7qrt83oi0qmenjo FOREIGN KEY (rol_id) REFERENCES roles (id),
  CONSTRAINT FKqn9tvfdrn5cocyuk109pone9j FOREIGN KEY (plan_id) REFERENCES planes (id)
) ENGINE=InnoDB AUTO_INCREMENT=167 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table: membresias_usuario
CREATE TABLE IF NOT EXISTS membresias_usuario (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  estado enum('ACTIVA','CANCELADA','CONGELADA','PENDIENTE','PRUEBA','VENCIDA') NOT NULL,
  fecha_creacion timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  fecha_fin date DEFAULT NULL,
  fecha_inicio date NOT NULL,
  observaciones text DEFAULT NULL,
  origen varchar(80) DEFAULT NULL,
  precio_snapshot decimal(10,2) NOT NULL,
  plan_id bigint(20) NOT NULL,
  usuario_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FKnumck0qw85hic6aq87wu8kb35 (plan_id),
  KEY FKg3uw7cp8i85cfptbgqekag3g4 (usuario_id),
  CONSTRAINT FKg3uw7cp8i85cfptbgqekag3g4 FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  CONSTRAINT FKnumck0qw85hic6aq87wu8kb35 FOREIGN KEY (plan_id) REFERENCES planes (id)
) ENGINE=InnoDB AUTO_INCREMENT=2080 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table: pagos
CREATE TABLE IF NOT EXISTS pagos (
  fecha_pago date DEFAULT NULL,
  fecha_vencimiento date NOT NULL,
  monto decimal(10,2) NOT NULL,
  id bigint(20) NOT NULL AUTO_INCREMENT,
  plan_id bigint(20) DEFAULT NULL,
  usuario_id bigint(20) NOT NULL,
  referencia varchar(36) NOT NULL,
  estado enum('PAGADO','PENDIENTE','PROGRAMADO','VENCIDO') NOT NULL,
  metodo_pago enum('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL,
  membresia_usuario_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKdavos0a2qshm9gu0c36rqpt8g (referencia),
  KEY FKsxl5xhgrm2ss18nm5yky13nnk (plan_id),
  KEY FKnv3skx0qnku57yljq0bu88oot (usuario_id),
  KEY FKhy4i3ofkl56tqfxltd8rl71i0 (membresia_usuario_id),
  CONSTRAINT FKhy4i3ofkl56tqfxltd8rl71i0 FOREIGN KEY (membresia_usuario_id) REFERENCES membresias_usuario (id),
  CONSTRAINT FKnv3skx0qnku57yljq0bu88oot FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  CONSTRAINT FKsxl5xhgrm2ss18nm5yky13nnk FOREIGN KEY (plan_id) REFERENCES planes (id)
) ENGINE=InnoDB AUTO_INCREMENT=8837 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Restore constraints
SET FOREIGN_KEY_CHECKS=1;
COMMIT;
