# Contribución y workflow

## Objetivo

Mantener un flujo de trabajo simple, profesional y fácil de seguir durante el desarrollo académico de FlacoFitness.

## Ramas permanentes

- `main`: versión estable del proyecto
- `develop`: integración de trabajo en curso

## Ramas temporales

Crear ramas desde `develop` con los siguientes prefijos:

- `feature/nombre-corto`: nueva funcionalidad
- `fix/nombre-corto`: corrección funcional o técnica
- `docs/nombre-corto`: cambios en documentación
- `refactor/nombre-corto`: reorganización interna sin cambio funcional
- `chore/nombre-corto`: mantenimiento, tooling o configuración
- `release/version`: preparación de entrega

Crear ramas desde `main` solo para incidencias urgentes:

- `hotfix/nombre-corto`

## Flujo recomendado

1. Actualizar `develop`.
2. Crear una rama de trabajo con prefijo correcto.
3. Hacer cambios pequeños y coherentes.
4. Abrir Pull Request contra `develop`.
5. Ejecutar revisión y validaciones automáticas.
6. Fusionar a `main` únicamente cuando el cambio esté listo para entrega.

## Commits

Formato recomendado:

```text
tipo(scope): resumen breve
```

Ejemplos:

- `feat(usuarios): crear formulario base`
- `docs(agents-memory): actualizar roadmap`
- `chore(ci): añadir workflow de Maven`

Tipos sugeridos:

- `feat`
- `fix`
- `docs`
- `refactor`
- `test`
- `chore`

## Pull Requests

Cada Pull Request debería incluir:

- objetivo del cambio
- impacto esperado
- pruebas realizadas
- riesgos o puntos pendientes

## Documentación viva

Si un cambio altera arquitectura, alcance, dominio o decisiones, actualizar también:

- `docs/agents-memory/architecture.md`
- `docs/agents-memory/domain-model.md`
- `docs/agents-memory/decisions-log.md`
- `docs/agents-memory/changelog-functional.md`
