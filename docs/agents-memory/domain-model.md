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
- relacion opcional con `MembresiaUsuario` para conectar el cobro con un contrato comercial real sin romper pagos legacy

### StaffPerfil

Perfil operativo del equipo del gimnasio. No duplica personas: se vincula a `Usuario` mediante una relacion `OneToOne` y permite tratar a recepcion, entrenadores y gerencia como miembros internos del sistema.

Estado actual:
- entidad JPA implementada
- enum `RolStaff`: RECEPCION, ENTRENADOR, GERENTE, ADMINISTRACION
- campos de especialidad, activo/inactivo, fecha de alta y observaciones
- se puede asociar a sesiones y a rutinas como responsable operativo

### MembresiaUsuario

Contrato real entre un usuario y un plan. Resuelve la diferencia entre catalogo comercial (`Plan`) y la membresia concreta contratada por una persona.

Estado actual:
- entidad JPA implementada
- relacion `ManyToOne` con `Usuario` y `Plan`
- fechas de inicio/fin, estado, precio snapshot, origen y observaciones
- enum `EstadoMembresia`: ACTIVA, PENDIENTE, VENCIDA, CONGELADA, CANCELADA, PRUEBA
- sincroniza `Usuario.plan` y `Usuario.fechaProximoPago` cuando el contrato activo cambia

### Trial

Lead comercial o dia de prueba antes de convertirse en usuario real. Permite explicar el embudo de captacion de un gimnasio local.

Estado actual:
- entidad JPA implementada
- datos de contacto, origen, fecha de prueba, estado y staff responsable opcional
- enum `EstadoTrial`: PENDIENTE, ASISTIO, NO_ASISTIO, CONVERTIDO, CANCELADO
- conversion controlada a `Usuario` desde servicio, reutilizando email si ya existe

### Clase

Catalogo reutilizable de actividades grupales como HIIT, Yoga o Spinning. Define la actividad, no el horario concreto.

Estado actual:
- entidad JPA implementada
- nombre, descripcion, capacidad sugerida, activa y observaciones

### SesionClase

Ocurrencia programada de una `Clase`. Representa fecha, hora, cupo, estado, staff responsable y rutina opcional.

Estado actual:
- entidad JPA implementada
- relacion `ManyToOne` con `Clase`
- relacion opcional con `StaffPerfil` y `Rutina`
- enum `EstadoSesion`: PROGRAMADA, CANCELADA, FINALIZADA
- soporta registro de asistencias vinculadas a una sesion concreta

### ReservaSesion

Relacion entre `Usuario` y `SesionClase` para gestionar inscripciones, cupos y asistencia a clases.

Estado actual:
- entidad JPA implementada
- restriccion unica por usuario y sesion
- enum `EstadoReservaSesion`: RESERVADA, ASISTIO, CANCELADA, NO_ASISTIO
- permite reservar, quitar reserva y marcar asistencia desde la sesion

## Nota de modelado

El modelo mantiene `Plan` como catalogo comercial, `MembresiaUsuario` como contrato y `Pago` como cobro. Esta separacion evita duplicidades y hace que el proyecto sea explicable como producto SaaS realista para un gimnasio local.
