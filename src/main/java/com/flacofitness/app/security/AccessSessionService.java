package com.flacofitness.app.security;

import com.flacofitness.app.config.AccessSettings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class AccessSessionService {

    public static final String ATTR_GRANTED = "ff.access.granted";
    public static final String ATTR_FAILED_ATTEMPTS = "ff.access.failedAttempts";
    public static final String ATTR_LOCKED_UNTIL = "ff.access.lockedUntil";
    public static final String ATTR_TARGET_URI = "ff.access.targetUri";
    public static final String ATTR_PROFILE = "ff.access.profile";

    private final AccessSettings accessSettings;
    private static final DateTimeFormatter LOCK_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

    public AccessSessionService(AccessSettings accessSettings) {
        this.accessSettings = accessSettings;
    }

    public boolean isGranted(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_GRANTED));
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
        if (value instanceof Instant instant) {
            return instant;
        }

        return null;
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

    public boolean canAccess(HttpSession session, String path, String method) {
        return getCurrentProfile(session).canAccess(path, method);
    }

    public AccessAttemptResult verifyPin(HttpSession session, String rawPin, AccessProfile profile) {
        if (session == null) {
            return new AccessAttemptResult(false, false, 0, null, "No se pudo abrir la sesion de acceso.");
        }

        if (isLocked(session)) {
            return new AccessAttemptResult(false, true, 0, getLockedUntil(session), buildLockMessage(session));
        }

        String normalizedPin = rawPin == null ? "" : rawPin.trim();
        if (accessSettings.getPin().equals(normalizedPin)) {
            grantAccess(session, profile);
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
                "PIN incorrecto. Te quedan " + remainingAttempts + " intento(s).");
    }

    public void grantAccess(HttpSession session, AccessProfile profile) {
        if (session == null) {
            return;
        }

        session.setAttribute(ATTR_GRANTED, Boolean.TRUE);
        session.setAttribute(ATTR_PROFILE, (profile == null ? AccessProfile.ADMIN : profile).name());
        session.setAttribute(ATTR_FAILED_ATTEMPTS, 0);
        session.removeAttribute(ATTR_LOCKED_UNTIL);
    }

    public void clear(HttpSession session) {
        if (session == null) {
            return;
        }

        session.invalidate();
    }

    public void clearTarget(HttpSession session) {
        if (session != null) {
            session.removeAttribute(ATTR_TARGET_URI);
        }
    }

    private int getFailedAttempts(HttpSession session) {
        if (session == null) {
            return 0;
        }

        Object value = session.getAttribute(ATTR_FAILED_ATTEMPTS);
        if (value instanceof Integer count) {
            return Math.max(0, count);
        }

        return 0;
    }

    private String buildLockMessage(HttpSession session) {
        Instant lockedUntil = getLockedUntil(session);
        return buildLockMessage(lockedUntil);
    }

    private String buildLockMessage(Instant lockedUntil) {
        if (lockedUntil == null) {
            return "El acceso quedo bloqueado temporalmente.";
        }

        return "Demasiados intentos. El acceso quedo bloqueado hasta "
                + LOCK_TIME_FORMATTER.format(lockedUntil) + ".";
    }
}
