# GitHub Setup Checklist

Fecha de referencia: 2026-03-24

## Objetivo

Dejar registrado qué configuración del repositorio ya está preparada en código y qué ajustes conviene activar manualmente en GitHub para asegurar un flujo sólido de trabajo.

## Ya preparado en el repositorio

- ramas base `main` y `develop`
- workflow de CI para Maven
- validación de nombre de rama y título de PR
- autoetiquetado de Pull Requests por rutas
- plantillas de Pull Request e issues
- `CODEOWNERS`
- `dependabot`
- convenciones documentadas en `CONTRIBUTING.md`

## Configuración manual recomendada en GitHub

### Protección de `main`

- Require a pull request before merging
- Require approvals: 1
- Dismiss stale approvals when new commits are pushed
- Require status checks to pass before merging
- Status checks recomendados:
  - `build-and-test`
  - `branch-name`
  - `pr-title`
- Restrict direct pushes a `main`

### Protección de `develop`

- Require a pull request before merging
- Require status checks to pass before merging
- Status checks recomendados:
  - `build-and-test`
  - `branch-name`
  - `pr-title`

### Configuración general

- Activar auto-delete head branches
- Activar squash merge
- Desactivar merge commits si se quiere un historial más limpio
- Mantener issues y projects activos

## Etiquetas sugeridas

- `bug`
- `enhancement`
- `task`
- `documentation`
- `backend`
- `frontend`
- `database`
- `ci`
- `docker`

## Estado actual

- Repositorio remoto creado y sincronizado
- Configuración avanzada de protección pendiente de activación manual en la interfaz de GitHub
