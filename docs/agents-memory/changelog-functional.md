# Changelog Functional

## Criterio de uso

Registrar aqui cambios funcionales acumulativos que afecten comportamiento, modulos o alcance del producto. No usarlo para cambios puramente cosmeticos o tecnicos sin impacto funcional.

## Historial

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
- Se mantuvo el perfil `local` con H2 sembrado para desarrollo rapido sin dependencia de MySQL.
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

- Se estabilizo la fuente de datos por perfil: el sembrado SQL queda restringido al perfil `local` (H2) y se deshabilita por defecto en MySQL para evitar inconsistencias y contaminacion de datos demo.
- Se mejoro el diagnostico de arranque: el log de inicio ahora reporta el producto de base de datos y el catalogo activo (MySQL/MariaDB vs H2) para que la ejecucion sea explicable en defensa.
- Se refactorizo la UX de usuarios: se elimina el boton de “ojo” en listados y la fila completa es clicable para abrir el detalle sin romper acciones de editar/desactivar (incluye accesibilidad por teclado).
- En el detalle de usuario, la foto queda como solo consulta: el cambio de imagen se realiza exclusivamente desde la vista de edicion.
- Se corrigio una regresion critica del dashboard: la home fallaba al renderizar por truncar referencias de pago cortas con `substring(0, 8)`, lo que impedia que la pagina llegara a pintar los graficos.
- Se reforzo la fuente de verdad del dashboard serializando el estado inicial a JSON explicito desde el controlador, evitando depender de la serializacion implicita del template.
- Se endurecio la configuracion de Chart.js con fusion profunda de opciones y exclusion de escalas cartesianas en charts radiales, recuperando correctamente el grafico de barras de ingresos, el doughnut de usuarios por plan y las series temporales.
- Se validaron el perfil `local` y el dashboard en ejecucion real con servidor limpio y captura headless, confirmando render de KPIs, charts y datos sembrados.
