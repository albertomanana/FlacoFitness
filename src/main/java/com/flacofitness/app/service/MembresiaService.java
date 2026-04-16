package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.MembresiaUsuario;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoMembresia;
import com.flacofitness.app.model.enums.TipoMembresia;
import com.flacofitness.app.repository.MembresiaUsuarioRepository;
import com.flacofitness.app.repository.PlanRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MembresiaService {

    private static final Collection<EstadoMembresia> ESTADOS_OPERATIVOS = List.of(
            EstadoMembresia.ACTIVA,
            EstadoMembresia.PENDIENTE,
            EstadoMembresia.PRUEBA
    );

    private final PlanRepository planRepository;
    private final MembresiaUsuarioRepository membresiaUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public MembresiaService(PlanRepository planRepository,
                            MembresiaUsuarioRepository membresiaUsuarioRepository,
                            UsuarioRepository usuarioRepository) {
        this.planRepository = planRepository;
        this.membresiaUsuarioRepository = membresiaUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Plan> listarCatalogo() {
        return planRepository.findAll();
    }

    public List<Plan> listarPlanesActivos() {
        return planRepository.findByActivoTrue();
    }

    public Plan buscarPlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membresia no encontrada con id: " + id));
    }

    public long contarCatalogoActivo() {
        return planRepository.countByActivoTrue();
    }

    @Transactional
    public Plan guardarPlan(Plan plan) {
        normalizarPlan(plan);
        return planRepository.save(plan);
    }

    @Transactional
    public Plan actualizarPlan(Long id, Plan planActualizado) {
        Plan plan = buscarPlan(id);
        plan.setNombre(planActualizado.getNombre());
        plan.setDescripcion(planActualizado.getDescripcion());
        plan.setTipoMembresia(planActualizado.getTipoMembresia());
        plan.setBeneficios(planActualizado.getBeneficios());
        plan.setPrecioMensual(planActualizado.getPrecioMensual());
        plan.setDuracionDias(planActualizado.getDuracionDias());
        plan.setActivo(planActualizado.getActivo());
        normalizarPlan(plan);
        return planRepository.save(plan);
    }

    @Transactional
    public void desactivarPlan(Long id) {
        Plan plan = buscarPlan(id);
        plan.setActivo(false);
        planRepository.save(plan);
    }

    @Transactional
    public void activarPlan(Long id) {
        Plan plan = buscarPlan(id);
        plan.setActivo(true);
        planRepository.save(plan);
    }

    public List<MembresiaUsuario> listarContratos() {
        return membresiaUsuarioRepository.findAll();
    }

    public List<MembresiaUsuario> listarContratosPorUsuario(Long usuarioId) {
        return membresiaUsuarioRepository.findByUsuarioIdOrderByFechaInicioDescIdDesc(usuarioId);
    }

    public List<MembresiaUsuario> listarRecientes() {
        return membresiaUsuarioRepository.findTop8ByOrderByFechaCreacionDescIdDesc();
    }

    public MembresiaUsuario buscarContrato(Long id) {
        return membresiaUsuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato de membresia no encontrado con id: " + id));
    }

    public Optional<MembresiaUsuario> buscarContratoActivoPorUsuario(Long usuarioId) {
        return membresiaUsuarioRepository.findTopByUsuarioIdAndEstadoInOrderByFechaInicioDescIdDesc(usuarioId, ESTADOS_OPERATIVOS);
    }

    public long contarActivas() {
        return membresiaUsuarioRepository.countByEstado(EstadoMembresia.ACTIVA);
    }

    public long contarVencidas() {
        return membresiaUsuarioRepository.countByEstado(EstadoMembresia.VENCIDA)
                + membresiaUsuarioRepository.countByFechaFinBeforeAndEstadoIn(LocalDate.now(), ESTADOS_OPERATIVOS);
    }

    @Transactional
    public MembresiaUsuario asignarMembresia(Long usuarioId, Long planId, LocalDate fechaInicio, EstadoMembresia estado, String origen) {
        MembresiaUsuario contrato = new MembresiaUsuario();
        contrato.setUsuario(obtenerUsuario(usuarioId));
        contrato.setPlan(obtenerPlanActivo(planId));
        contrato.setFechaInicio(fechaInicio != null ? fechaInicio : LocalDate.now());
        contrato.setEstado(estado != null ? estado : EstadoMembresia.ACTIVA);
        contrato.setOrigen(origen);
        return guardarContrato(contrato);
    }

    @Transactional
    public MembresiaUsuario guardarContrato(MembresiaUsuario contrato) {
        Usuario usuario = obtenerUsuarioValido(contrato.getUsuario());
        Plan plan = obtenerPlanActivo(contrato.getPlan());

        contrato.setUsuario(usuario);
        contrato.setPlan(plan);
        normalizarContrato(contrato);

        if (contrato.getEstado() == EstadoMembresia.ACTIVA) {
            sincronizarUsuarioConContrato(usuario, contrato);
        }

        return membresiaUsuarioRepository.save(contrato);
    }

    @Transactional
    public MembresiaUsuario actualizarContrato(Long id, MembresiaUsuario contratoActualizado) {
        MembresiaUsuario contrato = buscarContrato(id);
        Usuario usuario = obtenerUsuarioValido(contratoActualizado.getUsuario());
        Plan plan = obtenerPlanActivo(contratoActualizado.getPlan());

        contrato.setUsuario(usuario);
        contrato.setPlan(plan);
        contrato.setFechaInicio(contratoActualizado.getFechaInicio());
        contrato.setFechaFin(contratoActualizado.getFechaFin());
        contrato.setEstado(contratoActualizado.getEstado());
        contrato.setPrecioSnapshot(contratoActualizado.getPrecioSnapshot());
        contrato.setOrigen(contratoActualizado.getOrigen());
        contrato.setObservaciones(contratoActualizado.getObservaciones());
        normalizarContrato(contrato);

        if (contrato.getEstado() == EstadoMembresia.ACTIVA) {
            sincronizarUsuarioConContrato(usuario, contrato);
        }

        return membresiaUsuarioRepository.save(contrato);
    }

    @Transactional
    public void cancelarContrato(Long id) {
        MembresiaUsuario contrato = buscarContrato(id);
        contrato.setEstado(EstadoMembresia.CANCELADA);
        membresiaUsuarioRepository.save(contrato);
    }

    private void normalizarPlan(Plan plan) {
        if (plan.getActivo() == null) {
            plan.setActivo(true);
        }
        if (plan.getDuracionDias() == null || plan.getDuracionDias() <= 0) {
            plan.setDuracionDias(30);
        }
        if (plan.getPrecioMensual() == null) {
            plan.setPrecioMensual(BigDecimal.ZERO);
        }
        if (plan.getTipoMembresia() == null) {
            plan.setTipoMembresia(TipoMembresia.MENSUAL);
        }
    }

    private void normalizarContrato(MembresiaUsuario contrato) {
        if (contrato.getFechaInicio() == null) {
            contrato.setFechaInicio(LocalDate.now());
        }
        if (contrato.getEstado() == null) {
            contrato.setEstado(EstadoMembresia.ACTIVA);
        }
        if (contrato.getPrecioSnapshot() == null && contrato.getPlan() != null) {
            contrato.setPrecioSnapshot(contrato.getPlan().getPrecioMensual());
        }
        if (contrato.getFechaFin() == null && contrato.getPlan() != null) {
            contrato.setFechaFin(contrato.getFechaInicio().plusDays(Math.max(contrato.getPlan().getDuracionDias(), 1)));
        }
    }

    private void sincronizarUsuarioConContrato(Usuario usuario, MembresiaUsuario contrato) {
        usuario.setPlan(contrato.getPlan());
        usuario.setFechaProximoPago(contrato.getFechaFin());
        usuarioRepository.save(usuario);
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessValidationException("El contrato debe estar asociado a un usuario valido");
        }
        return obtenerUsuario(usuario.getId());
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
    }

    private Plan obtenerPlanActivo(Plan plan) {
        if (plan == null || plan.getId() == null) {
            throw new BusinessValidationException("Debes seleccionar una membresia del catalogo");
        }
        return obtenerPlanActivo(plan.getId());
    }

    private Plan obtenerPlanActivo(Long planId) {
        Plan plan = buscarPlan(planId);
        if (!Boolean.TRUE.equals(plan.getActivo())) {
            throw new BusinessValidationException("La membresia seleccionada debe estar activa");
        }
        return plan;
    }
}
