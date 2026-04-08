# Setup Status

Fecha de referencia: 2026-04-08

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Preparado | Maven Wrapper operativo, validacion local realizada con JDK 17 y perfil `local` con H2 sembrado mediante `data-local.sql` para levantar la UI sin depender de MySQL |
| Base de datos | Preparado | MariaDB local reparada, phpMyAdmin vuelve a conectar, `flacofitness` responde con `root` y `flaco_user`, y el esquema de `rutinas` se alinea automaticamente con la relacion `ManyToMany` hacia `usuario_rutina` |
| Docker | Preparado | `Dockerfile` y `docker-compose.yml` creados como base funcional |
| Backend | En progreso | Proyecto Spring Boot inicializado, entidades JPA, repositorios, servicios y controladores MVC base para usuarios, rutinas, pagos y asistencias, con almacenamiento local de fotos, estadisticas JSON agregadas en `/stats/dashboard`, pagos mensuales automaticos y manejo global de errores MVC |
| Frontend | En progreso | Layout administrativo consolidado con sidebar oscuro de alto contraste, dashboard con Chart.js y filtros por periodo, DataTables operativa en listados, fotos de usuario con fallback visual, preview local antes de subir, formularios asistidos y vistas de error alineadas con el sistema visual |
| Documentacion | Preparado | Carpeta `docs/agents-memory/` actualizada con decisiones, estado real del entorno, avances del dashboard, manejo de errores MVC y criterios del refactor funcional y visual |
| Repositorio Git | Preparado | Ramas `main` y `develop`, CI, plantillas, Dependabot y guias de workflow activas; las mejoras actuales se integran directamente en `develop` con memoria viva sincronizada |
