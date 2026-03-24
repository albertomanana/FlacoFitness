# Setup Status

Fecha de referencia: 2026-03-24

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | En progreso | Maven Wrapper operativo en el repositorio, validacion local realizada con JDK 17 y perfil `local` disponible para levantar la UI con H2 |
| Base de datos | Pendiente | Configuracion MySQL definida con variables de entorno, sin esquema final implementado |
| Docker | Preparado | `Dockerfile` y `docker-compose.yml` creados como base funcional |
| Backend | En progreso | Proyecto Spring Boot inicializado, entidades JPA, repositorios, servicios y controladores MVC base para usuarios, rutinas, pagos y asistencias |
| Frontend | En progreso | CRUD MVC de usuarios, rutinas, pagos y asistencias implementados con formularios, listados y vistas de detalle, ademas de un layout administrativo coherente con sidebar, topbar y detalle visual unificado |
| Documentacion | Preparado | Carpeta `docs/agents-memory/` creada con memoria viva inicial |
| Repositorio Git | Preparado | Ramas `main` y `develop`, CI, plantillas, Dependabot y guias de workflow anadidas |
