package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.DashboardGuideStepView;
import com.flacofitness.app.model.dto.DashboardGuideView;
import com.flacofitness.app.model.dto.UxModuleStateView;
import com.flacofitness.app.model.entity.UxMemoryState;
import com.flacofitness.app.model.enums.EstadoMembresia;
import com.flacofitness.app.repository.MembresiaUsuarioRepository;
import com.flacofitness.app.repository.PagoRepository;
import com.flacofitness.app.repository.UxMemoryStateRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import com.flacofitness.app.security.AccessProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UxMemoryStateService {

    private final UxMemoryStateRepository uxMemoryStateRepository;
    private final UsuarioRepository usuarioRepository;
    private final MembresiaUsuarioRepository membresiaUsuarioRepository;
    private final PagoRepository pagoRepository;

    public UxMemoryStateService(UxMemoryStateRepository uxMemoryStateRepository,
                                UsuarioRepository usuarioRepository,
                                MembresiaUsuarioRepository membresiaUsuarioRepository,
                                PagoRepository pagoRepository) {
        this.uxMemoryStateRepository = uxMemoryStateRepository;
        this.usuarioRepository = usuarioRepository;
        this.membresiaUsuarioRepository = membresiaUsuarioRepository;
        this.pagoRepository = pagoRepository;
    }

    public UxModuleStateView resolveModuleState(String browserToken, AccessProfile profile, String moduleKey) {
        var state = uxMemoryStateRepository
                .findByBrowserTokenAndAccessProfileAndModuleKey(browserToken, profile.name(), moduleKey);
        if (state == null || state.isEmpty()) {
            return new UxModuleStateView(false, false, null);
        }
        return new UxModuleStateView(Boolean.TRUE.equals(state.get().getTooltipSeen()),
                Boolean.TRUE.equals(state.get().getEmptyStateDismissed()),
                state.get().getGuideStepState());
    }

    public DashboardGuideView buildDashboardGuide(String browserToken, AccessProfile profile) {
        UxModuleStateView state = resolveModuleState(browserToken, profile, "dashboard");
        List<DashboardGuideStepView> steps = List.of(
                new DashboardGuideStepView(
                        "crear-cliente",
                        "Crear cliente",
                        "Empieza dando de alta al primer miembro para activar el flujo comercial.",
                        "/usuarios/nuevo",
                        usuarioRepository.countByActivoTrue() > 0
                ),
                new DashboardGuideStepView(
                        "asignar-membresia",
                        "Asignar membresia",
                        "Conecta un plan real con el cliente para que pagos y renovaciones tengan sentido.",
                        "/membresias",
                        membresiaUsuarioRepository.countByEstadoIn(Set.of(EstadoMembresia.ACTIVA, EstadoMembresia.PENDIENTE)) > 0
                ),
                new DashboardGuideStepView(
                        "registrar-pago",
                        "Registrar pago",
                        "Cierra el onboarding operativo dejando el primer cobro registrado.",
                        "/pagos/nuevo",
                        pagoRepository.count() > 0
                )
        );
        boolean completed = steps.stream().allMatch(DashboardGuideStepView::completed);
        return new DashboardGuideView(completed, state.emptyStateDismissed(), steps);
    }

    @Transactional
    public void markTooltipSeen(String browserToken, AccessProfile profile, String moduleKey) {
        UxMemoryState state = getOrCreate(browserToken, profile, moduleKey);
        state.setTooltipSeen(true);
        uxMemoryStateRepository.save(state);
    }

    @Transactional
    public void markEmptyStateDismissed(String browserToken, AccessProfile profile, String moduleKey) {
        UxMemoryState state = getOrCreate(browserToken, profile, moduleKey);
        state.setEmptyStateDismissed(true);
        uxMemoryStateRepository.save(state);
    }

    @Transactional
    public void updateGuideStep(String browserToken, AccessProfile profile, String moduleKey, String guideStepState) {
        UxMemoryState state = getOrCreate(browserToken, profile, moduleKey);
        state.setGuideStepState(guideStepState);
        uxMemoryStateRepository.save(state);
    }

    private UxMemoryState getOrCreate(String browserToken, AccessProfile profile, String moduleKey) {
        var existing = uxMemoryStateRepository
                .findByBrowserTokenAndAccessProfileAndModuleKey(browserToken, profile.name(), moduleKey);
        if (existing != null && existing.isPresent()) {
            return existing.get();
        }
        UxMemoryState state = new UxMemoryState();
        state.setBrowserToken(browserToken);
        state.setAccessProfile(profile.name());
        state.setModuleKey(moduleKey);
        return state;
    }
}
