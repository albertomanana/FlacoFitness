# Claude Code Handoff

Fecha de referencia: 2026-04-22

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
- No borrar datos existentes ni tablas sin migracion justificada.
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
- Acceso por PIN:
  - PIN por defecto: `2468`
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
- `PagoService`
- `GastoService`
- `GastoRecurrenteService`
- `NominaService`
- `ShellNotificationService`
- `UsuarioControlCenterService`

## 9. Estado de tiempo y automatizacion

### Estado actual

- Ya no existe simulacion manual persistida del reloj.
- `OperationalClockService` usa tiempo real del sistema.
- Sigue siendo la abstraccion oficial para reglas de negocio temporales.

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

### 2026-04-22

- Se retiro la simulacion manual del reloj y se dejo tiempo real de sistema.
- Se corrigio el bug de "modulos en blanco": el problema estaba en la shell visual, no en backend ni en Thymeleaf.
- Se anadio fail-safe en `app.js` y `styles.css` para que la splash o una transicion fallida no oculten `.ff-main`.
- Se validaron visualmente `usuarios` y `pagos` con Chrome headless despues del fix.

## 12. Problemas ya conocidos y resueltos

- Pantallas en blanco por splash/transicion: resuelto.
- Doble disparo de automatizacion financiera: resuelto.
- H2 y configuraciones paralelas: eliminadas.
- Error por `CURDATE()` en reparacion financiera: resuelto.

## 13. Riesgos actuales que Claude debe vigilar

- El arbol de trabajo no esta limpio.
- El bloque financiero tiene bastante superficie y no conviene refactorizarlo de golpe.
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

1. QA visual profunda modulo por modulo.
2. Cierre de inconsistencias menores del shell y acciones secundarias.
3. Consolidacion financiera visual y PDF.
4. Mas pruebas MVC por perfiles.
5. Si el producto sigue creciendo, preparar una rama de estabilizacion y un commit base limpio.
