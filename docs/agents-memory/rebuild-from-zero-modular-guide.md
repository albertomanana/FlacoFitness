# Rebuild Total Desde Cero (Modulo por Modulo)

Fecha: 2026-04-26
Estado: Plan maestro para rehacer la aplicacion completa desde cero

## 1) Nueva identidad del producto

Para evitar acoplar el reinicio al estado roto actual, se define una identidad nueva:

- Nombre de producto (alternativo): AtlasGym OS
- Nombre tecnico de aplicacion: atlasgym-os
- Paquete base Java: com.atlasgym.os
- Base de datos alternativa (MySQL): atlasgym_core
- Usuario DB sugerido: atlas_user
- Password DB sugerida: atlas_pass_2026

Nota: este documento describe la reconstruccion total como producto nuevo. El proyecto legado FlacoFitness queda como referencia funcional, no como base de codigo a copiar sin criterio.

## 2) Objetivo del rebuild

Reconstruir una plataforma SaaS de gestion de gimnasio con arquitectura MVC monolitica (Spring Boot + Thymeleaf), manteniendo:

- alta mantenibilidad
- coherencia funcional entre membresias, pagos, asistencias, gastos y nominas
- UI premium administrativa
- trazabilidad completa modulo por modulo

## 3) Arquitectura objetivo (desde cero)

### 3.1 Stack

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- Spring Validation
- Thymeleaf
- Bootstrap 5 + JS ligero
- MySQL 8+
- Maven Wrapper
- OpenHTMLtoPDF para reportes PDF

### 3.2 Capas

- controller: orquestacion HTTP y modelado de vistas
- service: reglas de negocio y transacciones
- repository: acceso a datos con Spring Data JPA
- model.entity: entidades JPA
- model.enums: estados/tipos de negocio
- model.dto: agregados de dashboard, respuestas JSON y forms

### 3.3 Reglas de diseno

- nada de SPA
- controladores finos
- logica de negocio solo en servicios
- automatizacion financiera centralizada en un unico servicio
- tiempo de negocio centralizado en OperationalClockService
- autorizacion por perfiles de acceso y rutas

## 4) Setup inicial del proyecto nuevo

### 4.1 Crear proyecto base

1. Crear proyecto Spring Boot con dependencias:
   - Spring Web
   - Thymeleaf
   - Spring Data JPA
   - Validation
   - Spring Security Crypto
   - MySQL Driver
   - Lombok
2. Definir group/artifact:
   - groupId: com.atlasgym
   - artifactId: atlasgym-os
3. Configurar empaquetado jar y Java 17.

### 4.2 Configuracion de application.properties

Definir desde el inicio:

- spring.application.name=AtlasGymOS
- spring.profiles.active=local
- spring.datasource.url=jdbc:mysql://localhost:3306/atlasgym_core?... 
- spring.datasource.username=atlas_user
- spring.datasource.password=atlas_pass_2026
- spring.jpa.hibernate.ddl-auto=update
- spring.sql.init.mode=never
- spring.jpa.open-in-view=false
- app.auth.max-attempts=5
- app.auth.lock-minutes=1

### 4.3 Estructura de paquetes objetivo

com.atlasgym.os
- config
- controller
- exception
- model
  - dto
  - entity
  - enums
- repository
- security
- service
- util

## 5) Modelo de datos objetivo (core)

### 5.1 Entidades maestras

- Rol
- Usuario
- StaffPerfil
- Plan
- MembresiaUsuario
- Pago
- Trial
- Rutina
- Clase
- SesionClase
- ReservaSesion
- Asistencia
- Gasto
- GastoRecurrente
- Nomina
- Maquina
- Material
- RecentVisit
- UxMemoryState
- ActivityLog

### 5.2 Relaciones clave

- Usuario -> Rol (N:1)
- StaffPerfil -> Usuario (1:1)
- MembresiaUsuario -> Usuario (N:1)
- MembresiaUsuario -> Plan (N:1)
- Pago -> Usuario (N:1)
- Pago -> Plan (N:1, opcional)
- Pago -> MembresiaUsuario (N:1, opcional)
- SesionClase -> Clase (N:1)
- SesionClase -> StaffPerfil (N:1, opcional)
- ReservaSesion -> SesionClase (N:1)
- ReservaSesion -> Usuario (N:1)
- Asistencia -> Usuario (N:1)
- Asistencia -> SesionClase (N:1, opcional)
- Gasto -> GastoRecurrente (N:1, opcional)
- Gasto -> StaffPerfil/Maquina/Material (N:1, opcional)
- Nomina -> StaffPerfil (N:1)
- Nomina -> Gasto (1:1, opcional)

## 6) Modulos funcionales (rebuild modulo por modulo)

