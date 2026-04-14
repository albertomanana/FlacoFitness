# Backlog

## Prioridad alta

- Consolidar una primera version estable para defensa y valorar promocion controlada a `main`
- Completar una demostracion mas rica con datos semilla funcionales para dashboard, pagos y asistencias
- Validar el dashboard tambien sobre MySQL real con captura visual, para cerrar definitivamente la paridad entre `local-demo` y `mysql-real`
- Ejecutar bloque 4: eliminar buscador global del topbar, activar notificaciones funcionales y definir autenticacion simple por PIN con limite de intentos
- Revisar si el acceso PIN necesita persistencia de sesion mas larga o mensajes de bloqueo mas visibles en defensa

## Prioridad media
- Anadir CRUD visual para planes y roles si se necesita una defensa mas completa del modelo administrativo
- Crear diagramas de arquitectura y base de datos para la documentacion tecnica
- Anadir pruebas MVC de pagos para filtros por usuario/estado y regla anti-duplicados por `fecha_vencimiento`
- Anadir pruebas MVC o de integracion para rutinas sin objetivo y con fila clicable
- Implementar modo oscuro global respetando los design tokens ya definidos
- Anadir pruebas unitarias basicas para los servicios principales
- Anadir pruebas MVC o de integracion que cubran el render de la home y eviten regresiones silenciosas de Thymeleaf en el dashboard
- Anadir pruebas MVC o de integracion que cubran el render del detalle de usuario y su timeline para evitar regresiones silenciosas de Thymeleaf
- Reducir dependencia de CDNs: mover gradualmente librerias criticas (jQuery/DataTables/Sortable) a WebJars o recursos locales para demos offline
- Explorar animacion de transicion entre paginas para una experiencia mas fluida

## Prioridad baja

- Preparar scripts de despliegue y revision final de Docker
- Valorar exportacion simple de tablas a CSV si aporta valor real a la entrega final
- Investigar lazy-loading de imagenes de avatar en listados con muchos registros
- Documentar el sistema de design tokens en un archivo de referencia para futuras extensiones
