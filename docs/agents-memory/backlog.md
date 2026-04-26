# Backlog

## Prioridad alta

- Arrancar la app con MySQL local disponible y validar smoke real de:
  - dashboard premium
  - login con email/username + password
  - cambio de password
  - reset temporal por admin
  - builder de nomina, emision y PDF
  - busqueda global
  - FAB por perfil
  - ultimos visitados
  - actividad reciente
  - tooltips first-use
- Ejecutar QA visual profunda por modulo sobre MySQL real: `/rutinas`, `/asistencias`, `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/maquinas`, `/materiales` y `/cliente`.
- Verificar smoke por perfiles reales (`ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE`, `CLIENTE`) incluyendo accesos denegados, redirecciones y visibilidad de sidebar.
- Ejecutar QA visual especifica del bloque financiero tras el nuevo builder de nominas, el detalle premium y el PDF individual.
- Revisar dark mode modulo por modulo con foco en login, formularios, tablas, dropdowns y charts.
- Revisar notificaciones restantes para que todas apunten a detalle o listado filtrado, no a modulos genericos.
- Anadir tests dedicados para `ProductIntelligenceService`, `GlobalSearchService`, truncado de `RecentVisitService` y flujo de `NominaService`.

## Prioridad media

- Crear tests unitarios adicionales para reglas de `AccessProfile`, `ShellNotificationService` y `ActivityLogService`.
- Documentar un flujo de defensa claro: cuenta -> usuario -> membresia -> pago -> sesion -> asistencia -> nomina/gasto.
- Revisar si `spring.profiles.active` debe quedar por defecto en `local` o moverse a variable de entorno para despliegue real.
- Persistir historial de notificaciones importantes en backend para auditar alertas vistas/no vistas por usuario.
- Ampliar busqueda global v1 a `gastos`, `nominas`, `maquinas` y `materiales` solo si la primera version demuestra uso real.
- Refinar aun mas el builder de nominas con avatar del staff o selector enriquecido si no rompe simplicidad MVC.

## Prioridad baja

- Evaluar migracion de la auth custom actual a Spring Security solo si el proyecto necesita hardening comercial real.
- Preparar exportacion CSV de pagos, asistencias, trials y sesiones.
- Mover todas las librerias frontend a recursos locales o WebJars para demos sin internet.
- Crear diagramas ER y diagrama de arquitectura en `docs/diagrams/`.
- Evaluar multi-gimnasio/multi-tenant solo si aparece un caso comercial real.
- Revisar codificacion de algunos markdown antiguos para eliminar restos de mojibake.

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

## COMPLETADO 2026-04-23 (coherencia UX y navegacion)

- [x] Redirecciones post-accion mejoradas para volver a detalle en `staff`, `materiales`, `maquinas`, `membresias`, `pagos`, `gastos` y `recurrentes`.
- [x] Fichas secundarias enriquecidas: `staff/detail`, `maquinas/detail`, `materiales/detail`, `membresias/detail` y `pagos/detail`.
- [x] Botones redundantes reducidos en listados con fila clicable (`staff`, `membresias`, `gastos`, `recurrentes`, `pagos`).
- [x] Notificaciones mas contextuales para usuarios inactivos, renovaciones, gastos criticos, recurrentes y maquinas fuera de servicio.
- [x] `ViewControllerTest` ampliado para cubrir detalles de `staff`, `maquinas` y `materiales`.
- [x] Limpieza de copy visible y sidebar con textos coherentes en ASCII.

## COMPLETADO 2026-04-23 (producto premium e inteligencia)

- [x] `ProductIntelligenceService` creado para clasificar usuarios, detectar membresias por caducar y marcar gastos anomalos.
- [x] `DashboardStatsResponse` ampliado con panel `Requiere atencion`.
- [x] Busqueda global v1 creada con pagina `/busqueda` y endpoint `/api/busqueda/global`.
- [x] FAB global por perfil anadido al shell sin romper MVC ni roles actuales.
- [x] `RecentVisitService` y persistencia `recent_visit` integrados en topbar y dashboard.
- [x] `UxMemoryStateService` y persistencia `ux_memory_state` integrados para tooltips y onboarding.
- [x] `ActivityLogService` y persistencia `activity_log` integrados en dashboard, usuario y staff.
- [x] Tooltips first-use anadidos a dashboard, usuarios, pagos, membresias, sesiones, trials, staff, gastos, maquinas y materiales.
- [x] Filtros persistentes en navegador aplicados a listados prioritarios.

