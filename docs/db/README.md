# Base de datos

Esta carpeta agrupa los artefactos relacionados con la base de datos del proyecto.

- `create-database.sql`: script minimo para crear la base `flacofitness` si no existe
- `schema.sql`: referencia del esquema y convenciones de modelado SQL
- `seeds.sql`: datos iniciales o de demostracion para pruebas y defensa
- `cleanup-2026-04-26-precheck.sql`: auditoria no destructiva de deuda legacy en MySQL
- `cleanup-2026-04-26.sql`: limpieza segura aplicada sobre MySQL real tras backup

La fuente ejecutable principal son las entidades JPA. Estos archivos deben evolucionar de forma coordinada con:

- `docs/agents-memory/domain-model.md`
- `docs/agents-memory/decisions-log.md`
- las entidades JPA en `src/main/java/com/flacofitness/app/model/entity`

Los scripts antiguos `docs/data.sql`, `docs/init.sql` y `src/main/resources/data.sql` fueron retirados porque apuntaban a tablas legacy ya eliminadas (`staff`, `sesiones`, `ejercicios`, `rutina_ejercicios`). Para datos demo, usar `DemoDataSeeder` con `app.demo-seeder.enabled=true` solo en entornos controlados.
