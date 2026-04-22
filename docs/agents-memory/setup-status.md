# Setup Status

Fecha de referencia: 2026-04-22

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Operativo | Maven Wrapper funciona. `mvnw.cmd clean -DskipTests compile` y `mvnw.cmd test` pasan. El sistema actual usa JDK 25 en la maquina, pero el proyecto compila con `release 17`. |
| Maven / Lombok | Corregido | Se elimino la ruta absoluta a `javac` del `pom.xml` y se actualizo Lombok a `1.18.44` para evitar `TypeTag UNKNOWN` con JDK moderno. |
| Base de datos local | Operativo | `application.properties` y `application-local.properties` apuntan a MySQL/phpMyAdmin, base `flacofitness`, usuario `flaco_user` y `spring.sql.init.mode=never`. |
| Persistencia | Seguro | `spring.jpa.hibernate.ddl-auto=update` actualiza tablas sin borrar datos; no hay fallback en memoria ni seeds automaticos. |
| Backend | Recuperado | Se restauro el nucleo SaaS avanzado: `StaffPerfil`, `MembresiaUsuario`, `Trial`, `Clase`, `SesionClase`, `ReservaSesion`, gastos, maquinas y materiales. |
| Finanzas internas | Consolidado | El bloque financiero incluye gastos, recurrentes, nominas, PDF y automatizacion mensual ligada al reloj operativo mediante `FinancialAutomationService`. |
| Acceso PIN | Implementado | Acceso MVP por PIN con limite de intentos, bloqueo temporal y perfil de sesion. No se usa Spring Security en esta fase. |
| Perfiles | Implementado | Existen perfiles `ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE` y `CLIENTE`, con rutas filtradas por interceptor y sidebar contextual. |
| Staff operativo | Corregido | Solo entrenadores o staff marcado con `puedeImpartirClases` pueden ser responsables de sesiones o rutinas. Gerencia no aparece como instructora por defecto. |
| Frontend | Estabilizado | Sidebar/topbar, dashboard, modulos existentes y modulos SaaS integrados con Thymeleaf, Bootstrap, Chart.js y DataTables, incluyendo modo oscuro persistente y mejoras de legibilidad en formularios/tablas. El bug de modulos en blanco por splash/transicion ya esta corregido. |
| Notificaciones | Mejorado | El centro de notificaciones prioriza alertas por tono y muestra accesos accionables a modulos criticos. |
| Reloj operativo | Simplificado | Se elimino la simulacion manual y la persistencia en `app_clock_settings`. `OperationalClockService` usa solo fecha/hora real del sistema y mantiene una unica referencia temporal para pagos, asistencias, sesiones, gastos, maquinaria, trials y membresias. |
| Tests | En verde | `FlacoFitnessApplicationTests`, `ViewControllerTest`, `AccessProfileTest`, `PagoServiceTest` y `GastoServiceTest` pasan tras simplificar reloj operativo y ajustar pruebas de servicios. |
| QA financiera pendiente | Parcial | Queda pendiente validacion visual en navegador/MySQL real de PDFs y pantallas financieras, pero la compilacion y tests automatizados ya estan en verde. |
| Git | Limpio | Rama actual: `recovery/restore-core-saas-plan-a`. Tree limpio. Stash pre-recovery eliminado (era estado con root/sin-pass, superado). Ultimo commit: bloques 2-4 (filtros, deuda tecnica, panel cliente). |

## Comandos de validacion ejecutados

```powershell
.\mvnw.cmd clean -DskipTests compile
.\mvnw.cmd test
Write-Output "LASTEXITCODE=$LASTEXITCODE"
.\mvnw.cmd -Dtest=ViewControllerTest test
```

## Validacion 2026-04-22

- `.\mvnw.cmd -DskipTests compile`: correcto.
- `.\mvnw.cmd test`: correcto, 16 tests en verde.
- Se eliminaron dependencias de simulacion temporal en servicios, controlador y topbar, manteniendo automatizacion financiera central.
- Se validaron visualmente `usuarios` y `pagos` tras corregir el estado en blanco del shell.

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

## Validacion del reloj operativo

- Se valido la eliminacion de `POST /reloj-operativo` y de controles de ajuste en el topbar.
- Se valido compilacion y tests con reloj operativo basado solo en tiempo real de sistema.

## Estado de seeds

- MySQL/phpMyAdmin: no ejecuta seeds por defecto.
- Los datos existentes se conservan; la app no usa inicializacion destructiva.
- Los scripts de `docs/db/` quedan como soporte/documentacion y no deben tratarse como migraciones automaticas.
