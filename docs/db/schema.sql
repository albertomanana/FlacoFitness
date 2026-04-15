-- FlacoFitness - referencia de esquema SaaS MVP
-- La fuente ejecutable principal son las entidades JPA. Este archivo documenta
-- las tablas relevantes para defensa y revision tecnica.

-- Identidad y clientes:
-- roles(id, nombre)
-- usuarios(id, nombre, apellidos, dni, email, telefono, fecha_nacimiento,
--          direccion, foto_path, activo, fecha_registro, fecha_proximo_pago,
--          rol_id, plan_id)

-- Catalogo comercial y contratos:
-- planes(id, nombre, descripcion, tipo_membresia, beneficios,
--        precio_mensual, duracion_dias, activo)
-- membresias_usuario(id, usuario_id, plan_id, fecha_inicio, fecha_fin,
--                    estado, precio_snapshot, origen, observaciones,
--                    fecha_creacion)

-- Equipo interno:
-- staff_perfiles(id, usuario_id, especialidad, rol_staff, activo,
--                fecha_alta, observaciones)

-- Rutinas:
-- rutinas(id, nombre, descripcion, fecha_creacion, tipo_rutina, activa,
--         staff_responsable_id)
-- usuario_rutina(usuario_id, rutina_id)

-- Agenda:
-- clases(id, nombre, descripcion, capacidad_sugerida, activa, observaciones)
-- sesiones_clase(id, clase_id, fecha, hora_inicio, hora_fin, aforo, estado,
--                rutina_id, staff_responsable_id, observaciones)
-- reservas_sesion(id, sesion_clase_id, usuario_id, estado, fecha_reserva,
--                 observaciones)

-- Operacion diaria:
-- asistencias(id, fecha, hora_entrada, observaciones, usuario_id,
--             sesion_clase_id)
-- pagos(id, fecha_pago, fecha_vencimiento, monto, metodo_pago, estado,
--       referencia, usuario_id, plan_id, membresia_usuario_id)

-- Comercial:
-- trials(id, nombre, apellidos, telefono, email, origen, fecha_prueba,
--        estado, staff_responsable_id, usuario_convertido_id,
--        observaciones, fecha_registro)

-- Restricciones clave:
-- usuarios.email debe ser unico.
-- reservas_sesion debe ser unico por (sesion_clase_id, usuario_id).
-- staff_perfiles.usuario_id debe ser unico.
-- pagos.referencia debe ser unica.
