package com.flacofitness.app.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.dto.AsistenciaCheckInBatchResult;
import com.flacofitness.app.model.entity.Asistencia;
import com.flacofitness.app.model.entity.Clase;
import com.flacofitness.app.model.entity.ReservaSesion;
import com.flacofitness.app.model.entity.Rutina;
import com.flacofitness.app.model.entity.SesionClase;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoReservaSesion;
import com.flacofitness.app.model.enums.EstadoSesion;
import com.flacofitness.app.model.enums.RolStaff;
import com.flacofitness.app.repository.AsistenciaRepository;
import com.flacofitness.app.repository.ClaseRepository;
import com.flacofitness.app.repository.ReservaSesionRepository;
import com.flacofitness.app.repository.RutinaRepository;
import com.flacofitness.app.repository.SesionClaseRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import com.flacofitness.app.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class SesionClaseService {

    private static final Collection<EstadoReservaSesion> ESTADOS_OCUPAN_CUPO = List.of(
            EstadoReservaSesion.RESERVADA,
            EstadoReservaSesion.ASISTIO
    );

    private final SesionClaseRepository sesionClaseRepository;
    private final ReservaSesionRepository reservaSesionRepository;
    private final ClaseRepository claseRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final RutinaRepository rutinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final OperationalClockService operationalClockService;

    public SesionClaseService(SesionClaseRepository sesionClaseRepository,
                              ReservaSesionRepository reservaSesionRepository,
                              ClaseRepository claseRepository,
                              StaffPerfilRepository staffPerfilRepository,
                              RutinaRepository rutinaRepository,
                              UsuarioRepository usuarioRepository,
                              AsistenciaRepository asistenciaRepository,
                              OperationalClockService operationalClockService) {
        this.sesionClaseRepository = sesionClaseRepository;
        this.reservaSesionRepository = reservaSesionRepository;
        this.claseRepository = claseRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.rutinaRepository = rutinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.operationalClockService = operationalClockService;
    }

    public List<SesionClase> listarTodas() {
        return sesionClaseRepository.findAllOrdered();
    }

    public List<SesionClase> listarPorFecha(LocalDate fecha) {
        return sesionClaseRepository.findByFechaOrderByHoraInicioAscIdAsc(fecha);
    }

    public List<SesionClase> listarFiltrados(LocalDate fecha, EstadoSesion estado) {
        if (fecha == null && estado == null) {
            return listarTodas();
        }
        return sesionClaseRepository.findByFiltros(fecha, estado);
    }

    public List<SesionClase> listarProximas() {
        return sesionClaseRepository.findTop8ByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraInicioAscIdAsc(
                operationalClockService.today(), EstadoSesion.PROGRAMADA);
    }

    public SesionClase buscarPorId(Long id) {
        return sesionClaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesion no encontrada con id: " + id));
    }

    public List<ReservaSesion> listarReservas(Long sesionId) {
        return reservaSesionRepository.findBySesionClaseIdOrderByFechaReservaDescIdDesc(sesionId);
    }

    public List<Asistencia> listarAsistencias(Long sesionId) {
        return asistenciaRepository.findBySesionClaseIdOrderByHoraEntradaDescIdDesc(sesionId);
    }

    public long contarSesionesHoy() {
        return sesionClaseRepository.countByFechaAndEstado(operationalClockService.today(), EstadoSesion.PROGRAMADA);
    }

    public long contarProgramadas() {
        return sesionClaseRepository.countProgramadasDesde(operationalClockService.today(), EstadoSesion.PROGRAMADA);
    }

    @Transactional
    public int cerrarSesionesFinalizadas() {
        LocalDate hoy = operationalClockService.today();
        List<SesionClase> vencidas = sesionClaseRepository.findByFechaBeforeAndEstado(hoy, EstadoSesion.PROGRAMADA);
        int cerradas = 0;
        for (SesionClase sesion : vencidas) {
            sesion.setEstado(EstadoSesion.FINALIZADA);
            sesionClaseRepository.save(sesion);
            cerradas++;
        }
        return cerradas;
    }

    public long contarCuposLlenosProximos() {
        return listarProximas().stream()
                .filter(this::estaLlena)
                .count();
    }

    public long contarReservasActivas(Long sesionId) {
        return reservaSesionRepository.countBySesionClaseIdAndEstadoIn(sesionId, ESTADOS_OCUPAN_CUPO);
    }

    public boolean estaLlena(SesionClase sesionClase) {
        if (sesionClase == null || sesionClase.getId() == null || sesionClase.getAforo() == null) {
            return false;
        }
        return contarReservasActivas(sesionClase.getId()) >= sesionClase.getAforo();
    }

    @Transactional
    public SesionClase guardar(SesionClase sesionClase) {
        normalizarSesion(sesionClase);
        return sesionClaseRepository.save(sesionClase);
    }

    @Transactional
    public SesionClase actualizar(Long id, SesionClase sesionActualizada) {
        SesionClase sesion = buscarPorId(id);
        sesion.setClase(sesionActualizada.getClase());
        sesion.setFecha(sesionActualizada.getFecha());
        sesion.setHoraInicio(sesionActualizada.getHoraInicio());
        sesion.setHoraFin(sesionActualizada.getHoraFin());
        sesion.setAforo(sesionActualizada.getAforo());
        sesion.setEstado(sesionActualizada.getEstado());
        sesion.setRutina(sesionActualizada.getRutina());
        sesion.setStaffResponsable(sesionActualizada.getStaffResponsable());
        sesion.setObservaciones(sesionActualizada.getObservaciones());
        normalizarSesion(sesion);
        return sesionClaseRepository.save(sesion);
    }

    @Transactional
    public void cancelar(Long id) {
        SesionClase sesion = buscarPorId(id);
        sesion.setEstado(EstadoSesion.CANCELADA);
        sesionClaseRepository.save(sesion);
    }

    @Transactional
    public ReservaSesion reservarUsuario(Long sesionId, Long usuarioId) {
        SesionClase sesion = buscarPorId(sesionId);
        Usuario usuario = obtenerUsuarioActivo(usuarioId);

        if (sesion.getEstado() != EstadoSesion.PROGRAMADA) {
            throw new BusinessValidationException("Solo se pueden reservar sesiones programadas");
        }

        return reservaSesionRepository.findBySesionClaseIdAndUsuarioId(sesionId, usuarioId)
                .map(reserva -> reactivarReserva(reserva, sesion))
                .orElseGet(() -> crearReserva(sesion, usuario));
    }

    @Transactional
    public void quitarReserva(Long sesionId, Long usuarioId) {
        ReservaSesion reserva = reservaSesionRepository.findBySesionClaseIdAndUsuarioId(sesionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        reserva.setEstado(EstadoReservaSesion.CANCELADA);
        reservaSesionRepository.save(reserva);
    }

    @Transactional
    public AsistenciaCheckInBatchResult registrarAsistenciaSesion(Long sesionId, List<Long> usuarioIds, String observaciones) {
        SesionClase sesion = buscarPorId(sesionId);
        if (sesion.getEstado() == EstadoSesion.CANCELADA) {
            throw new BusinessValidationException("No se puede registrar asistencia en una sesion cancelada");
        }

        Set<Long> idsUnicos = usuarioIds == null ? Set.of() : usuarioIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (idsUnicos.isEmpty()) {
            throw new BusinessValidationException("Debes seleccionar al menos un usuario");
        }

        int creados = 0;
        int omitidos = 0;
        LocalTime horaEntrada = operationalClockService.time().withSecond(0).withNano(0);

        for (Long usuarioId : idsUnicos) {
            Usuario usuario = obtenerUsuarioActivo(usuarioId);
            if (asistenciaRepository.existsByUsuarioIdAndSesionClaseId(usuarioId, sesionId)) {
                omitidos++;
                continue;
            }

            Asistencia asistencia = new Asistencia();
            asistencia.setUsuario(usuario);
            asistencia.setSesionClase(sesion);
            asistencia.setFecha(sesion.getFecha());
            asistencia.setHoraEntrada(horaEntrada);
            asistencia.setObservaciones(normalizarTexto(observaciones));
            asistenciaRepository.save(asistencia);

            reservaSesionRepository.findBySesionClaseIdAndUsuarioId(sesionId, usuarioId)
                    .ifPresentOrElse(reserva -> {
                        reserva.setEstado(EstadoReservaSesion.ASISTIO);
                        reservaSesionRepository.save(reserva);
                    }, () -> {
                        ReservaSesion reserva = new ReservaSesion();
                        reserva.setSesionClase(sesion);
                        reserva.setUsuario(usuario);
                        reserva.setEstado(EstadoReservaSesion.ASISTIO);
                        reservaSesionRepository.save(reserva);
                    });

            creados++;
        }

        return new AsistenciaCheckInBatchResult(creados, omitidos);
    }

    private ReservaSesion reactivarReserva(ReservaSesion reserva, SesionClase sesion) {
        if (reserva.getEstado() == EstadoReservaSesion.CANCELADA || reserva.getEstado() == EstadoReservaSesion.NO_ASISTIO) {
            validarCupoDisponible(sesion);
            reserva.setEstado(EstadoReservaSesion.RESERVADA);
            return reservaSesionRepository.save(reserva);
        }
        return reserva;
    }

    private ReservaSesion crearReserva(SesionClase sesion, Usuario usuario) {
        validarCupoDisponible(sesion);
        ReservaSesion reserva = new ReservaSesion();
        reserva.setSesionClase(sesion);
        reserva.setUsuario(usuario);
        reserva.setEstado(EstadoReservaSesion.RESERVADA);
        return reservaSesionRepository.save(reserva);
    }

    private void validarCupoDisponible(SesionClase sesion) {
        if (sesion.getAforo() != null && contarReservasActivas(sesion.getId()) >= sesion.getAforo()) {
            throw new BusinessValidationException("La sesion ya tiene el cupo completo");
        }
    }

    private void normalizarSesion(SesionClase sesionClase) {
        sesionClase.setClase(obtenerClaseActiva(sesionClase.getClase()));
        sesionClase.setStaffResponsable(obtenerStaffOpcional(sesionClase.getStaffResponsable()));
        sesionClase.setRutina(obtenerRutinaOpcional(sesionClase.getRutina()));
        if (sesionClase.getFecha() == null) {
            sesionClase.setFecha(operationalClockService.today());
        }
        if (sesionClase.getHoraInicio() == null) {
            sesionClase.setHoraInicio(operationalClockService.time().withSecond(0).withNano(0));
        }
        if (sesionClase.getAforo() == null || sesionClase.getAforo() <= 0) {
            Integer capacidad = sesionClase.getClase().getCapacidadSugerida();
            sesionClase.setAforo(capacidad != null && capacidad > 0 ? capacidad : 12);
        }
        if (sesionClase.getEstado() == null) {
            sesionClase.setEstado(EstadoSesion.PROGRAMADA);
        }
        if (sesionClase.getHoraFin() != null && !sesionClase.getHoraFin().isAfter(sesionClase.getHoraInicio())) {
            throw new BusinessValidationException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private Clase obtenerClaseActiva(Clase clase) {
        if (clase == null || clase.getId() == null) {
            throw new BusinessValidationException("Debes seleccionar una clase");
        }
        Clase clasePersistida = claseRepository.findById(clase.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada con id: " + clase.getId()));
        if (!Boolean.TRUE.equals(clasePersistida.getActiva())) {
            throw new BusinessValidationException("La clase seleccionada debe estar activa");
        }
        return clasePersistida;
    }

    private StaffPerfil obtenerStaffOpcional(StaffPerfil staffPerfil) {
        if (staffPerfil == null || staffPerfil.getId() == null) {
            return null;
        }
        return staffPerfilRepository.findById(staffPerfil.getId())
                .map(this::validarStaffEntrenador)
                .orElseThrow(() -> new ResourceNotFoundException("Staff no encontrado con id: " + staffPerfil.getId()));
    }

    private StaffPerfil validarStaffEntrenador(StaffPerfil staffPerfil) {
        if (!Boolean.TRUE.equals(staffPerfil.getActivo())) {
            throw new BusinessValidationException("El staff responsable debe estar activo");
        }
        boolean entrenador = staffPerfil.getRolStaff() == RolStaff.ENTRENADOR;
        boolean excepcionExplicita = Boolean.TRUE.equals(staffPerfil.getPuedeImpartirClases());
        if (!entrenador && !excepcionExplicita) {
            throw new BusinessValidationException("Solo entrenadores o staff autorizado pueden impartir sesiones");
        }
        return staffPerfil;
    }

    private Rutina obtenerRutinaOpcional(Rutina rutina) {
        if (rutina == null || rutina.getId() == null) {
            return null;
        }
        return rutinaRepository.findById(rutina.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada con id: " + rutina.getId()));
    }

    private Usuario obtenerUsuarioActivo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessValidationException("El usuario seleccionado debe estar activo");
        }
        return usuario;
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
