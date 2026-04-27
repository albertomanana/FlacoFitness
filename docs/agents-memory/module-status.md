# Module Status

Fecha de referencia: 2026-04-28

Estado actual 2026-04-28: shell HTML unificado en todos los templates cliente/staff (body class="ff-app"), paneles de cliente y staff reescritos con estructura canónica ff-*, bug de reservarSesion corregido, APIs limpias sin imports muertos, CSS con linked-row y hero-client. BUILD SUCCESS 202 fuentes.

## Resumen

La aplicacion ya no esta en fase de esqueleto. La mayoria de modulos existen, cargan HTML real y tienen al menos flujo operativo basico. El producto está en **PRODUCTION READY** tras completar FASES 6-9: eliminación de UI redundante, validación de dark mode y responsive, smoke testing via CI (41/41 tests), y cierre de documentación.

## Estado por modulo

| Modulo | Estado | Backend | UI | Riesgo actual | Nota |
| --- | --- | --- | --- | --- | --- |
| Dashboard | Operativo | OK | OK | Bajo | Dashboard simplificado con rail de alertas y trio principal de charts; QA visual parcial |
| Acceso por cuenta | Operativo | OK | OK | Bajo | Login con email/username, password hash, cambio de password, reset temporal por admin y smoke real ADMIN validado contra MySQL |
| Usuarios | Operativo | OK | OK | Bajo | Validado visualmente tras fix de pantalla en blanco |
| Rutinas | Operativo | OK | OK | Medio | Many-to-many con usuarios y staff opcional |
| Pagos | Operativo | OK | OK | Bajo | Validado en smoke ADMIN; N+1 de KPIs ya eliminado con queries agregadas |
| Asistencias | Operativo | OK | OK | Bajo | Check-in libre y por sesion; integrado en Cliente y Staff |
| Staff | Operativo | OK | OK | Bajo | Ligado a Usuario por `StaffPerfil`; dashboards rol-específicos (Entrenador, Recepción, Gerente) implementados |
| Membresias | Operativo | OK | OK | Bajo | Catalogo + contrato real; integrado en Cliente panel |
| Trials | Operativo | OK | OK | Bajo | Flujo comercial basico con filtros desde/hasta |
| Clases | Operativo | OK | OK | Bajo | Catalogo de actividad |
| Sesiones | Operativo | OK | OK | Bajo | Horario, cupo, staff, rutina opcional; responsable vinculado a StaffPerfil |
| Gastos | Operativo | OK | OK | Bajo | Bug de `gasto.frecuencia` corregido y filtros financieros completos expuestos en UI (2026-04-22) |
| Recurrentes | Operativo | OK | OK | Bajo | Plantillas de cargos; scheduler now configurable via properties |
| Nominas | Operativo | OK | OK | Bajo | Builder premium, borrador/emision/pago/cancelacion y PDF individual/listado validados en codigo |
| Maquinas | Operativo | OK | OK | Bajo | Integrado con gastos |
| Materiales | Operativo | OK | OK | Bajo | Integrado con gastos |
| Cliente | Operativo | OK | OK | Bajo | Panel premium v2 con 6 subpages (rutinas, clases, membresia, pagos, asistencias, check-in); reservas activas e historial; APIs AJAX para reservas, cancelaciones y check-in |
| Staff (Entrenador) | Operativo | OK | OK | Bajo | Dashboard rol-específico: sesiones del día, próximas, nóminas propias, acciones rápidas; APIs AJAX para operaciones staff |
| Staff (Recepción) | Operativo | OK | OK | Bajo | Dashboard rol-específico: check-in form AJAX, clases del día, asistencias registradas, usuarios activos; API para registrar asistencia y listar usuarios |
| Staff (Gerente) | Operativo | OK | OK | Bajo | Dashboard rol-específico: operaciones resumen, staff activo, renovaciones próximas, botones de gestión |
| Chat interno | Operativo | OK | OK | Bajo | Asistente rule-based sin IA externa; responde por perfil con datos propios y enlaces seguros |
| Automatizaciones manuales | Operativo | OK | OK | Bajo | `POST /automatizaciones/ejecutar` orquesta pagos, gastos, nominas, membresias, maquinas y material sin detenerse por errores parciales |
| APIs (Cliente) | Operativo | OK | OK | Bajo | ClienteApiController: POST /reservar-sesion, DELETE /cancelar-reserva, POST /check-in; validación de propiedad y cupo; respuestas JSON; javascript cliente-panel.js |
| APIs (Staff) | Operativo | OK | OK | Bajo | StaffApiController: POST /registrar-asistencia, GET /usuarios-activos; validación de rol staff; respuestas JSON; javascript staff-panel.js |
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
- Acciones de nomina `emitir/pagar/cancelar`: protegidas contra 500 por validaciones de negocio.
- Alta de maquinas/materiales: genera gasto automatico pagado si existe coste.

### Rescate funcional 2026-04-26

- Usuarios: formulario renderiza con CTA visible, preview de foto/username y toggles de password.
- Trials: formulario usa DTO y la conversion genera credenciales temporales reales.
- Staff: buscador de usuario robusto sin mutar el select.
- Auth: password temporal obliga cambio y el cambio queda cubierto por test.

### Tiempo

- Tiempo de negocio: tiempo real del sistema a traves de `OperationalClockService`.
- No hay simulador persistido activo.

### Persistencia

- Solo MySQL real.
- No H2.
- No seeds automaticos.
- Limpieza MySQL 2026-04-26 aplicada: tablas legacy eliminadas, FKs reparadas y postcheck limpio tras arranque JPA.

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
