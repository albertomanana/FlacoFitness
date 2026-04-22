# Project Overview

## Nombre del proyecto

FlacoFitness

## Que es hoy

FlacoFitness es una aplicacion web academica de gestion para gimnasio con enfoque SaaS administrativo. El proyecto ya no es solo un conjunto de CRUDs: tiene shell visual coherente, acceso por PIN, dashboard, modulos conectados y automatizacion financiera interna.

## Objetivo actual

Dejar una base estable, profesional y defendible para seguir iterando sin romper:

- monolito MVC con Spring Boot + Thymeleaf
- una unica base de datos MySQL real
- experiencia administrativa tipo SaaS
- reglas de negocio explicables en defensa academica

## Stack principal

- Java 17 como target del build
- Spring Boot 3.3.5
- Spring Data JPA
- Thymeleaf
- Bootstrap 5
- JavaScript ligero
- Chart.js
- DataTables
- MySQL / phpMyAdmin
- Maven Wrapper
- OpenHTMLtoPDF para PDFs

## Estado funcional resumido

- Usuarios, rutinas, pagos y asistencias: implementados y conectados.
- Staff, membresias, trials, clases, sesiones y reservas: implementados.
- Gastos, recurrentes y nominas: implementados a nivel de dominio, servicios, vistas y exportacion PDF.
- Dashboard: operativo con metricas, graficos, notificaciones y un rail premium de alertas accionables.
- Acceso por PIN con perfiles: operativo.
- Shell premium: operativo, con splash y transiciones ya protegidas con fail-safe para no dejar modulos en blanco.
- Nominas: detalle y PDF ya presentan una lectura mas profesional y menos CRUD.

## Base de datos

- Base valida unica: `flacofitness`
- Motor: MySQL / MariaDB en `localhost:3306`
- Usuario por defecto del proyecto: `flaco_user`
- Password por defecto del proyecto: `flaco_pass`
- `spring.sql.init.mode=never`
- `spring.jpa.hibernate.ddl-auto=update`

No hay H2, no hay fallback en memoria y no debe reintroducirse.

## Estado tecnico actual

- Rama actual: `recovery/restore-core-saas-plan-a`
- El arbol de trabajo no esta limpio; no hacer reset ciego.
- `.\mvnw.cmd clean -DskipTests compile` pasa.
- `.\mvnw.cmd test` pasa.
- La app responde en `http://localhost:8080`.
- Acceso inicial: `PIN 2468`

## Reloj operativo

El proyecto ya no usa reloj simulado persistido. `OperationalClockService` sigue siendo la abstraccion temporal unica del negocio, pero ahora delega en la fecha y hora reales del sistema.

## Fuente de verdad para continuidad

Si otro agente o desarrollador entra al proyecto, debe leer en este orden:

1. `docs/agents-memory/claude-code-handoff.md`
2. `docs/agents-memory/agent-working-rules.md`
3. `docs/agents-memory/claude-plan-status.md`
4. `docs/agents-memory/architecture.md`
5. `docs/agents-memory/domain-model.md`
6. `docs/agents-memory/setup-status.md`
7. `docs/agents-memory/module-status.md`
8. `docs/agents-memory/decisions-log.md`
9. `docs/agents-memory/backlog.md`

## Nota de continuidad

Este repositorio ya paso por una recuperacion fuerte. Hay trabajo previo importante preservado en Git y en la rama actual. La prioridad no es crecer a lo loco, sino continuar con cambios pequenos, validados y documentados.
