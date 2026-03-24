# Domain Model

## Entidades planeadas

### Usuario

Representa a una persona registrada en el sistema. Puede corresponder a cliente del gimnasio, entrenador o administrador, según su rol y permisos funcionales futuros.

Estado actual:
- entidad JPA inicial implementada con relación a `Rol` y `Plan`
- campos base definidos: nombre, email, activo y fecha de registro

### Rol

Define el tipo de usuario dentro del sistema, por ejemplo administrador, entrenador o cliente. Permitirá organizar responsabilidades y flujos internos sin incorporar todavía seguridad avanzada.

Estado actual:
- entidad JPA inicial implementada
- relación `OneToMany` con `Usuario`

### Plan

Modela los planes o membresías del gimnasio, incluyendo nombre, precio, duración y posibles beneficios asociados.

Estado actual:
- entidad JPA inicial implementada
- relación `OneToMany` con `Usuario`

### Rutina

Agrupa una planificación de entrenamiento asignable a un usuario. Servirá para registrar objetivos y estructura general del entrenamiento.

Estado actual:
- entidad JPA inicial implementada
- relación `ManyToOne` con `Usuario`
- enum `ObjetivoRutina` definido

### Ejercicio

Describe cada ejercicio individual que puede formar parte de una rutina, con información técnica básica como nombre, grupo muscular y observaciones.

### RutinaEjercicio

Entidad intermedia para relacionar rutinas y ejercicios. Permitirá registrar orden, series, repeticiones, tiempos o parámetros específicos por ejercicio dentro de cada rutina.

### Asistencia

Registra la presencia de un usuario en el gimnasio en una fecha y hora determinada. Puede usarse para control de acceso y seguimiento de actividad.

Estado actual:
- entidad JPA inicial implementada
- relación `ManyToOne` con `Usuario`

### Pago

Representa el registro de cobros o pagos asociados a usuarios y planes. Permitirá controlar estado de cuotas, fechas y método de pago.

Estado actual:
- entidad JPA inicial implementada
- relación `ManyToOne` con `Usuario` y `Plan`
- enums `MetodoPago` y `EstadoPago` definidos

## Nota de modelado

El modelo definitivo se concretará en la fase de implementación del dominio. Este documento sirve como referencia base para mantener coherencia entre backend, vistas, base de datos y documentación.
