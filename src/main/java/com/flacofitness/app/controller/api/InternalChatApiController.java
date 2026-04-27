package com.flacofitness.app.controller.api;

import com.flacofitness.app.model.dto.InternalChatRequest;
import com.flacofitness.app.model.dto.InternalChatResponse;
import com.flacofitness.app.security.AccessSessionService;
import com.flacofitness.app.service.InternalAssistantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class InternalChatApiController {

    private final AccessSessionService accessSessionService;
    private final InternalAssistantService internalAssistantService;

    public InternalChatApiController(AccessSessionService accessSessionService,
                                     InternalAssistantService internalAssistantService) {
        this.accessSessionService = accessSessionService;
        this.internalAssistantService = internalAssistantService;
    }

    @PostMapping("/consulta")
    public ResponseEntity<InternalChatResponse> consulta(@RequestBody InternalChatRequest request,
                                                         HttpSession session) {
        return accessSessionService.getCurrentUser(session)
                .map(usuario -> ResponseEntity.ok(internalAssistantService.answer(
                        accessSessionService.getCurrentProfile(session),
                        usuario,
                        request == null ? "" : request.message())))
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(new InternalChatResponse("Inicia sesion para usar la ayuda.", java.util.List.of())));
    }
}
