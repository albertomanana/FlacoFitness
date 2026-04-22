# CLAUDE.md recomendado para FlacoFitness

```md
# Claude Code instructions for FlacoFitness

## Project
FlacoFitness is a premium SaaS-style gym management application built with:
- Spring Boot
- Spring Data JPA
- Thymeleaf
- Bootstrap 5
- JavaScript
- MySQL database `flacofitness`

## Architecture rules
- Keep MVC architecture.
- Do not convert the app into a SPA.
- Keep Thymeleaf + Bootstrap + lightweight JS.
- Never use H2.
- Always use MySQL database `flacofitness`.
- Always validate dashboard, finances, payments, memberships, staff, attendance, routines, classes, expenses, machines and materials after important changes.
- Avoid large uncontrolled rewrites.
- Prefer safe incremental refactors.
- Always inspect docs/agents-memory/* before making important changes.
- Always update docs/agents-memory after important changes.
- Never introduce random frontend libraries without a strong reason.
- Keep the UI premium, modern, elegant and coherent.

## UX rules
- Prefer click-on-row navigation instead of too many action buttons.
- Keep visual hierarchy clean.
- Keep spacing consistent.
- Use animations with moderation.
- Keep dashboard readable and not overloaded.
- Keep sidebar organized and scalable.

## Finance rules
- Payments must always use the current real PC date.
- Expenses and payrolls must always use the current real PC date.
- Avoid duplicated recurring payments or expenses.
- Financial logic must be centralized.
- Memberships, payments, payrolls and expenses must stay coherent.
- Dashboard financial metrics must match real stored data.

## Testing rules
- Validate CRUDs.
- Validate charts.
- Validate notifications.
- Validate dark mode.
- Validate filters.
- Validate dashboard.
- Validate MySQL persistence.
- Validate recurring expenses and payments.

## Documentation rules
Always update:
- docs/agents-memory/project-overview.md
- docs/agents-memory/architecture.md
- docs/agents-memory/domain-model.md
- docs/agents-memory/setup-status.md
- docs/agents-memory/backlog.md
- docs/agents-memory/changelog-functional.md
- docs/agents-memory/decisions-log.md
```

---

# Top prompts para Claude Code

## Prompt 1 — Auditoría total del proyecto

```text
Lee primero docs/agents-memory/* y revisa todo el proyecto FlacoFitness.

No cambies nada todavía.

Quiero un diagnóstico brutalmente honesto sobre:
- módulos existentes
- bugs
- deuda técnica
- incoherencias de dominio
- relaciones mal planteadas
- dashboard
- finanzas
- notificaciones
- dark mode
- permisos
- sidebar
- UX/UI

Después dame:
1. problemas prioritarios
2. riesgos
3. plan corto por bloques
4. quick wins de alto impacto
```

## Prompt 2 — Rediseño total del módulo financiero

```text
Quiero rehacer completamente el área financiera de FlacoFitness para que se sienta como un producto premium y profesional.

Objetivos:
- pagos coherentes con membresías
- gastos bien organizados
- nóminas funcionales y elegantes
- automatización de gastos recurrentes
- cálculo de beneficio estimado
- próximos vencimientos
- deuda
- ingresos vs gastos
- semáforo financiero
- gráficos financieros modernos
- notificaciones útiles

Reglas:
- usar SIEMPRE la fecha real del PC
- eliminar cualquier dependencia de simulación hardcodeada
- evitar duplicados
- mantener coherencia entre pagos, gastos, membresías y staff

Quiero que:
1. audites el módulo actual
2. detectes incoherencias
3. rehagas el modelo si hace falta
4. mejores dashboard y UX
5. dejes el módulo listo para demo comercial
```

## Prompt 3 — Nóminas premium

```text
Quiero rehacer completamente la sección de nóminas.

La implementación actual no funciona bien.

Quiero una solución premium:
- ligada a staff
- ligada a gastos
- ligada a PDFs
- visualmente profesional
- con logo
- con salario base, bonus, deducciones y salario neto
- con estado
- con referencia
- con exportación PDF elegante

Además:
- si un empleado tiene salario mensual configurado, debe poder generarse automáticamente la nómina del mes
- la nómina debe crear o relacionarse con un gasto de tipo NOMINA
- las nóminas deben verse como documentos reales y premium
```

