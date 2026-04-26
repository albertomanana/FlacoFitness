package com.flacofitness.app.security;

import com.flacofitness.app.config.AccessSettings;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class AccessSessionService {

    public static final String ATTR_GRANTED = "ff.access.granted";
    public static final String ATTR_FAILED_ATTEMPTS = "ff.access.failedAttempts";
    public static final String ATTR_LOCKED_UNTIL = "ff.access.lockedUntil";
    public static final String ATTR_TARGET_URI = "ff.access.targetUri";
    public static final String ATTR_PROFILE = "ff.access.profile";
    public static final String ATTR_USER_ID = "ff.access.userId";

    private static final DateTimeFormatter LOCK_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

    private final AccessSettings accessSettings;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessProfileResolver accessProfileResolver;

    public AccessSessionService(AccessSettings accessSettings,
                                UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                AccessProfileResolver accessProfileResolver) {
        this.accessSettings = accessSettings;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessProfileResolver = accessProfileResolver;
    }

    public boolean isGranted(HttpSession session) {
        return session != null
                && Boolean.TRUE.equals(session.getAttribute(ATTR_GRANTED))
                && getCurrentUserId(session) != null;
    }

    public boolean isLocked(HttpSession session) {
        if (session == null) {
            return false;
        }

        Instant lockedUntil = getLockedUntil(session);
        if (lockedUntil == null) {
            return false;
        }

        if (lockedUntil.isAfter(Instant.now())) {
            return true;
        }

        session.removeAttribute(ATTR_LOCKED_UNTIL);
        session.setAttribute(ATTR_FAILED_ATTEMPTS, 0);
        return false;
    }

    public int getRemainingAttempts(HttpSession session) {
        if (isLocked(session)) {
            return 0;
        }
        int failedAttempts = getFailedAttempts(session);
        return Math.max(0, accessSettings.getMaxAttempts() - failedAttempts);
    }

    public Instant getLockedUntil(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTR_LOCKED_UNTIL);
        return value instanceof Instant instant ? instant : null;
    }

    public String resolveTarget(HttpSession session) {
        if (session == null) {
            return "/";
        }
        Object value = session.getAttribute(ATTR_TARGET_URI);
        if (value instanceof String target && !target.isBlank()) {
            return target;
        }
        return "/";
    }

    public void rememberTarget(HttpSession session, HttpServletRequest request) {
        if (session == null || request == null) {
            return;
        }

        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String target = requestUri.startsWith(contextPath)
                ? requestUri.substring(contextPath.length())
                : requestUri;

        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            target = target + "?" + request.getQueryString();
        }

        if (target.isBlank() || "/acceso".equals(target) || "/salir".equals(target)) {
            return;
        }

        session.setAttribute(ATTR_TARGET_URI, target);
    }

    public AccessProfile getCurrentProfile(HttpSession session) {
        if (!isGranted(session)) {
            return AccessProfile.ADMIN;
        }

        Object value = session.getAttribute(ATTR_PROFILE);
        return value instanceof String rawProfile ? AccessProfile.from(rawProfile) : AccessProfile.ADMIN;
    }

    public Optional<Usuario> getCurrentUser(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Optional.empty();
        }
        return usuarioRepository.findById(userId);
    }

    public Long getCurrentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTR_USER_ID);
        return value instanceof Long userId ? userId : null;
    }

    public boolean canAccess(HttpSession session, String path, String method) {
        return getCurrentProfile(session).canAccess(path, method);
    }

    public AccessAttemptResult authenticate(HttpSession session, String rawLogin, String rawPassword) {
        if (session == null) {
            return new AccessAttemptResult(false, false, 0, null, "No se pudo abrir la sesion de acceso.");
        }

        if (isLocked(session)) {
            return new AccessAttemptResult(false, true, 0, getLockedUntil(session), buildLockMessage(session));
        }

        String login = rawLogin == null ? "" : rawLogin.trim();
        String password = rawPassword == null ? "" : rawPassword.trim();
        Optional<Usuario> usuarioOpt = findByLogin(login);

        if (usuarioOpt.isPresent() && credentialsAreValid(usuarioOpt.get(), password)) {
            grantAccess(session, usuarioOpt.get());
            return new AccessAttemptResult(true, false, accessSettings.getMaxAttempts(), null, "Acceso concedido.");
        }

        int failedAttempts = getFailedAttempts(session) + 1;
        session.setAttribute(ATTR_FAILED_ATTEMPTS, failedAttempts);

        if (failedAttempts >= accessSettings.getMaxAttempts()) {
            Instant lockedUntil = Instant.now().plus(accessSettings.getLockDuration());
            session.setAttribute(ATTR_LOCKED_UNTIL, lockedUntil);
            return new AccessAttemptResult(false, true, 0, lockedUntil, buildLockMessage(lockedUntil));
        }

        int remainingAttempts = Math.max(0, accessSettings.getMaxAttempts() - failedAttempts);
        return new AccessAttemptResult(false, false, remainingAttempts, null,
                "Credenciales incorrectas. Quedan " + remainingAttempts + " intento(s).");
    }

    public void refreshSession(HttpSession session, Usuario usuario) {
        if (session == null || usuario == null || usuario.getId() == null) {
            return;
        }
        session.setAttribute(ATTR_GRANTED, Boolean.TRUE);
        session.setAttribute(ATTR_PROFILE, accessProfileResolver.resolveFor(usuario).name());
        session.setAttribute(ATTR_USER_ID, usuario.getId());
    }

    public void grantAccess(HttpSession session, AccessProfile profile) {
        if (session == null) {
            return;
        }
        session.setAttribute(ATTR_GRANTED, Boolean.TRUE);
        session.setAttribute(ATTR_PROFILE, (profile == null ? AccessProfile.ADMIN : profile).name());
        session.setAttribute(ATTR_USER_ID, -1L);
        session.setAttribute(ATTR_FAILED_ATTEMPTS, 0);
        session.removeAttribute(ATTR_LOCKED_UNTIL);
    }

    public void clear(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    public void clearTarget(HttpSession session) {
        if (session != null) {
            session.removeAttribute(ATTR_TARGET_URI);
        }
    }

    private void grantAccess(HttpSession session, Usuario usuario) {
        session.setAttribute(ATTR_GRANTED, Boolean.TRUE);
        session.setAttribute(ATTR_PROFILE, accessProfileResolver.resolveFor(usuario).name());
        session.setAttribute(ATTR_USER_ID, usuario.getId());
        session.setAttribute(ATTR_FAILED_ATTEMPTS, 0);
        session.removeAttribute(ATTR_LOCKED_UNTIL);
    }

    private boolean credentialsAreValid(Usuario usuario, String rawPassword) {
        return Boolean.TRUE.equals(usuario.getActivo())
                && usuario.getPasswordHash() != null
                && !usuario.getPasswordHash().isBlank()
                && rawPassword != null
                && !rawPassword.isBlank()
                && passwordEncoder.matches(rawPassword, usuario.getPasswordHash());
    }

    private Optional<Usuario> findByLogin(String login) {
        if (login.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByEmailIgnoreCase(login)
                .or(() -> usuarioRepository.findByUsernameIgnoreCase(login));
    }

    private int getFailedAttempts(HttpSession session) {
        if (session == null) {
            return 0;
        }
        Object value = session.getAttribute(ATTR_FAILED_ATTEMPTS);
        return value instanceof Integer count ? Math.max(0, count) : 0;
    }

    private String buildLockMessage(HttpSession session) {
        return buildLockMessage(getLockedUntil(session));
    }

    private String buildLockMessage(Instant lockedUntil) {
        if (lockedUntil == null) {
            return "El acceso quedo bloqueado temporalmente.";
        }

        return "Demasiados intentos. El acceso quedo bloqueado hasta "
                + LOCK_TIME_FORMATTER.format(lockedUntil) + ".";
    }
}
