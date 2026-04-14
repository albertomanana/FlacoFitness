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
            return true;
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