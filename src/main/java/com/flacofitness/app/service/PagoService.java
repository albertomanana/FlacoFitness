package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.PagoRepository;
import com.flacofitness.app.repository.PlanRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PagoService {

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
}
