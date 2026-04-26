# Repository Workflow

Fecha de referencia: 2026-04-22

## Objetivo

Definir un flujo de repositorio consistente para que el desarrollo de FlacoFitness sea mantenible, auditable y fácil de seguir tanto por personas como por agentes.

## Estrategia de ramas

### Ramas permanentes

- `main`: estado estable, entregable y demostrable
- `develop`: integración de cambios en curso

### Ramas temporales por prefijo

- `feature/*`: nuevas funcionalidades
- `fix/*`: correcciones regulares
- `hotfix/*`: correcciones urgentes desde `main`
- `release/*`: estabilización antes de una entrega
- `docs/*`: documentación
- `refactor/*`: reorganización de código
- `chore/*`: tareas técnicas y tooling

## Reglas operativas

- Todo desarrollo normal parte desde `develop`
- `main` no debe usarse para trabajo diario
- Cada PR debe ser pequeño, trazable y con objetivo claro
- La documentación viva debe actualizarse junto con cambios relevantes
- Antes de fusionar, debe pasar el workflow de CI si existe en remoto

## Flujo de entrega recomendado

1. Trabajar en rama temporal desde `develop`
2. Fusionar a `develop` cuando el cambio esté revisado
3. Crear `release/*` si se prepara una versión para exposición o entrega
4. Fusionar la release estable a `main`
5. Etiquetar versiones académicas si conviene, por ejemplo `v0.1-entrega-inicial`

## Convención de etiquetas

- `v0.1-entrega-inicial`
- `v0.2-crud-base`
- `v1.0-defensa-final`

## Estado actual

- Rama activa de recuperacion funcional: `recovery/restore-core-saas-plan-a`
- Se mantienen ramas base `main` y `develop` como estrategia objetivo de integracion
- Workflow de CI orientado a compilacion y test Maven
- Repositorio remoto publicado en GitHub
- Pendiente activar manualmente proteccion de ramas en la interfaz de GitHub
- El working tree local no esta limpio; antes de refactorizar o fusionar, revisar `git status --short` y decidir si conviene crear una rama nueva de estabilizacion
