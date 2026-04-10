package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.dto.IngresoMensualStatsItem;
import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoPago;
import com.flacofitness.app.model.enums.MetodoPago;
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

    public PagoService(PagoRepository pagoRepository,
                       UsuarioRepository usuarioRepository,
                       PlanRepository planRepository) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
    }

    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }

    public List<Pago> listarPorUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId);
    }

    public List<Pago> listarRecientesPorUsuario(Long usuarioId) {
        return pagoRepository.findTop5ByUsuarioIdOrderByFechaPagoDescIdDesc(usuarioId);
    }

    public Optional<Pago> buscarUltimoPorUsuario(Long usuarioId) {
        return pagoRepository.findTopByUsuarioIdOrderByFechaPagoDescIdDesc(usuarioId);
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
        return pagoRepository.findTop8ByOrderByFechaPagoDescIdDesc();
    }

    @Transactional
    public int generarPagosMensuales() {
        LocalDate fechaReferencia = LocalDate.now();
        List<Usuario> usuariosConPagoPendiente = usuarioRepository.findUsuariosConPagoPendiente(fechaReferencia);
        int pagosGenerados = 0;

        for (Usuario usuario : usuariosConPagoPendiente) {
            Plan plan = usuario.getPlan();
            LocalDate fechaProximoPago = resolverFechaProximoPago(usuario, plan, fechaReferencia);

            while (fechaProximoPago != null && !fechaProximoPago.isAfter(fechaReferencia)) {
                if (!pagoRepository.existsByUsuarioIdAndFechaPago(usuario.getId(), fechaProximoPago)) {
                    pagoRepository.save(crearPagoAutomatico(usuario, plan, fechaProximoPago));
                    pagosGenerados++;
                }

                fechaProximoPago = calcularSiguienteFechaPago(fechaProximoPago, plan);
            }

            usuario.setFechaProximoPago(fechaProximoPago);
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
        pago.setPlan(resolverPlan(pago.getPlan(), usuario));
        return pagoRepository.save(pago);
    }

    @Transactional
    public Pago actualizar(Long id, Pago pagoActualizado) {
        Pago pagoExistente = buscarPorId(id);
        Usuario usuario = obtenerUsuarioValido(pagoActualizado.getUsuario());
        Plan plan = resolverPlan(pagoActualizado.getPlan(), usuario);

        pagoExistente.setFechaPago(pagoActualizado.getFechaPago());
        pagoExistente.setMetodoPago(pagoActualizado.getMetodoPago());
        pagoExistente.setEstado(pagoActualizado.getEstado());
        pagoExistente.setUsuario(usuario);
        pagoExistente.setPlan(plan);

        return pagoRepository.save(pagoExistente);
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

        return pagoRepository.findTopByUsuarioIdOrderByFechaPagoDescIdDesc(usuario.getId())
                .map(pago -> calcularSiguienteFechaPago(pago.getFechaPago(), plan))
                .orElseGet(() -> calcularSiguienteFechaPago(fechaReferencia, plan));
    }

    private Pago crearPagoAutomatico(Usuario usuario, Plan plan, LocalDate fechaPago) {
        Pago pago = new Pago();
        pago.setUsuario(usuario);
        pago.setPlan(plan);
        pago.setFechaPago(fechaPago);
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setEstado(EstadoPago.PENDIENTE);
        return pago;
    }

    private LocalDate calcularSiguienteFechaPago(LocalDate fechaBase, Plan plan) {
        return fechaBase.plusDays(Math.max(plan.getDuracionDias(), 1));
    }
}
