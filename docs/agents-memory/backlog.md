# Backlog

## Prioridad alta

- Ejecutar QA visual profunda por modulo sobre MySQL real: `/`, `/acceso`, `/cliente`, `/usuarios`, `/rutinas`, `/pagos`, `/asistencias`, `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/gastos`, `/gastos/recurrentes`, `/nominas`, `/maquinas` y `/materiales`.
- Validar por navegador el bloque financiero completo: gastos, recurrentes, nominas y exportaciones PDF con MySQL real.
- Verificar smoke por perfiles reales (`ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE`, `CLIENTE`) incluyendo accesos denegados, redirecciones y visibilidad de sidebar.
- Cerrar detalles visuales menores del shell premium en vistas secundarias y formularios legacy.

## COMPLETADO 2026-04-22 (bloques 1-4)

- [x] Git limpio: tree commiteado, stash pre-recovery eliminado (era estado incompleto con root/sin-pass).
- [x] `PagoSchedulerService` deprecated eliminado.
- [x] Proyecciones JPA (*PorMesView, *PorPlanView, GastoPorCategoriaView) movidas de `repository/` a `model/dto/`.
- [x] Trial: filtro por rango de fechas (desde/hasta) añadido en controller, service, repo y template.
- [x] `AccessProfile.canAccessAsManager` corregido (indentacion de /nominas).
- [x] `UsuarioService.guardar()` cierra automaticamente trials pendientes con el mismo email.
- [x] Panel Cliente ampliado con seccion "Reservas activas" e "Historial de asistencias".

## Prioridad media

- Anadir pruebas MVC para rutas protegidas por perfil y render de las pantallas principales.
- Crear tests unitarios adicionales para reglas de `AccessProfile` y `ShellNotificationService`.
- Mejorar el panel cliente para resolver el usuario real de sesion cuando exista autenticacion formal.
- Documentar un flujo de defensa claro: trial -> usuario -> membresia -> pago -> sesion -> asistencia.
- Revisar si `spring.profiles.active` debe quedar por defecto en `local` o moverse a variable de entorno para despliegue real.
- Persistir historial de notificaciones importantes en backend para auditar alertas vistas/no vistas por usuario.

## Prioridad baja

- Migrar el PIN MVP a Spring Security cuando el proyecto deje de ser academico.
- Preparar exportacion CSV de pagos, asistencias, trials y sesiones.
- Mover todas las librerias frontend a recursos locales o WebJars para demos sin internet.
- Crear diagramas ER y diagrama de arquitectura en `docs/diagrams/`.
- Evaluar multi-gimnasio/multi-tenant solo si aparece un caso comercial real.
- Revisar codificacion de algunos markdown antiguos para eliminar restos de mojibake.
