package com.flacofitness.app.controller;

import com.flacofitness.app.model.dto.ShellNotificationItem;
import com.flacofitness.app.service.ShellNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/notificaciones")
public class ShellNotificationController {

    private final ShellNotificationService shellNotificationService;

    public ShellNotificationController(ShellNotificationService shellNotificationService) {
        this.shellNotificationService = shellNotificationService;
    }

    @PostMapping("/marcar-leidas")
    public String marcarLeidas(HttpSession session, HttpServletRequest request) {
        if (session != null) {
            List<ShellNotificationItem> notifications = shellNotificationService.buildNotifications();
            String signature = shellNotificationService.buildSignature(notifications);
            session.setAttribute(ShellViewAdvice.ATTR_DISMISSED_SIGNATURE, signature);
        }

        return "redirect:" + resolveReturnPath(request);
    }

    private String resolveReturnPath(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/";
        }

        try {
            URI uri = URI.create(referer);
            String contextPath = request.getContextPath();
            String path = uri.getPath();

            if (path == null || path.isBlank()) {
                return "/";
            }

            if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            if (path.isBlank() || !path.startsWith("/")) {
                return "/";
            }

            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                return path + "?" + uri.getQuery();
            }

            return path;
        } catch (Exception ex) {
            return "/";
        }
    }
}