# Project Overview

## Nombre del proyecto

FlacoFitness

## Objetivo general

FlacoFitness es una aplicacion web academica de gestion para gimnasio con enfoque de panel SaaS administrativo. El objetivo actual es consolidar una experiencia conectada y automatizada, defendible en contexto academico y potencialmente vendible para un gimnasio local, cubriendo captacion, clientes, staff, membresias, pagos, rutinas, clases, sesiones, asistencias, gastos e inventario.

## Stack tecnologico

- Java 17 como version objetivo del proyecto.
- Spring Boot 3.3.x.
- Spring Data JPA.
- Thymeleaf.
- Bootstrap 5.
- JavaScript ligero.
- Chart.js y DataTables.
- MySQL/phpMyAdmin como unica base de datos valida.
- Maven Wrapper.

## Alcance actual

- Monolito MVC con capas `controller`, `service`, `repository` y `model`.
- CRUDs y flujos operativos para usuarios, rutinas, pagos y asistencias.
- Nucleo SaaS recuperado con staff, membresias contractuales, trials, clases, sesiones y reservas.
- Dashboard administrativo con metricas, graficos, notificaciones y navegacion contextual.
- Acceso MVP por PIN y perfiles de sesion sin Spring Security.
- Documentacion viva en `docs/agents-memory/`.

## Publico objetivo

- Profesorado evaluador del proyecto academico.
- Estudiantes o desarrolladores que continuen el sistema.
- Personal de un gimnasio local: administracion, recepcion, entrenadores y gerencia.

## Estado actual del proyecto

- Fecha de referencia: 2026-04-20.
- Rama de trabajo: `recovery/restore-core-saas-plan-a`.
- Estado: cierre funcional avanzado con automatizaciones activas y shell SaaS premium estabilizado.
- Backend: compila y los tests pasan con Maven Wrapper en la rama de recuperacion.
- Frontend: shell SaaS con sidebar, topbar, dashboard con KPIs conectados, comparativa ingresos vs gastos y tablas optimizadas para mobile/desktop.
- Base de datos: perfil `local` y ejecucion principal usan MySQL sobre la base `flacofitness`; `spring.sql.init.mode=never` evita borrar o duplicar datos.
- Acceso: PIN global con perfil de sesion `ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE` o `CLIENTE`.

## Capacidades diferenciales actuales

- Dashboard operativo con actualizacion de metricas y lectura cruzada de negocio (pagos, gastos, trials, stock y estado de maquinas).
- Centro de notificaciones accionables priorizadas por criticidad para pagos vencidos, inactividad, sesiones y renovaciones.
- Modo oscuro con persistencia local para mejorar experiencia de uso continuo.
- Modulos conectados mediante redirecciones contextuales (usuario, pago, asistencia, rutina, membresia y sesiones).
- Reloj operativo persistido en MySQL para simular meses anteriores o siguientes sin modificar la fecha real del servidor ni borrar datos.

## Nota de continuidad

El estado sucio previo a la recuperacion se preservo en un stash Git llamado `backup before recovery core saas plan a`. No eliminar ese stash hasta confirmar que no contiene nada que se quiera rescatar manualmente.
