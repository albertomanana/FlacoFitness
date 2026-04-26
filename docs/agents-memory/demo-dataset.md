# Demo Dataset for FlacoFitness

## Schema Diagnosis

Detected domain tables/entities used by the app:
- `roles`
- `planes`
- `usuarios`
- `rutinas`
- `usuario_rutina`
- `asistencias`
- `pagos`

Key constraints and behaviors respected:
- `usuarios.email` is unique.
- `pagos.referencia` is unique.
- `rutinas.tipo_rutina` is a string enum with values `GENERAL` and `PERSONALIZADA`.
- `pagos.estado` uses `PAGADO`, `PENDIENTE`, `VENCIDO`.
- `asistencias` links to `usuarios` through `usuario_id`.
- `rutinas` links to `usuarios` through `usuario_rutina`.
- The UI resolves custom photos from `uploads/**` or falls back to `/img/avatar-placeholder.svg`.

## Strategy

Implemented a deterministic demo seeder in `src/main/java/com/flacofitness/app/config/DemoDataSeeder.java`:
- Upserts the reference catalog (`roles`, `planes`).
- Enriches the original demo users and adds more users until reaching a balanced dataset.
- Generates local portrait SVGs under `uploads/users/` so the UI always shows an image without external dependencies.
- Seeds routines, `usuario_rutina`, asistencias and pagos with coherent business rules.
- Keeps the seed idempotent by reusing natural keys and cleaning dependent demo data before reinserting.

## Photo Convention

Because the workspace did not contain the 50 binary portraits as importable files, the demo uses deterministic local portraits instead.

Path pattern:
- `uploads/users/portrait-01.svg`
- ...
- `uploads/users/portrait-50.svg`

The first five legacy demo accounts are updated to use the first five portraits, and the rest of the photo-backed users use the remaining portrait files.

## Expected Dataset Shape

- Total users: 80
- Users with photo: 50
- Users without photo: 30
- Roles: `STAFF`, `CLIENTE`
- Active plans: `Basico`, `Premium`, `Plus`, `Estudiante`, `Trimestral`
- Routines: 10 general + up to 10 personalized
- Attendance history: last 3-6 months, biased toward active weekdays
- Payments: historical monthly/quarterly cycles with `PAGADO`, `PENDIENTE`, `VENCIDO`

## Validation

Validated after the code change:
- Maven build succeeded.
- Full Java test suite passed.

## Notes for Defense

The dataset is realistic because it combines:
- Spanish-looking names, phones, DNI/NIF patterns, and addresses.
- Plan distribution weighted toward `Basico` and `Premium`.
- Attendance patterns that separate active, moderate, low and inactive users.
- Financial states that show users al día, con deuda and vencidos.
- Routine assignment patterns that produce meaningful dashboards and list views.