Cada modulo se reconstruye con su vertical completa:
Entidad -> Repositorio -> Servicio -> Controlador -> Vistas -> Test.

### Modulo 0: Base tecnica y shell

Incluye:
- layout base (fragments head/sidebar/topbar/footer)
- sistema de alertas flash
- tema visual base y dark mode
- manejo de errores 400/403/404/500

Rutas:
- / (dashboard)
- /error/*

Criterio de salida:
- render consistente en todas las vistas
- contenido principal siempre visible (sin bug de pantalla en blanco)

### Modulo 1: Acceso y autorizacion

Incluye:
- login por email/username + password
- hash BCrypt
- bloqueo temporal por intentos
- cambio de password obligatorio para bootstrap
- perfiles AccessProfile

Rutas:
- GET/POST /acceso
- POST /salir
- GET/POST /cuenta/password

Criterio de salida:
- acceso por perfiles funcionando
- rutas protegidas por interceptor

### Modulo 2: Usuarios

Incluye:
- CRUD de usuarios
- activacion/desactivacion
- foto de perfil
- detalle enriquecido con resumen operativo

Rutas:
- /usuarios
- /usuarios/nuevo
- /usuarios/{id}
- /usuarios/{id}/editar

Criterio de salida:
- usuario listo para vincular staff, membresias, pagos y asistencias

### Modulo 3: Staff

Incluye:
- StaffPerfil ligado a Usuario
- rol staff (entrenador/recepcion/gerencia)
- bandera puedeImpartirClases

Rutas:
- /staff
- /staff/nuevo
- /staff/{id}
- /staff/{id}/editar

Criterio de salida:
- validacion de staff operativo para clases/sesiones/rutinas

### Modulo 4: Planes y membresias

Incluye:
- catalogo Plan
- contrato real MembresiaUsuario
- estados ACTIVA/EXPIRADA/CANCELADA

Rutas:
- /membresias
- /membresias/nuevo
- /membresias/{id}
- /membresias/{id}/editar

Criterio de salida:
- separacion limpia entre catalogo (plan) y contrato (membresia)

### Modulo 5: Pagos

Incluye:
- registro de cuotas
- estados PENDIENTE/PAGADO/VENCIDO/PROGRAMADO
- fecha vencimiento y fecha pago
- referencia unica

Rutas:
- /pagos
- /pagos/nuevo
- /pagos/{id}
- /pagos/{id}/editar
- acciones de marcar pagado

Criterio de salida:
- sin duplicados por ciclo usuario+vencimiento

### Modulo 6: Trials

Incluye:
- leads
- estado trial
- conversion a usuario

Rutas:
- /trials
- /trials/nuevo
- /trials/{id}
- /trials/{id}/editar
- /trials/{id}/convertir

Criterio de salida:
- al convertir, crear usuario y membresia cuando corresponda

### Modulo 7: Rutinas

Incluye:
- biblioteca de rutinas
- asignacion a usuarios
- staff responsable opcional

Rutas:
- /rutinas
- /rutinas/nuevo
- /rutinas/{id}
- /rutinas/{id}/editar
- /rutinas/usuario/{usuarioId}

Criterio de salida:
- filtros por activo/tipo y asignacion consistente

### Modulo 8: Clases

Incluye:
- catalogo de clases reutilizables

Rutas:
- /clases
- /clases/nuevo
- /clases/{id}
- /clases/{id}/editar

Criterio de salida:
- catalogo listo para agenda de sesiones

### Modulo 9: Sesiones y reservas

Incluye:
- agenda (SesionClase)
- reservas por usuario
- cupos
- responsable staff

Rutas:
- /sesiones
- /sesiones/nueva
- /sesiones/{id}
- /sesiones/{id}/editar
- acciones de reservar/cancelar/asistencia

Criterio de salida:
- sin sobre-reserva por cupo

### Modulo 10: Asistencias

Incluye:
- check-in libre
- check-in vinculado a sesion

Rutas:
- /asistencias
- /asistencias/nueva
- /asistencias/{id}

Criterio de salida:
- trazabilidad por usuario y fecha

### Modulo 11: Gastos

Incluye:
- gasto operativo
- categorias y tipos
- estado y vencimiento
- vinculacion opcional con staff/maquina/material

Rutas:
- /gastos
- /gastos/nuevo
- /gastos/{id}
- /gastos/{id}/editar
- /gastos/{id}/pdf

Criterio de salida:
- filtros completos y detalle estable

### Modulo 12: Gastos recurrentes

Incluye:
- plantillas periodicas
- frecuencia y proximo cargo
- generacion idempotente

Rutas:
- /gastos/recurrentes
- /gastos/recurrentes/nuevo
- /gastos/recurrentes/{id}
- /gastos/recurrentes/{id}/editar

Criterio de salida:
- no duplicar cargos por misma plantilla y fecha

### Modulo 13: Nominas

Incluye:
- flujo BORRADOR -> EMITIDA -> PAGADA/CANCELADA
- calculo salario neto
- gasto asociado de tipo NOMINA
- PDF individual y listado

Rutas:
- /nominas
- /nominas/nueva
- /nominas/{id}
- /nominas/{id}/editar
- /nominas/{id}/emitir
- /nominas/{id}/pagar
- /nominas/{id}/cancelar

Criterio de salida:
- estados protegidos por reglas de transicion

### Modulo 14: Inventario (maquinas y materiales)

Incluye:
- CRUD maquinas
- CRUD materiales
- estados de servicio y stock
- integracion con gastos

Rutas:
- /maquinas
- /maquinas/nueva
- /maquinas/{id}
- /maquinas/{id}/editar
- /materiales
- /materiales/nuevo
- /materiales/{id}
- /materiales/{id}/editar

Criterio de salida:
- KPIs de fuera de servicio y bajo stock

### Modulo 15: Dashboard, stats y finanzas agregadas

Incluye:
- dashboard SSR + endpoint stats
- modulo centro financiero (/finanzas)
- KPIs de ingresos, gastos, deuda y beneficio
- charts de ingresos vs gastos, asistencias, distribucion por plan

Rutas:
- /
- /stats/dashboard
- /stats/finanzas
- /finanzas

Criterio de salida:
- coherencia entre datos dashboard y datos persistidos

### Modulo 16: Busqueda global, UX memory y actividad

Incluye:
- /api/busqueda/global
- /busqueda
- ultimos visitados
- memoria UX por browser token + perfil
- actividad reciente

Rutas:
- /busqueda
- /api/busqueda/global
- /ux/*
- /notificaciones

Criterio de salida:
- experiencia de producto no CRUD, sin romper MVC

## 7) Servicios transversales obligatorios

- OperationalClockService: fuente unica de fecha/hora real
- FinancialAutomationService: unica fachada de automatizacion financiera
- RecurrenceService: calculo de periodicidad
- AccessSessionService: autenticacion y estado de sesion
- ShellNotificationService: alertas operativas

## 8) Orden recomendado de implementacion (sprints)

Sprint A (fundacion)
- modulo 0 + 1 + 2

Sprint B (comercial)
- modulo 3 + 4 + 5 + 6

Sprint C (operacion diaria)
- modulo 7 + 8 + 9 + 10

Sprint D (finanzas e inventario)
- modulo 11 + 12 + 13 + 14

Sprint E (producto premium)
- modulo 15 + 16 + hardening UX

## 9) Estrategia de pruebas

### 9.1 Minimo por modulo

- pruebas de servicio de casos felices
- pruebas de servicio de validaciones criticas
- pruebas de rutas principales con MockMvc

### 9.2 Pruebas de regresion obligatorias

- login y bloqueo de acceso
- conversion trial -> usuario
- generacion de pagos mensuales
- generacion de gastos recurrentes sin duplicados
- flujo completo de nomina
- stats/dashboard sin errores de render

### 9.3 Comandos de verificacion

- .\mvnw.cmd clean -DskipTests compile
- .\mvnw.cmd test

## 10) Definicion de listo global (DoD)

El rebuild se considera listo cuando:

- todos los modulos 0..16 estan operativos
- tests de negocio principales pasan
- no hay rutas criticas con error 500
- dashboard y finanzas muestran datos consistentes
- permisos por perfil son coherentes
- exportacion PDF de gastos y nominas es estable
- documentacion viva actualizada

## 11) Checklist para renombrar el proyecto legado al nuevo

1. `pom.xml`
   - groupId -> com.atlasgym
   - artifactId -> atlasgym-os
   - name -> AtlasGymOS
2. paquete java raiz
   - com.flacofitness.app -> com.atlasgym.os
3. `application.properties`
   - spring.application.name -> AtlasGymOS
   - datasource -> atlasgym_core
4. scripts DB y docs
   - crear `docs/db/create-database-atlasgym.sql`
   - reflejar nuevo nombre en README y docs
5. branding UI
   - logo, nombre en sidebar/topbar, footer

## 12) SQL base para la nueva BD alternativa

```sql
CREATE DATABASE IF NOT EXISTS atlasgym_core
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'atlas_user'@'localhost' IDENTIFIED BY 'atlas_pass_2026';
GRANT ALL PRIVILEGES ON atlasgym_core.* TO 'atlas_user'@'localhost';
FLUSH PRIVILEGES;
```

## 13) Recomendacion final de ejecucion

No hacer un "big bang". Reconstruir por verticales completas y cerrar cada modulo con compilacion, test y smoke visual antes de pasar al siguiente.
