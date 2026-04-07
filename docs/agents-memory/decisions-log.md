# Decisions Log

## Como registrar una decision

Usar una entrada por decision tecnica relevante. Mantener el registro en orden cronologico y reflejar tanto el motivo como el impacto esperado.

## Plantilla

| Fecha | Decision | Motivo | Impacto |
| --- | --- | --- | --- |
| AAAA-MM-DD | Describir la decision tecnica | Explicar por que se toma | Indicar consecuencias, beneficios o limitaciones |

## Registro inicial

| Fecha | Decision | Motivo | Impacto |
| --- | --- | --- | --- |
| 2026-03-23 | Adoptar una arquitectura monolitica MVC con Spring Boot y Thymeleaf | Se ajusta al alcance academico, facilita despliegue y simplifica la defensa del proyecto | Permite avanzar rapido con una base coherente y mantenible |
| 2026-03-24 | Adoptar flujo de ramas con `main` estable y `develop` como integracion | Mejora trazabilidad y reduce riesgo de mezclar trabajo inestable con entregas | Facilita colaboracion, revision y automatizacion del repositorio |
| 2026-04-07 | Ejecutar una alineacion automatica del esquema de `rutinas` al arrancar | La base MySQL conservaba `rutinas.usuario_id` aunque el dominio ya usa una relacion `ManyToMany` con `usuario_rutina` | Se migran relaciones legacy sin perder datos y se evita el error 500 al crear o editar rutinas |
