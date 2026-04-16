# Backlog

## Prioridad alta

- Validar visualmente en navegador el flujo completo recuperado: `/`, `/acceso`, `/cliente`, `/usuarios`, `/rutinas`, `/pagos`, `/asistencias`, `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`, `/gastos`, `/maquinas` y `/materiales`.
- Probar acceso por perfiles reales:
  - `ADMIN`: ve y opera todo.
  - `STAFF_ENTRENADOR`: opera rutinas, clases, sesiones y asistencias.
  - `STAFF_RECEPCION`: opera usuarios, trials, pagos, asistencias y reservas.
  - `STAFF_GERENTE`: ve gestion, finanzas e inventario sin aparecer como instructor por defecto.
  - `CLIENTE`: solo panel propio y enlaces permitidos.
- Revisar el stash `backup before recovery core saas plan a` y rescatar manualmente cualquier detalle util que no exista en la rama recuperada.
- Validar contra MySQL real con `spring.sql.init.mode=never` para confirmar que no se reinsertan datos demo.

## Prioridad media

- Anadir pruebas MVC para rutas protegidas por perfil y render de las pantallas principales.
- Crear tests unitarios para reglas de `AccessProfile`.
- Mejorar el panel cliente para resolver el usuario real de sesion cuando exista autenticacion formal.
- Completar filtros especificos en sesiones, trials, gastos, maquinas y materiales.
- Documentar un flujo de defensa: trial -> usuario -> membresia -> pago -> sesion -> asistencia.
- Revisar si `spring.profiles.active` debe quedar por defecto en `local` o moverse a variable de entorno para despliegue real.

## Prioridad baja

- Migrar el PIN MVP a Spring Security cuando el proyecto deje de ser academico.
- Preparar exportacion CSV de pagos, asistencias, trials y sesiones.
- Mover todas las librerias frontend a recursos locales o WebJars para demos sin internet.
- Crear diagramas ER y diagrama de arquitectura en `docs/diagrams/`.
- Evaluar multi-gimnasio/multi-tenant solo si aparece un caso comercial real.
