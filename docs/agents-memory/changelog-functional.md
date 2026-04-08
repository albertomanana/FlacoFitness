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
