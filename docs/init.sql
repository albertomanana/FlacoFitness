-- ═══════════════════════════════════════════════════════════════════════════
-- FlacoFitness — Script de inicialización de la base de datos
-- Base de datos: flacofitness | Versión: 1.0.0 | Motor: MySQL 8+ / MariaDB
-- ═══════════════════════════════════════════════════════════════════════════
-- Uso: mysql -u root -p < docs/init.sql
-- Este script crea la BD desde cero. Si ya existe, la elimina y recrea.
-- ═══════════════════════════════════════════════════════════════════════════

SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS flacofitness;
CREATE DATABASE flacofitness
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE flacofitness;

-- ─────────────────────────────────────────────
-- TABLA: roles
-- Roles de acceso del sistema (STAFF, CLIENTE)
-- ─────────────────────────────────────────────
CREATE TABLE `roles` (
  `id`     BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100)  NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_roles_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: planes
-- Planes de membresía disponibles en el gimnasio
-- ─────────────────────────────────────────────
CREATE TABLE `planes` (
  `id`             BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`         VARCHAR(100)  NOT NULL,
  `descripcion`    TEXT          DEFAULT NULL,
  `precio_mensual` DECIMAL(10,2) NOT NULL,
  `duracion_dias`  INT(11)       NOT NULL DEFAULT 30,
  `activo`         TINYINT(1)    DEFAULT 1,
  `beneficios`     TEXT          DEFAULT NULL,
  `tipo_membresia` ENUM('ESTUDIANTE','MENSUAL','PREMIUM','PRUEBA','TRIMESTRAL') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: usuarios
-- Miembros del gimnasio (clientes y staff)
-- ─────────────────────────────────────────────
CREATE TABLE `usuarios` (
  `id`                 BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`             VARCHAR(100)  NOT NULL,
  `apellidos`          VARCHAR(150)  DEFAULT NULL,
  `email`              VARCHAR(150)  NOT NULL,
  `dni`                VARCHAR(20)   DEFAULT NULL,
  `telefono`           VARCHAR(20)   DEFAULT NULL,
  `direccion`          VARCHAR(255)  DEFAULT NULL,
  `activo`             BIT(1)        NOT NULL DEFAULT 1,
  `fecha_registro`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_nacimiento`   DATE          DEFAULT NULL,
  `fecha_proximo_pago` DATE          DEFAULT NULL,
  `rol_id`             BIGINT(20)    DEFAULT NULL,
  `plan_id`            BIGINT(20)    DEFAULT NULL,
  `foto_path`          VARCHAR(255)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuarios_email` (`email`),
  KEY `fk_usuarios_rol`  (`rol_id`),
  KEY `fk_usuarios_plan` (`plan_id`),
  CONSTRAINT `fk_usuarios_rol`  FOREIGN KEY (`rol_id`)  REFERENCES `roles`  (`id`),
  CONSTRAINT `fk_usuarios_plan` FOREIGN KEY (`plan_id`) REFERENCES `planes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: staff (tabla heredada — empleados legacy)
-- ─────────────────────────────────────────────
CREATE TABLE `staff` (
  `id`         BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`     VARCHAR(100)  NOT NULL,
  `apellidos`  VARCHAR(150)  DEFAULT NULL,
  `email`      VARCHAR(150)  NOT NULL,
  `telefono`   VARCHAR(20)   DEFAULT NULL,
  `cargo`      VARCHAR(120)  DEFAULT NULL,
  `activo`     BIT(1)        NOT NULL DEFAULT 1,
  `fecha_alta` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: staff_perfiles
-- Perfil operativo de cada miembro del personal
-- vinculado a un usuario del sistema
-- ─────────────────────────────────────────────
CREATE TABLE `staff_perfiles` (
  `id`           BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `usuario_id`   BIGINT(20)    NOT NULL,
  `rol_staff`    ENUM('ADMINISTRACION','ENTRENADOR','GERENTE','RECEPCION') NOT NULL,
  `especialidad` VARCHAR(120)  DEFAULT NULL,
  `observaciones`TEXT          DEFAULT NULL,
  `activo`       BIT(1)        NOT NULL DEFAULT 1,
  `fecha_alta`   DATE          NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_perfiles_usuario` (`usuario_id`),
  CONSTRAINT `fk_staff_perfiles_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: rutinas
-- Programas de entrenamiento (generales o personalizadas)
-- ─────────────────────────────────────────────
CREATE TABLE `rutinas` (
  `id`                   BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`               VARCHAR(120)  NOT NULL,
  `descripcion`          VARCHAR(500)  DEFAULT NULL,
  `tipo_rutina`          ENUM('GENERAL','PERSONALIZADA') NOT NULL,
  `objetivo`             ENUM('FUERZA','HIPERTROFIA','MANTENIMIENTO','PERDIDA_GRASA') NOT NULL,
  `activa`               TINYINT(1)    DEFAULT 1,
  `fecha_creacion`       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `staff_responsable_id` BIGINT(20)    DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_rutinas_staff` (`staff_responsable_id`),
  CONSTRAINT `fk_rutinas_staff` FOREIGN KEY (`staff_responsable_id`) REFERENCES `staff_perfiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: ejercicios
-- Catálogo de ejercicios individuales
-- ─────────────────────────────────────────────
CREATE TABLE `ejercicios` (
  `id`               BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`           VARCHAR(100)  NOT NULL,
  `grupo_muscular`   VARCHAR(100)  DEFAULT NULL,
  `descripcion`      TEXT          DEFAULT NULL,
  `nivel_dificultad` VARCHAR(50)   DEFAULT NULL,
  `activo`           TINYINT(1)    DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: rutina_ejercicios
-- Detalle de ejercicios dentro de cada rutina
-- ─────────────────────────────────────────────
CREATE TABLE `rutina_ejercicios` (
  `id`                BIGINT(20) NOT NULL AUTO_INCREMENT,
  `rutina_id`         BIGINT(20) NOT NULL,
  `ejercicio_id`      BIGINT(20) NOT NULL,
  `series`            INT(11)    DEFAULT NULL,
  `repeticiones`      INT(11)    DEFAULT NULL,
  `descanso_segundos` INT(11)    DEFAULT NULL,
  `orden`             INT(11)    DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rutina_ejercicio` (`rutina_id`, `ejercicio_id`),
  KEY `fk_re_ejercicio` (`ejercicio_id`),
  CONSTRAINT `fk_re_rutina`    FOREIGN KEY (`rutina_id`)    REFERENCES `rutinas`    (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_re_ejercicio` FOREIGN KEY (`ejercicio_id`) REFERENCES `ejercicios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: usuario_rutina
-- Relación muchos a muchos usuario ↔ rutina
-- ─────────────────────────────────────────────
CREATE TABLE `usuario_rutina` (
  `usuario_id` BIGINT(20) NOT NULL,
  `rutina_id`  BIGINT(20) NOT NULL,
  PRIMARY KEY (`rutina_id`, `usuario_id`),
  KEY `fk_ur_usuario` (`usuario_id`),
  CONSTRAINT `fk_ur_rutina`  FOREIGN KEY (`rutina_id`)  REFERENCES `rutinas`  (`id`),
  CONSTRAINT `fk_ur_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: clases
-- Tipos de clase que ofrece el gimnasio
-- ─────────────────────────────────────────────
CREATE TABLE `clases` (
  `id`                 BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`             VARCHAR(120)  NOT NULL,
  `descripcion`        TEXT          DEFAULT NULL,
  `observaciones`      TEXT          DEFAULT NULL,
  `capacidad_sugerida` INT(11)       NOT NULL DEFAULT 20,
  `activa`             BIT(1)        NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: sesiones_clase
-- Instancias planificadas de una clase concreta
-- ─────────────────────────────────────────────
CREATE TABLE `sesiones_clase` (
  `id`                   BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `clase_id`             BIGINT(20)    NOT NULL,
  `staff_responsable_id` BIGINT(20)    DEFAULT NULL,
  `rutina_id`            BIGINT(20)    DEFAULT NULL,
  `fecha`                DATE          NOT NULL,
  `hora_inicio`          TIME(6)       NOT NULL,
  `hora_fin`             TIME(6)       DEFAULT NULL,
  `aforo`                INT(11)       NOT NULL DEFAULT 20,
  `estado`               ENUM('CANCELADA','FINALIZADA','PROGRAMADA') NOT NULL DEFAULT 'PROGRAMADA',
  `observaciones`        TEXT          DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_sc_clase`   (`clase_id`),
  KEY `fk_sc_staff`   (`staff_responsable_id`),
  KEY `fk_sc_rutina`  (`rutina_id`),
  CONSTRAINT `fk_sc_clase`   FOREIGN KEY (`clase_id`)             REFERENCES `clases`        (`id`),
  CONSTRAINT `fk_sc_staff`   FOREIGN KEY (`staff_responsable_id`) REFERENCES `staff_perfiles`(`id`),
  CONSTRAINT `fk_sc_rutina`  FOREIGN KEY (`rutina_id`)            REFERENCES `rutinas`       (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: sesiones (alias / tabla duplicada heredada)
-- Mantiene compatibilidad con FK en reservas_sesion
-- ─────────────────────────────────────────────
CREATE TABLE `sesiones` (
  `id`                   BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `clase_id`             BIGINT(20)    NOT NULL,
  `staff_responsable_id` BIGINT(20)    DEFAULT NULL,
  `rutina_id`            BIGINT(20)    DEFAULT NULL,
  `fecha`                DATE          NOT NULL,
  `hora_inicio`          TIME(6)       NOT NULL,
  `hora_fin`             TIME(6)       DEFAULT NULL,
  `aforo`                INT(11)       NOT NULL DEFAULT 20,
  `estado`               ENUM('CANCELADA','FINALIZADA','PROGRAMADA') NOT NULL DEFAULT 'PROGRAMADA',
  `observaciones`        TEXT          DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ses_clase`   (`clase_id`),
  KEY `fk_ses_staff`   (`staff_responsable_id`),
  KEY `fk_ses_rutina`  (`rutina_id`),
  CONSTRAINT `fk_ses_clase`  FOREIGN KEY (`clase_id`)             REFERENCES `clases`  (`id`),
  CONSTRAINT `fk_ses_staff`  FOREIGN KEY (`staff_responsable_id`) REFERENCES `staff`   (`id`),
  CONSTRAINT `fk_ses_rutina` FOREIGN KEY (`rutina_id`)            REFERENCES `rutinas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: asistencias
-- Registro de entrada/asistencia al gimnasio
-- ─────────────────────────────────────────────
CREATE TABLE `asistencias` (
  `id`              BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `usuario_id`      BIGINT(20)    NOT NULL,
  `fecha`           DATE          NOT NULL,
  `hora_entrada`    TIME(6)       DEFAULT NULL,
  `sesion_clase_id` BIGINT(20)    DEFAULT NULL,
  `observaciones`   VARCHAR(500)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_asist_usuario`  (`usuario_id`),
  KEY `fk_asist_sesion`   (`sesion_clase_id`),
  CONSTRAINT `fk_asist_usuario` FOREIGN KEY (`usuario_id`)      REFERENCES `usuarios`      (`id`),
  CONSTRAINT `fk_asist_sesion`  FOREIGN KEY (`sesion_clase_id`) REFERENCES `sesiones_clase`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: membresias
-- Tipos de membresía disponibles (catálogo)
-- ─────────────────────────────────────────────
CREATE TABLE `membresias` (
  `id`             BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`         VARCHAR(120)  NOT NULL,
  `descripcion`    TEXT          DEFAULT NULL,
  `tipo_membresia` ENUM('ESTUDIANTE','MENSUAL','PREMIUM','PRUEBA','TRIMESTRAL') NOT NULL,
  `precio_mensual` DECIMAL(10,2) NOT NULL,
  `duracion_dias`  INT(11)       NOT NULL DEFAULT 30,
  `beneficios`     TEXT          DEFAULT NULL,
  `activo`         BIT(1)        NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: membresias_usuario
-- Contrato activo de un usuario con un plan
-- ─────────────────────────────────────────────
CREATE TABLE `membresias_usuario` (
  `id`               BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `usuario_id`       BIGINT(20)    NOT NULL,
  `plan_id`          BIGINT(20)    NOT NULL,
  `estado`           ENUM('ACTIVA','CANCELADA','CONGELADA','PENDIENTE','PRUEBA','VENCIDA') NOT NULL,
  `fecha_inicio`     DATE          NOT NULL,
  `fecha_fin`        DATE          DEFAULT NULL,
  `precio_snapshot`  DECIMAL(10,2) NOT NULL,
  `origen`           VARCHAR(80)   DEFAULT NULL,
  `observaciones`    TEXT          DEFAULT NULL,
  `fecha_creacion`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_mu_usuario` (`usuario_id`),
  KEY `fk_mu_plan`    (`plan_id`),
  CONSTRAINT `fk_mu_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_mu_plan`    FOREIGN KEY (`plan_id`)    REFERENCES `planes`   (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: pagos
-- Registro de cobros por membresía
-- ─────────────────────────────────────────────
CREATE TABLE `pagos` (
  `id`                   BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `usuario_id`           BIGINT(20)    NOT NULL,
  `plan_id`              BIGINT(20)    DEFAULT NULL,
  `membresia_usuario_id` BIGINT(20)    DEFAULT NULL,
  `referencia`           VARCHAR(36)   NOT NULL,
  `monto`                DECIMAL(10,2) NOT NULL,
  `metodo_pago`          ENUM('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL,
  `estado`               ENUM('PAGADO','PENDIENTE','VENCIDO') NOT NULL,
  `fecha_pago`           DATE          DEFAULT NULL,
  `fecha_vencimiento`    DATE          NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pagos_referencia` (`referencia`),
  KEY `fk_pagos_usuario`           (`usuario_id`),
  KEY `fk_pagos_plan`              (`plan_id`),
  KEY `fk_pagos_membresia_usuario` (`membresia_usuario_id`),
  CONSTRAINT `fk_pagos_usuario`           FOREIGN KEY (`usuario_id`)           REFERENCES `usuarios`          (`id`),
  CONSTRAINT `fk_pagos_plan`              FOREIGN KEY (`plan_id`)              REFERENCES `planes`            (`id`),
  CONSTRAINT `fk_pagos_membresia_usuario` FOREIGN KEY (`membresia_usuario_id`) REFERENCES `membresias_usuario`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: trials
-- Leads / días de prueba (CRM comercial)
-- ─────────────────────────────────────────────
CREATE TABLE `trials` (
  `id`                   BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`               VARCHAR(100)  NOT NULL,
  `apellidos`            VARCHAR(150)  DEFAULT NULL,
  `email`                VARCHAR(150)  DEFAULT NULL,
  `telefono`             VARCHAR(20)   DEFAULT NULL,
  `origen`               VARCHAR(100)  DEFAULT NULL,
  `estado`               ENUM('ASISTIO','CANCELADO','CONVERTIDO','NO_ASISTIO','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_prueba`         DATE          NOT NULL,
  `observaciones`        TEXT          DEFAULT NULL,
  `staff_responsable_id` BIGINT(20)    DEFAULT NULL,
  `usuario_convertido_id`BIGINT(20)    DEFAULT NULL,
  `fecha_registro`       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_trials_staff`    (`staff_responsable_id`),
  KEY `fk_trials_usuario`  (`usuario_convertido_id`),
  CONSTRAINT `fk_trials_staff`   FOREIGN KEY (`staff_responsable_id`)  REFERENCES `staff_perfiles` (`id`),
  CONSTRAINT `fk_trials_usuario` FOREIGN KEY (`usuario_convertido_id`) REFERENCES `usuarios`       (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: reservas_sesion
-- Reservas de un usuario para una sesión de clase
-- ─────────────────────────────────────────────
CREATE TABLE `reservas_sesion` (
  `id`              BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `sesion_clase_id` BIGINT(20)    NOT NULL,
  `sesion_id`       BIGINT(20)    NOT NULL,
  `usuario_id`      BIGINT(20)    NOT NULL,
  `estado`          ENUM('ASISTIO','CANCELADA','NO_ASISTIO','RESERVADA') NOT NULL DEFAULT 'RESERVADA',
  `asistio`         BIT(1)        NOT NULL DEFAULT 0,
  `observaciones`   TEXT          DEFAULT NULL,
  `fecha_reserva`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reserva_sesion_usuario`    (`sesion_id`,      `usuario_id`),
  UNIQUE KEY `uk_reserva_sesionclase_usuario`(`sesion_clase_id`, `usuario_id`),
  KEY `fk_res_usuario`    (`usuario_id`),
  CONSTRAINT `fk_res_sesionclase` FOREIGN KEY (`sesion_clase_id`) REFERENCES `sesiones_clase`(`id`),
  CONSTRAINT `fk_res_sesion`      FOREIGN KEY (`sesion_id`)       REFERENCES `sesiones`      (`id`),
  CONSTRAINT `fk_res_usuario`     FOREIGN KEY (`usuario_id`)      REFERENCES `usuarios`      (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: maquinas
-- Inventario de máquinas del gimnasio
-- ─────────────────────────────────────────────
CREATE TABLE `maquinas` (
  `id`               BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`           VARCHAR(120)  NOT NULL,
  `descripcion`      TEXT          DEFAULT NULL,
  `categoria`        ENUM('CARDIO','FUERZA','FUNCIONAL','MOVILIDAD','OTROS') NOT NULL,
  `estado`           ENUM('ACTIVA','FUERA_SERVICIO','MANTENIMIENTO') NOT NULL DEFAULT 'ACTIVA',
  `marca`            VARCHAR(80)   DEFAULT NULL,
  `modelo`           VARCHAR(80)   DEFAULT NULL,
  `numero_serie`     VARCHAR(120)  DEFAULT NULL,
  `ubicacion`        VARCHAR(140)  DEFAULT NULL,
  `fecha_compra`     DATE          DEFAULT NULL,
  `ultima_revision`  DATE          DEFAULT NULL,
  `proxima_revision` DATE          DEFAULT NULL,
  `observaciones`    TEXT          DEFAULT NULL,
  `foto_path`        VARCHAR(255)  DEFAULT NULL,
  `activo`           BIT(1)        NOT NULL DEFAULT 1,
  `fecha_creacion`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_maquinas_num_serie` (`numero_serie`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: materiales
-- Inventario de material fungible / equipamiento
-- ─────────────────────────────────────────────
CREATE TABLE `materiales` (
  `id`            BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `nombre`        VARCHAR(120)  NOT NULL,
  `descripcion`   TEXT          DEFAULT NULL,
  `categoria`     ENUM('CONSUMIBLE','ENTRENAMIENTO','LIMPIEZA','OFICINA','OTROS','SEGURIDAD') NOT NULL,
  `estado`        ENUM('AGOTADO','BAJO_STOCK','DISPONIBLE','INACTIVO') NOT NULL DEFAULT 'DISPONIBLE',
  `stock`         INT(11)       NOT NULL DEFAULT 0,
  `stock_minimo`  INT(11)       NOT NULL DEFAULT 1,
  `coste_unitario`DECIMAL(10,2) DEFAULT NULL,
  `proveedor`     VARCHAR(120)  DEFAULT NULL,
  `ubicacion`     VARCHAR(140)  DEFAULT NULL,
  `observaciones` TEXT          DEFAULT NULL,
  `activo`        BIT(1)        NOT NULL DEFAULT 1,
  `fecha_creacion`TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ─────────────────────────────────────────────
-- TABLA: gastos
-- Registro de gastos operativos del gimnasio
-- ─────────────────────────────────────────────
CREATE TABLE `gastos` (
  `id`                   BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `concepto`             VARCHAR(160)  NOT NULL,
  `descripcion`          TEXT          DEFAULT NULL,
  `categoria`            ENUM('ALQUILER','COMPRA_MATERIAL','MANTENIMIENTO','MARKETING','NOMINA','OTROS','SOFTWARE','SUMINISTROS') NOT NULL,
  `monto`                DECIMAL(10,2) NOT NULL,
  `importe`              DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `estado`               ENUM('APROBADO','CANCELADO','RECHAZADO','REGISTRADO') NOT NULL DEFAULT 'REGISTRADO',
  `fecha_gasto`          DATE          NOT NULL,
  `pagado`               BIT(1)        NOT NULL DEFAULT 0,
  `recurrente`           BIT(1)        NOT NULL DEFAULT 0,
  `frecuencia`           ENUM('ANUAL','MENSUAL','QUINCENAL','SEMANAL','TRIMESTRAL') DEFAULT NULL,
  `proveedor`            VARCHAR(120)  DEFAULT NULL,
  `observaciones`        TEXT          DEFAULT NULL,
  `activo`               BIT(1)        NOT NULL DEFAULT 1,
  `maquina_id`           BIGINT(20)    DEFAULT NULL,
  `material_id`          BIGINT(20)    DEFAULT NULL,
  `staff_id`             BIGINT(20)    DEFAULT NULL,
  `staff_responsable_id` BIGINT(20)    DEFAULT NULL,
  `fecha_creacion`       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_gastos_maquina`  (`maquina_id`),
  KEY `fk_gastos_material` (`material_id`),
  KEY `fk_gastos_staff`    (`staff_id`),
  KEY `fk_gastos_resp`     (`staff_responsable_id`),
  CONSTRAINT `fk_gastos_maquina`  FOREIGN KEY (`maquina_id`)           REFERENCES `maquinas`      (`id`),
  CONSTRAINT `fk_gastos_material` FOREIGN KEY (`material_id`)          REFERENCES `materiales`    (`id`),
  CONSTRAINT `fk_gastos_staff`    FOREIGN KEY (`staff_id`)             REFERENCES `staff`         (`id`),
  CONSTRAINT `fk_gastos_resp`     FOREIGN KEY (`staff_responsable_id`) REFERENCES `staff_perfiles`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ═══════════════════════════════════════════════════════════════════════════
-- FIN: todas las tablas creadas correctamente
-- Ejecutar docs/data.sql a continuación para poblar con datos de demostración
-- ═══════════════════════════════════════════════════════════════════════════
