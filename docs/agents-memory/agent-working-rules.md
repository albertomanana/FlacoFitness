# Agent Working Rules

Estas reglas resumen como debe seguir evolucionando FlacoFitness cualquier agente futuro.

## Regla 1. No romper el sistema por crecer

Cada bloque debe entrar de forma incremental, validada y explicable. Si una mejora tiene alto riesgo, dividirla.

## Regla 2. La base de datos manda

- Solo MySQL / phpMyAdmin.
- Base oficial: `flacofitness`.
- Nada de H2.
- Nada de seeds automaticos destructivos.
- Nada de cambios de esquema sin pensar compatibilidad.

## Regla 3. MVC se mantiene

- Thymeleaf en servidor.
- Controladores finos.
- Servicios con reglas de negocio.
- Repositorios solo para acceso a datos.
- Nada de SPA.

## Regla 4. El stack frontend no se mueve

- Bootstrap 5
- CSS propio
- JavaScript ligero
- Chart.js
- DataTables

No introducir librerias pesadas si no aportan valor real.

## Regla 5. Calidad antes que volumen

El usuario ha pedido repetidamente:

- no parches rapidos
- no caos
- no sobreingenieria
- no soluciones arbitrarias
- no crecimiento desordenado

## Regla 6. El proyecto debe ser defendible

Toda decision debe poder explicarse ante profesorado:

- que problema resuelve
- por que esta solucion y no una mas simple o mas compleja
- que gana el gimnasio o el usuario

## Regla 7. Fuente de verdad documental

Despues de cambios importantes, actualizar:

- `project-overview.md`
- `architecture.md`
- `domain-model.md`
- `setup-status.md`
- `backlog.md`
- `changelog-functional.md`
- `decisions-log.md`
- `prompts-history.md` si el prompt fue estructural

## Regla 8. Validacion minima obligatoria

Antes de cerrar un bloque:

```powershell
.\mvnw.cmd clean -DskipTests compile
.\mvnw.cmd test
```

Y si se toca UI:

- abrir la ruta real en navegador o Chrome headless
- confirmar que no hay pantalla en blanco
- confirmar que el HTML se ve y no queda oculto por splash/overlay/CSS

## Regla 9. Mantener coherencia de acceso

No basta con ocultar modulos en sidebar. Hay que mantener coherencia entre:

- `AccessProfile`
- interceptor de acceso
- shell visual
- rutas disponibles

## Regla 10. Finanzas y tiempo no se duplican

- `OperationalClockService`: abstraccion temporal oficial
- `FinancialAutomationService`: automatizacion financiera oficial
- `RecurrenceService`: calculo de recurrencia oficial

No crear una segunda logica paralela.
