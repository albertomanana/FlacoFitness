# Prompts History

## Objetivo

Registrar prompts relevantes usados con agentes o asistentes para que el contexto del proyecto no dependa solo de conversaciones externas al repositorio.

## Plantilla sugerida

### Fecha

AAAA-MM-DD

### Contexto

Modulo o problema sobre el que se trabajo.

### Prompt

Texto del prompt utilizado.

### Resultado

Resumen breve del resultado obtenido o decision derivada.

## Entrada inicial

### Fecha

2026-03-23

### Contexto

Inicializacion del repositorio academico FlacoFitness.

### Prompt

Generar la estructura inicial completa del proyecto, incluyendo Maven, Spring Boot, Thymeleaf, Docker y documentacion viva en `docs/agents-memory`.

### Resultado

Se creo el esqueleto profesional del proyecto y la base documental editable para siguientes iteraciones.

## Entrada 2026-04-08

### Fecha

2026-04-08

### Contexto

Generacion de un prompt maestro para elevar de forma global la calidad del proyecto FlacoFitness sin romper la arquitectura MVC ni la explicabilidad academica.

### Prompt

Actua como un lead engineer senior especializado en Spring Boot, Thymeleaf, Bootstrap 5 y proyectos academicos defendibles. Quiero que analices y mejores de forma integral el proyecto FlacoFitness para llevarlo a un nivel 5 veces superior en calidad tecnica, coherencia visual, mantenibilidad, robustez funcional y capacidad de defensa academica.

Contexto real del proyecto:
- Nombre: FlacoFitness
- Stack: Java 17, Spring Boot 3, Spring Data JPA, Thymeleaf, Bootstrap 5, MySQL, Maven, Docker
- Arquitectura: monolito MVC
- Paquete base: com.flacofitness.app
- Ya existen CRUDs funcionales para usuarios, rutinas, pagos y asistencias
- Ya existe dashboard con Chart.js y tablas con DataTables
- Ya existe subida local de fotos de usuario
- Ya existe automatizacion de pagos mensuales
- Ya existe documentacion viva en docs/agents-memory
- El proyecto debe seguir siendo simple de explicar en una defensa academica

Restricciones no negociables:
- No convertir el proyecto en SPA
- No introducir React, Vue, Angular ni frameworks pesados
- Mantener Thymeleaf + Bootstrap + JavaScript ligero
- No romper funcionalidad existente
- No sobreingenierizar
- Mantener nombres claros, codigo limpio y separacion de responsabilidades
- Reutilizar la documentacion viva del repo y actualizarla
- Validar siempre con Maven Wrapper y, si aplica, con comprobacion visual o funcional

Primero analiza el proyecto completo antes de proponer cambios:
1. Revisa estructura, entidades, repositorios, servicios, controladores, vistas, recursos estaticos y documentacion viva.
2. Identifica puntos fuertes, deuda tecnica, incoherencias visuales, riesgos funcionales, gaps academicos y mejoras de alto impacto.
3. Resume el estado actual en un diagnostico breve pero preciso.

Despues genera un plan de mejora priorizado por impacto real, dividido en fases cortas y ejecutables. El plan debe cubrir como minimo:
- Arquitectura y mantenibilidad
- Calidad de modelo y base de datos
- Robustez de servicios y validaciones
- Controladores MVC y manejo global de errores
- Dashboard, metricas y UX
- Formularios, tablas, estados vacios y feedback visual
- Seguridad basica razonable para una futura evolucion
- Pruebas, seeds, consistencia de datos y facilidad de demo
- Documentacion tecnica y defensa academica

Luego ejecuta las mejoras mas importantes, con estas prioridades:

Prioridad 1. Robustez tecnica
- Revisar consistencia entre entidades y esquema MySQL
- Eliminar puntos fragiles de persistencia
- Mejorar validaciones de negocio y manejo de errores MVC
- Asegurar mensajes claros para errores de usuario y errores tecnicos
- Revisar consultas para evitar N+1 y cargas innecesarias

Prioridad 2. Calidad funcional
- Mejorar la experiencia real de usuarios, pagos, rutinas y asistencias
- Refinar dashboard para que los KPIs y graficos cuenten una historia clara
- Asegurar que formularios, listados y detalles sean consistentes entre modulos
- Completar pequenos vacios funcionales que aporten valor real a una demo academica

