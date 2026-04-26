# Arquitectura

## Descripcion general

FlacoFitness mantiene una arquitectura monolitica MVC con Spring Boot. La aplicacion renderiza vistas en servidor con Thymeleaf, usa Bootstrap 5 para interfaz y JavaScript ligero para interacciones concretas como dashboard, tablas, transiciones, filtros y feedback visual.

La decision principal es conservar un monolito claro: es suficiente para el alcance academico, evita sobreingenieria y permite explicar facilmente el recorrido completo desde vista, controlador, servicio, repositorio y base de datos.

## Capas del sistema

### Presentacion

- Vistas Thymeleaf en `src/main/resources/templates`.
- Fragmentos reutilizables para `head`, `sidebar`, `topbar`, `footer` y alertas.
- Bootstrap 5 como base visual.
- CSS propio en `static/css/styles.css`.
- JavaScript ligero en `static/js/`.
- Chart.js para dashboard.
- DataTables para listados interactivos.
- La shell premium usa splash y transiciones, pero desde 2026-04-22 `.ff-main` queda visible por defecto y existe fail-safe para que un fallo visual no deje modulos en blanco. Esta regla es inviolable en cualquier bloque de UI futuro.
- Desde 2026-04-24 la shell sigue el sistema "Operations Deck" (ver Design system v4 mas abajo): dark-first con glassmorphism, acento cian HUD `#38BDF8`, verde `#22C55E` para marca/CTAs, tokens CSS en `--ff-*`, todas las recetas en `styles.css`.
- La navegacion visible prioriza fila clicable en listados y reduce botones redundantes cuando abrir detalle y pulsar "ver" aportan exactamente lo mismo.
- `topbar` incorpora buscador global v1, centro de alertas y accesos recientes por perfil.
- `footer` aloja un FAB global por perfil para acciones de alta frecuencia sin introducir SPA.

### Controladores

- Reciben peticiones HTTP MVC.
- Preparan modelos para Thymeleaf.
- Delegan reglas de negocio a servicios.
- Mantienen rutas de modulos como usuarios, rutinas, pagos, asistencias, staff, membresias, trials, clases, sesiones, gastos, nominas, maquinas y materiales.
- La regla actual de flujo es: tras crear o editar, volver al detalle de la entidad; tras activar, desactivar o marcar pagado, volver al detalle si la accion nace alli y al listado si nace desde una tabla.
- `ShellViewAdvice` es el punto comun de shell: inyecta perfil, usuario actual, reloj, notificaciones, browser token, memoria UX y ultimos visitados.

### Servicios

- Centralizan reglas de negocio.
- Validan consistencia de relaciones.
- Evitan duplicidades funcionales.
- Orquestan casos de uso como alta de membresia, conversion de trial, reservas de sesion, pagos, check-in y nominas.
- Usan `OperationalClockService` como fuente temporal de negocio cuando una regla depende de "hoy" o "ahora".
- `FinancialAutomationService` es la unica fachada para automatizacion financiera: actualiza pagos/gastos vencidos y genera pagos, gastos recurrentes y nominas.
- `RecurrenceService` centraliza calculos de siguiente ciclo por duracion de plan o frecuencia recurrente.
- `ProductIntelligenceService` centraliza clasificaciones de producto y construye el panel `Requiere atencion`.
- `GlobalSearchService` resuelve la busqueda global agrupada de usuarios, staff y sesiones.
- `RecentVisitService` guarda y limita las ultimas fichas visitadas por `browserToken + accessProfile`.
- `UxMemoryStateService` persiste tooltips first-use y estados de onboarding por modulo.
- `ActivityLogService` y `ControllerActivityLogger` registran actividad de producto usando principalmente el perfil de sesion; la autenticacion ya es individual por cuenta, pero la auditoria ligera sigue priorizando el perfil para mantener trazabilidad simple y defendible.

### Repositorios