## COMPLETADO 2026-04-23 (cierre profesional SaaS)

- [x] Dead code eliminado: `fragments/navbar.html` confirmado como huerfano con grep y borrado.
- [x] `ViewControllerTest` ampliado con `adminPuedeVerListadoGastos` y `adminPuedeVerDetalleGasto`; cubre regresion EL1008E de `gastos/detail.html`.
- [x] Sistema de KPI cards unificado: `pagos/list.html`, `gastos/list.html`, `nominas/list.html` migrados a `.ff-kpi-card` con variantes semanticas `ff-kpi-positive/warning/danger/neutral`.
- [x] CSS: variantes semanticas de KPI cards anadidas a `styles.css` con dark mode overrides. `.ff-filter-panel` anadido para envolver filtros en los modulos financieros.
- [x] Status badges normalizados: `pagos/list.html` usa `.ff-status-badge` en lugar de clases Bootstrap inline; `nominas/list.html` corregido de `ff-status-warning` a `ff-status-pending`.
- [x] Beneficio estimado del dashboard: `data-kpi-profit` anadido con `th:attr` en `home/index.html`; `updateProfitCardColor()` en `dashboard.js` actualiza el color en cada refresco de datos.
- [x] Dark mode para Chart.js: `getChartColors()` anadida a `dashboard.js`; `renderPlanChart`, `renderIngresosGastosChart` y `renderAsistenciasChart` usan colores del tema en lugar de hex hardcodeados.
- [x] N+1 eliminado en `PagoService`: tres queries JPQL de agregacion anadidas a `PagoRepository`; `contarUsuariosAlDia/ConDeuda/ConPagosVencidos` reemplazados con llamadas directas al repositorio.
- [x] Trial->MembresiaUsuario: `TrialService.convertirAUsuario()` auto-crea `MembresiaUsuario` cuando el trial se convierte a un usuario nuevo con plan asignado y sin membresia activa previa.
- [x] Tests: 21 en verde.

## COMPLETADO 2026-04-24 (auth por cuenta + nominas)

- [x] Acceso compartido por PIN sustituido por autenticacion por cuenta con `email/username + password`.
- [x] `Usuario` ampliado con `username`, `passwordHash` y `mustChangePassword`.
- [x] Hash seguro con BCrypt via `PasswordEncoder`.
- [x] Cambio de password implementado en `/cuenta/password`.
- [x] Reset temporal por admin implementado desde `UsuarioController`.
- [x] Backfill de credenciales legacy con `AuthBootstrapRunner`.
- [x] `NominaService` ampliado con flujo `BORRADOR -> EMITIDA -> PAGADA/CANCELADA`.
- [x] Builder premium de nomina con preview en vivo y detalle/PDF mas profesional.

## COMPLETADO 2026-04-26 (estabilizacion premium)

- [x] Busqueda global v2 ligera: usuarios, staff y sesiones usan queries limitadas de repositorio en lugar de `listarTodos().stream()`.
- [x] Contrato `/api/busqueda/global` mantenido sin cambios y protegido con `GlobalSearchServiceTest`.
- [x] Dashboard, stats y PDF de gastos reducen calculos repetidos de ingresos/gastos mensuales.
- [x] `StaffController` evita cargar dos veces el listado de staff.
- [x] `nominas/list.html` incorpora filtros persistentes, copy mas limpio y empty state accionable.
- [x] Contadores del dashboard suavizados para una animacion menos brusca.
- [x] `cookies.txt` tratado como artefacto local e ignorado por Git.
- [x] Validacion final: compile, tests, MySQL y smoke HTTP basico con app temporal en `8081`.

## COMPLETADO 2026-04-26 (Command Center UI)

- [x] Dark mode convertido en experiencia principal por defecto sin eliminar light mode.
- [x] Nuevas fuentes: `Space Grotesk` para display/KPIs y `IBM Plex Sans` para UI.
- [x] Capa CSS Command Center con paleta tactica, glassmorphism, HUD cards, grids apilados, tablas, formularios y empty states.
- [x] Pantallas clave marcadas con `ff-command-stack`: dashboard, usuarios, cliente, finanzas, asistencias, staff, rutinas y nominas.
- [x] Anime.js local añadido y `hud-motion.js` implementado con fallback seguro y respeto de reduced motion.
- [x] Smoke de assets nuevos y rutas principales completado en puerto temporal `8082`.