Prioridad 3. Calidad visual
- Consolidar un sistema visual SaaS administrativo coherente
- Mantener contraste correcto, jerarquia visual, cards limpias, tablas legibles y formularios comodos
- Reducir texto irrelevante y ruido visual
- Mejorar responsive y microinteracciones sin exagerar

Prioridad 4. Calidad academica
- Dejar el proyecto facil de defender
- Actualizar docs/agents-memory con decisiones, backlog, changelog y estado real
- Mejorar README, notas de arquitectura y argumentos de defensa cuando aporte valor

Forma de trabajo obligatoria:
- No hagas cambios arbitrarios; justifica cada bloque por impacto real
- Trabaja por slices pequenos pero completos
- Si tocas vistas, preserva Thymeleaf y rutas existentes
- Si tocas backend, preserva MVC y separacion controller-service-repository
- Si creas scripts o configuraciones, mantenlos simples y razonables
- Si detectas algo incorrecto en el proyecto, corrigelo y explicalo
- Usa comentarios solo cuando ayuden de verdad

Salida esperada:
1. Diagnostico inicial del proyecto
2. Plan priorizado de mejora
3. Implementacion real de las mejoras de mayor impacto
4. Validacion tecnica de lo cambiado
5. Resumen final claro con:
   - que se mejoro
   - que queda pendiente
   - por que las decisiones tomadas mejoran el proyecto de forma real

Quiero un resultado final que haga que FlacoFitness se vea y se sienta como una aplicacion academica muy solida, moderna, coherente y profesional, sin perder sencillez ni explicabilidad.

### Resultado

Se genero un prompt maestro reutilizable para auditoria, mejora integral y ejecucion progresiva del proyecto con foco tecnico, visual y academico.

## Entrada 2026-04-08 (Rescate UI/UX)

### Fecha

2026-04-08

### Contexto

Rescate total de UI/UX y hardening funcional tras detectar graficos rotos en dashboard y solapamiento en vista de detalle de usuario.

### Prompt

MegaPrompt extendido solicitando correcciones criticas (Chart.js, solapamiento foto/resumen), mejora UI/UX x20 con coherencia visual, microinteracciones y drag & drop, refactor CSS mobile-first, y actualizacion documental completa. Se aprobo SortableJS como libreria de drag & drop.

### Resultado

- **Bugs corregidos**: Chart.js renderiza correctamente (deep-merge de opciones + exclusion de escalas en charts radiales + visibilidad inmediata de canvases). Solapamiento foto/resumen eliminado (sticky solo en foto card, resumen siempre estatico).
- **CSS refactorizado**: 1400+ lineas reescritas a mobile-first con design tokens semanticos expandidos (accent/success/warning/error, escalas de tipografia y espaciado).
- **Drag & drop**: KPI cards reordenables con SortableJS, persistencia en localStorage, soporte tactil.
- **Microinteracciones**: shimmer loading en charts, hover/transform en detail items, focus-ring solo con teclado.
- **Archivos modificados**: `styles.css`, `app.js`, `dashboard.js`, `tables.js`, `drag-drop.js` (nuevo), `footer.html`, `detail.html` (usuarios).
- **Documentacion actualizada**: changelog-functional.md, decisions-log.md, backlog.md, setup-status.md, prompts-history.md.

### Handoff Note

Todo el trabajo quedo completo en esta sesion. No hay archivos a medio tocar. Para la siguiente iteracion:
1. Verificar graficos con datos reales persistidos en MySQL.
2. Hacer pruebas responsive en dispositivos fisicos (solo se verifico en viewport de navegador).
3. Abordar items del backlog: CRUD de planes/roles, modo oscuro, pruebas unitarias.
4. Considerar promocion a `main` si la defensa academica es inminente.

## Entrada 2026-04-15 (Nucleo vendible SaaS)

### Fecha

2026-04-15

### Contexto

Implementacion del nucleo vendible de FlacoFitness para evolucionar el proyecto academico hacia un MVP SaaS realista para gimnasio local.

### Prompt

Se solicito implementar un plan cerrado con staff, membresias, trials, clases, sesiones, horarios, reservas, integracion con pagos/asistencias y actualizacion de dashboard, semillas y documentacion. Restricciones: mantener MVC, Thymeleaf, Bootstrap, JavaScript ligero, no introducir Spring Security ni pagos online reales, y no romper CRUDs existentes.

### Resultado

