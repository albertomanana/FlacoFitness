package com.flacofitness.app.controller;

import com.flacofitness.app.service.OperationsAutomationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/automatizaciones")
public class OperationsAutomationController {

    private final OperationsAutomationService operationsAutomationService;

    public OperationsAutomationController(OperationsAutomationService operationsAutomationService) {
        this.operationsAutomationService = operationsAutomationService;
    }

    @PostMapping("/ejecutar")
    public String ejecutar(RedirectAttributes redirectAttributes) {
        var result = operationsAutomationService.runAll();
        redirectAttributes.addFlashAttribute(
                result.hasErrors() ? "mensajeError" : "mensajeExito",
                "Automatizacion terminada. " + result.toHumanSummary());
        return "redirect:/";
    }
}
