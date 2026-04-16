# Project Overview

## Nombre del proyecto

FlacoFitness

## Objetivo general

FlacoFitness es una aplicacion web academica de gestion para gimnasio con enfoque de panel SaaS administrativo. El objetivo actual es mantener una base funcional, defendible y potencialmente vendible para un gimnasio local, cubriendo captacion, clientes, staff, membresias, pagos, rutinas, clases, sesiones y asistencias.

## Stack tecnologico

- Java 17 como version objetivo del proyecto.
- Spring Boot 3.3.x.
- Spring Data JPA.
- Thymeleaf.
- Bootstrap 5.
- JavaScript ligero.
- Chart.js y DataTables.
- MySQL para ejecucion real.
- H2 en perfil `local` para demo controlada.
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

- Fecha de referencia: 2026-04-16.
- Rama de trabajo: `recovery/restore-core-saas-plan-a`.
- Estado: recuperacion funcional avanzada tras una integracion parcial rota.
- Backend: compila y los tests pasan con Maven Wrapper.
- Frontend: shell SaaS con sidebar, topbar, dashboard, tablas, formularios y vistas de detalle.
- Base de datos: perfil `local` usa H2 con demo; MySQL real queda protegido con `spring.sql.init.mode=never`.
- Acceso: PIN global con perfil de sesion `ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE` o `CLIENTE`.

## Nota de continuidad

El estado sucio previo a la recuperacion se preservo en un stash Git llamado `backup before recovery core saas plan a`. No eliminar ese stash hasta confirmar que no contiene nada que se quiera rescatar manualmente.
