# Setup Status

Fecha de referencia: 2026-04-08

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Preparado | Maven Wrapper operativo, validacion local realizada con JDK 17 y perfil `local` con H2 sembrado mediante `data-local.sql` para levantar la UI sin depender de MySQL |
| Base de datos | Preparado | MariaDB local reparada, phpMyAdmin vuelve a conectar, `flacofitness` responde con `root` y `flaco_user`, y el esquema de `rutinas` se alinea automaticamente con la relacion `ManyToMany` hacia `usuario_rutina` |
| Docker | Preparado | `Dockerfile` y `docker-compose.yml` creados como base funcional |
| Backend | En progreso | Proyecto Spring Boot inicializado, entidades JPA, repositorios, servicios y controladores MVC base para usuarios, rutinas, pagos y asistencias, con almacenamiento local de fotos de usuario, estadisticas JSON y pagos mensuales automaticos |
| Frontend | En progreso | Layout administrativo consolidado con sidebar oscuro de alto contraste, dashboard con Chart.js funcionando, DataTables operativa en listados, fotos de usuario con fallback visual, tarjeta sticky en detalle y copy simplificado para una experiencia mas cercana a un SaaS administrativo |
| Documentacion | Preparado | Carpeta `docs/agents-memory/` actualizada con decisiones, estado del entorno, dashboard agregado y criterios del refactor visual |
| Repositorio Git | Preparado | Ramas `main` y `develop`, CI, plantillas, Dependabot y guias de workflow activas; el trabajo en curso continua en rama feature con trazabilidad viva |
