# Changelog Functional

## Criterio de uso

Registrar aquí cambios funcionales acumulativos que afecten comportamiento, módulos o alcance del producto. No usarlo para cambios puramente cosméticos o técnicos sin impacto funcional.

## Historial

### 2026-03-23

- Se creó la estructura inicial del proyecto FlacoFitness.
- Se dejaron preparados los módulos base de usuarios, rutinas, pagos y asistencias a nivel de vistas placeholder.
- Se incorporó la documentación viva del proyecto dentro del repositorio.

### 2026-03-24

- Se configuró el repositorio Git con ramas base `main` y `develop`.
- Se añadieron plantillas y automatizaciones de GitHub para facilitar el trabajo continuo.
- Se implementaron las entidades JPA iniciales `Rol`, `Plan` y `Usuario`.
- Se implementaron las entidades JPA `Rutina`, `Asistencia` y `Pago`, junto con sus enums de dominio.
- Se añadió la capa inicial de repositorios Spring Data JPA y la configuración base para validar persistencia con datos semilla.
- Se añadió la capa de servicios con validaciones de negocio básicas y excepciones personalizadas.
- Se implementó el primer CRUD MVC completo para `Usuario` con controlador y vistas Thymeleaf reutilizables.
