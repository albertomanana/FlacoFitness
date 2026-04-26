package com.flacofitness.app.service;

import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.dto.PlanDistribucionStatsItem;
import com.flacofitness.app.model.dto.UsuarioAltaMensualStatsItem;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoTrial;
import com.flacofitness.app.repository.TrialRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TrialRepository trialRepository;
    private final OperationalClockService operationalClockService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          TrialRepository trialRepository,
                          OperationalClockService operationalClockService,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.trialRepository = trialRepository;
        this.operationalClockService = operationalClockService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public long contarTotal() {
        return usuarioRepository.count();
    }

    public long contarActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    public List<Usuario> listarRecientes() {
        return usuarioRepository.findTop8ByOrderByFechaRegistroDescIdDesc();
    }

    public List<Usuario> listarRenovacionesProximas() {
        return usuarioRepository.findRenovacionesProximas(operationalClockService.today()).stream()
                .limit(6)
                .toList();
    }

    public List<PlanDistribucionStatsItem> obtenerDistribucionPorPlan() {
        return usuarioRepository.countGroupedByPlan().stream()
                .map(item -> new PlanDistribucionStatsItem(
                        item.getPlanNombre() == null || item.getPlanNombre().isBlank() ? "Sin plan" : item.getPlanNombre(),
                        item.getTotal() == null ? 0L : item.getTotal()))
                .toList();
    }

    public List<UsuarioAltaMensualStatsItem> obtenerAltasMensuales() {
        return usuarioRepository.countAltasGroupedByMes().stream()
                .map(item -> new UsuarioAltaMensualStatsItem(
                        YearMonth.of(item.getAnio(), item.getMes()).toString(),
                        item.getTotal() == null ? 0L : item.getTotal()))
                .toList();
    }

    public long contarRenovacionesProximas(int dias) {
        LocalDate fechaDesde = operationalClockService.today();
        LocalDate fechaHasta = fechaDesde.plusDays(Math.max(dias, 1));
        return usuarioRepository.countByActivoTrueAndFechaProximoPagoBetween(fechaDesde, fechaHasta);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        validarEmailDuplicado(usuario.getEmail(), null);
        validarUsernameDuplicado(usuario.getUsername(), usuario.getEmail(), null);
        prepararCredencialesAlta(usuario);
        inicializarFechaProximoPago(usuario);
        Usuario guardado = usuarioRepository.save(usuario);
        cerrarTrialsPendientesPorEmail(guardado);
        return guardado;
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        Usuario usuarioExistente = buscarPorId(id);
        validarEmailDuplicado(usuarioActualizado.getEmail(), id);
        validarUsernameDuplicado(usuarioActualizado.getUsername(), usuarioActualizado.getEmail(), id);
        Long planAnteriorId = usuarioExistente.getPlan() != null ? usuarioExistente.getPlan().getId() : null;

        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setApellidos(usuarioActualizado.getApellidos());
        usuarioExistente.setDni(usuarioActualizado.getDni());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setUsername(normalizarUsername(usuarioActualizado.getUsername(), usuarioActualizado.getEmail()));
        usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
        usuarioExistente.setFechaNacimiento(usuarioActualizado.getFechaNacimiento());
        usuarioExistente.setDireccion(usuarioActualizado.getDireccion());
        if (usuarioActualizado.getFotoPath() != null) {
            usuarioExistente.setFotoPath(usuarioActualizado.getFotoPath());
        }
        usuarioExistente.setActivo(usuarioActualizado.getActivo());
        usuarioExistente.setRol(usuarioActualizado.getRol());
        usuarioExistente.setPlan(usuarioActualizado.getPlan());
        actualizarPasswordSiCorresponde(usuarioExistente, usuarioActualizado);
        sincronizarFechaProximoPago(usuarioExistente, planAnteriorId);

        return usuarioRepository.save(usuarioExistente);
    }

    @Transactional
    public Usuario actualizarFotoPath(Long id, String fotoPath) {
        Usuario usuario = buscarPorId(id);
        usuario.setFotoPath(fotoPath);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(false);
        usuario.setFechaProximoPago(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void activar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(true);
        sincronizarFechaProximoPago(usuario, usuario.getPlan() != null ? usuario.getPlan().getId() : null);
        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public void cambiarPassword(Long usuarioId, String currentPassword, String newPassword, String confirmation) {
        Usuario usuario = buscarPorId(usuarioId);
        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
            throw new BusinessValidationException("La cuenta todavia no tiene una contrasena activa.");
        }
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, usuario.getPasswordHash())) {
            throw new BusinessValidationException("La contrasena actual no es correcta.");
        }
        aplicarNuevoPassword(usuario, newPassword, confirmation);
        usuario.setMustChangePassword(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public String resetPasswordTemporal(Long usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        String temporal = "FF-" + UUID.randomUUID().toString().substring(0, 8);
        usuario.setPasswordHash(passwordEncoder.encode(temporal));
        usuario.setMustChangePassword(true);
        usuarioRepository.save(usuario);
        return temporal;
    }

    @Transactional
    public int bootstrapCredencialesFaltantes(String bootstrapPassword) {
        int updated = 0;
        for (Usuario usuario : usuarioRepository.findAll()) {
            boolean dirty = false;
            String normalizedUsername = normalizarUsername(usuario.getUsername(), usuario.getEmail());
            if (!Objects.equals(normalizedUsername, usuario.getUsername())) {
                usuario.setUsername(ensureUniqueUsername(normalizedUsername, usuario.getId()));
                dirty = true;
            }
            if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
                usuario.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
                usuario.setMustChangePassword(true);
                dirty = true;
            }
            if (usuario.getMustChangePassword() == null) {
                usuario.setMustChangePassword(Boolean.FALSE);
                dirty = true;
            }
            if (dirty) {
                usuarioRepository.save(usuario);
                updated++;
            }
        }
        return updated;
    }

    private void validarEmailDuplicado(String email, Long usuarioIdActual) {
        usuarioRepository.findByEmail(email)
                .filter(usuario -> !usuario.getId().equals(usuarioIdActual))
                .ifPresent(usuario -> {
                    throw new DuplicateResourceException("Ya existe un usuario con email: " + email);
                });
    }

    private void validarUsernameDuplicado(String username, String fallbackEmail, Long usuarioIdActual) {
        String normalized = normalizarUsername(username, fallbackEmail);
        if (normalized == null) {
            throw new BusinessValidationException("El username es obligatorio para la cuenta.");
        }
        usuarioRepository.findByUsernameIgnoreCase(normalized)
                .filter(usuario -> !usuario.getId().equals(usuarioIdActual))
                .ifPresent(usuario -> {
                    throw new DuplicateResourceException("Ya existe una cuenta con username: " + normalized);
                });
    }

    private void inicializarFechaProximoPago(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getPlan() == null) {
            usuario.setFechaProximoPago(null);
            return;
        }

        if (usuario.getFechaProximoPago() == null) {
            usuario.setFechaProximoPago(operationalClockService.today().plusDays(obtenerFrecuenciaCobro(usuario.getPlan())));
        }
    }

    private void sincronizarFechaProximoPago(Usuario usuario, Long planAnteriorId) {
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getPlan() == null) {
            usuario.setFechaProximoPago(null);
            return;
        }

        Long planActualId = usuario.getPlan().getId();
        boolean planCambio = !Objects.equals(planAnteriorId, planActualId);

        if (planCambio || usuario.getFechaProximoPago() == null) {
            usuario.setFechaProximoPago(operationalClockService.today().plusDays(obtenerFrecuenciaCobro(usuario.getPlan())));
        }
    }

    private long obtenerFrecuenciaCobro(Plan plan) {
        return Math.max(plan.getDuracionDias(), 1);
    }

    private void prepararCredencialesAlta(Usuario usuario) {
        usuario.setUsername(ensureUniqueUsername(normalizarUsername(usuario.getUsername(), usuario.getEmail()), null));
        aplicarNuevoPassword(usuario, usuario.getRawPassword(), usuario.getConfirmPassword());
        usuario.setMustChangePassword(Boolean.FALSE);
    }

    private void actualizarPasswordSiCorresponde(Usuario usuarioExistente, Usuario usuarioActualizado) {
        String rawPassword = usuarioActualizado.getRawPassword();
        String confirmation = usuarioActualizado.getConfirmPassword();
        if ((rawPassword == null || rawPassword.isBlank()) && (confirmation == null || confirmation.isBlank())) {
            return;
        }
        aplicarNuevoPassword(usuarioExistente, rawPassword, confirmation);
        usuarioExistente.setMustChangePassword(false);
    }

    private void aplicarNuevoPassword(Usuario usuario, String rawPassword, String confirmation) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessValidationException("Debes indicar una contrasena para la cuenta.");
        }
        if (!rawPassword.equals(confirmation)) {
            throw new BusinessValidationException("La confirmacion de contrasena no coincide.");
        }
        if (rawPassword.length() < 8) {
            throw new BusinessValidationException("La contrasena debe tener al menos 8 caracteres.");
        }
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
    }

    private String ensureUniqueUsername(String normalized, Long currentUserId) {
        String base = normalized == null || normalized.isBlank() ? "usuario" : normalized;
        String candidate = base;
        int attempt = 1;
        while (usuarioRepository.findByUsernameIgnoreCase(candidate)
                .filter(usuario -> !usuario.getId().equals(currentUserId))
                .isPresent()) {
            candidate = base + attempt;
            attempt++;
        }
        return candidate;
    }

    private String normalizarUsername(String username, String fallbackEmail) {
        String source = username;
        if ((source == null || source.isBlank()) && fallbackEmail != null && !fallbackEmail.isBlank()) {
            int atIndex = fallbackEmail.indexOf('@');
            source = atIndex > 0 ? fallbackEmail.substring(0, atIndex) : fallbackEmail;
        }
        if (source == null || source.isBlank()) {
            return null;
        }

        String normalized = Normalizer.normalize(source.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9._-]", "")
                .toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private void cerrarTrialsPendientesPorEmail(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return;
        }
        List<Trial> pendientes = trialRepository.findByEmailAndEstadoNot(usuario.getEmail(), EstadoTrial.CONVERTIDO);
        for (Trial trial : pendientes) {
            trial.setEstado(EstadoTrial.CONVERTIDO);
            trial.setUsuarioConvertido(usuario);
            trialRepository.save(trial);
        }
    }
}
