# Setup Status

Fecha de referencia: 2026-04-15

| Area | Estado | Detalle |
| --- | --- | --- |
| Entorno local | Preparado | Maven Wrapper operativo. La validacion debe ejecutarse con `mvnw.cmd` porque `mvn` global no esta disponible en esta maquina. |
| Base de datos local/demo | Preparado | Perfil `local` con H2 y `DemoDataSeeder` para datos ricos de usuarios, pagos, asistencias, rutinas, staff, membresias, trials, clases, sesiones y reservas. |
| Base de datos MySQL | Preparado con cautela | `data.sql` queda reducido a semilla segura de roles y catalogo minimo para evitar contaminar datos reales. Las nuevas columnas son compatibles con `ddl-auto=update`. |
| Backend | En progreso avanzado | CRUDs existentes conservados. Nuevo nucleo vendible implementado con `StaffPerfil`, `MembresiaUsuario`, `Trial`, `Clase`, `SesionClase` y `ReservaSesion`, mas integracion en pagos, asistencias, rutinas, dashboard y notificaciones. |
| Frontend | En progreso avanzado | Nuevas pantallas Thymeleaf para staff, membresias, trials, clases y sesiones, integradas con sidebar, topbar, cards, DataTables y filas/enlaces contextuales. |
| Dashboard | En progreso avanzado | `/stats/dashboard` ampliado con staff activo, trials pendientes/hoy, sesiones de hoy, membresias activas y vencidas manteniendo compatibilidad con los KPIs anteriores y Chart.js. |
| Documentacion | En curso | Memoria viva actualizada con el nuevo criterio de dominio: `Plan` como catalogo, `MembresiaUsuario` como contrato, `Pago` como cobro y `Clase/SesionClase` como separacion entre actividad y horario. |
| Repositorio Git | En curso | Trabajo actual en `feature/core-saas-modules`. Pendiente validar arranque local completo, ejecutar tests y subir rama/PR. |
