# Claude Code Handoff

Fecha de referencia: 2026-04-26

Este documento existe para que Claude Code o cualquier otro agente pueda continuar el trabajo sin perder contexto ni romper el sistema.

## 1. Punto de entrada obligatorio

Antes de tocar codigo:

1. Leer este archivo completo.
2. Leer `agent-working-rules.md`.
3. Leer `architecture.md`, `domain-model.md` y `setup-status.md`.
4. Ejecutar `git status --short`.
5. Ejecutar `.\mvnw.cmd clean -DskipTests compile`.
6. Ejecutar `.\mvnw.cmd test`.

## 2. Reglas no negociables

- Base de datos unica: MySQL / phpMyAdmin, base `flacofitness`.
- No usar H2 ni introducir fallback en memoria.
- Mantener arquitectura MVC monolitica.
- Mantener Thymeleaf + Bootstrap + JS ligero.
- No convertir el proyecto en SPA.
- No introducir React, Vue, Angular ni frameworks frontend pesados.
- No ejecutar seeds automaticamente en MySQL real.
- No borrar datos existentes ni tablas sin migracion justificada y backup previo.
- No reintroducir tablas legacy ya eliminadas: `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`, `app_clock_settings`.
- No reintroducir `src/main/resources/data.sql`.
- No hacer parches rapidos que mezclen logica de negocio en vistas o controladores.
- Toda mejora importante debe actualizar `docs/agents-memory/`.

## 3. Estado real del repositorio

- Rama activa: `recovery/restore-core-saas-plan-a`
- El working tree ya venia sucio antes de este handoff.
- Hay cambios tracked y untracked relacionados con el bloque SaaS y financiero.
- No hacer `reset --hard`.
- No borrar archivos no trackeados sin revisar si pertenecen al trabajo actual.

Recomendacion para Claude:

1. Ejecutar `git status --short`.
2. Si hace falta estabilizar, crear una rama nueva desde el estado actual.
3. Trabajar de forma incremental y validada.

## 4. Versiones y dependencias relevantes

- Spring Boot: `3.3.5`
- Java target: `17`
- Lombok: `1.18.44`
- MySQL driver: `mysql-connector-j`
- PDF: `openhtmltopdf-pdfbox 1.0.10`
- Frontend libs:
  - Bootstrap `5.3.3`
  - DataTables `2.3.7`
  - Chart.js local en `src/main/resources/static/vendor/chartjs/chart.umd.min.js`

## 5. Configuracion local activa

Archivos clave:

- `src/main/resources/application.properties`
- `src/main/resources/application-local.properties`

Valores importantes:

- `spring.profiles.active=local`
- `spring.datasource.url=jdbc:mysql://localhost:3306/flacofitness...`
- `spring.datasource.username=flaco_user`
- `spring.datasource.password=flaco_pass`
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.sql.init.mode=never`
- Cleanup MySQL vigente documentado en `docs/db/cleanup-2026-04-26-*.sql`.

## 6. Comandos de trabajo utiles

Build y tests:

```powershell
.\mvnw.cmd clean -DskipTests compile
.\mvnw.cmd test
```

Arranque local:

```powershell
.\mvnw.cmd spring-boot:run
```

Comprobacion de BD:

```powershell
mysql -h localhost -P 3306 -u flaco_user -pflaco_pass -D flacofitness
```

## 7. Acceso funcional de la app

- URL local: `http://localhost:8080`
- Acceso por cuenta:
  - login con `email` o `username`
  - admin local validado: `admin@flacofitness.local` / `FlacoAdmin2026!`
  - password temporal bootstrap para usuarios legacy: `FlacoTemp2026!`
  - `mustChangePassword=true` fuerza cambio de password al primer acceso bootstrap
- Perfiles disponibles:
  - `ADMIN`
  - `STAFF_ENTRENADOR`
  - `STAFF_RECEPCION`
  - `STAFF_GERENTE`
  - `CLIENTE`

## 8. Mapa rapido de modulos

Controladores principales:

- `UsuarioController`
- `RutinaController`
- `PagoController`
- `AsistenciaController`
- `StaffController`
- `MembresiaController`
- `TrialController`
- `ClaseController`
- `SesionClaseController`
- `GastoController`
- `GastoRecurrenteController`
- `NominaController`
- `MaquinaController`
- `MaterialController`
- `ClientePortalController`
- `StatsController`
- `AccessController`

Servicios clave:

- `OperationalClockService`
- `FinancialAutomationService`
- `RecurrenceService`
- `AccessSessionService`
- `AccessProfileResolver`
- `PagoService`
- `GastoService`
- `GastoRecurrenteService`
- `NominaService`
- `ShellNotificationService`
- `UsuarioControlCenterService`

## 9. Estado de autenticacion, tiempo y automatizacion

### Autenticacion

- Ya no existe acceso compartido por PIN.
- `Usuario` tiene `username`, `passwordHash` y `mustChangePassword`.
- `PasswordConfig` expone BCrypt.
- `AccessSessionService` autentica por email o username.
- `CuentaController` gestiona cambio de password.
- `UsuarioController` permite reset temporal por admin.
- `AuthBootstrapRunner` backfillea credenciales para usuarios legacy sin password hash.

