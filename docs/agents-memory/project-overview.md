# Project Overview

## Nombre del proyecto

FlacoFitness

## Que es hoy

FlacoFitness es una aplicacion web academica de gestion para gimnasio con enfoque SaaS administrativo. El proyecto ya no es solo un conjunto de CRUDs: tiene shell visual coherente, autenticacion por cuenta con password hash, dashboard, modulos conectados y automatizacion financiera interna.

## Objetivo actual

Dejar una base estable, profesional y defendible para seguir iterando sin romper:

- monolito MVC con Spring Boot + Thymeleaf
- una unica base de datos MySQL real
- experiencia administrativa tipo SaaS
- reglas de negocio explicables en defensa academica

## Stack principal

- Java 17 como target del build
- Spring Boot 3.3.5
- Spring Data JPA
- Thymeleaf
- Bootstrap 5
- JavaScript ligero
- Chart.js
- DataTables
- MySQL / phpMyAdmin
- Maven Wrapper
- OpenHTMLtoPDF para PDFs
- BCrypt via `spring-security-crypto`

## Estado funcional resumido

- Usuarios, rutinas, pagos y asistencias: implementados y conectados.
- Staff, membresias, trials, clases, sesiones y reservas: implementados.
- Gastos, recurrentes y nominas: implementados a nivel de dominio, servicios, vistas y exportacion PDF.
- Dashboard: operativo con metricas, graficos, notificaciones y un rail premium de alertas accionables.
- Dashboard premium v2: incorpora bloque `Requiere atencion`, guia inicial, actividad reciente y ultimos visitados.
- Acceso por cuenta con `email/username + password`: operativo.
- Shell premium "Operations Deck": dark-first, glassmorphism, acento cian HUD, barra activa deslizante, scanline topbar, HUD KPI cards con corner ticks, Chart.js paleta tematica, DataTables y forms reskinned, stepper de nominas 4-estados. Light mode conservado funcional.
- Nominas: formulario premium con preview en vivo, flujo `BORRADOR -> EMITIDA -> PAGADA/CANCELADA`, detalle tipo expediente y PDF profesional.
- Navegacion operativa: en progreso avanzado. Los modulos principales ya redirigen a detalle tras crear/editar y varias fichas secundarias dejaron de ser CRUDs planos.
- Productividad: FAB global por perfil, acciones contextuales, buscador global v1, filtros persistentes en navegador y memoria UX por modulo ya estan integrados.
- Trazabilidad de producto: existen `recent_visit`, `ux_memory_state` y `activity_log` para dar sensacion de SaaS vivo y no de simple CRUD.

## Base de datos

- Base valida unica: `flacofitness`
- Motor: MySQL / MariaDB en `localhost:3306`
- Usuario por defecto del proyecto: `flaco_user`
- Password por defecto del proyecto: `flaco_pass`
- `spring.sql.init.mode=never`
- `spring.jpa.hibernate.ddl-auto=update`
- Limpieza MySQL 2026-04-26 aplicada: se retiraron tablas legacy sin entidad (`app_clock_settings`, `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`) y columnas legacy de `gastos`/`reservas_sesion` tras backup SQL.
- Catalogo comercial activo simplificado: solo `Basico` a 29 EUR y `Estudiante` a 19 EUR quedan activos; `Premium`, `Plus` y `Trimestral` se conservan inactivos como historico.

No hay H2, no hay fallback en memoria y no debe reintroducirse.

## Estado tecnico actual

- Rama actual: `recovery/restore-core-saas-plan-a`
- El arbol de trabajo no esta limpio; no hacer reset ciego.
- `.\mvnw.cmd clean -DskipTests compile` pasa.
- `.\mvnw.cmd test` pasa.
- Suite actual validada: 35 tests en verde.
- La app arranca en `http://localhost:8080` cuando MySQL local esta disponible.
- En la validacion del 2026-04-26 MySQL respondio, la app arranco en `8083` y se verificaron rutas criticas con sesion ADMIN real.
- Credenciales bootstrap para usuarios legacy: password temporal definida por `app.auth.bootstrap-password` y obligacion de cambio al primer acceso.

## Ultima iteracion cerrada

