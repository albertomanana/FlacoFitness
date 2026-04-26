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

En 2026-04-23 se anadio una capa de dominio de producto ligera, enfocada a experiencia y trazabilidad:

- `UxMemoryState`: memoria UX por navegador y perfil.
- `RecentVisit`: ultimas fichas visitadas.
- `ActivityLog`: actividad reciente del sistema.

## Entidades principales

### Usuario

Persona registrada en el sistema. Puede ser cliente, staff o admin segun su `Rol` y, para staff, su `StaffPerfil`.

Campos clave:
- datos personales y contacto
- `dni`
- `fotoPath`
- `username`
- `passwordHash`
- `mustChangePassword`
- `activo`
- `fechaRegistro`
- `fechaProximoPago`
- relacion con `Rol`
- relacion legacy con `Plan`

La relacion legacy con `Plan` se mantiene por compatibilidad; el contrato real vive en `MembresiaUsuario`.

### Rol

Clasificacion base del usuario: `ADMIN`, `STAFF` o `CLIENTE` segun semillas y uso del sistema. No sustituye al `AccessProfile` derivado para autorizacion, pero ayuda a modelar identidad y permisos base en base de datos.

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

Catalogo activo tras la limpieza 2026-04-26:
- `Basico`: activo, 29 EUR, mensual.
- `Estudiante`: activo, 19 EUR, mensual.
- `Premium`, `Plus` y `Trimestral`: historicos inactivos; no se eliminan para conservar trazabilidad.

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

Reglas de automatizacion:
- `MembresiaUsuario` activa es la fuente primaria para cuotas recurrentes.
- `Usuario.plan` y `Usuario.fechaProximoPago` se mantienen como compatibilidad legacy.
- La idempotencia funcional se mantiene por usuario/membresia y `fecha_vencimiento`.
- Si la fecha operativa supera el vencimiento y el pago no esta pagado, pasa a `VENCIDO`.

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
- sesion clase mediante `reservas_sesion.sesion_id` apuntando a `sesiones_clase.id`
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
- tipo de gasto
- importe
- fecha
- fecha vencimiento
- estado
- proveedor
- staff responsable opcional
- maquina opcional
- material opcional
- plantilla recurrente opcional
- observaciones

Los gastos recurrentes se generan desde `GastoRecurrente`, no desde el propio `Gasto`, para separar plantilla y cargo real.

### GastoRecurrente

Plantilla de gasto periodico.

Campos clave:
- concepto
- categoria
- tipo de gasto
- importe
- frecuencia
- fecha inicio
- fecha proximo cargo
- relaciones opcionales con staff, maquina o material
- activo

La combinacion plantilla + vencimiento evita duplicar cargos aunque la automatizacion se ejecute varias veces.

### Nomina

Nomina experimental vinculada a `StaffPerfil` y opcionalmente a `Gasto`.

Campos clave:
- staff perfil
- periodo
- fecha emision
- salario base
- bonus
- deducciones
- salario neto
- estado
- referencia
- gasto asociado

Estados principales:
- `BORRADOR`
- `EMITIDA`
- `PAGADA`
- `CANCELADA`

Reglas:
- no se puede duplicar `staff + periodo`;
- `salarioNeto = salarioBase + bonus - deducciones`;
- al emitir puede generar automaticamente un `Gasto` de categoria `NOMINA`.

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

### UxMemoryState

Memoria de onboarding y first-use persistida por navegador y perfil de acceso.

Campos clave:
- browser token
- access profile
- module key
- tooltip seen
- empty state dismissed
- guide step state

No representa negocio de gimnasio; representa continuidad de producto y reduccion de friccion.

### RecentVisit

Historial corto de ultimas fichas consultadas.

Campos clave:
- browser token
- access profile
- entity type
- entity id
- title
- url
- icon key
- visited at

Se limita a 10 registros por navegador/perfil para no inflar la tabla.

### ActivityLog

Registro ligero de actividad de producto para continuidad operativa.

Campos clave:
- browser token
- access profile
- modulo
- accion
- entidad y entidadId opcionales
- created at

## Nota 2026-04-26 (Rebuild de dominio desde cero)

- Se documento un blueprint integral de dominio para reconstruccion completa en `docs/agents-memory/rebuild-from-zero-modular-guide.md`.
- El documento organiza el dominio por verticales (usuarios, staff, membresias, pagos, trials, clases, sesiones, asistencias, gastos, recurrentes, nominas, inventario, dashboard y UX memory) para rehacer la aplicacion sin arrastrar deuda tecnica.

Auditoria ligera de experiencia y operacion.

Campos clave:
- module key
- action key
- entity type
- entity id
- title
- description
- actor profile
- browser token
- route
- occurred at

Regla importante:
- el actor persistido sigue siendo el perfil de sesion actual (`ADMIN`, `STAFF_RECEPCION`, etc.), aunque la autenticacion ya sea individual por cuenta.

## Enums relevantes

