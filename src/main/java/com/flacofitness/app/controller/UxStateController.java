package com.flacofitness.app.controller;

import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.BrowserTokenService;
import com.flacofitness.app.service.UxMemoryStateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ux")
public class UxStateController {

    private final UxMemoryStateService uxMemoryStateService;
    private final AccessSessionService accessSessionService;
    private final BrowserTokenService browserTokenService;

    public UxStateController(UxMemoryStateService uxMemoryStateService,
                             AccessSessionService accessSessionService,
                             BrowserTokenService browserTokenService) {
        this.uxMemoryStateService = uxMemoryStateService;
        this.accessSessionService = accessSessionService;
        this.browserTokenService = browserTokenService;
    }

    @PostMapping("/tooltip/seen")
    public ResponseEntity<Void> markTooltipSeen(@RequestParam("moduleKey") String moduleKey,
                                                HttpSession session,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        uxMemoryStateService.markTooltipSeen(
                browserTokenService.resolveOrCreate(request, response),
                accessSessionService.getCurrentProfile(session),
                moduleKey);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/empty-state/dismiss")
    public ResponseEntity<Void> dismissEmptyState(@RequestParam("moduleKey") String moduleKey,
                                                  HttpSession session,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        uxMemoryStateService.markEmptyStateDismissed(
                browserTokenService.resolveOrCreate(request, response),
                accessSessionService.getCurrentProfile(session),
                moduleKey);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/guide/step")
    public ResponseEntity<Void> updateGuideStep(@RequestParam("moduleKey") String moduleKey,
                                                @RequestParam("state") String state,
                                                HttpSession session,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        uxMemoryStateService.updateGuideStep(
                browserTokenService.resolveOrCreate(request, response),
                accessSessionService.getCurrentProfile(session),
                moduleKey,
                state);
        return ResponseEntity.ok().build();
    }
}