### Estado actual

- Ya no existe simulacion manual persistida del reloj.
- `OperationalClockService` usa tiempo real del sistema.
- Sigue siendo la abstraccion oficial para reglas de negocio temporales.
- La tabla `app_clock_settings` fue eliminada de MySQL; no recrearla.

### Automatizacion financiera

`FinancialAutomationService` es la unica fachada permitida para:

- actualizar pagos vencidos
- actualizar gastos vencidos
- generar pagos
- generar gastos recurrentes
- generar nominas

No duplicar esta logica en:

- controladores
- schedulers paralelos
- vistas
- hooks ad hoc

`RecurrenceService` es el punto comun para calculo de siguientes ciclos.

## 9.1 Limpieza MySQL aplicada el 2026-04-26

- Backup previo: `tmp/db-backups/flacofitness-cleanup-20260426-195946.sql`.
- Scripts:
  - `docs/db/cleanup-2026-04-26-precheck.sql`
  - `docs/db/cleanup-2026-04-26.sql`
  - `docs/db/cleanup-2026-04-26-postcheck.sql`
- Tablas eliminadas: `app_clock_settings`, `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`.
- Columnas eliminadas de `gastos`: `monto`, `descripcion`, `pagado`, `recurrente`, `frecuencia`, `staff_id`.
- Columnas eliminadas de `reservas_sesion`: `sesion_clase_id`, `asistio`, `observaciones`.
- Postcheck limpio tras arrancar JPA: sin tablas legacy, sin FKs legacy y sin columnas legacy.

## 10. Estado de acceso y permisos

### ADMIN

- control total
- todos los modulos

### STAFF_ENTRENADOR

- clases
- sesiones
- rutinas
- asistencias
- lectura limitada de usuarios

### STAFF_RECEPCION

- usuarios
- membresias
- trials
- pagos operativos
- asistencias
- reservas/sesiones operativas

### STAFF_GERENTE

- gestion
- finanzas
- inventario
- vision global
- no imparte clases por defecto

### CLIENTE

- panel personal
- sus pagos
- sus rutinas
- su membresia
- sus asistencias visibles

## 11. Ultimos cambios importantes antes del handoff

### 2026-04-21

- Se centralizo la automatizacion financiera en `FinancialAutomationService`.
- Se unifico la recurrencia en `RecurrenceService`.
- Se anadio `PROGRAMADO` a pagos.
- Se alinearon pagos y gastos con una sola capa temporal.

### 2026-04-22 (bloques 1-4 — shell, reloj, filtros, panel cliente)

- Se retiro la simulacion manual del reloj y se dejo tiempo real de sistema.
- Se corrigio el bug de "modulos en blanco": el problema estaba en la shell visual, no en backend ni en Thymeleaf.
- Se anadio fail-safe en `app.js` y `styles.css` para que la splash o una transicion fallida no oculten `.ff-main`.
- Se validaron visualmente `usuarios` y `pagos` con Chrome headless despues del fix.
- `AccessProfile.canAccessAsManager` corregido (indentacion erronea en `/nominas`).
- `UsuarioService.guardar()` cierra automaticamente trials pendientes con el mismo email.
- Panel Cliente ampliado: seccion "Reservas activas" e "Historial de asistencias".
- Filtros por fecha (desde/hasta) anadidos en `/trials`.
- Proyecciones JPA movidas de `repository/` a `model/dto/`.
- `PagoSchedulerService` deprecated eliminado.

### 2026-04-22 (auditoria bloque financiero completo)

- **Bug critico corregido:** `gastos/detail.html` accedia a `gasto.frecuencia` que no existe en `Gasto`; causaba `EL1008E PropertyAccessException` (500) en cualquier `/gastos/{id}`. Corregido a `gasto.gastoRecurrente.frecuencia` con null-guards correctos. Se anadio ademas `tipoGasto`, `fechaVencimiento` y badge de estado con colores semanticos.
- **`SaaSSchedulerService` corregido:** el scheduler ignoraba `app.pagos.scheduler.enabled` y `app.pagos.scheduler.cron` de `application.properties`. Se inyectaron via `@Value` y el cron ahora usa SpEL `${app.pagos.scheduler.cron:0 0 0 * * *}`.
- Auditoria completa sin bugs adicionales: `GastoService`, `PagoService`, `NominaService`, `GastoRecurrenteService`, `FinancialAutomationService`, `RecurrenceService`, `ShellNotificationService`, `StatsController`, `AccessProfile`, templates de nominas/ y gastos/recurrentes/.
- N+1 en `PagoService.contarUsuarios*` documentado en backlog — resuelto en 2026-04-23 (ver bloque siguiente).

### 2026-04-23 (cierre profesional SaaS)