- Encapsulan persistencia con Spring Data JPA.
- Exponen consultas por estado, usuario, fechas, membresia, sesion o staff.
- Desde la limpieza 2026-04-26 no deben aparecer repositorios ni queries contra tablas legacy eliminadas: `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios` o `app_clock_settings`.

### Modelo

- Entidades JPA en `model.entity`.
- Enums en `model.enums`.
- DTOs de respuesta o agregados en `model.dto`.
- El modelo evita duplicar personas: `StaffPerfil` extiende operativamente a `Usuario`.
- La capa de UX persistente se modela tambien en base de datos con:
  - `ux_memory_state`
  - `recent_visit`
  - `activity_log`

## Autenticacion y autorizacion

No se usa Spring Security web en esta fase, pero ya no existe acceso compartido por PIN. La autenticacion es por cuenta y la autorizacion sigue resolviendose con interceptor MVC y perfiles derivados:

- `PasswordConfig`: expone `PasswordEncoder` BCrypt.
- `AccessSettings`: lee intentos maximos, bloqueo temporal y password bootstrap de recuperacion.
- `AccessSessionService`: autentica por `email/username + password`, mantiene sesion, bloqueo temporal e identidad actual.
- `AccessProfileResolver`: deriva el `AccessProfile` desde `Usuario` y `StaffPerfil`.
- `AccessGuardInterceptor`: protege rutas, aplica permisos por perfil y fuerza cambio de password cuando `mustChangePassword = true`.
- `AccessProfile`: define perfiles y permisos de navegacion.
- `CuentaController`: cambio de password del usuario autenticado.
- `AuthBootstrapRunner`: backfill de credenciales para usuarios legacy sin password hash.

Perfiles actuales:

- `ADMIN`: control total.
- `STAFF_ENTRENADOR`: rutinas, clases, sesiones y asistencias; lectura limitada de usuarios.
- `STAFF_RECEPCION`: usuarios, trials, pagos operativos, asistencias y reservas.
- `STAFF_GERENTE`: vision de gestion, finanzas e inventario; no imparte clases por defecto.
- `CLIENTE`: panel personal limitado a `/cliente` y sus acciones propias.

Esta capa sigue siendo intencionadamente simple y defendible: autenticacion por cuenta, hash seguro y permisos claros sin introducir todavia la complejidad completa de Spring Security web.

## Contexto de navegador y memoria UX

- `BrowserContextInterceptor` garantiza que cada peticion shell tenga un `browser_token` persistido en cookie.
- Ese token se espeja en `localStorage` solo para continuidad de UI y se sincroniza con MySQL.
- La persistencia UX se separa en dos niveles:
  - filtros de tabla y formularios: locales en navegador
  - memoria UX de onboarding/tooltips y visitas recientes: persistida en MySQL por `browser_token + access_profile`

Esto evita acoplar demasiado la UI efimera a la base, pero permite que la app recuerde contexto y se sienta mas producto.

## Modulos funcionales actuales

- `usuarios`: centro operativo del cliente, con foto, datos, pagos, asistencias, rutinas y resumen inteligente.
- `cliente`: panel limitado para perfil cliente autenticado.
- `staff`: perfiles internos ligados a usuarios.
- `membresias`: catalogo comercial basado en `Plan` y contratos mediante `MembresiaUsuario`.
- `trials`: gestion de leads y dias de prueba.
- `clases`: catalogo de actividades.
- `sesiones`: agenda de clases programadas, cupo, staff responsable, reservas y asistencia.
- `rutinas`: biblioteca y asignacion de entrenamientos, con staff responsable opcional.
- `pagos`: cobros asociados a usuario, plan y contrato cuando existe.
- `asistencias`: check-in libre o asistencia asociada a sesion.
- `gastos`: control financiero basico.
- `nominas`: salarios experimentales internos con gasto asociado y PDF.
- `maquinas` y `materiales`: inventario operativo.
- `busqueda`: pagina agrupada y endpoint JSON para busqueda global.

## Criterio de dominio

