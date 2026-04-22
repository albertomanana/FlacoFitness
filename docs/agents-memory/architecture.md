# Arquitectura

## Descripcion general

FlacoFitness mantiene una arquitectura monolitica MVC con Spring Boot. La aplicacion renderiza vistas en servidor con Thymeleaf, usa Bootstrap 5 para interfaz y JavaScript ligero para interacciones concretas como dashboard, tablas, transiciones y filtros.

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
- La shell premium usa splash y transiciones, pero desde 2026-04-22 `.ff-main` queda visible por defecto y existe fail-safe para que un fallo visual no deje modulos en blanco.

### Controladores

- Reciben peticiones HTTP MVC.
- Preparan modelos para Thymeleaf.
- Delegan reglas de negocio a servicios.
- Mantienen rutas de modulos como usuarios, rutinas, pagos, asistencias, staff, membresias, trials, clases y sesiones.

### Servicios

- Centralizan reglas de negocio.
- Validan consistencia de relaciones.
- Evitan duplicidades funcionales.
- Orquestan casos de uso como alta de membresia, conversion de trial, reservas de sesion, pagos y check-in.
- Usan `OperationalClockService` como fuente temporal de negocio cuando una regla depende de "hoy" o "ahora".
- El cambio del reloj operativo persiste primero la fecha y ejecuta `FinancialAutomationService`; si una automatizacion falla, no se revierte el reloj.
- `FinancialAutomationService` es la unica fachada para automatizacion financiera: actualiza pagos/gastos vencidos y genera pagos, gastos recurrentes y nominas.
- `RecurrenceService` centraliza calculos de siguiente ciclo por duracion de plan o frecuencia recurrente.

### Repositorios

- Encapsulan persistencia con Spring Data JPA.
- Exponen consultas por estado, usuario, fechas, membresia, sesion o staff.

### Modelo

- Entidades JPA en `model.entity`.
- Enums en `model.enums`.
- DTOs de respuesta o agregados en `model.dto`.
- El modelo evita duplicar personas: `StaffPerfil` extiende operativamente a `Usuario`.

## Seguridad MVP

No se usa Spring Security en esta fase. El acceso esta protegido por un interceptor MVC y un PIN global configurable:

- `AccessSettings`: lee PIN, intentos maximos y bloqueo temporal.
- `AccessSessionService`: guarda acceso concedido, perfil de sesion y bloqueo.
- `AccessGuardInterceptor`: protege rutas y aplica permisos por perfil.
- `AccessProfile`: define perfiles y permisos de navegacion.

Perfiles actuales:

- `ADMIN`: control total.
- `STAFF_ENTRENADOR`: rutinas, clases, sesiones y asistencias; lectura limitada de usuarios.
- `STAFF_RECEPCION`: usuarios, trials, pagos operativos, asistencias y reservas.
- `STAFF_GERENTE`: vision de gestion, finanzas e inventario; no imparte clases por defecto.
- `CLIENTE`: panel personal limitado.

Esta capa es intencionadamente simple y defendible. Spring Security queda como evolucion futura.

## Modulos funcionales actuales

- `usuarios`: centro operativo del cliente, con foto, datos, pagos, asistencias, rutinas y resumen inteligente.
- `cliente`: panel limitado para perfil cliente.
- `staff`: perfiles internos ligados a usuarios.
- `membresias`: catalogo comercial basado en `Plan` y contratos mediante `MembresiaUsuario`.
- `trials`: gestion de leads y dias de prueba.
- `clases`: catalogo de actividades.
- `sesiones`: agenda de clases programadas, cupo, staff responsable, reservas y asistencia.
- `rutinas`: biblioteca y asignacion de entrenamientos, con staff responsable opcional.
- `pagos`: cobros asociados a usuario, plan y contrato cuando existe.
- `asistencias`: check-in libre o asistencia asociada a sesion.
- `gastos`: control financiero basico.
- `maquinas` y `materiales`: inventario operativo.

## Criterio de dominio

- `Plan` define una oferta comercial.
- `MembresiaUsuario` define el contrato real de un usuario.
- `Pago` registra cobros y estado financiero.
- `Clase` define una actividad reutilizable.
- `SesionClase` define una ocurrencia con fecha, hora, cupo y responsable.
- `ReservaSesion` conecta usuarios con sesiones.
- `Asistencia` conserva check-in libre y puede asociarse opcionalmente a una sesion.
- `StaffPerfil` se liga a `Usuario` para no duplicar identidad.

## Reloj operativo

El topbar muestra la fecha y hora operativa basada en el reloj real del sistema.

No existe ruta de ajuste manual del reloj. `OperationalClockService` expone una abstraccion de tiempo unica para reglas de negocio, pero siempre delega en la fecha y hora reales del servidor.

Al cambiar la fecha, la app ejecuta una automatizacion financiera central:

- marca pagos no pagados como `VENCIDO` cuando `fecha_vencimiento` queda antes de la fecha operativa;
- marca gastos abiertos como `VENCIDO` con la misma regla;
- genera pagos mensuales desde `MembresiaUsuario` activa y mantiene compatibilidad con `Usuario.plan`;
- genera gastos desde `GastoRecurrente` sin duplicar por plantilla y vencimiento;
- genera nominas automaticas si el staff tiene salario y automatizacion activa.

Excepciones permitidas de fecha real:

- bloqueo temporal del PIN en `AccessSessionService`, porque es seguridad de sesion y no tiempo de negocio;
- metadatos tecnicos de actualizacion del propio reloj;
- seeder demo desactivable, que no forma parte de la operacion real de MySQL.

## Convencion de paquetes

```text
com.flacofitness.app
├── config
├── controller
├── exception
├── model
│   ├── dto
│   ├── entity
│   └── enums
├── repository
├── security
├── service
└── util
```

## Reglas de mantenimiento

- No introducir SPA ni frameworks frontend pesados.
- No ejecutar seeds demo contra MySQL real.
- No introducir bases en memoria ni fallbacks de persistencia: MySQL/phpMyAdmin es la unica fuente de verdad.
- Mantener controladores finos y servicios con reglas de negocio.
- Mantener compatibilidad con datos legacy cuando una relacion nueva sea opcional.
- Documentar decisiones relevantes en `docs/agents-memory/decisions-log.md`.
