# Branching Guide

## Ramas base

- `main`: estable, entregable
- `develop`: integración del trabajo diario

## Cuándo usar cada prefijo

- `feature/`: una funcionalidad nueva
- `fix/`: una corrección normal
- `hotfix/`: una corrección urgente que sale de `main`
- `release/`: estabilización previa a una entrega
- `docs/`: cambios de documentación
- `refactor/`: reestructuración sin cambio funcional
- `chore/`: mantenimiento, tooling o configuración

## Ejemplos

- `feature/usuarios-crud-base`
- `fix/formulario-rutinas-validacion`
- `docs/actualizar-roadmap`
- `chore/configurar-dependabot`

## Regla práctica

Si el cambio afecta lógica o producto, parte desde `develop`. Si es urgente y ya impacta una versión estable, usar `hotfix/*` desde `main`.
