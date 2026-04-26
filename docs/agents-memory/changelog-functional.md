# Changelog Functional

## Criterio de uso

Registrar aqui cambios funcionales acumulativos que afecten comportamiento, modulos o alcance del producto. No usarlo para cambios puramente cosmeticos o tecnicos sin impacto funcional.

## Historial

### 2026-04-24 (Operations Deck — rediseño visual completo)

- Se lanzó un rediseño visual completo de la shell y los módulos con la estética "Operations Deck": dark-first, glassmorphism sutil, acento HUD cian `#38BDF8`, verde `#22C55E` preservado para marca y CTAs.
- Bloque 1: tokens de color renovados en `:root[data-theme="dark"]` con paleta táctica (azul near-black, superficies glass, borders luminosos). Tokens cian añadidos al root claro como fallback. Clase `.ff-tabular` para dígitos métricos.
- Bloque 2: sidebar con fondo glass, barra activa cian con glow, scanline animada en topbar (8 s, 6 % opacidad, off en reduced-motion), dropdowns glass, toggle rail persistido.
- Bloque 3: `.ff-kpi-card` evolucionada con eyebrow/chip/value/foot, corner ticks via pseudo-elementos, hover lift 1.005 con glow cian. `.ff-surface-card` recibe header con regla gradiente.
- Bloque 4: override global de DataTables (header Manrope, filas 52 px, paginación ghost pills, activo cian), formularios con input glass, focus ring cian halo, barra roja en error. Botones primary con gradiente verde glow.
- Bloque 5: `getChartColors()` actualizado con paleta temática via `getComputedStyle`; tooltip glass panel con borde cian y fuentes Manrope/Inter; re-render en cambio de tema.
- Bloque 6: stepper horizontal 4-estados (BORRADOR → EMITIDA → PAGADA, con rama CANCELADA) añadido a `nominas/detail.html`; CSS para nodos HUD, conector línea, estado cancelado dashed rojo; empty state `.ff-empty` con marco dashed cian y corner ticks; alert rail del dashboard con borde izquierdo semántico.
- Corrección de bug crítico preexistente: `UsuarioController.java` tenía `UserPhotoStorageService` declarado dos veces en el constructor — causa de ~200 errores en cascada. Eliminado parámetro duplicado; `BUILD SUCCESS` recuperado.

### 2026-04-24 (auth por cuenta + nominas — registrado por separado)

- Se sustituyo el acceso compartido por PIN por autenticacion por cuenta con `email/username + password`.
- `Usuario` ahora soporta `username`, `passwordHash` y `mustChangePassword`.
- Se anadio `PasswordEncoder` BCrypt y se mantuvo la capa de autorizacion basada en `AccessProfile`.
- `AccessSessionService` ahora autentica usuarios reales, mantiene bloqueo temporal por intentos y guarda identidad actual en sesion.
- `AuthBootstrapRunner` crea credenciales temporales para usuarios legacy sin password hash y fuerza cambio de password en el primer acceso.
- Se anadio cambio de password en `/cuenta/password` y reset temporal por admin desde la ficha de usuario.
- `ClientePortalController` deja de depender de un usuario demo y usa la cuenta autenticada.
- Se rehizo el login para aceptar email o username y una password real, con UI mas premium.
- Se profesionalizo el flujo manual de nominas:
  - guardado como borrador
  - emision posterior
  - marcado como pagada
  - cancelacion
  - generacion de gasto `NOMINA` al emitir
- Se rediseño `nominas/form.html` con preview lateral en vivo y `nominas/detail.html` como expediente salarial.
- Se reforzo `reportes/nomina-detalle.html` para que el PDF tenga mejor cabecera, branding y desglose.
- Se validaron `.\mvnw.cmd clean -DskipTests compile` y `.\mvnw.cmd test` con 21 pruebas en verde.
- Se corrigio un `500` real en login por `LazyInitializationException` al resolver el rol del usuario autenticado.
- Se redujo el bloqueo temporal del acceso a 1 minuto para hacer la recuperacion mas util en entorno local.
- Se dejo operativa una cuenta admin real en MySQL: `admin` / `FlacoAdmin2026!`.

### 2026-03-23

- Se creo la estructura inicial del proyecto FlacoFitness.
- Se dejaron preparados los modulos base de usuarios, rutinas, pagos y asistencias a nivel de vistas placeholder.
- Se incorporo la documentacion viva del proyecto dentro del repositorio.

### 2026-03-24