En la iteracion 2026-04-26 se ejecuto la limpieza definitiva de base de datos y residuos:

- se creo backup SQL previo en `tmp/db-backups/flacofitness-cleanup-20260426-195946.sql`;
- se añadieron scripts auditables `docs/db/cleanup-2026-04-26-precheck.sql`, `cleanup-2026-04-26.sql` y `cleanup-2026-04-26-postcheck.sql`;
- se migraron datos utiles de columnas legacy de `gastos` antes de eliminarlas (`monto`, `descripcion`, `pagado`, `recurrente`, `frecuencia`, `staff_id`);
- `reservas_sesion.sesion_id` queda alineada con `sesiones_clase` y se retiraron columnas duplicadas legacy;
- se eliminaron los scripts SQL legacy `src/main/resources/data.sql`, `docs/data.sql` y `docs/init.sql` para impedir ejecuciones accidentales contra MySQL real;
- se limpio la UI de autenticacion con mostrar/ocultar password y se retiro CSS residual del antiguo reloj simulado;
- validacion cerrada: compile, 35 tests, postcheck MySQL limpio y smoke HTTP ADMIN en `/`, `/usuarios`, `/staff`, `/membresias`, `/trials`, `/pagos`, `/gastos`, `/nominas`, `/sesiones`, `/maquinas` y `/materiales`.

En la iteracion 2026-04-26 se cerro un bloque de estabilizacion premium enfocado en rendimiento, copy y QA:

- la busqueda global mantiene `/api/busqueda/global` y su JSON, pero ya no escanea listas completas en memoria para usuarios, staff y sesiones;
- dashboard, stats y PDF de gastos reutilizan calculos mensuales ya obtenidos para evitar llamadas repetidas;
- `StaffController` evita una doble carga del listado de staff;
- nominas y gastos recibieron una pasada ligera de copy/UX, filtros persistentes y empty state mas accionable;
- los contadores del dashboard se suavizaron y `cookies.txt` quedo tratado como artefacto local ignorado;
- la suite subio a 23 tests en verde con cobertura dedicada para `GlobalSearchService`;
- MySQL `localhost:3306` respondio y la app arranco correctamente en un puerto temporal para smoke HTTP basico.

En la iteracion 2026-04-26 tambien se ejecuto el rediseño Command Center de interfaz:

- dark mode pasa a ser experiencia principal por defecto, con light mode conservado como variante usable;
- `styles.css` incorpora una capa final Command Center con paleta tactica, glassmorphism, grids apilados, HUD cards, tablas compactas, forms y empty states;
- las pantallas clave (`dashboard`, `usuarios`, `cliente`, `pagos`, `gastos`, `recurrentes`, `asistencias`, `staff`, `rutinas`, `nominas`) activan `ff-command-stack`;
- se añadieron fuentes `Space Grotesk` e `IBM Plex Sans`;
- Anime.js queda servido localmente y `hud-motion.js` añade motion ligero con fallback seguro y respeto de `prefers-reduced-motion`;
- no se cambiaron rutas, controladores, contratos JSON ni reglas de negocio.

En la iteracion 2026-04-26 tambien se ejecuto el bloque Performance + Finanzas + Nominas Estables:

- `NominaController` dejo de bindear la entidad `Nomina` en formularios y ahora usa `NominaForm`, evitando errores por campos generados en backend (`salarioNeto`, `fechaEmision`, `estado`, `referencia`);
- `NominaService` queda reforzado como unica fuente para calcular neto, referencia, fecha real, emision, pago y gasto asociado de categoria `NOMINA`;
- se anadio `/finanzas` como Centro financiero MVC y `/stats/finanzas` como agregado JSON para ingresos, gastos, deuda, beneficio, pagos, recurrentes y nominas;
- el dashboard ya no dispara fetch automatico si ya tiene estado inicial renderizado por servidor y los charts se actualizan sin destruirse si conservan tipo;
- `hud-motion.js` evita animaciones de `box-shadow` y mantiene motion solo en `transform/opacity`;
- la suite subio a 33 tests en verde y MySQL `flacofitness` respondio correctamente.

En la iteracion 2026-04-24 se cerro el cambio mas estructural del producto:

