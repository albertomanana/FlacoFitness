# Setup Status

Fecha de referencia: 2026-04-16

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Operativo | Maven Wrapper funciona. `mvnw.cmd clean -DskipTests compile` y `mvnw.cmd test` pasan. El sistema actual usa JDK 25 en la maquina, pero el proyecto compila con `release 17`. |
| Maven / Lombok | Corregido | Se elimino la ruta absoluta a `javac` del `pom.xml` y se actualizo Lombok a `1.18.44` para evitar `TypeTag UNKNOWN` con JDK moderno. |
| Perfil local/demo | Operativo | `application-local.properties` usa H2 en memoria, `spring.sql.init.mode=always` y `data-local.sql` para datos de demo controlados. |
| MySQL real | Seguro | `application.properties` mantiene MySQL con `spring.sql.init.mode=never` para evitar reinsercion de datos demo o contaminacion de tablas reales. |
| Backend | Recuperado | Se restauro el nucleo SaaS avanzado: `StaffPerfil`, `MembresiaUsuario`, `Trial`, `Clase`, `SesionClase`, `ReservaSesion`, gastos, maquinas y materiales. |
| Acceso PIN | Implementado | Acceso MVP por PIN con limite de intentos, bloqueo temporal y perfil de sesion. No se usa Spring Security en esta fase. |
| Perfiles | Implementado | Existen perfiles `ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE` y `CLIENTE`, con rutas filtradas por interceptor y sidebar contextual. |
| Staff operativo | Corregido | Solo entrenadores o staff marcado con `puedeImpartirClases` pueden ser responsables de sesiones o rutinas. Gerencia no aparece como instructora por defecto. |
| Frontend | Recuperado | Sidebar/topbar, dashboard, modulos existentes y modulos SaaS siguen integrados con Thymeleaf, Bootstrap, Chart.js y DataTables. |
| Tests | En verde | `FlacoFitnessApplicationTests` y `PagoServiceTest` pasan tras la recuperacion. |
| Git | En recuperacion | Rama actual: `recovery/restore-core-saas-plan-a`. Hay un stash de respaldo previo a la restauracion. |

## Comandos de validacion ejecutados

```powershell
.\mvnw.cmd clean -DskipTests compile
.\mvnw.cmd test
```

## Estado de seeds

- MySQL real: no ejecuta seeds por defecto.
- Perfil `local`: H2 se recrea con datos demo para defensa y desarrollo rapido.
- Los scripts de `docs/db/` quedan como soporte/documentacion y no deben tratarse como migraciones automaticas.
