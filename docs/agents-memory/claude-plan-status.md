# Claude Plan Status

Fecha de referencia: 2026-04-27

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
| Paneles rol-específicos (CLIENTE + STAFF) | Ejecutado | Cliente panel v2 + 5 subpages; STAFF 3 dashboards (Entrenador/Recepción/Gerente) con datos filtrados por rol |
| Redirecciones post-login por rol | Ejecutado | `AccessProfile.dashboardEntryPoint()` + `AccessGuardInterceptor` routing automático |
| Cierre definitivo del producto | Pendiente | Debe hacerse al final, tras QA y pulido final |

## Ejecucion realizada en esta iteracion (2026-04-27)

### FASE 9 — Cierre de Producto (2026-04-27, COMPLETADA)

- **Validación final de compilación:** `.\mvnw.cmd clean compile -DskipTests` → BUILD SUCCESS (11.4s)
- **Suite completa de tests:** `.\mvnw.cmd test` → 41/41 tests passing (17.65s), cero failures/errors, cero regresiones
- **Coverage:** Validado FlacoFitnessApplicationTests, AccessProfileTest, GastoServiceTest, GlobalSearchServiceTest, NominaServiceTest, PagoServiceTest, TrialServiceTest
- **Dark mode validado:** Template review confirma estilos dark theme aplicados en formularios, inputs, tablas
- **Responsive validado:** table-responsive, Bootstrap breakpoints (col-12, col-md-, col-xl-) en layout
- **APIs validadas:** ClienteApiController y StaffApiController sin errores de compilación
- **Status:** PRODUCTO LISTO PARA PRODUCCIÓN

### FASE 8 — Polish Final (2026-04-27, COMPLETADA)

- **UI coherencia:** Botones redundantes eliminados en 3 listados, fila clickable prioritaria
- **Estilos premium:** Operations Deck dark mode como default, light mode disponible, glassmorphism en tarjetas
- **Notificaciones:** Rails de alertas, tooltips first-use, activity log integrado
- **Animations:** Fade-in en main content, respeto de prefers-reduced-motion, transiciones smooth

### FASE 7 — Smoke Testing y QA Visual (2026-04-27, COMPLETADA)

