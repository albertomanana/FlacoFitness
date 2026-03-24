# FlacoFitness

FlacoFitness es una aplicación web académica de gestión para gimnasio y fitness. El proyecto se plantea como una solución monolítica con arquitectura MVC, orientada a la administración de usuarios, planes, rutinas, asistencias y pagos.

## Stack tecnológico

- Backend: Java 17, Spring Boot, Spring Data JPA, Spring Validation
- Frontend: Thymeleaf, Bootstrap 5, CSS y JavaScript
- Base de datos: MySQL
- Build tool: Maven
- Contenedores: Docker y Docker Compose

## Estructura del proyecto

```text
flacofitness/
├── docs/
│   ├── agents-memory/
│   ├── db/
│   ├── defense/
│   └── diagrams/
├── src/
│   ├── main/
│   │   ├── java/com/flacofitness/app/
│   │   └── resources/
│   └── test/
├── .env.example
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Ejecución local

1. Instala Java 17 y MySQL.
2. Crea un archivo `.env` a partir de `.env.example` y ajusta los valores.
3. Crea la base de datos configurada en MySQL.
4. Ejecuta la aplicación:

```bash
./mvnw spring-boot:run
```

La aplicación quedará disponible por defecto en `http://localhost:8080`.

### Vista rápida sin MySQL

Si solo quieres abrir la interfaz y navegar por el proyecto sin depender de MySQL, arranca con el perfil local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Esto levantará una base H2 en memoria y dejará la aplicación disponible en `http://localhost:8080`.

## Ejecución con Docker

1. Crea el archivo `.env` a partir de `.env.example`.
2. Levanta los servicios:

```bash
docker compose up --build
```

Esto iniciará la aplicación Spring Boot y una instancia de MySQL.

## Documentación viva

La carpeta `docs/agents-memory/` centraliza la memoria documental del proyecto para trabajo colaborativo con agentes y asistentes de código. Su objetivo es mantener contexto técnico, decisiones, backlog, roadmap y estado general directamente dentro del repositorio.

## Workflow del repositorio

Estrategia de ramas propuesta:

- `main`: rama estable y lista para entrega o demostración
- `develop`: rama de integración continua del desarrollo
- `feature/*`: nuevas funcionalidades
- `fix/*`: correcciones normales
- `hotfix/*`: correcciones urgentes sobre `main`
- `release/*`: preparación de entregas
- `docs/*`: cambios documentales
- `chore/*`: tareas técnicas o mantenimiento

El detalle operativo del flujo está documentado en `CONTRIBUTING.md` y `docs/agents-memory/repository-workflow.md`.

Automatizaciones incluidas:

- CI con compilación y test Maven en GitHub Actions
- validación de nombre de rama y título de Pull Request
- plantillas de issues y PR
- autoetiquetado por rutas
- actualizaciones automáticas con Dependabot

## Nota académica

Este repositorio corresponde a un proyecto académico final. La estructura actual prioriza orden, mantenibilidad y facilidad de evolución antes de desarrollar toda la lógica de negocio.