- se sustituyo el acceso compartido por PIN por autenticacion por cuenta con `email/username + password` y hash BCrypt;
- se mantuvo `AccessProfile` como capa de autorizacion por perfil, sin introducir Spring Security web todavia;
- se anadio cambio de password obligatorio para cuentas bootstrap y reset temporal por admin;
- se rehizo el flujo de nominas para soportar borrador, emision, pago, cancelacion y gasto asociado al emitir;
- se rediseñaron login, cambio de password, builder de nomina y detalle documental para reforzar percepcion premium.

En la iteracion 2026-04-23 se trabajo sobre coherencia global y percepcion de producto, no sobre expansion de dominio:

- redirecciones post-accion mas naturales hacia fichas de detalle;
- menos botones redundantes en listados con fila clicable;
- detalles ricos para `staff`, `maquinas`, `materiales`, `membresias` y `pagos`;
- notificaciones con destinos mas utiles y menos enlaces genericos;
- limpieza de copy visible y correccion de restos de mojibake en vistas clave;
- ampliacion de `ViewControllerTest` para cubrir nuevas fichas secundarias;
- capa de inteligencia de producto para usuarios en riesgo, membresias por caducar y gastos anomalos;
- buscador global agrupado para usuarios, staff y sesiones;
- memoria UX persistente por navegador/perfil con tooltips first-use;
- panel de actividad reciente y ultimos visitados para reforzar continuidad operativa;
- auditoria ligera por perfil de sesion en altas, cambios de estado y operaciones clave.

## Reloj operativo

El proyecto ya no usa reloj simulado persistido. `OperationalClockService` sigue siendo la abstraccion temporal unica del negocio, pero ahora delega en la fecha y hora reales del sistema.

## Fuente de verdad para continuidad

Si otro agente o desarrollador entra al proyecto, debe leer en este orden:

1. `docs/agents-memory/claude-code-handoff.md`
2. `docs/agents-memory/agent-working-rules.md`
3. `docs/agents-memory/claude-plan-status.md`
4. `docs/agents-memory/architecture.md`
5. `docs/agents-memory/domain-model.md`
6. `docs/agents-memory/setup-status.md`
7. `docs/agents-memory/module-status.md`
8. `docs/agents-memory/decisions-log.md`
9. `docs/agents-memory/backlog.md`

## Nota de continuidad

Este repositorio ya paso por una recuperacion fuerte. Hay trabajo previo importante preservado en Git y en la rama actual. La prioridad no es crecer a lo loco, sino continuar con cambios pequenos, validados y documentados.

## Nota 2026-04-26 (rebuild total desde cero)

- Se documento una guia maestra para reconstruir el producto completo desde cero, modulo por modulo, en `docs/agents-memory/rebuild-from-zero-modular-guide.md`.
- La guia define una propuesta de identidad alternativa para el rebuild (`AtlasGym OS`) y una base alternativa (`atlasgym_core`) como blueprint de nueva implementacion.
- Este material no cambia el runtime actual de FlacoFitness; sirve como documento de ejecucion para un reinicio controlado y defendible.

## Nota 2026-04-26 (rescate funcional usuarios, trials, nominas e inventario)

- Se cerro un bloque de rescate orientado a evitar 500 en flujos simples: emitir/pagar/cancelar nominas ahora captura errores de negocio y vuelve con flash.
- El login con password temporal redirige directamente a `/cuenta/password`; el cambio de password refresca sesion y limpia `mustChangePassword`.
- `trials` deja de bindear la entidad completa en formularios y usa `TrialForm`; la conversion a usuario genera username, password temporal BCrypt y obliga cambio al entrar.
- `usuarios/form` mantiene el submit sticky y anade CTA visible en la columna derecha, preview de foto/username y carga de imagen junto al resumen.
- El buscador del selector de usuario en staff ya no reconstruye el `<select>`; solo oculta opciones y conserva seleccion valida.
- Maquinas y materiales generan un gasto `PAGADO` al alta cuando tienen coste de compra/coste unitario y stock.
- Validacion: `.\mvnw.cmd clean -DskipTests compile` correcto y `.\mvnw.cmd test` correcto con 43 tests en verde.
