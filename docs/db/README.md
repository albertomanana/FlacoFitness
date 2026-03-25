# Base de datos

Esta carpeta agrupa los artefactos relacionados con la base de datos del proyecto.

- `create-database.sql`: script minimo para crear la base `flacofitness` si no existe
- `schema.sql`: referencia del esquema y convenciones de modelado SQL
- `seeds.sql`: datos iniciales o de demostracion para pruebas y defensa

Mientras el dominio siga en construccion, estos archivos deben evolucionar de forma coordinada con:

- `docs/agents-memory/domain-model.md`
- `docs/agents-memory/decisions-log.md`
- las futuras entidades JPA en `src/main/java/com/flacofitness/app/model/entity`