- Se configuro el repositorio Git con ramas base `main` y `develop`.
- Se anadieron plantillas y automatizaciones de GitHub para facilitar el trabajo continuo.
- Se implementaron las entidades JPA iniciales `Rol`, `Plan` y `Usuario`.
- Se implementaron las entidades JPA `Rutina`, `Asistencia` y `Pago`, junto con sus enums de dominio.
- Se anadio la capa inicial de repositorios Spring Data JPA y la configuracion base para validar persistencia con datos semilla.
- Se anadio la capa de servicios con validaciones de negocio basicas y excepciones personalizadas.
- Se implemento el CRUD MVC completo para `Usuario` con controlador y vistas Thymeleaf reutilizables.
- Se implemento el CRUD MVC completo para `Rutina`, incluyendo controlador, vistas Thymeleaf, seleccion de usuarios activos y edicion del objetivo mediante enum.
- Se implemento el CRUD MVC completo para `Pago`, con filtros por usuario, formularios reutilizables y soporte de enums para metodo y estado del pago.
- Se implemento el CRUD MVC de `Asistencia`, con registro simple, asignacion automatica de fecha actual y listado contextual por usuario.
- Se incorporo un sistema visual administrativo inspirado en Stitch con sidebar, topbar, dashboard real y tablas unificadas para los modulos principales.
- Se refinaron los formularios y vistas de detalle para que compartan el mismo sistema visual administrativo, con mejor jerarquia, paneles de contexto y grids de informacion.

### 2026-03-25

- Se corrigio la configuracion de conexion a MySQL para arrancar correctamente en entorno local.
- Se alineo la entidad `Plan` y los datos semilla con el esquema real de la base `flacofitness`.
- Se anadieron logs claros de diagnostico para errores de conexion y confirmacion explicita del datasource al iniciar la aplicacion.
- Se documento un script SQL para crear la base de datos si no existe.
- Se refactorizo el modelo para ampliar `Usuario` con `dni` y `fotoPath`, renombrar el rol base `ADMIN` a `STAFF` y actualizar las semillas asociadas.
- Se transformo `Rutina` para soportar relacion `ManyToMany` con `Usuario` mediante `usuario_rutina`, incorporando tambien el enum `TipoRutina`.
- Se simplifico `Pago` para derivar automaticamente `monto` desde `Plan` y generar `referencia` UUID sin entrada manual.
- Se simplifico `Asistencia` para asignar la fecha automaticamente y reducir la logica manual del registro.
- Se implemento la subida de fotos de usuario, almacenando archivos en `uploads/users/`, guardando `fotoPath` en base de datos y sirviendo las imagenes desde la aplicacion.
- Se incorporaron endpoints JSON de estadisticas en `/stats` para usuarios, pagos, asistencias y rutinas, listos para alimentar graficos reales del dashboard.

### 2026-03-26

- Se amplio `/stats/pagos` con una serie mensual de ingresos para soportar visualizacion temporal real en el dashboard.
- Se conecto el dashboard principal con los endpoints `/stats/*` mediante `fetch` y se anadieron graficos responsivos con Chart.js para usuarios, ingresos y asistencias.
- Se automatizo la generacion de pagos mensuales a partir del plan activo del usuario, incorporando `fechaProximoPago`, referencias UUID y scheduler configurable para ejecucion periodica.

### 2026-04-07

- Se reparo MariaDB local y se restablecio el acceso desde phpMyAdmin y desde la aplicacion.
- Se anadio una alineacion automatica de esquema para migrar el legado `rutinas.usuario_id` hacia la tabla intermedia `usuario_rutina`.
- Se corrigio el error 500 al crear rutinas asociadas a usuarios en la base MySQL real.
- Se mejoraron los datos semilla para evitar dependencia de IDs fijos en roles y planes.
- Se reforzo el contraste visual del logo en sidebar y de textos clave del dashboard para mejorar legibilidad.
- Se mantuvo un flujo local de demostracion controlado sin afectar datos reales de MySQL.
- Se refactorizo la interfaz para consolidar un sistema visual SaaS coherente en dashboard, sidebar, tablas y vistas de usuario.
- Se anadieron tablas interactivas con DataTables en usuarios, pagos, asistencias, rutinas y actividad reciente del dashboard.
- Se rehizo el dashboard principal con KPIs mas utiles, graficos de ingresos, asistencias, usuarios por plan y altas recientes.
- Se corrigio la visualizacion de fotos de usuario con fallback de avatar por defecto y preview consistente en lista, detalle y formulario.
- Se incorporo un resolver dedicado para `fotoPath`, evitando fallos por rutas relativas o inconsistentes en registros ya existentes.

### 2026-04-08

