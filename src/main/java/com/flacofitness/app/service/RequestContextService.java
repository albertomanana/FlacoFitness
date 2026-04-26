package com.flacofitness.app.service;

import com.flacofitness.app.security.AccessProfile;
import com.flacofitness.app.security.AccessSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class RequestContextService {

    private final AccessSessionService accessSessionService;

    public RequestContextService(AccessSessionService accessSessionService) {
        this.accessSessionService = accessSessionService;
    }

    public AccessProfile resolveProfile(HttpSession session) {
        return accessSessionService.getCurrentProfile(session);
    }

    public AccessProfile resolveProfile(HttpServletRequest request) {
        return resolveProfile(request != null ? request.getSession(false) : null);
    }
}
