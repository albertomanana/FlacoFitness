# Module Status

Fecha de referencia: 2026-04-22

## Resumen

La aplicacion ya no esta en fase de esqueleto. La mayoria de modulos existen, cargan HTML real y tienen al menos flujo operativo basico. Lo que falta ahora es consolidacion, QA visual completa y cierre fino.

## Estado por modulo

| Modulo | Estado | Backend | UI | Riesgo actual | Nota |
| --- | --- | --- | --- | --- | --- |
| Dashboard | Operativo | OK | OK | Bajo | Dashboard simplificado con rail de alertas y trio principal de charts; QA visual parcial |
| Acceso PIN | Operativo | OK | OK | Bajo | PIN por defecto `2468` |
| Usuarios | Operativo | OK | OK | Bajo | Validado visualmente tras fix de pantalla en blanco |
| Rutinas | Operativo | OK | Pendiente QA profunda | Medio | Many-to-many con usuarios y staff opcional |
| Pagos | Operativo | OK | OK | Bajo | Validado visualmente; N+1 en KPIs de usuarios (academico, no critico) |
| Asistencias | Operativo | OK | Pendiente QA profunda | Medio | Check-in libre y por sesion |
| Staff | Operativo | OK | Pendiente QA profunda | Medio | Ligado a Usuario por `StaffPerfil` |
| Membresias | Operativo | OK | Pendiente QA profunda | Medio | Catalogo + contrato real |
| Trials | Operativo | OK | Pendiente QA profunda | Medio | Flujo comercial basico con filtros desde/hasta |
| Clases | Operativo | OK | Pendiente QA profunda | Medio | Catalogo de actividad |
| Sesiones | Operativo | OK | Pendiente QA profunda | Medio | Horario, cupo, staff, rutina opcional |
| Gastos | Operativo | OK | OK | Bajo | Bug de `gasto.frecuencia` corregido y filtros financieros completos expuestos en UI (2026-04-22) |
| Recurrentes | Operativo | OK | OK | Bajo | Plantillas de cargos; scheduler now configurable via properties |
| Nominas | Operativo | OK | OK | Bajo | Detalle premium y PDF individual/listado validados |
| Maquinas | Operativo | OK | Pendiente QA profunda | Medio | Integrado con gastos |
| Materiales | Operativo | OK | Pendiente QA profunda | Medio | Integrado con gastos |
| Cliente | Operativo | OK | Pendiente QA profunda | Medio | Panel limitado con reservas activas e historial de asistencias |
| Notificaciones | Operativo | OK | OK | Bajo | Probado por bloques; cubre todos los modulos financieros |

## Progreso del plan `.claude`

- `CLAUDE.md` ya existe en raiz.
- `claude-plan-status.md` registra que partes del archivo de `.claude` ya se absorbieron.
- La ejecucion ya aterrizo dos bloques concretos:
  - finanzas mas profesionales en gastos/nominas
  - dashboard premium simplificado con rail de alertas accionables

## Estado transversal

### Shell visual

- Sidebar, topbar, footer y shell premium: activos.
- Modo oscuro: activo.
- Splash/transiciones: activas, con fail-safe agregado el 2026-04-22.
- Dashboard principal: menos ruido visual, KPIs principales y rail horizontal de alertas conectado a `shellNotifications`.

### Finanzas

- Pagos automaticos: activos.
- Gastos recurrentes: activos.
- Nominas: activas.
- PDFs: implementados.
- Scheduler configurable via `app.pagos.scheduler.enabled` y `app.pagos.scheduler.cron`.
- Nominas: detalle y PDFs con presentacion mas seria para demo y archivo interno.

### Tiempo

- Tiempo de negocio: tiempo real del sistema a traves de `OperationalClockService`.
- No hay simulador persistido activo.

### Persistencia

- Solo MySQL real.
- No H2.
- No seeds automaticos.

## Orden recomendado de QA visual

1. `/`
2. `/acceso`
3. `/usuarios`
4. `/pagos`
5. `/rutinas`
6. `/asistencias`
7. `/staff`
8. `/membresias`
9. `/trials`
10. `/clases`
11. `/sesiones`
12. `/gastos`
13. `/gastos/recurrentes`
14. `/nominas`
15. `/maquinas`
16. `/materiales`
17. `/cliente`
