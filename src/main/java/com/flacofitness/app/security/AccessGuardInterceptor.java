package com.flacofitness.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessGuardInterceptor implements HandlerInterceptor {

    private final AccessSessionService accessSessionService;

    public AccessGuardInterceptor(AccessSessionService accessSessionService) {
        this.accessSessionService = accessSessionService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (isPublicRequest(request)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (accessSessionService.isGranted(session)) {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            AccessProfile profile = accessSessionService.getCurrentProfile(session);
            boolean mustChangePassword = accessSessionService.getCurrentUser(session)
                    .map(usuario -> Boolean.TRUE.equals(usuario.getMustChangePassword()))
                    .orElse(false);
            if (mustChangePassword && !path.startsWith("/cuenta/password") && !path.equals("/salir")) {
                response.sendRedirect(request.getContextPath() + "/cuenta/password");
                return false;
            }
            // Redireccionar a dashboard específico por rol si se accede a /
            if ("/".equals(path) && profile != AccessProfile.ADMIN) {
                response.sendRedirect(request.getContextPath() + profile.dashboardEntryPoint());
                return false;
            }

            if (accessSessionService.canAccess(session, path, request.getMethod())) {
                return true;
            }

            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        HttpSession writableSession = session != null ? session : request.getSession(true);
        accessSessionService.rememberTarget(writableSession, request);
        response.sendRedirect(request.getContextPath() + "/acceso");
        return false;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/acceso")
                || path.equals("/salir")
                || path.equals("/error")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.startsWith("/vendor/")
                || path.startsWith("/uploads/")
                || path.equals("/favicon.ico");
    }
}