- Se creo la rama `feature/core-saas-modules`.
- Se implementaron `StaffPerfil`, `MembresiaUsuario`, `Trial`, `Clase`, `SesionClase` y `ReservaSesion`.
- Se actualizaron `Plan`, `Pago`, `Asistencia` y `Rutina` para conectar el nuevo dominio sin romper compatibilidad.
- Se crearon repositorios, servicios, controladores y vistas Thymeleaf para staff, membresias, trials, clases y sesiones.
- Se amplio dashboard y notificaciones con KPIs de staff, trials, sesiones y membresias.
- Se enriquecio `DemoDataSeeder` para el perfil local/demo y se dejo `data.sql` seguro para MySQL.
- Se validaron compilacion, tests y rutas principales con servidor local.

## Entrada 2026-04-16 (Recuperacion segura)

### Fecha

2026-04-16

### Contexto

Recuperacion del proyecto tras una integracion parcial que rompio coherencia de base de datos, tests y modulos SaaS.

### Prompt

Implementar el plan de recuperacion segura de FlacoFitness: preservar estado sucio, crear rama de recuperacion, restaurar el nucleo avanzado desde `feature/core-saas-modules`, corregir Maven, recuperar Plan A de roles/perfiles, dejar MySQL sin seeds automaticos, validar con Maven y actualizar documentacion viva.

### Resultado

- Se creo `recovery/restore-core-saas-plan-a`.
- Se preservo el estado previo en stash `backup before recovery core saas plan a`.
- Se recupero el modelo SaaS avanzado.
- Se corrigio Maven para evitar ruta absoluta a JDK y fallo de Lombok con JDK moderno.

## Entrada 2026-04-22 (Handoff para Claude Code)

### Fecha

2026-04-22

### Contexto

Preparar la carpeta `docs/agents-memory/` para que Claude Code pueda continuar el desarrollo sin depender del historial conversacional externo.

### Prompt

Se pidio dejar la carpeta de contexto "super actualizada" para pasar a trabajar con Claude Code, incluyendo reglas, instrucciones, versiones, estado real del proyecto, validaciones, arquitectura, modulos, riesgos, flujo de trabajo y cualquier detalle operativo importante.

### Resultado

- Se actualizo `project-overview.md` como entrada corta y actual del proyecto.
- Se creo `claude-code-handoff.md` como documento principal de relevo.
- Se creo `agent-working-rules.md` con reglas no negociables del proyecto.
- Se creo `module-status.md` con estado modulo a modulo.
- Se actualizaron `setup-status.md`, `backlog.md`, `changelog-functional.md` y `decisions-log.md`.
- Se dejo documentado el fix del bug de modulos en blanco causado por la shell visual.
- Se termino acceso PIN con perfiles de sesion y panel cliente.
- Se corrigio staff responsable para que gerencia no aparezca como instructora por defecto.
- Se validaron compilacion y tests.

## Entrada 2026-04-17 (Cierre premium UX + documentacion)

### Fecha

2026-04-17

### Contexto

Inicio de implementacion del cierre final: pulido UX transversal, hardening visual del shell SaaS y sincronizacion de documentacion obligatoria para defensa.

### Prompt

Start implementation.

### Resultado

- Se aplicaron mejoras inmediatas de UX: iconografia global consistente, estados invalidos de formularios mas legibles y ajustes responsive de tablas.
- Se mantuvo continuidad de arquitectura (Spring MVC + Thymeleaf + JS ligero) sin introducir frameworks SPA.
- Se actualizaron `project-overview`, `setup-status`, `backlog`, `roadmap`, `changelog-functional` y `decisions-log` al estado real del producto.
- Se dejo trazabilidad del cierre con validacion tecnica de compilacion y pruebas en verde.

## Entrada 2026-04-20 (Correccion reloj operativo)

### Fecha

2026-04-20

### Contexto

El usuario reporto que el hardcode del reloj operativo no funcionaba y pidio sincronizar la carpeta de contexto del agente.

### Prompt

Corregir el error del hardcode del reloj y actualizar `docs/agents-memory/` con contexto actualizado.

### Resultado

- Se corrigio el controller para parsear explicitamente `datetime-local`.
- Se desacoplo el guardado del reloj de las automatizaciones financieras para que la fecha se persista aunque pagos/gastos fallen.
- Se reemplazo el formulario siempre desplegado por un panel `Ajustar` visible en topbar.
- Se anadio prueba MVC del flujo `ADMIN -> POST /reloj-operativo`.
- Se valido el flujo HTTP real con login PIN, guardado en MySQL y reset a fecha real.
- Se actualizaron `setup-status`, `changelog-functional`, `decisions-log`, `domain-model`, `architecture`, `project-overview`, `backlog` y este historial.

