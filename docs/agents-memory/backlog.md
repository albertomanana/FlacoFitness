# Backlog

## Prioridad alta

- Exponer `fechaProximoPago` y el estado de renovacion automatica en las vistas de usuario y pagos
- Consolidar una primera version estable para defensa y valorar promocion controlada a `main`
- Completar una demostracion mas rica con datos semilla funcionales para dashboard, pagos y asistencias
- Validar el dashboard tambien sobre MySQL real con captura visual, para cerrar definitivamente la paridad entre `local-demo` y `mysql-real`
- Continuar con bloque 4: robustecer asignacion de rutinas con fallback sin drag and drop y mejorar gestion multiusuario

## Prioridad media

- Anadir CRUD visual para planes y roles si se necesita una defensa mas completa del modelo administrativo
- Crear diagramas de arquitectura y base de datos para la documentacion tecnica
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
