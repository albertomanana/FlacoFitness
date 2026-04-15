# Arquitectura

## Descripción general

FlacoFitness se plantea como una aplicación web monolítica basada en el patrón MVC. La solución concentra backend, renderizado de vistas y acceso a datos en un único proyecto Spring Boot, lo que simplifica desarrollo, despliegue y defensa académica.

## Capas del sistema

### Presentación

- Vistas Thymeleaf
- Fragmentos reutilizables HTML
- Bootstrap 5 para maquetación y componentes
- JavaScript ligero para comportamiento básico de interfaz

### Controladores

- Reciben peticiones HTTP
- Preparan el modelo para las vistas
- Delegan la lógica de negocio a servicios
- Definen navegación entre pantallas

### Servicios

- Centralizan reglas de negocio
- Coordinan validaciones, transacciones y casos de uso
- Orquestan interacción entre controladores y repositorios

### Repositorios

- Encapsulan acceso a datos mediante Spring Data JPA
- Exponen operaciones de persistencia sobre entidades

### Modelo

- Entidades JPA del dominio
- DTO para intercambio de datos entre capas
- Enumeraciones para estados y valores controlados

## Responsabilidades por capa

- `controller`: flujo web MVC y endpoints de navegación
- `service`: lógica de negocio y coordinación de casos de uso
- `repository`: acceso a datos
- `model.entity`: entidades persistentes
- `model.dto`: objetos de transferencia para formularios y vistas
- `model.enums`: catálogos y estados del dominio
- `config`: configuración transversal de la aplicación
- `exception`: manejo de errores y excepciones de negocio o infraestructura
- `util`: utilidades técnicas reutilizables

## Modulos funcionales actuales

- `usuarios`: centro operativo del cliente, con foto, plan, pagos, asistencias, rutinas y acceso a historial de membresias.
- `staff`: perfil interno ligado a `Usuario`, usado para entrenadores, recepcion y gerencia sin duplicar personas.
- `membresias`: catalogo comercial basado en `Plan` y contratos reales mediante `MembresiaUsuario`.
- `trials`: gestion de leads y dias de prueba, con conversion controlada a usuario.
- `clases`: catalogo de actividades reutilizables.
- `sesiones`: agenda de clases programadas, staff responsable, cupo, reservas y asistencia vinculada.
- `rutinas`: biblioteca y asignacion de entrenamientos a usuarios, con staff responsable opcional.
- `pagos`: cobros vinculados a usuario, plan y, cuando existe, contrato de membresia.
- `asistencias`: check-in libre y asistencia opcionalmente asociada a sesion.

## Criterio de modelado SaaS

La arquitectura conserva el monolito MVC porque es suficiente para el alcance academico y para un MVP vendible local. La separacion clave del dominio es:

- `Plan` define la oferta comercial.
- `MembresiaUsuario` define el contrato de una persona.
- `Pago` registra el cobro de ese contrato.
- `Clase` define una actividad.
- `SesionClase` define una fecha y hora concreta.
- `ReservaSesion` conecta usuarios con sesiones y permite controlar cupo.

## Convención de paquetes

Paquete base Java: `com.flacofitness.app`

Convención prevista:

```text
com.flacofitness.app
├── config
├── controller
├── service
├── repository
├── model
│   ├── entity
│   ├── dto
│   └── enums
├── exception
└── util
```

Esta convención favorece localización rápida del código, separación de responsabilidades y mantenimiento progresivo del proyecto.