## Entrada 2026-04-21 (Automatizacion temporal unica de pagos y gastos)

### Fecha

2026-04-21

### Contexto

El usuario pidio cerrar definitivamente pagos y gastos recurrentes para que dependan siempre de la fecha simulada y no de la fecha real del servidor.

### Prompt

Implementar el plan de cierre: `OperationalClockService` como fuente unica temporal, `FinancialAutomationService` como motor central, recurrencias comunes, pagos/gastos vencidos, idempotencia y documentacion viva.

### Resultado

- Se creo `FinancialAutomationService` para centralizar actualizacion de vencidos y generacion de pagos, gastos recurrentes y nominas.
- Se creo `RecurrenceService` para calcular siguientes ciclos de cuotas y gastos desde un punto comun.
- Se elimino el doble disparo financiero entre reloj y schedulers.
- Se corrigio `FinancialSchemaRepairRunner` para no usar `CURDATE()`.
- Se quitaron defaults de fecha real en entidades de negocio donde los servicios ya asignan fecha operativa.
- Se actualizaron pruebas de pagos y se anadio `GastoServiceTest`.
- Se validaron `compile` y `test` con Maven Wrapper.

## Entrada 2026-04-22 (Ejecucion del plan `.claude`)

### Fecha

2026-04-22

### Contexto

El usuario pidio ejecutar el archivo `.claude/claude_md_and_top_prompts_flacofitness_finance_gold.md` y dejar el contexto actualizado hasta el punto alcanzado.

### Prompt

Ejecutar el plan `.md` dentro de `.claude` y actualizar la carpeta de contexto hasta donde se llegue.

### Resultado

- Se interpreto el archivo de `.claude` como guia maestra y no como checklist literal.
- Se materializo esa guia en `CLAUDE.md` dentro de la raiz del repo.
- Se creo `docs/agents-memory/claude-plan-status.md` para registrar que bloques del plan `.claude` ya estan absorbidos y cuales siguen pendientes.
- Se ejecuto una primera tanda funcional alineada con ese plan:
  - filtros financieros completos en `/gastos`
  - nuevos KPIs financieros en el listado
  - exportacion PDF individual por gasto
  - menos botones redundantes en listados financieros con fila clicable
- Se actualizo la documentacion viva para que Claude Code pueda continuar desde este punto sin rehacer la auditoria previa.

## Entrada 2026-04-22 (Roadmap maestro absorbido)

### Fecha

2026-04-22

### Contexto

Ejecucion real del roadmap maestro para absorber el archivo `.claude` dentro del producto, aterrizando primero finanzas premium y dashboard premium sin reescrituras grandes.

### Prompt

Implementar el plan "Roadmap Maestro Para Absorber El `.claude` Y Cerrar FlacoFitness", en este orden:

1. finanzas premium y coherencia operativa
2. dashboard premium y carrusel/rail de alertas
3. navegacion, UX y dark mode
4. limpieza tecnica y QA total
5. revision final tipo CTO/Product

## Entrada 2026-04-26 (documentacion total para rehacer desde cero)

### Fecha

2026-04-26

### Contexto

Solicitud de documentar completamente el proyecto modulo por modulo para rehacer la app desde cero por estado roto, incluyendo nombre alternativo y base de datos alternativa.

### Prompt

"quiero que me generes una documentacion total de lo que consiste el proyecto modulo por modulo explicandolo todo , para crear la app desde cero, ya que está rota, vamos a crear todo de 0 una base de datos alternativa y un nombre alternativo"

### Resultado

- Se creo `docs/agents-memory/rebuild-from-zero-modular-guide.md` como plan maestro de reconstruccion.
- El documento incluye arquitectura objetivo, setup, modelo de datos, 17 modulos funcionales, servicios transversales, orden por sprints, estrategia de tests, checklist de renombrado y SQL de base alternativa.
- Se propuso nombre alternativo `AtlasGym OS` y base alternativa `atlasgym_core`.

Con reglas explicitas:
- sin H2
- sin SPA
- sin rediseñar el dominio salvo bloqueo real
- usando el arbol actual como baseline
- validando compile/tests
- actualizando `docs/agents-memory`

