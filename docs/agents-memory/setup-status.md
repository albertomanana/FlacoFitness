# Setup Status

Fecha de referencia: 2026-04-26

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Operativo | Maven Wrapper funciona. `mvnw.cmd clean -DskipTests compile` y `mvnw.cmd test` pasan. El sistema actual usa JDK 25 en la maquina, pero el proyecto compila con `release 17`. |
| Maven / Lombok | Corregido | Se elimino la ruta absoluta a `javac` del `pom.xml` y se actualizo Lombok a `1.18.44` para evitar `TypeTag UNKNOWN` con JDK moderno. |
| Base de datos local | Configurada | `application.properties` y `application-local.properties` apuntan a MySQL/phpMyAdmin, base `flacofitness`, usuario `flaco_user` y `spring.sql.init.mode=never`. |
| Persistencia | Limpia y segura | `spring.jpa.hibernate.ddl-auto=update` actualiza tablas sin borrar datos; no hay fallback en memoria ni seeds automaticos. Limpieza MySQL 2026-04-26 aplicada y validada con postcheck. |
| Backend | Recuperado | Se restauro el nucleo SaaS avanzado: `StaffPerfil`, `MembresiaUsuario`, `Trial`, `Clase`, `SesionClase`, `ReservaSesion`, gastos, maquinas y materiales. |
| Finanzas internas | Consolidado y validado | El bloque financiero incluye gastos, recurrentes, nominas, PDF y automatizacion mensual ligada al reloj operativo mediante `FinancialAutomationService`. |
| Acceso por cuenta | Implementado | Login con `email/username + password`, hash BCrypt, limite de intentos, bloqueo temporal, cambio de password y reset temporal por admin. No se usa Spring Security web en esta fase. |
| Perfiles | Implementado | Existen perfiles `ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE` y `CLIENTE`, con rutas filtradas por interceptor y sidebar contextual. |
| Staff operativo | Corregido | Solo entrenadores o staff marcado con `puedeImpartirClases` pueden ser responsables de sesiones o rutinas. Gerencia no aparece como instructora por defecto. |
| Frontend | Estabilizado | Sidebar/topbar, dashboard, modulos existentes y modulos SaaS integrados con Thymeleaf, Bootstrap, Chart.js y DataTables, incluyendo modo oscuro persistente y mejoras de legibilidad en formularios/tablas. El bug de modulos en blanco por splash/transicion ya esta corregido. |
| Producto premium | Implementado en progreso | Panel `Requiere atencion`, FAB global por perfil, busqueda global v1, ultimos visitados, actividad reciente y memoria UX por navegador/perfil ya estan integrados. Login y builder de nomina ya tienen capa visual premium. |
| Notificaciones | Mejorado | El centro de notificaciones prioriza alertas por tono y muestra accesos accionables a modulos criticos, incluyendo gastos, recurrentes y nominas. |
| Reloj operativo | Simplificado | Se elimino la simulacion manual persistida. `OperationalClockService` usa solo fecha/hora real del sistema y mantiene una unica referencia temporal para pagos, asistencias, sesiones, gastos, maquinaria, trials y membresias. |
| Tests | En verde | `FlacoFitnessApplicationTests`, `ViewControllerTest`, `AccessProfileTest`, `PagoServiceTest`, `GastoServiceTest`, `GlobalSearchServiceTest` y pruebas añadidas pasan. 35 pruebas en verde. |
| Git | Sucio (cambios pendientes de commit) | Rama actual: `recovery/restore-core-saas-plan-a`. Hay cambios pendientes de autenticacion, UX premium, payroll builder y documentacion viva. |

## Validacion 2026-04-26 (limpieza definitiva MySQL)

- Backup previo creado: `tmp/db-backups/flacofitness-cleanup-20260426-195946.sql`.
- Scripts añadidos:
  - `docs/db/cleanup-2026-04-26-precheck.sql`
  - `docs/db/cleanup-2026-04-26.sql`
  - `docs/db/cleanup-2026-04-26-postcheck.sql`
- Precheck MySQL ejecutado sin bloqueadores no migrables.
- Cleanup MySQL ejecutado correctamente: se retiraron `app_clock_settings`, `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`.
- Columnas legacy eliminadas de `gastos` y `reservas_sesion` tras migrar datos utiles.
- Postcheck MySQL posterior a arranque JPA:
  - `legacy_tables_remaining = 0`
  - `legacy_fk_remaining = 0`
  - `gastos_legacy_columns_remaining = 0`
  - `reservas_legacy_columns_remaining = 0`
