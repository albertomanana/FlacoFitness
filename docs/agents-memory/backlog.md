# Backlog

## Prioridad alta

- Validar en navegador el flujo completo de los nuevos modulos: `/staff`, `/membresias`, `/trials`, `/clases`, `/sesiones`.
- Ejecutar `mvnw.cmd test` y resolver cualquier regresion de servicios o plantillas.
- Probar arranque con perfil `local` y confirmar que dashboard, charts, DataTables y nuevas rutas cargan sin error 500.
- Crear PR de `feature/core-saas-modules` hacia `develop` cuando la validacion local quede cerrada.
- Revisar si el flujo de conversion de trial a usuario necesita asignar membresia inicial automaticamente.

## Prioridad media

- Anadir pruebas MVC para render de las nuevas pantallas principales.
- Anadir filtros especificos en sesiones por fecha, staff y estado.
- Anadir filtros especificos en trials por estado, origen y fecha de prueba.
- Mejorar el formulario de usuario para enlazar alta de membresia justo despues de crear un nuevo usuario.
- Mejorar el detalle de staff con calendario de proximas sesiones y rutinas asignadas.
- Mejorar el detalle de membresia de usuario con pagos asociados y renovacion rapida.
- Completar diagrama ER en `docs/diagrams`.

## Prioridad baja

- Evaluar exportacion CSV de pagos, asistencias, trials y sesiones.
- Preparar datos demo reducidos para MySQL opcional sin contaminar bases reales.
- Mover DataTables/jQuery/Sortable a recursos locales o WebJars para demos sin internet.
- Documentar una guia de defensa academica con casos de uso: lead -> trial -> usuario -> membresia -> pago -> sesion -> asistencia.
