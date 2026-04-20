# Backlog

## Prioridad alta

- Ejecutar QA funcional profundo por modulo sobre MySQL real: `/`, `/acceso`, `/cliente`, `/usuarios`, `/rutinas`, `/pagos`, `/asistencias`, `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/gastos`, `/maquinas` y `/materiales`.
- Validar en navegador el reloj operativo: fijar fecha, avanzar/retroceder meses, resetear a fecha real y comprobar dashboard/listados contra MySQL.
- Verificar smoke por perfiles reales (`ADMIN`, `STAFF_ENTRENADOR`, `STAFF_RECEPCION`, `STAFF_GERENTE`, `CLIENTE`) incluyendo accesos denegados y redirecciones.
- Cerrar deuda de consistencia visual en iconos/acciones secundarias de formularios legacy no migrados al shell premium.
- Revisar el stash `backup before recovery core saas plan a` y confirmar oficialmente si se archiva o descarta.

## Prioridad media

- Anadir pruebas MVC para rutas protegidas por perfil y render de las pantallas principales.
- Crear tests unitarios para reglas de `AccessProfile`.
- Mejorar el panel cliente para resolver el usuario real de sesion cuando exista autenticacion formal.
- Completar filtros especificos en sesiones, trials, gastos, maquinas y materiales.
- Documentar un flujo de defensa: trial -> usuario -> membresia -> pago -> sesion -> asistencia.
- Revisar si `spring.profiles.active` debe quedar por defecto en `local` o moverse a variable de entorno para despliegue real.
- Persistir historial de notificaciones importantes en backend para auditar alertas vistas/no vistas por usuario.

## Prioridad baja

- Migrar el PIN MVP a Spring Security cuando el proyecto deje de ser academico.
- Preparar exportacion CSV de pagos, asistencias, trials y sesiones.
- Mover todas las librerias frontend a recursos locales o WebJars para demos sin internet.
- Crear diagramas ER y diagrama de arquitectura en `docs/diagrams/`.
- Evaluar multi-gimnasio/multi-tenant solo si aparece un caso comercial real.