- `.\mvnw.cmd clean -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 35 pruebas en verde.
- Arranque real contra MySQL `flacofitness` validado en puerto temporal `8083`.
- Login ADMIN validado con `admin@flacofitness.local` / `FlacoAdmin2026!`.
- Smoke HTTP con sesion ADMIN: `/`, `/usuarios`, `/staff`, `/membresias`, `/trials`, `/pagos`, `/gastos`, `/nominas`, `/sesiones`, `/maquinas` y `/materiales` respondieron 200.

## Validacion 2026-04-26 (estabilizacion premium)

- `.\mvnw.cmd -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 23 pruebas en verde.
- `Test-NetConnection localhost -Port 3306`: `TcpTestSucceeded = True`.
- La app arranco con MySQL en puerto temporal `8081` porque `8080` ya estaba ocupado.
- Smoke HTTP basico:
  - `/acceso`: 200.
  - `/`, `/usuarios`, `/pagos`, `/gastos`, `/nominas` y `/api/busqueda/global?q=ana`: 302 hacia `/acceso`, esperado para rutas protegidas sin sesion.
- El login funcional no se pudo completar con credenciales locales conocidas en esta base; los intentos redirigieron de nuevo a `/acceso`. Queda como riesgo de credenciales/estado local, no como fallo de arranque.

## Validacion 2026-04-26 (Command Center UI)

- `.\mvnw.cmd -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 23 pruebas en verde.
- `Test-NetConnection localhost -Port 3306`: `TcpTestSucceeded = True`.
- La app arranco en puerto temporal `8082`.
- Smoke HTTP:
  - `/acceso`: 200.
  - `/css/styles.css`, `/js/hud-motion.js`, `/vendor/animejs/anime.umd.min.js`: 200.
  - `/`, `/usuarios`, `/pagos`, `/gastos`, `/gastos/recurrentes`, `/asistencias`, `/staff`, `/rutinas`, `/nominas`: 302 hacia `/acceso`, esperado sin sesion.
- Verificacion visual automatizada con `agent-browser` no ejecutada porque el comando no esta disponible en PATH en esta maquina.

## Validacion 2026-04-26 (performance + finanzas + nominas)

- `.\mvnw.cmd -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 33 pruebas en verde.
- `Test-NetConnection localhost -Port 3306`: `TcpTestSucceeded = True`.
- Arranque real validado contra MySQL `flacofitness` en puerto temporal `8081`.
- Smoke HTTP sin sesion:
  - `/acceso`: 200.
  - `/`, `/finanzas`, `/nominas`, `/stats/finanzas`: 302 hacia `/acceso`, esperado por rutas protegidas.
- La instancia temporal `8081` fue detenida al terminar la prueba.

## Validacion 2026-04-24 (auth por cuenta + nominas premium)

- `.\mvnw.cmd clean -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 21 pruebas en verde.
- Se sustituyo el acceso compartido por PIN por autenticacion por cuenta con `Usuario.username`, `passwordHash` y `mustChangePassword`.
- `AuthBootstrapRunner` genera credenciales bootstrap para usuarios legacy sin password hash usando `app.auth.bootstrap-password`.
- `AccessSessionService` autentica por email o username y mantiene bloqueo temporal por intentos fallidos.
- Se corrigio un `LazyInitializationException` en login cargando `rol` y `plan` desde `UsuarioRepository` para autenticacion por cuenta.
- El bloqueo temporal por intentos fallidos se redujo a 1 minuto por defecto (`app.auth.lock-minutes=1`).
- `CuentaController` anade cambio de password del usuario autenticado.
- `UsuarioController` permite reset temporal de password por admin.
- Se creo un usuario admin real en MySQL para operacion local. La password operativa vigente validada el 2026-04-26 es `FlacoAdmin2026!`.
- `NominaService` soporta `BORRADOR`, `EMITIDA`, `PAGADA` y `CANCELADA`, y genera `Gasto` de `NOMINA` al emitir.
- `nominas/form.html` incorpora preview en vivo y `nominas/detail.html` / `reportes/nomina-detalle.html` elevan la lectura documental.
- `Test-NetConnection localhost -Port 3306`: `TcpTestSucceeded = False`; no se pudo cerrar smoke HTTP real porque MySQL local no estaba disponible en esta sesion.

## Validacion 2026-04-23 (coherencia global)

- `.\mvnw.cmd clean -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 21 tests en verde.
- Se enriquecieron y validaron fichas de `staff`, `maquinas`, `materiales`, `membresias` y `pagos`.
- Se ampliaron redirecciones post-accion para volver a detalle en `staff`, `materiales`, `maquinas`, `membresias`, `pagos`, `gastos` y `recurrentes`.
- `ViewControllerTest` ahora cubre render de detalle de `staff`, `maquinas` y `materiales`.
- Se corrigieron restos visibles de copy roto en `sidebar`, `staff/detail`, `pagos/list` y `pagos/detail`.