### Resultado

- Se completo una iteracion fuerte sobre los bloques 1 y 2 del roadmap:
  - dashboard principal simplificado
  - rail premium de alertas accionables reutilizando `shellNotifications`
  - KPI de `maquinasFueraServicio` conectado a `/stats/dashboard`
  - eliminacion de la grafica secundaria de altas
  - mantenimiento de solo tres charts principales
- Se cerro una mejora visible y defendible en nominas:
  - detalle premium tipo expediente salarial
  - PDF individual profesional
  - PDF de listado con mejor lectura
- Se valido con:
  - `.\mvnw.cmd clean -DskipTests compile`
  - `.\mvnw.cmd test`
  - smoke HTTP real de `/`, `/nominas` y `/nominas/{id}/pdf`
- Se actualizo el contexto vivo para que Claude continúe desde este punto.

## Entrada 2026-04-23 (Coherencia global premium)

### Fecha

2026-04-23

### Contexto

Mejora transversal de calidad global, navegacion, detalles, tablas y continuidad UX sin reabrir el dominio ni la arquitectura.

### Prompt

Implementar la auditoria y plan de mejora global para FlacoFitness, priorizando coherencia sobre expansion, mejor navegacion, menos botones redundantes, detalles secundarios mas ricos, notificaciones mas utiles, limpieza de copy y validacion con compile y test.

### Resultado

- Se mejoraron redirects y retornos contextuales en modulos operativos clave.
- Se enriquecieron fichas de `staff`, `maquinas`, `materiales`, `membresias` y `pagos`.
- Se limpiaron listados con fila clicable y acciones mas claras.
- Se hicieron mas utiles varias notificaciones del shell.
- Se ampliaron pruebas MVC para validar nuevas fichas.
- Se cerraron `compile` y `test` con 21 pruebas en verde.

## Entrada 2026-04-23 (Producto premium, engagement y continuidad)

### Fecha

2026-04-23

### Contexto

Subir FlacoFitness de panel administrativo solido a producto SaaS con sensacion de pago, centrandose en inteligencia de producto, onboarding, productividad, continuidad de uso y trazabilidad.

### Prompt

Implementar el "Plan Maestro De Producto Premium Para FlacoFitness" con estos bloques:

- `ProductIntelligenceService` para usuarios en riesgo, membresias por caducar y gastos anomalos
- dashboard accionable con `Requiere atencion`, `Hoy`, `Actividad reciente` y `Ultimos visitados`
- FAB global por perfil y acciones contextuales
- memoria UX persistente con `browser_token + access_profile`
- busqueda global agrupada para usuarios, staff y sesiones
- auditoria ligera `activity_log`
- filtros persistentes en navegador
- documentacion viva completa

Restricciones: mantener MVC, Spring Boot, Thymeleaf, Bootstrap, JS ligero, MySQL `flacofitness`, fecha real del PC y sin reabrir el dominio central.

### Resultado

- Se crearon `ProductIntelligenceService`, `GlobalSearchService`, `RecentVisitService`, `UxMemoryStateService`, `ActivityLogService` y su soporte de cookie/interceptor para `browser_token`.
- Se anadieron las entidades `ux_memory_state`, `recent_visit` y `activity_log`.
- El dashboard ahora renderiza `attentionItems`, onboarding inicial, actividad reciente y ultimos visitados.
- Topbar incorpora buscador global y dropdown de visitas recientes; footer incorpora FAB global por perfil.
- Se anadieron tooltips first-use a modulos prioritarios y persistencia de filtros en navegador.
- Se reforzo el logging de actividad en usuarios, staff, membresias, pagos, gastos, sesiones, trials, nominas, maquinas y materiales.
- `.\mvnw.cmd clean -DskipTests compile` y `.\mvnw.cmd test` quedaron en verde.
- El smoke HTTP real no se pudo cerrar en esta sesion porque MySQL local devolvia `Connection refused`; se dejo documentado en `setup-status.md`.

## Entrada 2026-04-24 (Auth por cuenta + nominas profesionales)

### Fecha

2026-04-24

### Contexto

Subir el producto en cuatro frentes criticos sin romper el sistema: nominas exportables y profesionales, autenticacion seria por cuenta, rediseño fuerte de interfaz y dark mode premium.

### Prompt

Implementar:

