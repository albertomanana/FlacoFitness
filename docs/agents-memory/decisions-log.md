# Decisions Log

## Como registrar una decision

Usar una entrada por decision tecnica relevante. Mantener el registro en orden cronologico y reflejar tanto el motivo como el impacto esperado.

## Plantilla

| Fecha | Decision | Motivo | Impacto |
| --- | --- | --- | --- |
| AAAA-MM-DD | Describir la decision tecnica | Explicar por que se toma | Indicar consecuencias, beneficios o limitaciones |

## Registro inicial

| Fecha | Decision | Motivo | Impacto |
| --- | --- | --- | --- |
| 2026-03-23 | Adoptar una arquitectura monolitica MVC con Spring Boot y Thymeleaf | Se ajusta al alcance academico, facilita despliegue y simplifica la defensa del proyecto | Permite avanzar rapido con una base coherente y mantenible |
| 2026-03-24 | Adoptar flujo de ramas con `main` estable y `develop` como integracion | Mejora trazabilidad y reduce riesgo de mezclar trabajo inestable con entregas | Facilita colaboracion, revision y automatizacion del repositorio |
| 2026-04-07 | Ejecutar una alineacion automatica del esquema de `rutinas` al arrancar | La base MySQL conservaba `rutinas.usuario_id` aunque el dominio ya usa una relacion `ManyToMany` con `usuario_rutina` | Se migran relaciones legacy sin perder datos y se evita el error 500 al crear o editar rutinas |
| 2026-04-07 | Consolidar el dashboard con un endpoint agregado `/stats/dashboard` | El dashboard necesitaba mas metricas y series sin multiplicar llamadas ni complejidad en el frontend | Se simplifica la explicacion academica, se centraliza la lectura de KPIs y se mantiene Chart.js con JS ligero |
| 2026-04-07 | Resolver la URL publica de fotos de usuario mediante un componente dedicado | Los `fotoPath` existentes podian llegar en formatos distintos y la UI no tenia un fallback uniforme | Se asegura la visualizacion correcta en lista, detalle y formulario, con avatar por defecto sin romper usuarios existentes |
| 2026-04-07 | Integrar DataTables solo en listados clave y tabla reciente del dashboard | Se buscaba mejorar busqueda, ordenacion y paginacion sin convertir la aplicacion en SPA ni duplicar logica | Se gana usabilidad real con una libreria conocida y justificable, manteniendo Thymeleaf y controladores MVC intactos |
| 2026-04-08 | Reestructurar el fragmento `footer` para inyectar scripts globales desde Thymeleaf | El fragmento anterior solo devolvia el nodo `<footer>`, dejando fuera Chart.js, DataTables y el JS propio | Los graficos, las tablas interactivas y las mejoras de UX se aplican de forma consistente en todas las vistas |
| 2026-04-08 | Anadir `jQuery` como dependencia ligera de frontend para DataTables con Bootstrap 5 | La combinacion usada de DataTables y sus adaptadores Bootstrap/Responsive fallaba en navegador por `jQuery is not defined` | Se recupera la busqueda, paginacion y ordenacion sin rehacer los listados ni cambiar el stack MVC |
| 2026-04-08 | Simplificar el copy del panel y reforzar el contraste del sidebar | La interfaz seguia mostrando demasiado texto explicativo y el menu lateral perdia legibilidad frente al fondo | El sistema visual queda mas cercano a un SaaS administrativo real, con mejor jerarquia visual y explicacion academica mas clara |