## Validacion 2026-04-23 (producto premium)

- `.\mvnw.cmd clean -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 21 pruebas en verde.
- Se anadieron entidades y persistencia para `ux_memory_state`, `recent_visit` y `activity_log`.
- `DashboardStatsResponse` ahora incluye `usuariosEnRiesgo`, `membresiasPorCaducar`, `gastosAnomalos` y `attentionItems`.
- Topbar con busqueda global JSON + pagina agrupada operativa a nivel de codigo.
- FAB global por perfil ya renderiza acciones rapidas segun `AccessProfile`.
- Tooltips first-use por modulo prioritario y guia inicial del dashboard ya usan memoria UX persistente.
- Se registran visitas recientes y actividad operativa en usuarios, staff, pagos, gastos, membresias, sesiones, trials, nominas, maquinas y materiales.
- No se pudo cerrar smoke de arranque HTTP en aquella sesion porque MySQL local devolvia `Connection refused`; compile y tests quedaron validados, pero el arranque real depende de levantar MySQL/phpMyAdmin.

## Comandos de validacion ejecutados

```powershell
.\mvnw.cmd clean -DskipTests compile
.\mvnw.cmd test
Test-NetConnection -ComputerName localhost -Port 3306
```

## MySQL

- Configuracion esperada: `localhost:3306`, base `flacofitness`, usuario `flaco_user`, password `flaco_pass`.
- Validacion 2026-04-26: MySQL disponible, limpieza aplicada, postcheck limpio y smoke HTTP real completado con sesion ADMIN.

## Estado de seeds

- MySQL/phpMyAdmin: no ejecuta seeds por defecto.
- Los datos existentes se conservan; la app no usa inicializacion destructiva.
- Los scripts de `docs/db/` quedan como soporte/documentacion y no deben tratarse como migraciones automaticas.

## Nota 2026-04-26 (documentacion de rebuild total)

- Se agrego `docs/agents-memory/rebuild-from-zero-modular-guide.md` como plan maestro para reconstruir la aplicacion desde cero por modulos.
- La guia define un nombre alternativo de producto (`AtlasGym OS`) y una base alternativa (`atlasgym_core`) para una implementacion nueva.
- No se aplicaron cambios de runtime en esta iteracion: el entorno vigente sigue apuntando a `flacofitness`.

## Validacion 2026-04-26 (rescate funcional)

- `.\mvnw.cmd clean -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 43 pruebas en verde.
- `Test-NetConnection localhost:3306`: `TcpTestSucceeded = True`.
- Arranque real contra MySQL en puerto temporal `8084`: correcto.
- Smoke HTTP con ADMIN real (`admin@flacofitness.local` / password operativa vigente):
  - `/acceso`: 200.
  - `/usuarios/nuevo`, `/trials/nuevo`, `/nominas`, `/gastos`, `/maquinas/nuevo`, `/materiales/nuevo`: 200 con sesion.
  - rutas protegidas sin sesion redirigen a `/acceso` con 302.
- Cobertura anadida:
  - acciones invalidas de nomina redirigen con flash sin 500;
  - login con password temporal redirige a cambio obligatorio y permite guardar nueva password;
  - formulario de usuario renderiza sin error de Thymeleaf;
  - formulario y alta de trial usan `TrialForm` sin 500;
  - conversion de trial genera credenciales temporales BCrypt;
  - gastos automaticos de maquina/material quedan pagados y vinculados.
- No se ejecuto smoke mutante contra MySQL real para no crear datos de prueba en `flacofitness`; las altas y pagos se validaron con MockMvc/servicios.