- Se corrigio la carga real de Chart.js, DataTables y scripts propios refactorizando el fragmento `footer` para que las librerias entren en todas las vistas.
- Se soluciono el fallo de DataTables en navegador anadiendo la dependencia requerida de `jQuery`, recuperando busqueda, paginacion y ordenacion en los listados.
- Se reforzo el contraste del sidebar y del bloque de operaciones para mejorar la legibilidad del menu lateral.
- Se simplifico el copy visual del dashboard, listados, formularios y vistas detail para que la interfaz se perciba mas cercana a un panel SaaS real.
- Se mejoraron microinteracciones de tablas, tarjetas, avatares y vistas con animaciones suaves de entrada y hover.
- Se fijo la tarjeta de foto de usuario en detalle para que permanezca estable durante el desplazamiento en escritorio.
- Se traslado la subida de foto de usuario desde la vista de detalle hacia la vista de edicion, manteniendo la previsualizacion en detalle y el cambio de imagen dentro del flujo natural de actualizacion del usuario.
- Se anadio un endpoint agregado `/stats/dashboard` con rango configurable para alimentar el panel principal con una sola lectura JSON.
- Se renovaron los KPIs del dashboard con pagos vencidos, renovaciones proximas, ingresos totales y filtros de periodo operativos.
- Se incorporo una tabla de ultimos pagos y un bloque de renovaciones proximas dentro del dashboard para reforzar su utilidad en una demo academica.
- Se anadieron animaciones numericas en los indicadores del dashboard y una recarga manual ligera de metricas sin salir de Thymeleaf.
- Se implemento un manejo global de errores MVC con vistas coherentes para 400, 404 y 500.
- Se mejoro el formulario de pagos para mostrar en tiempo real el plan detectado y el importe que se derivara automaticamente.
- Se mejoro el formulario de usuarios con previsualizacion inmediata de foto antes de subirla al servidor.

### 2026-04-08 (Rescate UI/UX completo)

- Se corrigio el bug de Chart.js en el dashboard: se reemplazo el spread superficial de opciones por un deep-merge que preserva la configuracion base al combinar con opciones por grafico, y se excluyen escalas cartesianas en charts radiales (doughnut/pie) que causaban fallo silencioso.
- Se corrigio la visibilidad de los canvases de Chart.js: el IntersectionObserver ya no aplica opacity:0 a los contenedores de graficos, permitiendo que Chart.js calcule dimensiones correctas al inicializar.
- Se corrigio el solapamiento entre la tarjeta de "Foto del usuario" y "Resumen" en la vista de detalle: se separo el wrapper sticky para que solo la foto sea sticky y el resumen quede siempre estatico debajo.
- Se refactorizo el CSS completo a filosofia mobile-first con breakpoints progresivos desde movil hacia escritorio.
- Se expandieron los design tokens semanticos: se anadieron variables para accent, success, warning, error, escala tipografica (text-xs a text-3xl), escala de espaciado (space-1 a space-12), y sombras graduales (shadow-xs a shadow-hover).
- Se implemento drag & drop de tarjetas KPI en el dashboard con SortableJS, incluyendo feedback visual al arrastrar, persistencia del orden en localStorage, y soporte tactil con delay para moviles.
- Se anadieron esqueletos de carga (shimmer) en los contenedores de graficos mientras se obtienen datos del servidor.
- Se implemento gestion de focus-ring para accesibilidad: el anillo de enfoque solo aparece al navegar con teclado, no con raton.
- Se anadio validacion de tamano de archivo (5 MB max) en la previsualizacion de foto de usuario en el formulario de edicion.
- Se limpiaron selectores CSS obsoletos y se consolidaron reglas duplicadas para reducir el tamano del archivo de estilos.
- Se anadieron microinteracciones de hover en items de detalle, items laterales y lineas de timeline con desplazamiento y cambio de borde suaves.

### 2026-04-09

- Se estabilizo la fuente de datos por perfil: el sembrado SQL queda deshabilitado por defecto en MySQL para evitar inconsistencias y contaminacion de datos demo.
- Se mejoro el diagnostico de arranque para que la ejecucion con MySQL/MariaDB sea explicable en defensa.
- Se refactorizo la UX de usuarios: se elimina el boton de "ojo" en listados y la fila completa es clicable para abrir el detalle sin romper acciones de editar/desactivar (incluye accesibilidad por teclado).
- En el detalle de usuario, la foto queda como solo consulta: el cambio de imagen se realiza exclusivamente desde la vista de edicion.
- Se corrigio una regresion critica del dashboard: la home fallaba al renderizar por truncar referencias de pago cortas con `substring(0, 8)`, lo que impedia que la pagina llegara a pintar los graficos.
- Se reforzo la fuente de verdad del dashboard serializando el estado inicial a JSON explicito desde el controlador, evitando depender de la serializacion implicita del template.
- Se endurecio la configuracion de Chart.js con fusion profunda de opciones y exclusion de escalas cartesianas en charts radiales, recuperando correctamente el grafico de barras de ingresos, el doughnut de usuarios por plan y las series temporales.
- Se validaron el perfil `local` y el dashboard en ejecucion real con servidor limpio y captura headless, confirmando render de KPIs, charts y datos sembrados.