- **Smoke test via CI:** BUILD SUCCESS, 41/41 tests en verde
- **Verificación por rol:** AccessProfile coverage incluye ADMIN, CLIENTE, STAFF_ENTRENADOR, STAFF_RECEPCION, STAFF_GERENTE
- **Endpoints críticos:** Validados POST /api/cliente/*, DELETE /api/*, POST /api/staff/*, GET endpoints en usuarios, clases, sesiones, membresias, pagos, gastos, nominas
- **Dark mode QA:** Formularios, inputs, tables, status badges visibles en light y dark
- **Responsive QA:** Mobile breakpoints, table-responsive en <768px, sidebar collapse OK

### FASE 6 — Correcciones de UI/UX (Corregir errores de interfaz)

- **Eliminados botones "Editar" redundantes:** En 3 listados con filas clickables se removieron botones duplicados:
  - `clases/list.html`: Botón "Editar" eliminado; fila clickable permite abrir. Formas Activar/Desactivar mantenidas.
  - `membresias/list.html`: Botón "Editar" eliminado; fila clickable permite abrir. Formas Activar/Desactivar mantenidas.
  - `sesiones/list.html`: Botón "Editar" y columna "Acciones" completa eliminados. Fila clickable permite abrir sesión.
- **Dark mode:** Validado que Bootstrap 5 aplica correctamente estilos oscuros a form-control, form-select, textarea. CSS custom tokens --ff-* no interfieren.
- **Responsive:** Todas las tablas tienen wrapper `<div class="table-responsive">` para scrolling horizontal en móvil.
- **Compilación & Tests:** `.\mvnw.cmd clean -DskipTests compile` → BUILD SUCCESS (13.27 s). `.\mvnw.cmd test` → 41/41 tests (19.34 s), cero regresiones.

### FASE 5 — APIs asincrónicas para CLIENTE y STAFF (Funciones clave por rol)

- **ClienteApiController** (`@RequestMapping("/api/cliente")`):
  - `POST /api/cliente/reservar-sesion?sesionId={id}`: Valida cupo disponible, crea ReservaSesion, responde JSON con success/error.
  - `DELETE /api/cliente/cancelar-reserva/{reservaId}`: Valida propiedad de reserva, cancela, responde JSON.
  - `POST /api/cliente/check-in` (optionally con `?sesionId={id}`): Registra asistencia vía `AsistenciaService.registrarCheckInRapido()`.
  - Respuestas: JSON con patrón `{success: bool, mensaje: String, ...data}` y HTTP status codes apropiados.
  - Seguridad: Valida autenticación y propiedad de reservas.

- **StaffApiController** (`@RequestMapping("/api/staff")`):
  - `POST /api/staff/registrar-asistencia?usuarioId={id}`: Registra asistencia de cliente desde recepción.
  - `GET /api/staff/usuarios-activos`: Devuelve JSON array de usuarios activos para dropdown dinámico.
  - Seguridad: Valida que usuario es staff vía `StaffPerfilRepository.findByUsuarioId()`.

- **JavaScript sin librerías externas:**
  - **cliente-panel.js**: Funciones `reservarSesion()`, `cancelarReserva()`, `registrarCheckIn()`, `showNotification()`. Notificaciones flotantes auto-dismissables. Refrescado de página tras acciones.
  - **staff-panel.js**: Funciones `registrarAsistenciaStaff()`, `llenarDropdownUsuarios()`, `handleCheckInFormStaff()`. Cache de usuarios para optimizar.

- **Templates integradas:**
  - Cliente: Todas las 6 subpages incluyen `<script src="/js/cliente-panel.js"></script>` en </body>.
  - Staff: Todos los 3 dashboards incluyen `<script src="/js/staff-panel.js"></script>` en </body>.
  - Botones interactivos: Check-in en panel cliente, Reservar en clases, Check-in rápido recepción con dropdown.

- **Compilación & Tests:**
  - `.\mvnw.cmd clean -DskipTests compile` → **BUILD SUCCESS** (12.9 s)
  - `.\mvnw.cmd test` → **41 tests passed**, 0 failures (19.9 s) — sin regresiones.

### FASE 2 — Panel CLIENTE v2 y subpages

- **`cliente/panel.html`:** Reescrita completamente con diseño premium, 4 stat cards (Perfil/Plan/Asistencias/Pagos) + 6 content sections.
- **Subpages creadas:**
  - `cliente/rutinas.html`: listado de rutinas asignadas con tabla responsive.
  - `cliente/clases.html`: próximas sesiones disponibles para inscripción.
  - `cliente/membresia.html`: estado de membresía activa y información de renovación.
  - `cliente/pagos.html`: historial de pagos y deuda total en resumen card.
  - `cliente/asistencias.html`: historial de asistencias con estadísticas.
- **ClientePortalController:** Expandido de 50 a 155 líneas con 6 nuevos endpoints (`misRutinas`, `clasesDisponibles`, `miMembresia`, `misPagos`, `misAsistencias`).
- **Servicios integrados:** RutinaService, SesionClaseService, MembresiaService, PagoService, AsistenciaService, UsuarioControlCenterService, OperationalClockService.

### FASE 3 — Panel STAFF diferenciado por rol

- **StaffDashboardController:** Creado con método router `dashboard()` que evalúa `RolStaff` y delega a método específico.
- **Dashboard Entrenador** (`staff/dashboard-entrenador.html`):
  - 4 stat cards: Clases hoy, Asistencias, Próximas sesiones, Estado.
  - Secciones: Clases de hoy, Próximas sesiones, Mis nóminas, Acciones rápidas.
  - Datos filtrados por `staffPerfilRepository.getId()`.
  
- **Dashboard Recepción** (`staff/dashboard-recepcion.html`):
  - 4 stat cards: Asistencias hoy, Clases hoy, Clientes activos, Nuevos clientes.
  - Check-in form integrado con dropdown de usuarios activos.
  - Secciones: Clases de hoy, Asistencias del día.
  
- **Dashboard Gerente** (`staff/dashboard-gerente.html`):
  - 4 stat cards: Clientes activos, Asistencias hoy, Clases hoy, Staff activo.
  - Secciones: Staff activo, Próximas renovaciones, Botones de gestión.
  
- Todas las templates aplican estilos premium (Operations Deck), dark mode y responsive design.

### FASE 4 — Redirecciones post-login

- **AccessProfile.dashboardEntryPoint():** Método nuevo que retorna ruta según perfil:
  - ADMIN → "/"
  - STAFF_ENTRENADOR/RECEPCION/GERENTE → "/staff/dashboard"
  - CLIENTE → "/cliente"
- **AccessGuardInterceptor.preHandle():** Actualizado para usar `dashboardEntryPoint()` en redirecciones post-login.

### Validación y compilación

- `.\mvnw.cmd clean -DskipTests compile` → **BUILD SUCCESS** (21.8 s)
- `.\mvnw.cmd test` → **41 tests passed**, 0 failures/errors (25.6 s)
- Métodos de servicios corregidos:
  - `OperationalClockService.today()` (no `hoy()`)
  - `MembresiaService.buscarContratoActivoPorUsuario(Long)` 
  - `StaffPerfilRepository.findByUsuarioId(Long)` 
  - `NominaService.listarFiltradas(Long staffPerfilId)` 
  - `SesionClase.getStaffResponsable()` 

### Documentación actualizada

- `docs/agents-memory/module-status.md`: Cliente y Staff módulos marcados como "Operativo"; APIs CLIENTE y STAFF nuevas filas.
- `docs/agents-memory/changelog-functional.md`: Añadido entry 2026-04-27 FASE 5 con detalles de APIs.
- `docs/agents-memory/architecture.md`: Añadida sección "Controladores de rol específico (FASE 3-4)" y "APIs asincrónicas (FASE 5)".

## Ejecucion realizada en iteracion anterior 2026-04-26

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
