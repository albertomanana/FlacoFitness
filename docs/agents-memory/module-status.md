# Module Status

Fecha de referencia: 2026-04-22

## Resumen

La aplicacion ya no esta en fase de esqueleto. La mayoria de modulos existen, cargan HTML real y tienen al menos flujo operativo basico. Lo que falta ahora es consolidacion, QA visual completa y cierre fino.

## Estado por modulo

| Modulo | Estado | Backend | UI | Riesgo actual | Nota |
| --- | --- | --- | --- | --- | --- |
| Dashboard | Operativo | OK | OK | Medio | Validado con compile/tests; QA visual parcial |
| Acceso PIN | Operativo | OK | OK | Bajo | PIN por defecto `2468` |
| Usuarios | Operativo | OK | OK | Medio | Validado visualmente tras fix de pantalla en blanco |
| Rutinas | Operativo | OK | Pendiente QA profunda | Medio | Many-to-many con usuarios y staff opcional |
| Pagos | Operativo | OK | OK | Medio | Validado visualmente tras fix de pantalla en blanco |
| Asistencias | Operativo | OK | Pendiente QA profunda | Medio | Check-in libre y por sesion |
| Staff | Operativo | OK | Pendiente QA profunda | Medio | Ligado a Usuario por `StaffPerfil` |
| Membresias | Operativo | OK | Pendiente QA profunda | Medio | Catalogo + contrato real |
| Trials | Operativo | OK | Pendiente QA profunda | Medio | Flujo comercial basico |
| Clases | Operativo | OK | Pendiente QA profunda | Medio | Catalogo de actividad |
| Sesiones | Operativo | OK | Pendiente QA profunda | Medio | Horario, cupo, staff, rutina opcional |
| Gastos | Operativo | OK | Pendiente QA visual fuerte | Alto | Modulo financiero amplio |
| Recurrentes | Operativo | OK | Pendiente QA visual fuerte | Alto | Plantillas de cargos |
| Nominas | Operativo | OK | Pendiente QA visual fuerte | Alto | PDF y automatizacion presentes |
| Maquinas | Operativo | OK | Pendiente QA profunda | Medio | Integrado con gastos |
| Materiales | Operativo | OK | Pendiente QA profunda | Medio | Integrado con gastos |
| Cliente | Operativo | OK | Pendiente QA profunda | Medio | Panel limitado por perfil |
| Notificaciones | Operativo | OK | OK | Medio | Debe probarse por perfiles y estados reales |

## Estado transversal

### Shell visual

- Sidebar, topbar, footer y shell premium: activos.
- Modo oscuro: activo.
- Splash/transiciones: activas, con fail-safe agregado el 2026-04-22.

### Finanzas

- Pagos automaticos: activos.
- Gastos recurrentes: activos.
- Nominas: activas.
- PDFs: implementados.

### Tiempo

- Tiempo de negocio: tiempo real del sistema a traves de `OperationalClockService`.
- No hay simulador persistido activo.

### Persistencia

- Solo MySQL real.
- No H2.
- No seeds automaticos.

## Orden recomendado de QA visual

1. `/`
2. `/acceso`
3. `/usuarios`
4. `/pagos`
5. `/rutinas`
6. `/asistencias`
7. `/staff`
8. `/membresias`
9. `/trials`
10. `/clases`
11. `/sesiones`
12. `/gastos`
13. `/gastos/recurrentes`
14. `/nominas`
15. `/maquinas`
16. `/materiales`
17. `/cliente`