- `Plan` define una oferta comercial.
- `MembresiaUsuario` define el contrato real de un usuario.
- `Pago` registra cobros y estado financiero.
- `Clase` define una actividad reutilizable.
- `SesionClase` define una ocurrencia con fecha, hora, cupo y responsable.
- `ReservaSesion` conecta usuarios con sesiones.
- `Asistencia` conserva check-in libre y puede asociarse opcionalmente a una sesion.
- `StaffPerfil` se liga a `Usuario` para no duplicar identidad.
- `Nomina` ya no se trata como simple CRUD: soporta borrador, emision, pago, cancelacion y PDF profesional.

## Reloj operativo

El topbar muestra la fecha y hora operativa basada en el reloj real del sistema.

No existe ruta de ajuste manual del reloj. `OperationalClockService` expone una abstraccion de tiempo unica para reglas de negocio, pero siempre delega en la fecha y hora reales del servidor.

La tabla legacy `app_clock_settings` fue eliminada de MySQL en la limpieza 2026-04-26. Cualquier nueva simulacion temporal debe tratarse como cambio de arquitectura, no como reactivacion accidental de la tabla antigua.

Al evaluar la fecha, la app ejecuta automatizacion financiera central:

- marca pagos no pagados como `VENCIDO` cuando `fecha_vencimiento` queda antes de la fecha operativa;
- marca gastos abiertos como `VENCIDO` con la misma regla;
- genera pagos mensuales desde `MembresiaUsuario` activa y mantiene compatibilidad con `Usuario.plan`;
- genera gastos desde `GastoRecurrente` sin duplicar por plantilla y vencimiento;
- genera nominas automaticas si el staff tiene salario y automatizacion activa.

Excepciones permitidas de fecha real:

- bloqueo temporal del acceso por password en `AccessSessionService`, porque es seguridad de sesion y no tiempo de negocio;
- metadatos tecnicos de actualizacion del propio reloj;
- seeder demo desactivable, que no forma parte de la operacion real de MySQL.

## Convencion de paquetes

```text
com.flacofitness.app
|-- config
|-- controller
|-- exception
|-- model
|   |-- dto
|   |-- entity
|   `-- enums
|-- repository
|-- security
|-- service
`-- util
```

## Reglas de mantenimiento

- No introducir SPA ni frameworks frontend pesados.
- No ejecutar seeds demo contra MySQL real.
- No introducir bases en memoria ni fallbacks de persistencia: MySQL/phpMyAdmin es la unica fuente de verdad.
- No reintroducir `data.sql` en classpath ni scripts que creen tablas legacy ya eliminadas.
- Mantener controladores finos y servicios con reglas de negocio.
- Mantener compatibilidad con datos legacy cuando una relacion nueva sea opcional.
- Documentar decisiones relevantes en `docs/agents-memory/decisions-log.md`.

## Nota 2026-04-24

- No hubo reescritura de arquitectura ni cambio de stack.
- La evolucion principal fue sustituir el acceso por PIN por autenticacion por cuenta con password hash BCrypt.
- Se mantuvo la arquitectura MVC con interceptor propio y perfiles `AccessProfile`.
- El flujo de nominas paso a un modelo mas profesional: borrador, emision, pago, cancelacion y PDF documental.

## Nota 2026-04-26

- La busqueda global sigue expuesta por `/api/busqueda/global` y `/busqueda`, pero ahora se apoya en queries limitadas de repositorio para `Usuario`, `StaffPerfil` y `SesionClase`.
- No se cambio el contrato JSON, las rutas publicas ni el stack MVC.
- Se redujeron calculos repetidos en dashboard, stats y exportacion PDF de gastos reutilizando las metricas mensuales en el controlador.
- La mejora es de rendimiento y mantenibilidad, no de dominio: no se introdujeron entidades ni dependencias nuevas.

## Nota 2026-04-26 (Command Center UI)