- autenticacion por cuenta para admin, staff y cliente
- password hash segura
- cambio de password y reset temporal
- mejora fuerte del flujo de nominas con PDF y gasto asociado
- refinamiento visual de login, shell y builder de nomina
- actualizacion completa de `docs/agents-memory/*`

Restricciones:

- MySQL `flacofitness` como unica persistencia
- fecha real del PC
- MVC + Spring Boot + Thymeleaf + Bootstrap + JS ligero
- sin H2 y sin SPA

### Resultado

- El acceso por PIN quedo retirado del flujo principal y sustituido por login con `email/username + password`.
- `Usuario` incorpora `username`, `passwordHash` y `mustChangePassword`.
- `AccessSessionService` autentica con BCrypt y mantiene bloqueo temporal por intentos.
- `CuentaController` permite cambio de password y `UsuarioController` permite reset temporal por admin.
- `AuthBootstrapRunner` backfillea credenciales para cuentas legacy.
- `NominaService` ya soporta borrador, emision, pago, cancelacion y gasto de categoria `NOMINA` al emitir.
- `nominas/form.html`, `nominas/detail.html` y `reportes/nomina-detalle.html` se elevaron visualmente.
- `.\mvnw.cmd clean -DskipTests compile` y `.\mvnw.cmd test` quedaron en verde con 21 pruebas.
- El smoke HTTP real quedo pendiente porque `localhost:3306` no respondia en esta sesion.

## Entrada 2026-04-26 (Plan premium prioritario)

### Fecha

2026-04-26

### Contexto

Continuar el plan premium de FlacoFitness priorizando estabilidad, limpieza, rendimiento y pulido visible sin cambiar stack, rutas publicas ni contratos JSON.

### Prompt

Implementar el "Plan Premium Prioritario Para FlacoFitness" con foco en:

- corregir detalles visibles y codificacion/copy;
- optimizar calculos repetidos en dashboard, stats y PDFs;
- convertir la busqueda global en version ligera basada en repositorios;
- mantener MySQL `flacofitness`, MVC, Thymeleaf, Bootstrap y JS ligero;
- validar compile, tests, MySQL y smoke HTTP;
- actualizar documentacion viva.

### Resultado

- `/api/busqueda/global` conserva su contrato, pero ahora usa queries limitadas para usuarios, staff y sesiones.
- Se anadio `GlobalSearchServiceTest` y la suite queda en 23 pruebas.
- Se reutilizan calculos mensuales en `StatsController`, `ViewController` y PDF de gastos.
- `StaffController` evita doble consulta de staff.
- Se pulieron copy, filtros persistentes y empty state de nominas; los contadores del dashboard se suavizaron.
- `cookies.txt` quedo ignorado como artefacto local.
- Validacion: compile correcto, tests en verde, MySQL `localhost:3306` disponible y smoke HTTP basico en app temporal `8081`.

## Entrada 2026-04-26 (Command Center UI)

### Fecha

2026-04-26

### Contexto

Reestructurar la interfaz para corregir huecos visuales, apilar mejor el contenido, reforzar dark mode y dar un salto de navegacion/animacion con una estetica tactica tipo command center.

### Prompt

Implementar el plan "FlacoFitness Command Center":

- dark mode principal;
- layout stack premium;
- tarjetas HUD y glassmorphism sutil;
- topbar/sidebar como consola operativa;
- Anime.js local + motion ligero;
- pantallas clave: dashboard, usuarios/cliente, finanzas, asistencias, staff, rutinas y nominas;
- sin cambiar backend, rutas, JSON ni stack MVC.

### Resultado

- Se añadieron `Space Grotesk` e `IBM Plex Sans`.
- Dark mode es default si no hay preferencia guardada.
- `styles.css` incorpora capa Command Center con grids apilados, HUD cards, tablas, forms, empty states y contraste oscuro.
- Se marco `ff-command-stack` en pantallas clave.
- Se añadio Anime.js local y `hud-motion.js` con fallback seguro.
- Validacion: compile y tests en verde, MySQL disponible, app temporal `8082`, assets nuevos 200 y rutas protegidas redirigiendo a `/acceso`.

## Entrada 2026-04-26 (Performance + Finanzas + Nominas Estables)

### Fecha

2026-04-26

### Contexto

Optimizar tiempos de carga, animaciones y rendimiento general; hacer crecer Finanzas y reparar Nominas para que crear, emitir, pagar y exportar funcione de forma estable con fecha real y MySQL `flacofitness`.

### Prompt