### 2026-04-10

- Se convirtio la ficha de usuario en un centro de control con score, segmento, resumen inteligente, estado de pagos, ultima asistencia, total de asistencias y rutinas asignadas.
- Se anadio una timeline de usuario combinando asistencias, pagos recientes y el alta inicial del perfil sin duplicar estado en base de datos.

- Se cerro el bloque 4 del shell con eliminacion del buscador global del topbar, notificaciones accionables y una pantalla de acceso por PIN con limite de intentos y bloqueo temporal.
- Se reorganizo la barra lateral en secciones mas claras y se anadio una accion explicita para cerrar el acceso desde la sesion actual.
- Se incorporo un interceptor MVC ligero para exigir PIN antes de navegar por el panel y un panel de alertas global alimentado por estados reales de pagos, renovaciones y asistencias.
- Se valido el bloque 4 con compilacion Maven y pruebas en verde tras el refactor del shell y del acceso.
- Se incorporaron accesos rapidos desde el detalle de usuario hacia pagos, asistencias, edicion y rutinas filtradas por usuario.
- Se creo la ruta `/rutinas/usuario/{usuarioId}` para reutilizar la biblioteca de rutinas como vista contextual por miembro.
- Se amplio la capa de servicios con un agregado de perfil que compone datos de asistencias, pagos y rutinas manteniendo el controlador limpio y la logica explicable en defensa.
- Se valido en ejecucion real con perfil `local`: detalle de usuario, edicion, pagos por usuario, asistencias por usuario y rutinas por usuario responden correctamente.

### 2026-04-13

- Se cerro el bloque 2 de pagos con enfoque de coherencia financiera: el listado ahora admite filtros por usuario y estado sin romper DataTables ni rutas existentes.
- Se reemplazo la accion redundante de "ver" por filas clicables hacia detalle de pago y se mantuvo el enlace explicito al usuario dentro de cada registro para navegacion contextual.
- Se reforzo el formulario de pago con `fecha_vencimiento` visible y obligatoria, manteniendo `monto` derivado desde membresia/plan y referencia autogenerada.
- Se incorporo asistencia en frontend para pagos: deteccion de plan desde usuario, resumen de monto estimado y sugerencia operativa de vencimiento para reducir errores manuales.
- Se anadio validacion anti-duplicados por ciclo (`usuario + fecha_vencimiento`) en alta/edicion de pagos para eliminar la causa raiz de registros repetidos.
- Se normalizo el estado financiero al guardar: un pago `PAGADO` autocompleta `fecha_pago` cuando falta y pagos no pagados no arrastran fecha de cobro inconsistentes.
- Se validaron regresiones del modulo de pagos con compilacion Maven y pruebas de servicio (`PagoServiceTest`) en verde.
- Se cerro el bloque 3 de rutinas eliminando `objetivo` del dominio, formularios, listados, detalle, vistas de usuario y semillas locales.
- Se sustituyo el boton "Ver" por filas clicables en rutinas para homogeneizar la navegacion con asistencias y pagos.
- Se simplifico el panel de rutinas en home y contextos de usuario para mostrar solo tipo, usuarios y estado util.
- Se elimino el enum `ObjetivoRutina` y se actualizo la entidad `Rutina` para dejar el modelo mas limpio y defendible.

### 2026-04-15

- Se inicio el nucleo vendible SaaS de FlacoFitness con modulos reales de staff, membresias contractuales, trials comerciales, clases, sesiones programadas y reservas.
- Se modelo `StaffPerfil` como perfil operativo ligado a `Usuario`, evitando duplicar personas y manteniendo el rol `STAFF` como clasificacion base.
- Se mantuvo `Plan` como catalogo comercial y se anadio `MembresiaUsuario` como contrato real entre usuario y plan, con estado, fechas y precio snapshot.
- Se conecto `Pago` opcionalmente con `MembresiaUsuario` sin romper pagos existentes que solo dependen de usuario y plan.
- Se anadio `Trial` para gestionar leads y dias de prueba, con staff responsable opcional y conversion controlada a usuario real.
- Se separo el dominio de clases entre `Clase` como catalogo y `SesionClase` como ocurrencia programada con fecha, hora, staff, cupo y rutina opcional.
- Se incorporo `ReservaSesion` para inscribir usuarios a sesiones y registrar asistencia vinculada a una sesion concreta.
- Se actualizo `Asistencia` para seguir permitiendo check-in libre y aceptar una relacion opcional con `SesionClase`.
- Se ampliaron dashboard y notificaciones con staff activo, trials, sesiones de hoy y membresias activas/vencidas.
- Se anadieron vistas Thymeleaf para staff, membresias, trials, clases y sesiones, manteniendo Bootstrap, DataTables y JavaScript ligero.
- Se enriquecio el sembrado local/demo con datos de los nuevos modulos y se dejo `data.sql` seguro para MySQL real.