- La capa visual principal sigue en Thymeleaf + Bootstrap + CSS propio, sin SPA.
- `styles.css` contiene una capa final Command Center que gana en cascada sobre reglas legacy y evita reescribir templates completos.
- El dark mode es el default si no hay preferencia guardada; el `head` aplica `data-theme` temprano para evitar flash claro.
- Se añadio Anime.js como asset local UMD y `hud-motion.js` como inicializador progresivo: si la libreria no carga o el usuario reduce motion, la UI sigue funcional.
- Los patrones `ff-command-stack`, `ff-hud-grid`, `ff-panel-grid`, `ff-command-hero`, `ff-hud-card`, `ff-table-shell` y `ff-empty-hud` son CSS-first y compatibles con Bootstrap.

## Nota 2026-04-26 (Finanzas y performance)

- `NominaController` usa `NominaForm` como DTO de entrada; la entidad `Nomina` ya no se usa como backing bean de formulario.
- `NominaService` sigue siendo el limite transaccional para crear borrador, emitir, pagar, cancelar, calcular salario neto y vincular `Gasto` de categoria `NOMINA`.
- `FinancialCenterService` agrega lectura financiera para `/finanzas` y `/stats/finanzas` sin introducir SPA ni modificar contratos existentes.
- `AccessProfile` expone `finanzas` solo a ADMIN/GERENTE; `/stats/finanzas` queda restringido al mismo alcance.
- `dashboard.js` evita doble fetch tras SSR y reutiliza instancias Chart.js cuando el tipo de grafico no cambia.
- `finance-center.js` esta encapsulado en IIFE para no pisar funciones globales del dashboard.

## Nota 2026-04-26 (Blueprint rebuild total)

- Se definio un blueprint de reconstruccion completa por modulos en `docs/agents-memory/rebuild-from-zero-modular-guide.md`.
- El blueprint mantiene la arquitectura monolitica MVC y separacion por capas (controller/service/repository/entity), con identidad tecnica alternativa para un reinicio controlado.
- La propuesta de rebuild describe namespace y base alternativa para implementacion nueva sin afectar el contrato funcional del sistema actual.

## Nota 2026-04-26 (limpieza definitiva MySQL)

- Se aplico una migracion manual auditada, no automatica, para retirar deuda del esquema real `flacofitness`.
- El precheck y postcheck viven en `docs/db/cleanup-2026-04-26-*.sql`; el script de cambios es conservador y documenta backup previo.
- Las tablas actuales supervivientes del core son `usuarios`, `roles`, `planes`, `staff_perfiles`, `membresias_usuario`, `pagos`, `gastos`, `gastos_recurrentes`, `nominas`, `clases`, `sesiones_clase`, `reservas_sesion`, `asistencias`, `rutinas`, `usuario_rutina`, `trials`, `maquinas`, `materiales`, `activity_log`, `recent_visit` y `ux_memory_state`.
- `src/main/resources/data.sql`, `docs/data.sql` y `docs/init.sql` se eliminaron porque apuntaban a tablas/columnas legacy y podian confundir al siguiente agente.

## Nota 2026-04-26 (rescate anti-500)

- `NominaController` debe tratar las acciones POST de estado como comandos seguros: `emitir`, `pagar` y `cancelar` capturan `BusinessValidationException` y regresan al detalle con flash, nunca con 500.
- `TrialController` usa `TrialForm` como DTO de entrada para evitar binding fragil de relaciones JPA; `TrialService` resuelve `staffResponsableId` dentro de la transaccion.
- `TrialService.convertirAUsuarioConCredenciales` es el flujo preferido para conversion comercial porque crea credenciales reales si el usuario no existe.
- `MaquinaService` y `MaterialService` delegan en `GastoService` la creacion de gasto automatico de compra solo en altas, no en ediciones, para evitar duplicados.
- El buscador lightweight de selects en `app.js` conserva el `<select>` original; no debe volver a reconstruir opciones en memoria porque eso rompia submits MVC.