- **Dead code eliminado:** `fragments/navbar.html` borrado (confirmado huerfano con grep).
- **Regresion test:** `ViewControllerTest` ampliado con `adminPuedeVerListadoGastos()` y `adminPuedeVerDetalleGasto()` para cubrir el bug `EL1008E` de `gastos/detail`.
- **KPI cards unificadas:** `pagos/list.html`, `gastos/list.html` y `nominas/list.html` migrados al sistema `.ff-kpi-card` con variantes semanticas `ff-kpi-positive/warning/danger/neutral`. CSS ampliado en `styles.css` con las variantes y dark mode overrides. `.ff-filter-panel` como wrapper de filtros.
- **Status badges normalizados:** `pagos/list.html` usa `.ff-status-badge`; corregido `ff-status-warning` → `ff-status-pending` en `nominas/list.html`.
- **Beneficio estimado dinamico:** `data-kpi-profit` con `th:attr` en `home/index.html`; `updateProfitCardColor()` en `dashboard.js`.
- **Dark mode Chart.js:** `getChartColors()` en `dashboard.js`; los tres charts usan colores del tema en tiempo de render.
- **N+1 eliminado:** Tres queries JPQL de agregacion en `PagoRepository`; `contarUsuariosAlDia/ConDeuda/ConPagosVencidos` ya no hacen `findAll()`.
- **Trial→MembresiaUsuario:** `TrialService.convertirAUsuario()` auto-crea `MembresiaUsuario` al convertir trial nuevo con plan, sin membresia activa previa.
- **Tests:** 21 en verde.

## 12. Problemas ya conocidos y resueltos

- Pantallas en blanco por splash/transicion: resuelto.
- Doble disparo de automatizacion financiera: resuelto.
- H2 y configuraciones paralelas: eliminadas.
- Error por `CURDATE()` en reparacion financiera: resuelto.
- `gasto.frecuencia` en `gastos/detail.html` causando 500: resuelto (2026-04-22).
- `SaaSSchedulerService` ignorando properties configurables: resuelto (2026-04-22).
- N+1 en `PagoService.contarUsuarios*`: resuelto (2026-04-23).
- `ViewControllerTest` sin cobertura de `/gastos/{id}`: resuelto (2026-04-23).
- Trial convertido sin MembresiaUsuario: resuelto (2026-04-23).
- Nominas emitiendo/pagando con 500 por `BusinessValidationException`: resuelto (2026-04-26).
- Trials creados con binding fragil de entidad: resuelto con `TrialForm` (2026-04-26).
- Conversion de trial sin credenciales reales: resuelto con password temporal BCrypt (2026-04-26).
- Buscador de usuario en staff que reconstruia el select: resuelto; ahora solo oculta opciones (2026-04-26).

## 13. Riesgos actuales que Claude debe vigilar

- El arbol de trabajo no esta limpio (cambios sin commit en rama `recovery/restore-core-saas-plan-a`).
- QA visual profunda pendiente en: `/rutinas`, `/asistencias`, `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/maquinas`, `/materiales`, `/cliente`.
- Parte del proyecto fue recuperado desde una rama avanzada; no mezclar sin revisar con trabajo legacy.
- Hay docs antiguas que ya fueron corregidas, pero cualquier nueva contradiccion debe corregirse enseguida.

## 14. Flujo recomendado para continuar

1. Confirmar `git status --short`.
2. Ejecutar compile y tests.
3. Si se toca frontend, validar con navegador real o headless.
4. Si se toca SQL o JPA, validar contra MySQL real.
5. Si se toca permisos, probar al menos ADMIN y otro perfil.
6. Actualizar docs al cerrar cada bloque.

## 15. Que no debe hacer Claude

- No reintroducir H2.
- No mover el proyecto a SPA.
- No esconder problemas con CSS sin entender la causa.
- No duplicar reglas temporales o financieras.
- No meter logica de negocio en templates.
- No borrar work in progress del arbol actual sin copia.

## 16. Proximos pasos sugeridos

Orden recomendado:

1. Arrancar la app con `.\mvnw.cmd spring-boot:run` y hacer smoke real en navegador.
2. Verificar dashboard: KPI cards con colores semanticos, beneficio estimado verde/rojo, charts con dark mode correcto al toglear tema.
3. Verificar `/pagos`, `/gastos`, `/nominas`: KPI cards con variantes semanticas, filtros con `.ff-filter-panel`.
4. Crear un trial con email nuevo → convertir → verificar tabla `membresias_usuario` tiene nueva fila si el usuario tiene plan.
5. QA visual profunda modulo por modulo: `/rutinas`, `/asistencias`, `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/maquinas`, `/materiales`, `/cliente`.
6. Verificar smoke por perfiles reales (ADMIN, STAFF_ENTRENADOR, STAFF_RECEPCION, STAFF_GERENTE, CLIENTE): accesos denegados, redirecciones y sidebar contextual.
7. Hacer commit limpio de los cambios actuales.

## 17. Nota de rescate 2026-04-26

- Suite actual: 43 tests en verde.
- Ultimo compile limpio: `.\mvnw.cmd clean -DskipTests compile`.
- Si Claude retoma, priorizar smoke real en navegador de estos flujos:
  - crear/editar usuario con foto;
  - reset password, login temporal y cambio;
  - crear trial y convertirlo;
  - crear nomina, emitir y pagar;
  - crear maquina/material con coste y verificar gasto automatico.