### 2026-04-16

- Se creo la rama `recovery/restore-core-saas-plan-a` y se preservo el estado sucio previo en un stash llamado `backup before recovery core saas plan a`.
- Se recupero el nucleo SaaS avanzado desde `feature/core-saas-modules` evitando continuar sobre la integracion parcial rota.
- Se elimino un artefacto temporal `.codex-temp/curlcookies.txt` que no aportaba valor al repositorio.
- Se corrigio Maven eliminando una ruta absoluta local a `javac` y actualizando Lombok a `1.18.44` para compilar de forma portable con JDK moderno.
- Se restauro la regla de datos segura: MySQL real no ejecuta seeds automaticos y conserva datos existentes.
- Se termino el acceso MVP por PIN con perfiles de sesion `ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE` y `CLIENTE`.
- Se anadio un panel cliente limitado en `/cliente` con membresia, pagos y rutinas propias.
- Se filtro sidebar/topbar segun perfil y se reforzo el interceptor para bloquear rutas no autorizadas, no solo ocultar enlaces.
- Se corrigio la incoherencia de staff: solo entrenadores o perfiles con `puedeImpartirClases` pueden ser responsables de sesiones o rutinas.
- Se ajusto el seeder demo para no asignar gerentes como instructores por accidente.
- Se anadio vista 403 coherente para accesos denegados.
- Se validaron compilacion y tests con `mvnw.cmd clean -DskipTests compile` y `mvnw.cmd test`.

### 2026-04-17

- Se reforzo el dashboard como panel financiero-operativo con comparativa `Ingresos vs Gastos` y KPIs conectados a inventario, trials y revision de maquinas.
- Se consolido el modo oscuro con selector en topbar y persistencia local para mantener continuidad visual entre sesiones.
- Se mejoro el centro de notificaciones con orden por criticidad y mensajes accionables para guiar operacion diaria.
- Se corrigio la inconsistencia de iconografia global incorporando Font Awesome de forma centralizada en el fragmento `head`.
- Se mejoro la UX de formularios con estados de validacion invalidos mas visibles en tema claro y oscuro.
- Se pulio la legibilidad de tablas responsive para reducir friccion en uso movil durante demos y operacion.
- Se sincronizo la documentacion viva de `docs/agents-memory` con el estado real de arquitectura, roadmap, backlog y setup.

### 2026-04-20

- Se fijo MySQL/phpMyAdmin como unica base de datos valida del proyecto, usando la base `flacofitness` y desactivando cualquier inicializacion destructiva.
- Se configuro `spring.sql.init.mode=never` y `spring.jpa.hibernate.ddl-auto=update` para conservar datos existentes mientras Hibernate crea columnas/tablas nuevas.
- Se anadio el reloj operativo persistido en `app_clock_settings`, visible en el topbar y configurable por ADMIN para simular meses anteriores o siguientes.
- Se conecto el reloj operativo con servicios de asistencias, pagos, membresias, sesiones, gastos, maquinaria, staff, trials, renovaciones y notificaciones.
- Se verifico la existencia de la base `flacofitness` y la tabla `app_clock_settings` desde MySQL con las credenciales `flaco_user/flaco_pass`.
- Se corrigio el flujo de hardcode del reloj operativo: el formulario ahora usa un panel `Ajustar` visible, el parseo de `datetime-local` es explicito y las automatizaciones financieras ya no bloquean el guardado de la fecha si fallan.
- Se anadio prueba MVC para garantizar que un `ADMIN` puede fijar el reloj operativo desde el topbar y que se persiste `simulado=true` con la fecha indicada.

### 2026-04-20 (Bloque financiero interno)

- Se profesionalizo el bloque financiero con controladores dedicados para `gastos/recurrentes` y `/nominas`, manteniendo el modelo existente como base de negocio y no como CRUD aislado.
- Se anadio exportacion PDF para gastos, plantillas recurrentes y nominas mediante HTML renderizado con Thymeleaf y OpenHTMLtoPDF.
- Se conecto la automatizacion de nominas al reloj operativo y a los schedulers financieros, de forma que los cierres recurrentes se puedan generar junto con pagos y gastos automaticos.
- Se corrigio el formulario legacy de gastos para que coincida con el modelo actual y no enlace campos inexistentes.

