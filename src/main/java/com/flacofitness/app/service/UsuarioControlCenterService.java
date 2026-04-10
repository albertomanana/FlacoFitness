package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.UsuarioControlCenterView;
import com.flacofitness.app.model.dto.UsuarioTimelineItem;
import com.flacofitness.app.model.entity.Asistencia;
import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.entity.Rutina;
import com.flacofitness.app.model.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioControlCenterService {

    private static final int DIAS_ACTIVIDAD_RECIENTE = 14;
    private static final int DIAS_ACTIVIDAD_MEDIA = 30;

    private final AsistenciaService asistenciaService;
    private final PagoService pagoService;
    private final RutinaService rutinaService;

    public UsuarioControlCenterService(AsistenciaService asistenciaService,
                                       PagoService pagoService,
                                       RutinaService rutinaService) {
        this.asistenciaService = asistenciaService;
        this.pagoService = pagoService;
        this.rutinaService = rutinaService;
    }

    public UsuarioControlCenterView construirVista(Usuario usuario) {
        List<Rutina> rutinasAsignadas = rutinaService.listarPorUsuario(usuario.getId()).stream()
                .sorted(Comparator.comparing(Rutina::getActiva).reversed()
                        .thenComparing(Rutina::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<Asistencia> asistenciasRecientes = asistenciaService.listarRecientesPorUsuario(usuario.getId());
        List<Pago> pagosRecientes = pagoService.listarRecientesPorUsuario(usuario.getId());

        long totalAsistencias = asistenciaService.contarPorUsuario(usuario.getId());
        LocalDate ultimaAsistencia = asistenciaService.buscarUltimaPorUsuario(usuario.getId())
                .map(Asistencia::getFecha)
                .orElse(null);

        Long diasSinAsistencia = ultimaAsistencia != null
                ? ChronoUnit.DAYS.between(ultimaAsistencia, LocalDate.now())
                : null;

        boolean actividadReciente = usuario.getActivo() && diasSinAsistencia != null && diasSinAsistencia <= DIAS_ACTIVIDAD_RECIENTE;
        long pagosPendientes = pagoService.contarPendientesPorUsuario(usuario.getId());
        long pagosVencidos = pagoService.contarVencidosPorUsuario(usuario.getId());
        boolean pagosAlDia = pagosPendientes == 0 && pagosVencidos == 0;

        String estadoActividad = resolverEstadoActividad(usuario, diasSinAsistencia);
        String estadoPago = resolverEstadoPago(pagosPendientes, pagosVencidos);
        String segmento = resolverSegmento(usuario, actividadReciente, pagosPendientes, pagosVencidos);
        int score = calcularScore(usuario, totalAsistencias, diasSinAsistencia, pagosPendientes, pagosVencidos);

        return new UsuarioControlCenterView(
                totalAsistencias,
                ultimaAsistencia,
                diasSinAsistencia,
                actividadReciente,
                estadoActividad,
                pagosPendientes,
                pagosVencidos,
                pagosAlDia,
                estadoPago,
                score,
                resolverCategoriaScore(score),
                segmento,
                construirResumen(segmento, diasSinAsistencia, pagosPendientes, pagosVencidos, rutinasAsignadas.size()),
                rutinasAsignadas,
                asistenciasRecientes,
                pagosRecientes,
                construirTimeline(usuario, asistenciasRecientes, pagosRecientes));
    }

    private String resolverEstadoActividad(Usuario usuario, Long diasSinAsistencia) {
        if (!usuario.getActivo()) {
            return "Baja logica";
        }

        if (diasSinAsistencia == null) {
            return "Sin asistencias";
        }

        if (diasSinAsistencia <= DIAS_ACTIVIDAD_RECIENTE) {
            return "Usuario activo";
        }

        if (diasSinAsistencia <= DIAS_ACTIVIDAD_MEDIA) {
            return "Actividad moderada";
        }

        return "Usuario inactivo";
    }

    private String resolverEstadoPago(long pagosPendientes, long pagosVencidos) {
        if (pagosVencidos > 0) {
            return "Moroso";
        }
        if (pagosPendientes > 0) {
            return "Pago pendiente";
        }
        return "Al dia";
    }

    private String resolverSegmento(Usuario usuario, boolean actividadReciente, long pagosPendientes, long pagosVencidos) {
        if (pagosVencidos > 0 || pagosPendientes > 0) {
            return "Moroso";
        }
        if (usuario.getActivo() && actividadReciente) {
            return "Activo";
        }
        return "Inactivo";
    }

    private int calcularScore(Usuario usuario,
                              long totalAsistencias,
                              Long diasSinAsistencia,
                              long pagosPendientes,
                              long pagosVencidos) {
        int score = 0;

        if (usuario.getActivo()) {
            score += 10;
        }

        score += (int) Math.min(totalAsistencias * 3, 35);

        if (diasSinAsistencia == null) {
            score += 0;
        } else if (diasSinAsistencia <= DIAS_ACTIVIDAD_RECIENTE) {
            score += 25;
        } else if (diasSinAsistencia <= DIAS_ACTIVIDAD_MEDIA) {
            score += 12;
        }

        if (pagosVencidos > 0) {
            score += 0;
        } else if (pagosPendientes > 0) {
            score += 15;
        } else {
            score += 30;
        }

        return Math.min(score, 100);
    }

    private String resolverCategoriaScore(int score) {
        if (score >= 80) {
            return "Muy alto";
        }
        if (score >= 60) {
            return "Alto";
        }
        if (score >= 40) {
            return "Medio";
        }
        return "En riesgo";
    }

    private String construirResumen(String segmento,
                                    Long diasSinAsistencia,
                                    long pagosPendientes,
                                    long pagosVencidos,
                                    int rutinasAsignadas) {
        List<String> mensajes = new ArrayList<>();

        mensajes.add("Segmento actual: " + segmento.toLowerCase() + ".");

        if (diasSinAsistencia == null) {
            mensajes.add("Todavia no registra asistencias.");
        } else if (diasSinAsistencia == 0) {
            mensajes.add("Ha asistido hoy.");
        } else {
            mensajes.add("Ultima asistencia hace " + diasSinAsistencia + " dia(s).");
        }

        if (pagosVencidos > 0) {
            mensajes.add("Tiene " + pagosVencidos + " pago(s) vencido(s).");
        } else if (pagosPendientes > 0) {
            mensajes.add("Tiene " + pagosPendientes + " pago(s) pendiente(s).");
        } else {
            mensajes.add("Esta al dia con los pagos.");
        }

        mensajes.add("Rutinas asignadas: " + rutinasAsignadas + ".");
        return String.join(" ", mensajes);
    }

    private List<UsuarioTimelineItem> construirTimeline(Usuario usuario,
                                                        List<Asistencia> asistenciasRecientes,
                                                        List<Pago> pagosRecientes) {
        List<UsuarioTimelineItem> timeline = new ArrayList<>();

        for (Asistencia asistencia : asistenciasRecientes) {
            LocalTime hora = asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada() : LocalTime.MIN;
            timeline.add(new UsuarioTimelineItem(
                    "Asistencia",
                    "Check-in registrado",
                    asistencia.getFecha() != null
                            ? "Acceso del " + asistencia.getFecha()
                            : "Asistencia registrada",
                    asistencia.getFecha() != null ? asistencia.getFecha().atTime(hora) : LocalDateTime.MIN));
        }

        for (Pago pago : pagosRecientes) {
            String descripcion = pago.getEstado() != null
                    ? "Pago " + pago.getEstado().name().toLowerCase() + " por " + pago.getMonto()
                    : "Movimiento de pago registrado";
            timeline.add(new UsuarioTimelineItem(
                    "Pago",
                    "Movimiento financiero",
                    descripcion,
                    pago.getFechaPago() != null ? pago.getFechaPago().atStartOfDay() : LocalDateTime.MIN));
        }

        if (usuario.getFechaRegistro() != null) {
            timeline.add(new UsuarioTimelineItem(
                    "Perfil",
                    "Alta de usuario",
                    "Registro inicial del miembro en la plataforma",
                    usuario.getFechaRegistro()));
        }

        return timeline.stream()
                .sorted(Comparator.comparing(UsuarioTimelineItem::fechaHora).reversed())
                .limit(8)
                .toList();
    }
}