## Prompt 4 — Dashboard premium tipo SaaS caro

```text
Quiero rehacer el dashboard principal para que se vea como un SaaS premium y caro.

Problemas actuales:
- demasiados divs
- demasiadas cards
- ruido visual
- poca jerarquía

Objetivos:
- simplificar
- mantener solo KPIs realmente útiles
- reorganizar visualmente
- hacer que todo se vea premium

Quiero:
- KPI de usuarios activos
- KPI de ingresos del mes
- KPI de gastos del mes
- KPI de beneficio estimado
- KPI de pagos pendientes
- KPI de renovaciones
- KPI de trials
- KPI de máquinas fuera de servicio
- KPI de stock bajo

Además:
- un carrusel moderno y elegante con alertas importantes
- gráficos más grandes y más limpios
- mejor uso del espacio
- mejores sombras
- mejores animaciones
- cards con hover elegante
- transiciones suaves
```

## Prompt 5 — Carrusel premium del dashboard

```text
Quiero crear un carrusel premium en el dashboard.

No quiero un carrusel decorativo.

Debe servir para mostrar:
- pagos vencidos
- renovaciones próximas
- trials sin convertir
- clases de hoy
- usuarios inactivos
- máquinas averiadas
- stock bajo
- gastos importantes
- nóminas pendientes

Debe:
- verse moderno
- tener animaciones suaves
- funcionar bien en desktop y móvil
- tener modo oscuro
- permitir clic y redirección
- sentirse integrado con el dashboard
```

## Prompt 6 — Automatización definitiva de pagos y gastos

```text
Quiero dejar completamente automatizados pagos y gastos.

Reglas:
- usar siempre la fecha real actual del PC
- no usar simulación hardcodeada
- evitar duplicados
- centralizar la lógica

Pagos:
- coherentes con membresía
- con vencimiento
- con estados
- con deuda
- con próximos vencimientos

Gastos:
- recurrentes
- alquiler
- luz
- agua
- internet
- software
- nóminas
- limpieza

Además:
- generar notificaciones
- actualizar dashboard
- recalcular beneficio estimado
- recalcular deuda
```

## Prompt 7 — Navegación y UX premium

```text
Quiero revisar toda la navegación de FlacoFitness.

Objetivos:
- menos botones redundantes
- más clics naturales
- filas clicables
- redirecciones coherentes
- sidebar mejor organizado
- topbar más limpia
- dark mode elegante
- mejor spacing
- mejor feedback visual

Quiero que todo se sienta:
- más intuitivo
- más fluido
- más moderno
- más satisfactorio
```

## Prompt 8 — Revisión total de bugs y limpieza

```text
Haz una auditoría final completa del proyecto.

Detecta:
- bugs
- código duplicado
- código muerto
- CSS innecesario
- JS innecesario
- servicios redundantes
- problemas de dashboard
- problemas de rendimiento
- problemas de UX
- incoherencias de roles
- problemas de responsive

Después:
1. clasifica por prioridad
2. elimina lo innecesario
3. refactoriza
4. valida que todo siga funcionando
```

## Prompt 9 — Modo oscuro premium

```text
Quiero rehacer el dark mode para que se vea premium.

Debe:
- verse limpio
- mantener buen contraste
- funcionar en dashboard
- funcionar en gráficos
- funcionar en sidebar
- funcionar en tablas
- funcionar en formularios
- tener transición suave

No quiero un dark mode gris y triste.
Quiero algo elegante, moderno y bien equilibrado.
```

## Prompt 10 — Cierre definitivo del producto

```text
Quiero una revisión final completa de FlacoFitness como si fueras un CTO y Product Designer senior.

Revisa:
- dashboard
- usuarios
- staff
- membresías
- pagos
- gastos
- nóminas
- asistencias
- rutinas
- clases
- máquinas
- material
- notificaciones
- dark mode
- sidebar
- topbar
- responsive
- PDFs

Quiero:
1. detectar incoherencias
2. corregir bugs
3. mejorar UX
4. mejorar UI
5. simplificar lo innecesario
6. dejar el SaaS listo para demo seria y venta a gimnasio local
```

