package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.InternalChatAction;
import com.flacofitness.app.model.dto.InternalChatResponse;
import com.flacofitness.app.model.entity.MembresiaUsuario;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.security.AccessProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class InternalAssistantService {

    private final PagoService pagoService;
    private final MembresiaService membresiaService;
    private final RutinaService rutinaService;
    private final AsistenciaService asistenciaService;
    private final SesionClaseService sesionClaseService;
    private final NominaService nominaService;
    private final UsuarioService usuarioService;
    private final StaffPerfilRepository staffPerfilRepository;
    private final OperationalClockService operationalClockService;

    public InternalAssistantService(PagoService pagoService,
                                    MembresiaService membresiaService,
                                    RutinaService rutinaService,
                                    AsistenciaService asistenciaService,
                                    SesionClaseService sesionClaseService,
                                    NominaService nominaService,
                                    UsuarioService usuarioService,
                                    StaffPerfilRepository staffPerfilRepository,
                                    OperationalClockService operationalClockService) {
        this.pagoService = pagoService;
        this.membresiaService = membresiaService;
        this.rutinaService = rutinaService;
        this.asistenciaService = asistenciaService;
        this.sesionClaseService = sesionClaseService;
        this.nominaService = nominaService;
        this.usuarioService = usuarioService;
        this.staffPerfilRepository = staffPerfilRepository;
        this.operationalClockService = operationalClockService;
    }

    public InternalChatResponse answer(AccessProfile profile, Usuario usuario, String rawMessage) {
        String message = normalize(rawMessage);
        if (profile == AccessProfile.CLIENTE) {
            return answerClient(usuario, message);
        }
        if (profile == AccessProfile.STAFF_ENTRENADOR
                || profile == AccessProfile.STAFF_RECEPCION
                || profile == AccessProfile.STAFF_GERENTE) {
            return answerStaff(profile, usuario, message);
        }
        return answerAdmin(message);
    }

    private InternalChatResponse answerClient(Usuario usuario, String message) {
        Long userId = usuario.getId();
        if (containsAny(message, "pago", "deuda", "cuota")) {
            BigDecimal deuda = pagoService.calcularDeudaTotalPorUsuario(userId);
            long vencidos = pagoService.contarVencidosPorUsuario(userId);
            String text = deuda.signum() > 0
                    ? "Tienes " + deuda + " EUR pendientes. Pagos vencidos: " + vencidos + "."
                    : "Tus pagos estan al dia.";
            return response(text,
                    action("Ver mis pagos", "/cliente/pagos"),
                    action("Cambiar contraseña", "/cuenta/password"));
        }
        if (containsAny(message, "membresia", "plan", "renovacion")) {
            MembresiaUsuario contrato = membresiaService.buscarContratoActivoPorUsuario(userId).orElse(null);
            String text = contrato == null
                    ? "Ahora mismo no hay una membresia activa asociada a tu cuenta."
                    : "Tu plan es " + contrato.getPlan().getNombre() + " y vence el " + contrato.getFechaFin() + ".";
            return response(text, action("Ver membresia", "/cliente/membresia"));
        }
        if (containsAny(message, "rutina", "entreno", "entrenamiento")) {
            int total = rutinaService.listarPorUsuario(userId).size();
            return response("Tienes " + total + " rutina(s) asignada(s).",
                    action("Ver rutinas", "/cliente/rutinas"));
        }
        if (containsAny(message, "clase", "sesion", "horario", "reserva")) {
            int total = sesionClaseService.listarProximas().size();
            return response("Hay " + total + " clase(s) proximas disponibles.",
                    action("Ver clases", "/cliente/clases"));
        }
        if (containsAny(message, "asistencia", "check", "entrada")) {
            long total = asistenciaService.contarPorUsuario(userId);
            return response("Tienes " + total + " asistencia(s) registradas.",
                    action("Ver asistencias", "/cliente/asistencias"),
                    action("Hacer check-in", "/cliente"));
        }
        return response("Puedo ayudarte con pagos, membresia, rutinas, clases, asistencias o contraseña.",
                action("Mi panel", "/cliente"),
                action("Cambiar contraseña", "/cuenta/password"));
    }

    private InternalChatResponse answerStaff(AccessProfile profile, Usuario usuario, String message) {
        StaffPerfil staffPerfil = staffPerfilRepository.findByUsuarioId(usuario.getId()).orElse(null);
        LocalDate hoy = operationalClockService.today();
        if (containsAny(message, "nomina", "salario")) {
            int total = staffPerfil == null ? 0 : nominaService.listarFiltradas(staffPerfil.getId()).size();
            return response("Tienes " + total + " nomina(s) asociada(s) a tu perfil.",
                    action("Mis nominas", "/staff/nominas"));
        }
        if (containsAny(message, "clase", "sesion", "hoy", "agenda")) {
            int total = sesionClaseService.listarPorFecha(hoy).size();
            return response("Hoy hay " + total + " sesion(es) en agenda.",
                    action("Ver panel", "/staff/dashboard"),
                    action("Sesiones", "/sesiones"));
        }
        if (containsAny(message, "cliente", "usuario", "buscar")) {
            long activos = usuarioService.contarActivos();
            return response("Hay " + activos + " cliente(s) activos.",
                    action("Clientes", "/usuarios"));
        }
        if (containsAny(message, "check", "asistencia", "entrada")) {
            return response("Puedes registrar check-in desde tu panel de staff.",
                    action("Ir al panel", "/staff/dashboard"),
                    action("Asistencias", "/asistencias"));
        }
        String text = profile == AccessProfile.STAFF_GERENTE
                ? "Puedo ayudarte con agenda, clientes, nominas y accesos de gestion."
                : "Puedo ayudarte con agenda, clientes, check-in y tus nominas.";
        return response(text, action("Mi panel", "/staff/dashboard"));
    }

    private InternalChatResponse answerAdmin(String message) {
        if (containsAny(message, "automat", "procesar", "recurrente")) {
            return response("Puedes ejecutar pagos, gastos, nominas, membresias, maquinas y stock desde Automatizar.",
                    action("Dashboard", "/"),
                    action("Finanzas", "/finanzas"));
        }
        if (containsAny(message, "pago", "deuda")) {
            return response("Los pagos pendientes y vencidos se revisan desde Pagos.",
                    action("Pagos", "/pagos"));
        }
        if (containsAny(message, "gasto", "nomina", "finanza")) {
            return response("El resumen de gastos, nominas e ingresos esta en Finanzas.",
                    action("Finanzas", "/finanzas"),
                    action("Nominas", "/nominas"));
        }
        return response("Puedo orientarte hacia pagos, gastos, nominas, clientes, staff o automatizaciones.",
                action("Dashboard", "/"),
                action("Usuarios", "/usuarios"),
                action("Finanzas", "/finanzas"));
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private InternalChatResponse response(String message, InternalChatAction... actions) {
        return new InternalChatResponse(message, List.of(actions).stream().limit(3).toList());
    }

    private InternalChatAction action(String label, String url) {
        return new InternalChatAction(label, url);
    }
}