### 2026-04-21

- Se cerro la automatizacion temporal unica para pagos y gastos recurrentes.
- Se creo `FinancialAutomationService` como fachada central para actualizar vencidos y generar pagos, gastos recurrentes y nominas.
- Se creo `RecurrenceService` para calcular ciclos por duracion de plan o frecuencia de gasto desde un unico punto.
- Se elimino el doble disparo financiero: `OperationalClockController`, el scheduler diario, recurrentes y nominas delegan en la automatizacion central.
- Se extendio `EstadoPago` con `PROGRAMADO` y se marcaron automaticamente como `VENCIDO` los pagos no pagados con vencimiento anterior a la fecha operativa.
- Se mantuvo `MembresiaUsuario` como contrato primario de cuotas y `Usuario.plan` como compatibilidad legacy.
- Se corrigio `FinancialSchemaRepairRunner` para no usar `CURDATE()` de MySQL al normalizar estados legacy.
- Se retiraron defaults de fecha real en entidades de negocio; las fechas se asignan desde servicios con `OperationalClockService`.
- Se validaron compilacion y tests con 17 pruebas en verde, incluyendo pagos vencidos y gastos recurrentes idempotentes.

### 2026-04-22

- Se elimino definitivamente la simulacion de fecha/hora operativa: se retiro `POST /reloj-operativo`, se limpio el topbar de controles de ajuste y se dejo una sola fuente temporal basada en reloj real del sistema.
- Se eliminaron los artefactos persistidos de simulacion (`AppClockSetting` y su repositorio), evitando deuda tecnica y dobles fuentes de verdad temporal.
- Se ajustaron pruebas de servicios para instanciar `PagoService` y `GastoService` de forma directa (sin mockear clases concretas), corrigiendo fallos de Mockito inline con Java 25.
- Se revalido la suite completa con 16 pruebas en verde.
- Se corrigio el bug visual por el que los modulos podian aparecer en blanco aunque el HTML y los controladores estuvieran bien.
- La causa raiz del blanco estaba en la shell premium: splash y transiciones dependian demasiado de `ff-page-ready` y podian dejar `.ff-main` oculto.
- Se anadio un fail-safe en `static/js/app.js` para forzar visibilidad del shell y ocultar la splash/loading aunque una transicion falle.
- Se cambio la animacion de `.ff-main` en `static/css/styles.css` para que el contenido sea visible por defecto y la entrada sea decorativa, no bloqueante.
- Se validaron visualmente `usuarios` y `pagos` con Chrome headless tras el fix.
- Se dejo preparado un handoff documental completo para continuidad con Claude Code dentro de `docs/agents-memory/`.

### 2026-04-22 (Auditoria y correccion bloque financiero)

- Se ejecuto auditoria completa del bloque financiero: GastoService, PagoService, NominaService, GastoRecurrenteService, FinancialAutomationService, RecurrenceService, ShellNotificationService, StatsController, todos los controladores financieros y todos los templates del area.
- Se corrigio bug critico en `gastos/detail.html`: la vista accedia a `gasto.frecuencia` que no existe en la entidad `Gasto` (solo existe en `GastoRecurrente`). El campo correcto es `gasto.gastoRecurrente.frecuencia` con null-guards adecuados. El bug causaba `EL1008E PropertyAccessException` en Spring EL, rompiendo la vista de detalle de cualquier gasto.
- Se mejoró `gastos/detail.html` para mostrar el `estado` completo del gasto con badge semantico (PAGADO/VENCIDO/otros) y se anadio el campo `tipoGasto` que faltaba.
- Se corrigio `SaaSSchedulerService`: el cron diario ahora usa `${app.pagos.scheduler.cron:0 0 0 * * *}` desde properties en lugar de estar hardcodeado, y se anadio respeto de `${app.pagos.scheduler.enabled:true}` que existia en `application.properties` pero no se usaba.
- Se actualizo la documentacion viva (`module-status.md`, `backlog.md`, `setup-status.md`, `decisions-log.md`, `changelog-functional.md`, `claude-code-handoff.md`) para reflejar el estado real del bloque financiero tras la auditoria.

### 2026-04-22 (Plan `.claude` aterrizado a producto)

