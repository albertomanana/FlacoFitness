package com.flacofitness.app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class ControllerActivityLogger {

    private final ActivityLogService activityLogService;
    private final RequestContextService requestContextService;

    public ControllerActivityLogger(ActivityLogService activityLogService,
                                    RequestContextService requestContextService) {
        this.activityLogService = activityLogService;
        this.requestContextService = requestContextService;
    }

    public void log(HttpServletRequest request,
                    HttpSession session,
                    String moduleKey,
                    String actionKey,
                    String entityType,
                    Long entityId,
                    String title,
                    String description) {
        String browserToken = request != null
                ? (String) request.getAttribute(BrowserTokenService.REQUEST_ATTR)
                : null;
        String route = request != null ? request.getRequestURI() : null;
        activityLogService.log(
                moduleKey,
                actionKey,
                entityType,
                entityId,
                title,
                description,
                requestContextService.resolveProfile(session),
                browserToken,
                route);
    }
}
