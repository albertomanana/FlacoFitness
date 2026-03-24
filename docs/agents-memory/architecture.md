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
