# Setup Status

Fecha de referencia: 2026-04-22

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Operativo | Maven Wrapper funciona. `mvnw.cmd clean -DskipTests compile` y `mvnw.cmd test` pasan. El sistema actual usa JDK 25 en la maquina, pero el proyecto compila con `release 17`. |
| Maven / Lombok | Corregido | Se elimino la ruta absoluta a `javac` del `pom.xml` y se actualizo Lombok a `1.18.44` para evitar `TypeTag UNKNOWN` con JDK moderno. |
| Base de datos local | Operativo | `application.properties` y `application-local.properties` apuntan a MySQL/phpMyAdmin, base `flacofitness`, usuario `flaco_user` y `spring.sql.init.mode=never`. |
| Persistencia | Seguro | `spring.jpa.hibernate.ddl-auto=update` actualiza tablas sin borrar datos; no hay fallback en memoria ni seeds automaticos. |
| Backend | Recuperado | Se restauro el nucleo SaaS avanzado: `StaffPerfil`, `MembresiaUsuario`, `Trial`, `Clase`, `SesionClase`, `ReservaSesion`, gastos, maquinas y materiales. |
| Finanzas internas | Consolidado y validado | El bloque financiero incluye gastos, recurrentes, nominas, PDF y automatizacion mensual ligada al reloj operativo mediante `FinancialAutomationService`. Bug de `gasto.frecuencia` corregido en `gastos/detail.html`. Scheduler usa properties configurables y `/gastos` ya expone filtros financieros completos con exportacion PDF individual. |
| Acceso PIN | Implementado | Acceso MVP por PIN con limite de intentos, bloqueo temporal y perfil de sesion. No se usa Spring Security en esta fase. |
| Perfiles | Implementado | Existen perfiles `ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE` y `CLIENTE`, con rutas filtradas por interceptor y sidebar contextual. |
| Staff operativo | Corregido | Solo entrenadores o staff marcado con `puedeImpartirClases` pueden ser responsables de sesiones o rutinas. Gerencia no aparece como instructora por defecto. |
| Frontend | Estabilizado | Sidebar/topbar, dashboard, modulos existentes y modulos SaaS integrados con Thymeleaf, Bootstrap, Chart.js y DataTables, incluyendo modo oscuro persistente y mejoras de legibilidad en formularios/tablas. El bug de modulos en blanco por splash/transicion ya esta corregido. Dashboard premium simplificado y rail de alertas activo. |
| Notificaciones | Mejorado | El centro de notificaciones prioriza alertas por tono y muestra accesos accionables a modulos criticos, incluyendo gastos, recurrentes y nominas. |
| Reloj operativo | Simplificado | Se elimino la simulacion manual y la persistencia en `app_clock_settings`. `OperationalClockService` usa solo fecha/hora real del sistema y mantiene una unica referencia temporal para pagos, asistencias, sesiones, gastos, maquinaria, trials y membresias. |
| Tests | En verde | `FlacoFitnessApplicationTests`, `ViewControllerTest`, `AccessProfileTest`, `PagoServiceTest` y `GastoServiceTest` pasan. 16 pruebas en verde. |
| QA financiera | Completada (codigo) | Auditoria completa del bloque financiero: servicios, controladores, templates y repositorios revisados. Bug critico en `gastos/detail.html` corregido. Ver backlog para QA visual en navegador con MySQL real pendiente. |
| Git | Sucio (cambios pendientes de commit) | Rama actual: `recovery/restore-core-saas-plan-a`. Cambios no commiteados: correccion de `gastos/detail.html` y actualizacion de `SaaSSchedulerService`. |

## Validacion 2026-04-22 (plan `.claude` ejecutado parcialmente)

- Se creo `CLAUDE.md` en la raiz para que Claude Code tenga reglas del proyecto sin depender solo de la conversacion.
- Se creo `docs/agents-memory/claude-plan-status.md` para traducir el archivo `.claude/claude_md_and_top_prompts_flacofitness_finance_gold.md` a estado real de ejecucion.
- `/gastos` usa filtros completos de backend en UI: estado, tipo, proveedor, staff, maquina, material y recurrencia, ademas de fecha y categoria.
- Se mantiene exportacion PDF individual para detalle de gasto.
- El dashboard principal ya funciona con rail premium de alertas y set corto de KPIs.
- Nominas dispone de detalle premium y PDF individual/listado con una presentacion mas profesional.
- Smoke real con servidor levantado:
  - login por PIN `ADMIN` correcto
  - `/` devuelve `200`
  - `/gastos` devuelve `200`
  - `/gastos/{id}` devuelve `200`
  - `/gastos/{id}/pdf` devuelve `200`
  - `/nominas` devuelve `200`
  - `/nominas/{id}` devuelve `200`
  - `/nominas/{id}/pdf` devuelve `200`

## Comandos de validacion ejecutados

```powershell
.\mvnw.cmd clean -DskipTests compile
.\mvnw.cmd test
Write-Output "LASTEXITCODE=$LASTEXITCODE"
.\mvnw.cmd -Dtest=ViewControllerTest test
```

## Validacion 2026-04-22 (bloques 1-4)

- `.\mvnw.cmd -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 16 tests en verde.
- Se eliminaron dependencias de simulacion temporal en servicios, controlador y topbar, manteniendo automatizacion financiera central.
- Se validaron visualmente `usuarios` y `pagos` tras corregir el estado en blanco del shell.

## Validacion 2026-04-22 (bloque financiero + dashboard premium)

- Auditoria de codigo del bloque financiero completa.
- Bug critico identificado y corregido: `gastos/detail.html` accedia a `gasto.frecuencia` (propiedad inexistente en `Gasto`); correcto es `gasto.gastoRecurrente.frecuencia` con null-guard.
- `SaaSSchedulerService` corregido para usar `${app.pagos.scheduler.cron}` y `${app.pagos.scheduler.enabled}`.
- `DashboardStatsResponse` ampliado con `maquinasFueraServicio`.
- `.\mvnw.cmd clean -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 16 pruebas en verde.
- Smoke HTTP con sesion real:
  - dashboard con rail de alertas y charts principales: correcto
  - detalle de nomina y PDF individual: correctos

## Validacion visual shell 2026-04-22

- `/usuarios` y `/pagos` renderizan contenido completo.
- La causa del blanco no era backend ni Thymeleaf: era la shell visual.
- Se corrigio en `static/js/app.js` y `static/css/styles.css`:
  - fail-safe para ocultar splash/loading si algo falla
  - `.ff-main` visible por defecto
  - animacion de entrada no destructiva

## MySQL validado

- Puerto `3306` accesible en `localhost`
- Base `flacofitness` accesible con `flaco_user / flaco_pass`
- Ultima comprobacion CLI: base visible y tablas presentes

## Estado de seeds

- MySQL/phpMyAdmin: no ejecuta seeds por defecto.
- Los datos existentes se conservan; la app no usa inicializacion destructiva.
- Los scripts de `docs/db/` quedan como soporte/documentacion y no deben tratarse como migraciones automaticas.
