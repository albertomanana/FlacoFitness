package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.dto.IngresoMensualStatsItem;
import com.flacofitness.app.model.entity.MembresiaUsuario;
import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoMembresia;
import com.flacofitness.app.model.enums.EstadoPago;
import com.flacofitness.app.model.enums.MetodoPago;
import com.flacofitness.app.repository.MembresiaUsuarioRepository;
import com.flacofitness.app.repository.PagoRepository;
import com.flacofitness.app.repository.PlanRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PagoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;
    private final MembresiaUsuarioRepository membresiaUsuarioRepository;

    public PagoService(PagoRepository pagoRepository,
                       UsuarioRepository usuarioRepository,
                       PlanRepository planRepository,
                       MembresiaUsuarioRepository membresiaUsuarioRepository) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.membresiaUsuarioRepository = membresiaUsuarioRepository;
    }

    public List<Pago> listarTodos() {
        return pagoRepository.findAllByOrderByFechaVencimientoDescIdDesc();
    }

    public List<Pago> listarPorUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioIdOrderByFechaVencimientoDescIdDesc(usuarioId);
    }

    public List<Pago> listarFiltrados(Long usuarioId, EstadoPago estado) {
        if (usuarioId != null && estado != null) {
            return pagoRepository.findByUsuarioIdAndEstadoOrderByFechaVencimientoDescIdDesc(usuarioId, estado);
        }

        if (usuarioId != null) {
            return pagoRepository.findByUsuarioIdOrderByFechaVencimientoDescIdDesc(usuarioId);
        }

        if (estado != null) {
            return pagoRepository.findByEstadoOrderByFechaVencimientoDescIdDesc(estado);
        }

        return pagoRepository.findAllByOrderByFechaVencimientoDescIdDesc();
    }

    public List<Pago> listarRecientesPorUsuario(Long usuarioId) {
        return pagoRepository.findTop5ByUsuarioIdOrderByFechaVencimientoDescIdDesc(usuarioId);
    }

    public Optional<Pago> buscarUltimoPorUsuario(Long usuarioId) {
        return pagoRepository.findTopByUsuarioIdOrderByFechaVencimientoDescIdDesc(usuarioId);
    }

    public long contarPendientesPorUsuario(Long usuarioId) {
        return pagoRepository.countByUsuarioIdAndEstado(usuarioId, EstadoPago.PENDIENTE);
    }

    public long contarVencidosPorUsuario(Long usuarioId) {
        return pagoRepository.countByUsuarioIdAndEstado(usuarioId, EstadoPago.VENCIDO);
    }

    public long contarTodos() {
        return pagoRepository.count();
    }

    public BigDecimal calcularIngresosTotales() {
        return pagoRepository.sumMontoByEstado(EstadoPago.PAGADO);
    }

    public BigDecimal calcularDeudaTotalPorUsuario(Long usuarioId) {
        BigDecimal deudaPendiente = pagoRepository.findByUsuarioId(usuarioId).stream()
                .filter(p -> p.getEstado() != EstadoPago.PAGADO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return deudaPendiente;
    }

    public BigDecimal calcularIngresosMesActual() {
        LocalDate hoy = LocalDate.now();
        return pagoRepository.sumMontoByEstadoAndPeriodo(EstadoPago.PAGADO, hoy.getYear(), hoy.getMonthValue());
    }

    public long contarPagosPendientes() {
        return pagoRepository.countByEstado(EstadoPago.PENDIENTE);
    }

    public long contarPagosVencidos() {
        return pagoRepository.countByEstado(EstadoPago.VENCIDO);
    }

    public List<Pago> listarRecientes() {
        return pagoRepository.findTop8ByOrderByFechaVencimientoDescIdDesc();
    }

    @Transactional
    public int generarPagosMensuales() {
        LocalDate fechaReferencia = LocalDate.now();
        List<Usuario> usuariosConPagoPendiente = usuarioRepository.findUsuariosConPagoPendiente(fechaReferencia);
        int pagosGenerados = 0;

        for (Usuario usuario : usuariosConPagoPendiente) {
            Plan plan = usuario.getPlan();
            LocalDate fechaProximoVencimiento = resolverFechaProximoPago(usuario, plan, fechaReferencia);

            while (fechaProximoVencimiento != null && !fechaProximoVencimiento.isAfter(fechaReferencia)) {
                if (!pagoRepository.existsByUsuarioIdAndFechaVencimiento(usuario.getId(), fechaProximoVencimiento)) {
                    pagoRepository.save(crearPagoAutomatico(usuario, plan, fechaProximoVencimiento));
                    pagosGenerados++;
                }

                fechaProximoVencimiento = calcularSiguienteFechaPago(fechaProximoVencimiento, plan);
            }

            usuario.setFechaProximoPago(fechaProximoVencimiento);
        }

        LOGGER.info("Proceso de pagos automáticos completado. Pagos generados: {}", pagosGenerados);
        return pagosGenerados;
    }

    public List<IngresoMensualStatsItem> obtenerIngresosMensuales() {
        return pagoRepository.sumMontoGroupedByMes(EstadoPago.PAGADO).stream()
                .map(item -> new IngresoMensualStatsItem(
                        YearMonth.of(item.getAnio(), item.getMes()).toString(),
                        item.getTotal()))
                .toList();
    }

    public Pago buscarPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
    }

    @Transactional
    public Pago guardar(Pago pago) {
        Usuario usuario = obtenerUsuarioValido(pago.getUsuario());
        pago.setUsuario(usuario);
        MembresiaUsuario membresia = resolverMembresia(pago.getMembresiaUsuario(), usuario, pago.getPlan());
        pago.setMembresiaUsuario(membresia);
        pago.setPlan(membresia != null ? membresia.getPlan() : resolverPlan(pago.getPlan(), usuario));
        validarDuplicadoCiclo(pago.getUsuario().getId(), pago.getFechaVencimiento(), null);
        normalizarEstadoYFechas(pago);
        return pagoRepository.save(pago);
    }

    @Transactional
    public Pago actualizar(Long id, Pago pagoActualizado) {
        Pago pagoExistente = buscarPorId(id);
        Usuario usuario = obtenerUsuarioValido(pagoActualizado.getUsuario());
        MembresiaUsuario membresia = resolverMembresia(pagoActualizado.getMembresiaUsuario(), usuario, pagoActualizado.getPlan());
        Plan plan = membresia != null ? membresia.getPlan() : resolverPlan(pagoActualizado.getPlan(), usuario);

        pagoExistente.setFechaVencimiento(pagoActualizado.getFechaVencimiento());
        pagoExistente.setFechaPago(pagoActualizado.getFechaPago());
        pagoExistente.setMetodoPago(pagoActualizado.getMetodoPago());
        pagoExistente.setEstado(pagoActualizado.getEstado());
        pagoExistente.setUsuario(usuario);
        pagoExistente.setPlan(plan);
        pagoExistente.setMembresiaUsuario(membresia);
        validarDuplicadoCiclo(pagoExistente.getUsuario().getId(), pagoExistente.getFechaVencimiento(), pagoExistente.getId());
        normalizarEstadoYFechas(pagoExistente);

        return pagoRepository.save(pagoExistente);
    }
    
    @Transactional
    public Pago marcarComoPagado(Long id) {
        Pago pagoExistente = buscarPorId(id);
        pagoExistente.setEstado(EstadoPago.PAGADO);
        pagoExistente.setFechaPago(LocalDate.now());
        normalizarEstadoYFechas(pagoExistente);
        return pagoRepository.save(pagoExistente);
    }

    private void validarDuplicadoCiclo(Long usuarioId, LocalDate fechaVencimiento, Long idExcluir) {
        if (usuarioId == null || fechaVencimiento == null) {
            throw new BusinessValidationException("Debes indicar un usuario y una fecha de vencimiento validos");
        }

        boolean duplicado = idExcluir == null
                ? pagoRepository.existsByUsuarioIdAndFechaVencimiento(usuarioId, fechaVencimiento)
                : pagoRepository.existsByUsuarioIdAndFechaVencimientoAndIdNot(usuarioId, fechaVencimiento, idExcluir);

        if (duplicado) {
            throw new BusinessValidationException(
                    "Ya existe una cuota para este usuario con la misma fecha de vencimiento");
        }
    }

    private void normalizarEstadoYFechas(Pago pago) {
        if (pago.getEstado() == null) {
            pago.setEstado(EstadoPago.PENDIENTE);
        }

        if (pago.getEstado() == EstadoPago.PAGADO) {
            if (pago.getFechaPago() == null) {
                pago.setFechaPago(LocalDate.now());
            }
            return;
        }

        pago.setFechaPago(null);
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessValidationException("El pago debe estar asociado a un usuario valido");
        }

        return usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuario.getId()));
    }

    private Plan resolverPlan(Plan planSeleccionado, Usuario usuario) {
        if (planSeleccionado != null && planSeleccionado.getId() != null) {
            return obtenerPlanValido(planSeleccionado.getId());
        }

        if (usuario.getPlan() != null && usuario.getPlan().getId() != null) {
            return obtenerPlanValido(usuario.getPlan().getId());
        }

        throw new BusinessValidationException("El pago debe estar asociado a un plan valido o a un usuario con plan asignado");
    }

    private MembresiaUsuario resolverMembresia(MembresiaUsuario membresiaSeleccionada, Usuario usuario, Plan planSeleccionado) {
        if (membresiaSeleccionada != null && membresiaSeleccionada.getId() != null) {
            MembresiaUsuario membresia = membresiaUsuarioRepository.findById(membresiaSeleccionada.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Membresia de usuario no encontrada con id: " + membresiaSeleccionada.getId()));
            if (!membresia.getUsuario().getId().equals(usuario.getId())) {
                throw new BusinessValidationException("La membresia seleccionada no pertenece al usuario indicado");
            }
            return membresia;
        }

        Long planId = planSeleccionado != null ? planSeleccionado.getId() : null;
        if (planId == null && usuario.getPlan() != null) {
            planId = usuario.getPlan().getId();
        }

        Long finalPlanId = planId;
        return membresiaUsuarioRepository
                .findTopByUsuarioIdAndEstadoInOrderByFechaInicioDescIdDesc(
                        usuario.getId(),
                        List.of(EstadoMembresia.ACTIVA, EstadoMembresia.PENDIENTE, EstadoMembresia.PRUEBA))
                .filter(membresia -> finalPlanId == null || membresia.getPlan().getId().equals(finalPlanId))
                .orElse(null);
    }

    private Plan obtenerPlanValido(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con id: " + planId));

        if (!Boolean.TRUE.equals(plan.getActivo())) {
            throw new BusinessValidationException("El plan asociado al pago debe estar activo");
        }

        return plan;
    }

    private LocalDate resolverFechaProximoPago(Usuario usuario, Plan plan, LocalDate fechaReferencia) {
        if (usuario.getFechaProximoPago() != null) {
            return usuario.getFechaProximoPago();
        }

        return pagoRepository.findTopByUsuarioIdOrderByFechaVencimientoDescIdDesc(usuario.getId())
                .map(pago -> calcularSiguienteFechaPago(pago.getFechaVencimiento(), plan))
                .orElseGet(() -> calcularSiguienteFechaPago(fechaReferencia, plan));
    }

    private Pago crearPagoAutomatico(Usuario usuario, Plan plan, LocalDate fechaVencimiento) {
        Pago pago = new Pago();
        pago.setUsuario(usuario);
        MembresiaUsuario membresia = resolverMembresia(null, usuario, plan);
        pago.setMembresiaUsuario(membresia);
        pago.setPlan(membresia != null ? membresia.getPlan() : plan);
        pago.setFechaVencimiento(fechaVencimiento);
        pago.setFechaPago(null); 
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setEstado(EstadoPago.PENDIENTE);
        return pago;
    }

    private LocalDate calcularSiguienteFechaPago(LocalDate fechaBase, Plan plan) {
        return fechaBase.plusDays(Math.max(plan.getDuracionDias(), 1));
    }

    // NUEVAS MÉTRICAS PARA CONTROL FINANCIERO

    public enum EstadoFinanciero {
        AL_DIA, CON_DEUDA, CON_PAGOS_VENCIDOS
    }

    public EstadoFinanciero determinarEstadoFinanciero(Long usuarioId) {
        long vencidos = contarVencidosPorUsuario(usuarioId);
        if (vencidos > 0) {
            return EstadoFinanciero.CON_PAGOS_VENCIDOS;
        }
        long pendientes = contarPendientesPorUsuario(usuarioId);
        if (pendientes > 0) {
            return EstadoFinanciero.CON_DEUDA;
        }
        return EstadoFinanciero.AL_DIA;
    }

    public BigDecimal calcularDeudaPendientePorUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId).stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularDeudaVencidaPorUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId).stream()
                .filter(p -> p.getEstado() == EstadoPago.VENCIDO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long contarUsuariosAlDia() {
        return usuarioRepository.findAll().stream()
                .mapToLong(u -> u.getId())
                .filter(id -> determinarEstadoFinanciero(id) == EstadoFinanciero.AL_DIA)
                .count();
    }

    public long contarUsuariosConDeuda() {
        return usuarioRepository.findAll().stream()
                .mapToLong(u -> u.getId())
                .filter(id -> determinarEstadoFinanciero(id) == EstadoFinanciero.CON_DEUDA)
                .count();
    }

    public long contarUsuariosConPagosVencidos() {
        return usuarioRepository.findAll().stream()
                .mapToLong(u -> u.getId())
                .filter(id -> determinarEstadoFinanciero(id) == EstadoFinanciero.CON_PAGOS_VENCIDOS)
                .count();
    }
}
