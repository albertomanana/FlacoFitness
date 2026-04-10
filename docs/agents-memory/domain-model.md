# Domain Model

## Entidades planeadas

### Usuario

Representa a una persona registrada en el sistema. Puede corresponder a cliente del gimnasio, entrenador o administrador, segun su rol y permisos funcionales futuros.

Estado actual:
- entidad JPA implementada con relacion a `Rol` y `Plan`
- campos disponibles para CRUD MVC: nombre, apellidos, dni, email, telefono, fecha de nacimiento, direccion, fotoPath, activo y fecha de registro
- campo `fechaProximoPago` anadido para soportar renovacion automatica de cuotas segun el plan asignado

### Rol

Define el tipo de usuario dentro del sistema, por ejemplo staff o cliente. Permitira organizar responsabilidades y flujos internos sin incorporar todavia seguridad avanzada.

Estado actual:
- entidad JPA implementada
- relacion `OneToMany` con `Usuario`

### Plan

Modela los planes o membresias del gimnasio, incluyendo nombre, precio, duracion y posibles beneficios asociados.

Estado actual:
- entidad JPA implementada
- relacion `OneToMany` con `Usuario`
- campo `activo` anadido para gestionar disponibilidad del plan

### Rutina

Agrupa una planificacion de entrenamiento asignable a uno o varios usuarios. Servira para registrar objetivos, tipo de rutina y estructura general del entrenamiento.

Estado actual:
- entidad JPA implementada
- relacion `ManyToMany` con `Usuario` mediante la tabla intermedia `usuario_rutina`
- enums `ObjetivoRutina` y `TipoRutina` definidos

### Ejercicio

Describe cada ejercicio individual que puede formar parte de una rutina, con informacion tecnica basica como nombre, grupo muscular y observaciones.

### RutinaEjercicio

Entidad intermedia para relacionar rutinas y ejercicios. Permitira registrar orden, series, repeticiones, tiempos o parametros especificos por ejercicio dentro de cada rutina.

### Asistencia

Registra la presencia de un usuario en el gimnasio en una fecha y hora determinada. Puede usarse para control de acceso y seguimiento de actividad.

Estado actual:
- entidad JPA implementada
- relacion `ManyToOne` con `Usuario`
- fecha automatica al persistir el registro

### Pago

Representa el registro de cobros o pagos asociados a usuarios y planes. Permitira controlar estado de cuotas, fechas y metodo de pago.

Estado actual:
- entidad JPA implementada
- relacion `ManyToOne` con `Usuario` y `Plan`
- enums `MetodoPago` y `EstadoPago` definidos
- monto sincronizado automaticamente desde el `Plan`
- referencia automatica generada con UUID
- generacion automatica mensual disponible a traves de servicio y scheduler configurable

## Nota de modelado

El modelo definitivo se concretara en la fase de implementacion del dominio. Este documento sirve como referencia base para mantener coherencia entre backend, vistas, base de datos y documentacion.