- Se creo `CLAUDE.md` en la raiz del repositorio para que Claude Code tenga reglas vigentes del proyecto, orden de lectura y restricciones no negociables.
- Se creo `docs/agents-memory/claude-plan-status.md` para traducir el archivo `.claude/claude_md_and_top_prompts_flacofitness_finance_gold.md` a un estado de ejecucion real dentro del repo.
- Se abrieron en UI los filtros financieros completos del modulo de gastos, reutilizando la capacidad ya implementada en `GastoService` y `GastoRepository`.
- `/gastos` ahora permite filtrar por estado, tipo, staff, maquina, material, proveedor y recurrencia, ademas de fecha y categoria.
- Se anadieron KPIs operativos de gasto fijo, gasto variable y vencimientos proximos en la vista de gastos.
- Se anadio exportacion PDF individual para detalle de gasto y se redujo ruido de botones redundantes en listados financieros basados en fila clicable.
- Se corrigio una regresion real en el nuevo PDF individual de gasto: OpenHTMLtoPDF estaba fallando por `meta` no autocerrado en XHTML; se normalizaron los `meta charset` de los templates PDF y el endpoint ya responde correctamente.

### 2026-04-22 (Dashboard premium + nominas premium)

- Se simplifico el dashboard principal para dejar un set corto de KPIs de negocio:
  - usuarios activos
  - pagos pendientes
  - ingresos del mes
  - gastos del mes
  - beneficio estimado
  - renovaciones proximas
  - trials pendientes
  - maquinas fuera de servicio
  - stock bajo
- Se elimino la duplicidad visual de KPIs y se retiro la grafica secundaria de altas para reforzar jerarquia.
- Se mantuvieron solo tres graficos principales en la home:
  - ingresos vs gastos
  - usuarios por plan
  - asistencias recientes
- Se implemento un rail premium de alertas accionables dentro del dashboard reutilizando `shellNotifications`, sin crear backend ni librerias nuevas.
- Se amplio `DashboardStatsResponse` con `maquinasFueraServicio` para mantener el KPI sincronizado con `/stats/dashboard`.
- Se rehizo la vista de detalle de nomina como expediente salarial:
  - hero con neto destacado
  - cards de base, bonus, deducciones y neto
  - trazabilidad con staff y gasto vinculado
  - bloque de desglose economico
- Se profesionalizaron los PDFs de nominas:
  - detalle individual con cabecera, estado y desglose
  - listado con columna de estado
- Se valido con sesion real:
  - `/` responde 200 y contiene rail de alertas, `Ingresos vs gastos` y `Asistencias recientes`
  - `/nominas` responde 200
  - `/nominas/{id}` responde 200 con un registro real de validacion
  - `/nominas/{id}/pdf` responde 200 `application/pdf`

### 2026-04-23

- Se ejecuto una pasada fuerte de coherencia global sin cambiar el dominio ni reescribir la app.
- Se normalizaron redirecciones post-accion para que crear, editar o cambiar estado lleve a la ficha o al contexto mas logico en `staff`, `materiales`, `maquinas`, `membresias`, `pagos`, `gastos` y `recurrentes`.
- Se enriquecio `staff/detail` como panel operativo con agenda del dia, clientes inactivos, nominas recientes y gastos relacionados.
- Se elevaron `maquinas/detail` y `materiales/detail` a mini paneles de control con riesgos, accesos rapidos y relacion directa con gastos.
- Se reforzo `membresias/detail` como ficha comercial con activacion, uso reciente y contratos visibles.
- Se rehizo `pagos/detail` para convertirlo en una ficha de cobro util, con accion de registrar cobro, apertura de usuario y resumen operativo.
- Se limpio `pagos/list` para reforzar filtros, eliminar ruido y mantener `Cobrar` como accion primaria en una tabla con fila clicable.
- Se volvieron mas contextuales varias notificaciones del shell para evitar enlaces genericos a listados sin filtro.
- Se corrigieron restos de copy roto o mojibake en `sidebar`, `staff/detail`, `pagos/list`, `pagos/detail`, `topbar`, `footer` y `NominaController`.
- Se ampliaron pruebas MVC para cubrir render de detalle de `staff`, `maquinas` y `materiales`.
- Se validaron `.\mvnw.cmd clean -DskipTests compile` y `.\mvnw.cmd test` con 21 pruebas en verde.

### 2026-04-23 (Producto premium e inteligencia)

- Se anadio una capa de inteligencia de producto para que el dashboard detecte:
  - usuarios en riesgo
  - membresias por caducar
  - material bajo
  - maquinas con revision proxima
  - pagos vencidos
  - gastos anomalos
- El dashboard ya muestra:
  - bloque `Requiere atencion`
  - onboarding inicial de tres pasos
  - `Hoy`
  - `Actividad reciente`
  - `Ultimos visitados`
