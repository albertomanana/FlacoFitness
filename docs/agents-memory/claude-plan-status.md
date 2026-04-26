# Claude Plan Status

Fecha de referencia: 2026-04-26

## Origen

Este documento traduce el archivo `.claude/claude_md_and_top_prompts_flacofitness_finance_gold.md` a un estado de ejecucion real dentro del repositorio.

Ese archivo no era un plan tecnico paso a paso, sino:

- una propuesta de `CLAUDE.md`
- un conjunto de prompts maestros para Claude Code
- una direccion clara: consolidar finanzas, UX premium, navegacion, limpieza y cierre de producto

## Estado del plan `.claude`

| Bloque del `.claude` | Estado | Nota |
| --- | --- | --- |
| Reglas base para Claude Code | Ejecutado | Se materializa en `CLAUDE.md` y en esta carpeta de contexto |
| Auditoria total del proyecto | Ejecutado parcialmente | El diagnostico ya esta repartido entre `claude-code-handoff.md`, `module-status.md`, `backlog.md` y `decisions-log.md` |
| Rediseño total del modulo financiero | En progreso alto | Pagos, gastos, recurrentes y nominas existen; esta iteracion reforzo UX financiera y coherencia visible |
| Nominas premium | Ejecutado parcialmente | Generacion, estado, PDF y relacion con gasto existen; detalle y PDF ya tienen una presentacion profesional |
| Dashboard premium | En progreso alto | La home se simplifico, se redujeron KPIs duplicados y se mantuvieron solo los charts principales |
| Carrusel premium del dashboard | Ejecutado parcialmente | Se implemento un rail premium de alertas accionables usando `shellNotifications`, sin backend paralelo |
| Automatizacion definitiva pagos/gastos | Ejecutado | `FinancialAutomationService` centraliza vencidos y recurrencias; tiempo real via `OperationalClockService` |
| Navegacion y UX premium | En progreso | Fila clicable, shell premium, motion controlado y menos botones redundantes; queda QA transversal |
| Revision total de bugs y limpieza | En progreso alto | Limpieza MySQL aplicada, scripts legacy retirados, auth UI pulida y smoke real ADMIN completado; sigue pendiente QA visual modulo a modulo |
| Dark mode premium | En progreso | Existe y es funcional; queda mejora fina en algunos modulos secundarios |
| Cierre definitivo del producto | Pendiente | Debe hacerse al final, tras QA y pulido final |

## Ejecucion realizada en esta iteracion

### Limpieza definitiva 2026-04-26

- Backup previo creado en `tmp/db-backups/flacofitness-cleanup-20260426-195946.sql`.
- Scripts de precheck/cleanup/postcheck añadidos en `docs/db`.
- Tablas legacy eliminadas: `app_clock_settings`, `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`.
- Columnas legacy eliminadas de `gastos` y `reservas_sesion` tras migrar datos utiles.
- Scripts SQL antiguos `src/main/resources/data.sql`, `docs/data.sql` y `docs/init.sql` retirados.
- Login y cambio de password tienen mostrar/ocultar contraseña.
- Validacion cerrada: compile, 35 tests, postcheck MySQL y smoke HTTP ADMIN en rutas criticas.

### Finanzas

- Se mantienen y exponen en UI los filtros financieros completos de `/gastos`:
  - fecha
  - categoria
  - estado
  - tipo
  - staff
  - maquina
  - material
  - proveedor
  - recurrente
- Se amplian los KPIs del listado de gastos:
  - gasto fijo del mes
  - gasto variable del mes
  - vencimientos proximos
- Se mantiene la exportacion PDF individual para detalle de gasto.
- Se rehace la ficha de nomina con una jerarquia mas profesional:
  - hero salarial
  - neto destacado
  - desglose economico claro
  - trazabilidad con staff y gasto vinculado
- Se profesionaliza el PDF individual de nomina y el PDF de listado para demo financiera.

### Dashboard

- Se simplifica `home/index.html` para dejar solo el set principal de KPIs:
  - usuarios activos
  - pagos pendientes
  - ingresos del mes
  - gastos del mes
  - beneficio estimado
  - renovaciones proximas
  - trials pendientes
  - maquinas fuera de servicio
  - stock bajo
- Se elimina la duplicidad visual de bloques KPI y la grafica secundaria de altas.
- Se mantienen solo tres charts principales:
  - ingresos vs gastos
  - usuarios por plan
  - asistencias recientes
- Se implementa un rail premium de alertas dentro del dashboard apoyandose en `ShellNotificationService` y `shellNotifications`, con navegacion horizontal ligera y sin nuevas librerias.
- Se amplia `DashboardStatsResponse` para incluir `maquinasFueraServicio`, permitiendo refresco coherente del KPI desde `/stats/dashboard`.

### Preparacion para Claude Code

- Se mantiene `CLAUDE.md` en la raiz del repositorio con reglas vivas y orden de lectura.
- Este documento queda como traductor entre el archivo de `.claude` y el estado real del proyecto.

## Siguiente orden recomendado para Claude

1. Cerrar smoke real en navegador con datos MySQL de:
   - reset password -> login temporal -> cambiar password
   - crear/editar usuario con foto
   - crear trial -> convertir
   - crear nomina -> emitir -> pagar
   - crear maquina/material con coste -> comprobar gasto
2. Cerrar QA visual real de:
   - `/`
   - `/gastos`
   - `/gastos/recurrentes`
   - `/nominas`
   - `/usuarios/nuevo`
   - `/trials/nuevo`
3. Extender el patron de navegacion y pulido visual a modulos pendientes:
   - staff
   - membresias
   - clases
   - sesiones
   - maquinas
   - materiales
4. Hacer una pasada final de limpieza:
   - textos con mojibake
   - botones redundantes
   - templates financieros con copy inconsistente
5. Ejecutar la auditoria final de producto antes del cierre total.

## Reglas heredadas del `.claude`

- MySQL unicamente.
- Nada de H2.
- Nada de SPA.
- Mantener MVC con Thymeleaf.
- Validar compile/tests tras cambios relevantes.
- Actualizar siempre `docs/agents-memory`.
