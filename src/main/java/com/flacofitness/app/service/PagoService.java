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

import java.math.BigDecimal;
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
        validarMonto(pago.getMonto());
        pago.setUsuario(obtenerUsuarioValido(pago.getUsuario()));
        pago.setPlan(obtenerPlanValidoSiExiste(pago.getPlan()));

        return pagoRepository.save(pago);
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("El monto del pago debe ser mayor que cero");
        }
    }

    private Usuario obtenerUsuarioValido(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new BusinessValidationException("El pago debe estar asociado a un usuario válido");
        }

        return usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuario.getId()));
    }

    private Plan obtenerPlanValidoSiExiste(Plan plan) {
        if (plan == null) {
            return null;
        }

        if (plan.getId() == null) {
            throw new BusinessValidationException("El plan asociado al pago debe ser válido");
        }

        return planRepository.findById(plan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con id: " + plan.getId()));
    }
}
