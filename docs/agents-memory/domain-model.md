# Domain Model

## Vision general

El dominio actual diferencia identidad, operacion diaria y parte comercial. La regla clave es no mezclar catalogo, contrato y cobro:

- `Plan`: catalogo de membresias.
- `MembresiaUsuario`: contrato real de una persona.
- `Pago`: cobro o deuda.

Tambien se separa actividad y horario:

- `Clase`: actividad reutilizable.
- `SesionClase`: ocurrencia programada.
- `ReservaSesion`: inscripcion de usuario a una sesion.

## Entidades principales

### Usuario

Persona registrada en el sistema. Puede ser cliente, staff o admin segun su `Rol` y, para staff, su `StaffPerfil`.

Campos clave:
- datos personales y contacto
- `dni`
- `fotoPath`
- `activo`
- `fechaRegistro`
- `fechaProximoPago`
- relacion con `Rol`
- relacion legacy con `Plan`

La relacion legacy con `Plan` se mantiene por compatibilidad; el contrato real vive en `MembresiaUsuario`.

### Rol

Clasificacion base del usuario: `ADMIN`, `STAFF` o `CLIENTE` segun semillas y uso del sistema. No sustituye al perfil de acceso por PIN, pero ayuda a modelar identidad en base de datos.

### StaffPerfil

Perfil operativo ligado a `Usuario` mediante relacion `OneToOne`.

Campos clave:
- usuario
- especialidad
- `RolStaff`
- activo
- `puedeImpartirClases`
- fecha de alta
- observaciones

Regla recuperada:
- Un `ENTRENADOR` puede impartir clases y gestionar rutinas.
- Un `GERENTE` no imparte clases por defecto.
- Un staff no entrenador solo puede impartir clases si `puedeImpartirClases = true` de forma explicita.

### Plan

Catalogo comercial de membresias.

Campos clave:
- nombre
- precio mensual
- duracion
- tipo de membresia
- beneficios
- activo

### MembresiaUsuario

Contrato real entre usuario y plan.

Campos clave:
- usuario
- plan
- fecha inicio
- fecha fin
- estado
- precio snapshot
- origen
- observaciones

Permite que un usuario cambie de plan sin perder el historico comercial.

### Pago

Registro financiero de cobro o deuda.

Campos clave:
- usuario
- plan
- membresia usuario opcional
- monto
- fecha de pago
- fecha de vencimiento
- metodo de pago
- estado
- referencia

Los pagos legacy sin membresia contractual siguen siendo validos.

### Trial

Lead comercial o dia de prueba.

Campos clave:
- nombre y apellidos
- telefono
- email
- origen
- fecha de prueba
- estado
- staff responsable opcional
- observaciones

Permite convertir un lead a `Usuario`.

### Rutina

Entrenamiento asignable a varios usuarios.

Campos clave:
- nombre
- descripcion
- tipo de rutina
- activa
- fecha de creacion
- usuarios asignados
- staff responsable opcional

El campo ornamental `objetivo` fue eliminado del dominio para reducir ruido.

### Clase

Catalogo de actividades como Yoga, HIIT o Spinning. No representa una fecha concreta.

Campos clave:
- nombre
- descripcion
- capacidad sugerida
- activa
- observaciones

### SesionClase

Clase programada en una fecha y hora.

Campos clave:
- clase
- fecha
- hora inicio
- hora fin
- cupo
- estado
- staff responsable opcional
- rutina opcional
- observaciones

Regla recuperada:
- El staff responsable debe estar activo y ser entrenador o tener capacidad explicita para impartir clases.

### ReservaSesion

Relacion entre usuario y sesion.

Campos clave:
- usuario
- sesion clase
- estado de reserva
- fecha de reserva

Evita reservas duplicadas por usuario y sesion.

### Asistencia

Registro de presencia.

Campos clave:
- usuario
- fecha
- hora entrada
- observaciones
- sesion clase opcional

Puede representar check-in libre o asistencia vinculada a clase/sesion.

### Gasto

Registro de gasto operativo o financiero del gimnasio.

Campos clave:
- concepto
- categoria
- monto
- fecha
- estado
- observaciones

### Maquina

Elemento de equipamiento fisico.

Campos clave:
- nombre
- codigo
- estado
- ubicacion
- fecha de compra
- mantenimiento

### Material

Inventario operativo no necesariamente maquina.

Campos clave:
- nombre
- categoria
- cantidad
- estado
- ubicacion

## Enums relevantes

- `RolStaff`: RECEPCION, ENTRENADOR, GERENTE, ADMINISTRACION.
- `TipoMembresia`: MENSUAL, TRIMESTRAL, PREMIUM, ESTUDIANTE, PRUEBA.
- `EstadoMembresia`: ACTIVA, PENDIENTE, VENCIDA, CONGELADA, CANCELADA, PRUEBA.
- `EstadoTrial`: PENDIENTE, ASISTIO, NO_ASISTIO, CONVERTIDO, CANCELADO.
- `EstadoSesion`: PROGRAMADA, CANCELADA, FINALIZADA.
- `EstadoReservaSesion`: RESERVADA, ASISTIO, CANCELADA, NO_ASISTIO.
- `EstadoPago`: PENDIENTE, PAGADO, VENCIDO.
- `MetodoPago`: EFECTIVO, TARJETA, TRANSFERENCIA.
- `TipoRutina`: GENERAL, PERSONALIZADA.

## Compatibilidad legacy

- `Usuario.plan` no se elimina todavia.
- `Pago` puede existir sin `MembresiaUsuario`.
- `Asistencia` puede existir sin `SesionClase`.
- `Rutina` puede existir sin `StaffPerfil`.

Esta compatibilidad permite recuperar el proyecto sin romper datos ya creados.
