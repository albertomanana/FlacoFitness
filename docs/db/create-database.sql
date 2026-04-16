CREATE DATABASE IF NOT EXISTS flacofitness
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Usuario recomendado para entorno local si no se usa root:
-- CREATE USER IF NOT EXISTS 'flaco_user'@'localhost' IDENTIFIED BY 'flaco_pass';
-- GRANT ALL PRIVILEGES ON flacofitness.* TO 'flaco_user'@'localhost';
-- FLUSH PRIVILEGES;

-- Las tablas se crean/actualizan desde JPA con spring.jpa.hibernate.ddl-auto=update
-- durante el desarrollo. En produccion real convendria migrar a Flyway/Liquibase.
