package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.dto.AsistenciaCalendarDayView;
import com.flacofitness.app.model.dto.AsistenciaCalendarView;
import com.flacofitness.app.model.dto.AsistenciaCheckInBatchResult;
import com.flacofitness.app.model.dto.AsistenciaDiariaStatsItem;
import com.flacofitness.app.model.dto.AsistenciaMensualStatsItem;
import com.flacofitness.app.model.dto.UsuarioAsistenciaCountDto;
import com.flacofitness.app.model.entity.Asistencia;
import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.AsistenciaRepository;
import com.flacofitness.app.repository.SesionClaseRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SesionClaseRepository sesionClaseRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                             UsuarioRepository usuarioRepository,
                             SesionClaseRepository sesionClaseRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.sesionClaseRepository = sesionClaseRepository;
    }

    public List<Asistencia> listarTodas() {
        return asistenciaRepository.findAllOrdered();
    }

    public List<Asistencia> listarPorUsuario(Long usuarioId) {
        return asistenciaRepository.findByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(usuarioId);
    }

    public List<Asistencia> listarPorFecha(LocalDate fecha) {
        return asistenciaRepository.findByFechaOrderByHoraEntradaDescIdDesc(fecha);
    }

    public List<Asistencia> listarPorFechaYUsuario(LocalDate fecha, Long usuarioId) {
        return asistenciaRepository.findByFechaAndUsuarioIdOrderByHoraEntradaDescIdDesc(fecha, usuarioId);
    }

    public List<Asistencia> listarPorSesion(Long sesionClaseId) {
        return asistenciaRepository.findBySesionClaseIdOrderByHoraEntradaDescIdDesc(sesionClaseId);
    }

    public List<Asistencia> listarRecientesPorUsuario(Long usuarioId) {
        return asistenciaRepository.findTop5ByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(usuarioId);
    }

    public long contarPorUsuario(Long usuarioId) {
        return asistenciaRepository.countByUsuarioId(usuarioId);
    }

    public long contarPorFecha(LocalDate fecha) {
        return asistenciaRepository.countByFecha(fecha);
    }

    public Optional<Asistencia> buscarUltimaPorUsuario(Long usuarioId) {
        return asistenciaRepository.findTopByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(usuarioId);
    }

    public long contarTodas() {
        return asistenciaRepository.count();
    }

    public long contarHoy() {
        return asistenciaRepository.countByFecha(LocalDate.now());
    }

    public List<AsistenciaDiariaStatsItem> obtenerAsistenciasPorDia() {
        return asistenciaRepository.countGroupedByFecha().stream()
                .map(item -> new AsistenciaDiariaStatsItem(item.getFecha(), item.getTotal() == null ? 0L : item.getTotal()))
                .toList();
    }

    public List<AsistenciaDiariaStatsItem> obtenerAsistenciasUltimosDias(int dias) {
        LocalDate fechaDesde = LocalDate.now().minusDays(Math.max(dias - 1, 0));
        return asistenciaRepository.countGroupedByFechaDesde(fechaDesde).stream()
                .map(item -> new AsistenciaDiariaStatsItem(item.getFecha(), item.getTotal() == null ? 0L : item.getTotal()))
                .toList();
    }

    public List<AsistenciaMensualStatsItem> obtenerAsistenciasMensuales() {
        return asistenciaRepository.countGroupedByMes().stream()
                .map(item -> new AsistenciaMensualStatsItem(
                        YearMonth.of(item.getAnio(), item.getMes()).toString(),
                        item.getTotal() == null ? 0L : item.getTotal()))
                .toList();
    }

    public AsistenciaCalendarView construirCalendarioMensual(YearMonth mes, LocalDate fechaSeleccionada) {
        YearMonth mesObjetivo = mes != null ? mes : YearMonth.now();
        LocalDate primerDiaMes = mesObjetivo.atDay(1);
        LocalDate ultimoDiaMes = mesObjetivo.atEndOfMonth();

        Map<LocalDate, Long> totalesPorDia = asistenciaRepository.countGroupedByFechaBetween(primerDiaMes, ultimoDiaMes)
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getFecha(),
                        item -> item.getTotal() == null ? 0L : item.getTotal()));

        LocalDate primerDiaCalendario = primerDiaMes.minusDays(primerDiaMes.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        LocalDate ultimoDiaCalendario = ultimoDiaMes.plusDays(DayOfWeek.SUNDAY.getValue() - ultimoDiaMes.getDayOfWeek().getValue());

        List<AsistenciaCalendarDayView> dias = primerDiaCalendario.datesUntil(ultimoDiaCalendario.plusDays(1))
                .map(fecha -> new AsistenciaCalendarDayView(
                        fecha,
                        fecha.getDayOfMonth(),
                        fecha.getMonthValue() == mesObjetivo.getMonthValue(),
                        fecha.equals(LocalDate.now()),
                        fechaSeleccionada != null && fecha.equals(fechaSeleccionada),
                        totalesPorDia.getOrDefault(fecha, 0L)))
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));

        return new AsistenciaCalendarView(
                mesObjetivo.toString(),
                capitalizar(mesObjetivo.atDay(1).format(formatter)),
                mesObjetivo.minusMonths(1).toString(),
                mesObjetivo.plusMonths(1).toString(),
                List.of("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"),
                dias);
    }

    public Asistencia buscarPorId(Long id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada con id: " + id));
    }

    @Transactional
    public Asistencia registrar(Asistencia asistencia) {
        asistencia.setUsuario(obtenerUsuarioValido(asistencia.getUsuario()));
        asistencia.setSesionClase(obtenerSesionOpcional(asistencia.getSesionClase()));
        return asistenciaRepository.save(asistencia);
    }

    @Transactional
    public AsistenciaCheckInBatchResult registrarCheckInRapido(List<Long> usuarioIds, String observaciones) {
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            throw new BusinessValidationException("Debes seleccionar al menos un usuario para registrar el check-in");
        }

        Set<Long> idsUnicos = usuarioIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (idsUnicos.isEmpty()) {
            throw new BusinessValidationException("Debes seleccionar al menos un usuario para registrar el check-in");
        }

        List<Usuario> usuarios = usuarioRepository.findAllById(idsUnicos);
        if (usuarios.size() != idsUnicos.size()) {
            Set<Long> idsEncontrados = usuarios.stream()
                    .map(Usuario::getId)
                    .collect(Collectors.toSet());

            Long usuarioFaltante = idsUnicos.stream()
                    .filter(id -> !idsEncontrados.contains(id))
                    .findFirst()
                    .orElse(null);

            throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioFaltante);
        }

        LocalDate fechaRegistro = LocalDate.now();
        LocalTime horaRegistro = LocalTime.now();
        String observacion = normalizarObservaciones(observaciones);
        int creados = 0;
        int omitidos = 0;

        for (Usuario usuario : usuarios) {
            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                omitidos++;
                continue;
            }

            if (asistenciaRepository.existsByUsuarioIdAndFecha(usuario.getId(), fechaRegistro)) {
                omitidos++;
                continue;
            }

            Asistencia asistencia = new Asistencia();
            asistencia.setUsuario(usuario);
            asistencia.setFecha(fechaRegistro);
            asistencia.setHoraEntrada(horaRegistro);
            asistencia.setObservaciones(observacion);
            asistenciaRepository.save(asistencia);
            creados++;
        }

        return new AsistenciaCheckInBatchResult(creados, omitidos);
    }

    public int calcularRachaActual(Long usuarioId) {
        List<Asistencia> asistencias = asistenciaRepository.findTop5ByUsuarioIdOrderByFechaDescHoraEntradaDescIdDesc(usuarioId);
        if (asistencias.isEmpty()) {
            return 0;
        }

        int racha = 0;
        LocalDate fechaEvaluar = LocalDate.now();

        for (Asistencia asistencia : asistencias) {
            long diff = java.time.temporal.ChronoUnit.DAYS.between(asistencia.getFecha(), fechaEvaluar);
            if (diff <= 2) {
                racha++;
                fechaEvaluar = asistencia.getFecha();
            } else {
                break;
            }
        }

        return racha;
    }

    public boolean esUsuarioEnRiesgo(Long usuarioId) {
        Optional<Asistencia> ultima = buscarUltimaPorUsuario(usuarioId);
        if (ultima.isEmpty()) {
            return true;
        }

        long daysSinceLast = java.time.temporal.ChronoUnit.DAYS.between(ultima.get().getFecha(), LocalDate.now());
        return daysSinceLast >= 14;
    }

    public enum EstadoActividad {
        ACTIVO, INACTIVO
    }

    public EstadoActividad determinarEstadoActividad(Long usuarioId) {
        Optional<Asistencia> ultima = buscarUltimaPorUsuario(usuarioId);
        if (ultima.isEmpty()) {
            return EstadoActividad.INACTIVO;
        }

        long daysSinceLast = java.time.temporal.ChronoUnit.DAYS.between(ultima.get().getFecha(), LocalDate.now());
        return daysSinceLast <= 7 ? EstadoActividad.ACTIVO : EstadoActividad.INACTIVO;
    }

    public long contarAsistenciasMesActual(Long usuarioId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        return asistenciaRepository.countByUsuarioIdAndFechaBetween(usuarioId, inicioMes, LocalDate.now());
    }

    public long contarAsistenciasUltimosDias(Long usuarioId, int dias) {
        LocalDate desde = LocalDate.now().minusDays(dias);
        return asistenciaRepository.countByUsuarioIdAndFechaBetween(usuarioId, desde, LocalDate.now());
    }

    public List<UsuarioAsistenciaCountDto> obtenerRankingUsuariosActivos(int top) {
        LocalDate inicioMes = LocalDate.now().minusMonths(1);
        return asistenciaRepository.findTopUsuariosByAsistenciasDesde(inicioMes).stream()
                .limit(top)
                .toList();
    }

    public long contarUsuariosActivos() {
        LocalDate desde = LocalDate.now().minusDays(7);
        return asistenciaRepository.countDistinctUsuariosActivosDesde(desde);
    }

    public long contarUsuariosInactivos() {
        LocalDate desde = LocalDate.now().minusDays(14);
        return Math.max(0, usuarioRepository.countByActivoTrue() - asistenciaRepository.countDistinctUsuariosActivosDesde(desde));
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessValidationException("La asistencia debe estar asociada a un usuario valido");
        }

        return usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuario.getId()));
    }

    private SesionClase obtenerSesionOpcional(SesionClase sesionClase) {
        if (sesionClase == null || sesionClase.getId() == null) {
            return null;
        }

        return sesionClaseRepository.findById(sesionClase.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesion no encontrada con id: " + sesionClase.getId()));
    }

    private String normalizarObservaciones(String observaciones) {
        if (observaciones == null) {
            return null;
        }

        String valor = observaciones.trim();
        return valor.isEmpty() ? null : valor;
    }

    private String capitalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return valor;
        }

        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1);
    }
}