- Se incorporo busqueda global real en topbar con resultados agrupados y pagina completa en `/busqueda`.
- Se anadio un FAB global por perfil para reducir friccion en altas y operaciones frecuentes.
- Se implemento memoria UX persistente por `browser_token + access_profile` para tooltips y estados de onboarding.
- Se implemento `RecentVisit` para recuperar ultimas fichas visitadas desde dashboard y topbar.
- Se implemento `ActivityLog` para reflejar actividad reciente del sistema y dar trazabilidad a acciones operativas clave.
- Se reforzo el registro de actividad en usuarios, staff, membresias, pagos, gastos, sesiones, trials, nominas, maquinas y materiales.
- Se mejoraron los empty states de modulos prioritarios con copy mas accionable y orientado a flujo.
- Se validaron nuevamente `compile` y `test` con 21 pruebas en verde.

### 2026-04-23 (Cierre profesional SaaS)

- Se elimino `fragments/navbar.html`, unico archivo de dead code confirmado con grep.
- Se anadieron tests de regresion en `ViewControllerTest` para `GET /gastos` y `GET /gastos/1`, cubriendo el bug `EL1008E` que causaba 500 silencioso en detalle de gasto.
- Se unificaron las KPI cards de `pagos/list.html`, `gastos/list.html` y `nominas/list.html` al sistema `.ff-kpi-card` con variantes semanticas `ff-kpi-positive/warning/danger/neutral`.
- Se normalizaron los status badges en `pagos/list.html` para usar `.ff-status-badge` en lugar de clases Bootstrap inline; se corrigio `ff-status-warning` → `ff-status-pending` en `nominas/list.html`.
- Se anadio `.ff-filter-panel` como wrapper unificado para los bloques de filtros en los modulos financieros.
- Se implemento color dinamico del beneficio estimado en el dashboard: `data-kpi-profit` en `home/index.html` + `updateProfitCardColor()` en `dashboard.js`.
- Se anadio `getChartColors()` en `dashboard.js` para que los tres charts (donut, barras, linea) usen colores adaptados al tema claro/oscuro del sistema.
- Se elimino el N+1 en `PagoService`: tres nuevas queries JPQL de agregacion en `PagoRepository` reemplazan el patron `findAll()` + loop por usuario.
- Se cerro el flujo Trial→MembresiaUsuario: `TrialService.convertirAUsuario()` auto-crea `MembresiaUsuario` cuando el trial convierte un usuario nuevo que tiene plan asignado y no tiene membresia activa.
- Se validaron `compile` y `test` con 21 pruebas en verde.

### 2026-04-26 (Estabilizacion premium y busqueda global v2)

- Se sustituyo el escaneo completo de la busqueda global por consultas limitadas en `UsuarioRepository`, `StaffPerfilRepository` y `SesionClaseRepository`.
- Se mantuvo intacto el endpoint `/api/busqueda/global` y la forma JSON usada por topbar y pagina `/busqueda`.
- Se anadio `GlobalSearchServiceTest` para cubrir query corta sin acceso a repositorios y resultados agrupados de usuarios, staff y sesiones.
- Se redujeron calculos repetidos en `StatsController`, `ViewController` y exportacion PDF de `GastoController`.
- `StaffController` ya no invoca dos veces `staffService.listarTodos()` para el mismo listado.
- `nominas/list.html` recibio filtros persistentes, copy mas limpio y empty state con accion directa.
- Los contadores del dashboard se hicieron mas suaves y `cookies.txt` quedo ignorado como artefacto local.
- Validacion: `compile`, `test` con 23 pruebas, MySQL en `localhost:3306` y smoke HTTP basico en puerto temporal `8081`.

### 2026-04-26 (Command Center UI)

- Se incorporo una capa visual final en `styles.css` para convertir la shell en un Command Center oscuro, tactico y premium.
- Dark mode pasa a ser el tema por defecto cuando no hay preferencia guardada; el `head` aplica el tema temprano para evitar flash claro.
- Se sustituyo la fuente visual por `Space Grotesk` e `IBM Plex Sans`.
- Se añadieron patrones CSS reutilizables para stack premium, HUD grids, panel grids, hero operativo, HUD cards, table shell y empty states.
- Dashboard, usuarios, cliente, pagos, gastos, recurrentes, asistencias, staff, rutinas y nominas activan `ff-command-stack`.
- Se añadio Anime.js UMD local y `hud-motion.js` para stagger reveals, hover HUD y hints ligeros con fallback si la libreria no carga.
- Validacion: compile, tests, MySQL, arranque temporal en `8082`, assets nuevos 200 y rutas protegidas 302 a `/acceso`.