- `RolStaff`: RECEPCION, ENTRENADOR, GERENTE, ADMINISTRACION.
- `TipoMembresia`: MENSUAL, TRIMESTRAL, PREMIUM, ESTUDIANTE, PRUEBA.
- `EstadoMembresia`: ACTIVA, PENDIENTE, VENCIDA, CONGELADA, CANCELADA, PRUEBA.
- `EstadoTrial`: PENDIENTE, ASISTIO, NO_ASISTIO, CONVERTIDO, CANCELADO.
- `EstadoSesion`: PROGRAMADA, CANCELADA, FINALIZADA.
- `EstadoReservaSesion`: RESERVADA, ASISTIO, CANCELADA, NO_ASISTIO.
- `EstadoPago`: PROGRAMADO, PENDIENTE, PAGADO, VENCIDO.
- `EstadoGasto`: PROGRAMADO, PENDIENTE, PAGADO, VENCIDO, CANCELADO.
- `EstadoNomina`: BORRADOR, EMITIDA, PAGADA, CANCELADA.
- `MetodoPago`: EFECTIVO, TARJETA, TRANSFERENCIA.
- `TipoRutina`: GENERAL, PERSONALIZADA.

## Compatibilidad legacy

- `Usuario.plan` no se elimina todavia.
- `Pago` puede existir sin `MembresiaUsuario`.
- `Asistencia` puede existir sin `SesionClase`.
- `Rutina` puede existir sin `StaffPerfil`.
- El tiempo de negocio se consulta mediante `OperationalClockService`, pero ya no existe entidad persistida de reloj simulado.
- La memoria UX y los filtros persistentes no alteran el dominio central: son capas auxiliares de producto.

Limpieza fisica aplicada en MySQL el 2026-04-26:
- eliminadas tablas sin entidad vigente: `app_clock_settings`, `staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`;
- eliminadas columnas legacy de `gastos`: `monto`, `descripcion`, `pagado`, `recurrente`, `frecuencia`, `staff_id`;
- eliminadas columnas legacy de `reservas_sesion`: `sesion_clase_id`, `asistio`, `observaciones`;
- migrados datos utiles de `gastos` a `importe`, `observaciones`, `estado`, `staff_responsable_id` y `gasto_recurrente_id` antes de eliminar columnas.

Esta compatibilidad permite recuperar el proyecto sin romper datos ya creados.

## Nota 2026-04-24

No hubo cambios estructurales en el dominio core del gimnasio o de finanzas. La ampliacion principal fue de identidad y flujo:

- `Usuario` gano credenciales reales (`username`, `passwordHash`, `mustChangePassword`);
- `Nomina` paso a un flujo mas profesional sin cambiar su rol central en el dominio;
- `UxMemoryState`, `RecentVisit` y `ActivityLog` siguen sosteniendo onboarding, continuidad y percepcion premium del SaaS.

## Nota 2026-04-26

No hubo cambios en entidades ni reglas de negocio centrales. El bloque fue de estabilizacion:

- busqueda global optimizada mediante repositorios con limite;
- reutilizacion de calculos mensuales en controladores;
- mejora ligera de experiencia en nominas/gastos y persistencia de filtros;
- nuevo test unitario para proteger el contrato funcional de `GlobalSearchService`.

## Nota 2026-04-26 (finanzas y nominas)

No hubo migraciones destructivas ni entidades nuevas. La mejora fue de aplicacion y agregacion:

- `NominaForm` representa la entrada editable del formulario y evita validar campos persistidos/generados por backend.
- `Nomina` conserva su ciclo `BORRADOR -> EMITIDA -> PAGADA/CANCELADA`, unicidad `staff + periodo`, referencia unica y relacion opcional con `Gasto`.
- `FinancialCenterStatsResponse` es un DTO de lectura para el centro financiero; no persiste estado ni modifica el dominio.
- La deuda total se calcula desde pagos abiertos (`PROGRAMADO`, `PENDIENTE`, `VENCIDO`) y las nominas pendientes desde `EMITIDA`.

## Nota 2026-04-26 (rescate funcional)

- `TrialForm` es el DTO de formulario de trials; evita exponer `StaffPerfil` como objeto anidado en binding.
- `TrialConversionResult` devuelve usuario convertido, password temporal y si se creo una cuenta nueva.
- `Maquina` incorpora `costeCompra` nullable; `ddl-auto=update` anadira la columna `coste_compra` en MySQL si no existe.
- Gasto automatico de inventario:
  - maquina con `costeCompra > 0` al crear: `CategoriaGasto.MAQUINA`, `TipoGasto.VARIABLE`, `EstadoGasto.PAGADO`.
  - material con `costeUnitario > 0` y `stock > 0` al crear: `CategoriaGasto.MATERIAL`, `TipoGasto.VARIABLE`, `EstadoGasto.PAGADO`.
- Estos gastos no se generan en edicion para no duplicar costes historicos.
