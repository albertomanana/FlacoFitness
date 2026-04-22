# Backlog

## Prioridad alta

- Ejecutar QA visual profunda por modulo sobre MySQL real: `/rutinas`, `/asistencias`, `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/maquinas`, `/materiales` y `/cliente`.
- Verificar smoke por perfiles reales (`ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE`, `CLIENTE`) incluyendo accesos denegados, redirecciones y visibilidad de sidebar.
- Cerrar detalles visuales menores del shell premium en vistas secundarias y formularios legacy.
- Ejecutar QA visual especifica del bloque financiero tras la nueva presentacion de nominas y el rail de alertas del dashboard.
- Extender el patron de navegacion natural y pulido visual a `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/maquinas` y `/materiales`.

## COMPLETADO 2026-04-22 (bloque financiero)

- [x] Bug critico corregido: `gastos/detail.html` accedia a `gasto.frecuencia` que no existe en `Gasto`; corregido a `gasto.gastoRecurrente.frecuencia` con null-guards correctos.
- [x] `SaaSSchedulerService` ahora usa `${app.pagos.scheduler.cron}` y `${app.pagos.scheduler.enabled}` desde properties en lugar de cron hardcodeado.
- [x] QA de codigo completa del bloque financiero: GastoService, PagoService, NominaService, GastoRecurrenteService, FinancialAutomationService, RecurrenceService, ShellNotificationService auditados sin bugs adicionales.
- [x] AccessProfile.canAccessAsManager validado: cubre `/gastos`, `/nominas`, `/pagos`, `/maquinas`, `/materiales`.
- [x] StatsController validado: `/stats/dashboard` y `/stats/gastos` cubren todas las metricas financieras correctamente.
- [x] Templates nominas/ y gastos/recurrentes/ validados visualmente sin errores de propiedad.
- [x] `CLAUDE.md` creado en raiz con reglas del proyecto y orden de lectura para Claude Code.
- [x] `docs/agents-memory/claude-plan-status.md` creado para traducir el archivo `.claude` a estado real ejecutado.
- [x] `/gastos` actualizado para usar filtros financieros completos ya soportados por backend.
- [x] Exportacion PDF individual anadida para detalle de gasto.
- [x] Dashboard simplificado con set corto de KPIs y rail premium de alertas accionables.
- [x] Grafica secundaria de altas retirada para dejar solo los tres charts principales del dashboard.
- [x] Detalle de nomina rehecho como expediente salarial y PDF individual/listado reforzados.

## COMPLETADO 2026-04-22 (bloques 1-4 anteriores)

- [x] Git limpio: tree commiteado, stash pre-recovery eliminado (era estado incompleto con root/sin-pass).
- [x] `PagoSchedulerService` deprecated eliminado.
- [x] Proyecciones JPA (*PorMesView, *PorPlanView, GastoPorCategoriaView) movidas de `repository/` a `model/dto/`.
- [x] Trial: filtro por rango de fechas (desde/hasta) anadido en controller, service, repo y template.
- [x] `AccessProfile.canAccessAsManager` corregido (indentacion de /nominas).
- [x] `UsuarioService.guardar()` cierra automaticamente trials pendientes con el mismo email.
- [x] Panel Cliente ampliado con seccion "Reservas activas" e "Historial de asistencias".

## Prioridad media

- Anadir pruebas MVC para rutas protegidas por perfil y render de las pantallas principales incluyendo gastos/detail.
- Optimizar N+1 en `PagoService.contarUsuariosAlDia/ConDeuda/ConPagosVencidos`: actualmente carga todos los usuarios y ejecuta 2 queries por usuario; reemplazar con queries JPQL de agregacion directa.
- Crear tests unitarios adicionales para reglas de `AccessProfile` y `ShellNotificationService`.
- Mejorar el panel cliente para resolver el usuario real de sesion cuando exista autenticacion formal.
- Documentar un flujo de defensa claro: trial -> usuario -> membresia -> pago -> sesion -> asistencia.
- Revisar si `spring.profiles.active` debe quedar por defecto en `local` o moverse a variable de entorno para despliegue real.
- Persistir historial de notificaciones importantes en backend para auditar alertas vistas/no vistas por usuario.

## Prioridad baja

- Migrar el PIN MVP a Spring Security cuando el proyecto deje de ser academico.
- Preparar exportacion CSV de pagos, asistencias, trials y sesiones.
- Mover todas las librerias frontend a recursos locales o WebJars para demos sin internet.
- Crear diagramas ER y diagrama de arquitectura en `docs/diagrams/`.
- Evaluar multi-gimnasio/multi-tenant solo si aparece un caso comercial real.
- Revisar codificacion de algunos markdown antiguos para eliminar restos de mojibake.
