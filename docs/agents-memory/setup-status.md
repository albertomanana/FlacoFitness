# Setup Status

Fecha de referencia: 2026-04-10

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Preparado | Maven Wrapper operativo, validacion local realizada con JDK 17 y perfil `local` con H2 sembrado mediante `data-local.sql` para levantar la UI sin depender de MySQL |
| Base de datos | Preparado | MariaDB local reparada, phpMyAdmin vuelve a conectar, `flacofitness` responde con `root` y `flaco_user`, y el esquema de `rutinas` se alinea automaticamente con la relacion `ManyToMany` hacia `usuario_rutina`. El arranque ahora informa perfil activo, modo de datos (`mysql-real` o `local-demo`) e inicializacion SQL para evitar confusion en defensa |
| Docker | Preparado | `Dockerfile` y `docker-compose.yml` creados como base funcional |
| Backend | En progreso | Proyecto Spring Boot inicializado, entidades JPA, repositorios, servicios y controladores MVC base para usuarios, rutinas, pagos y asistencias, con almacenamiento local de fotos, estadisticas JSON agregadas en `/stats/dashboard`, pagos mensuales automaticos y manejo global de errores MVC. Semillas SQL deshabilitadas por defecto para MySQL y activas solo en perfil `local` (H2), manteniendo una sola fuente de verdad por ejecucion |
| Frontend | En progreso | CSS refactorizado a mobile-first con design tokens semanticos expandidos. Dashboard con Chart.js operativo y validado en navegador headless: la home ya no rompe al renderizar pagos recientes, el estado inicial del dashboard se inyecta como JSON explicito y los charts usan configuracion estable para barras, doughnut y lineas. DataTables en listados. UX de usuarios mejorada: filas clicables al detalle, foto solo editable desde formulario y ficha convertida en centro de control con score, timeline, accesos rapidos y modulos relacionados |
| Documentacion | Preparado | Carpeta `docs/agents-memory/` actualizada con decisiones, estado real del entorno, avances del dashboard, bloque 3 de usuarios, manejo de errores MVC, criterios del refactor visual mobile-first y adopcion de SortableJS |
| Repositorio Git | Preparado | Ramas `main` y `develop`, CI, plantillas y Dependabot activos. Identidad local vinculada a `albertomanana150307@alumnos.ilerna.com` para asegurar la visibilidad de contribuciones en GitHub. Flujo actual sobre rama de bloque 3 con memoria viva sincronizada |