Implementar el plan "Performance + Finanzas + Nominas Estables":

- crear `NominaForm` y dejar de bindear la entidad `Nomina`;
- anadir Centro financiero `/finanzas` y `/stats/finanzas`;
- evitar doble fetch del dashboard y animaciones pesadas;
- mantener MVC, Thymeleaf, Bootstrap, JS ligero y MySQL real;
- validar compile, tests, MySQL y smoke HTTP;
- actualizar documentacion viva.

### Resultado

- `NominaController` usa `NominaForm`; `NominaService` calcula neto, fecha, referencia, estado y gasto asociado.
- `/finanzas` entrega KPIs, riesgos, obligaciones, charts y quick actions conectadas.
- `/stats/finanzas` expone el agregado financiero JSON para el centro.
- Dashboard evita fetch inicial duplicado y actualiza charts sin destruirlos si el tipo se mantiene.
- `hud-motion.js` queda en transform/opacity y `finance-center.js` no contamina el scope global.
- Validacion: `compile`, 33 tests, MySQL disponible, app temporal `8081`, smoke HTTP protegido.

## Entrada 2026-04-26 (Limpieza definitiva de FlacoFitness)

### Fecha

2026-04-26

### Contexto

Limpiar profundamente MySQL, residuos de codigo, autenticacion, interfaz y scripts obsoletos sin reescribir la app ni perder datos relevantes.

### Prompt

Implementar el "Plan De Limpieza Definitiva De FlacoFitness":

- auditar entidades, repositorios, servicios, templates, CSS, JS, scripts SQL y tablas reales;
- crear backup y scripts de precheck/cleanup/postcheck para MySQL `flacofitness`;
- eliminar restos de H2 y simulacion temporal;
- limpiar scripts SQL legacy y residuos de interfaz/auth;
- validar compile, tests, MySQL y smoke HTTP real;
- actualizar `docs/agents-memory/*`.

Restricciones:

- MySQL `flacofitness` como unica persistencia;
- sin H2;
- sin borrar datos importantes sin backup;
- mantener MVC, Thymeleaf, Bootstrap y JS ligero;
- no tocar `atlasgym-os/` salvo orden explicita.

### Resultado

- Backup SQL creado en `tmp/db-backups/flacofitness-cleanup-20260426-195946.sql`.
- Scripts añadidos en `docs/db/cleanup-2026-04-26-precheck.sql`, `cleanup-2026-04-26.sql` y `cleanup-2026-04-26-postcheck.sql`.
- Eliminadas tablas legacy: `app_clock_settings`, `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`.
- Migrados datos utiles y eliminadas columnas legacy de `gastos` y `reservas_sesion`.
- Catalogo activo reducido a `Basico` y `Estudiante`, conservando planes legacy como inactivos.
- Eliminados `src/main/resources/data.sql`, `docs/data.sql` y `docs/init.sql`.
- Login y cambio de password incorporan mostrar/ocultar contraseña.
- Runtime sin H2 ni simulacion temporal activa en `src`.
- Validacion: `.\mvnw.cmd clean -DskipTests compile`, `.\mvnw.cmd test` con 35 pruebas, postcheck MySQL limpio y smoke ADMIN en rutas criticas con 200.

## Entrada 2026-04-26 (Rescate funcional anti-500)

### Fecha

2026-04-26

### Contexto

El usuario reporta muchos errores 500 en funciones basicas y bloqueos de uso real: nominas al emitir/cobrar, reset y cambio de password, formularios de usuario poco claros, trials fallando, selector de usuario en staff incomodo, e inventario sin gasto automatico.

### Prompt

Implementar el "Plan De Rescate Funcional Para FlacoFitness":

- eliminar 500 en acciones de nomina;
- hacer usable el reset temporal y cambio de password;
- mejorar formulario de usuario con preview y CTA claro;
- convertir trials con credenciales reales;
- arreglar buscador de usuario en staff;
- crear gastos automaticos desde maquinas/materiales;
- validar compile y tests.

### Resultado

- Nominas protegidas contra 500 en acciones de estado.
- Auth temporal validada con cambio obligatorio.
- Trials usan DTO y conversion con password temporal BCrypt.
- Usuarios tienen preview/foto en columna derecha y CTA reforzado.
- Staff search conserva el select real.
- Maquinas y materiales generan gasto pagado al alta con coste.
- Validacion: `compile` correcto y `test` correcto con 43 pruebas.
