package com.flacofitness.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flacofitness.app.model.dto.FinancialCenterStatsResponse;
import com.flacofitness.app.service.FinancialCenterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/finanzas")
public class FinanzasController {

    private final FinancialCenterService financialCenterService;
    private final ObjectMapper objectMapper;

    public FinanzasController(FinancialCenterService financialCenterService, ObjectMapper objectMapper) {
        this.financialCenterService = financialCenterService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String centroFinanciero(Model model) {
        FinancialCenterStatsResponse stats = financialCenterService.buildStats();
        model.addAttribute("financialStats", stats);
        model.addAttribute("financialStatsJson", toJson(stats));
        return "finanzas/index";
    }

    private String toJson(FinancialCenterStatsResponse stats) {
        try {
            return objectMapper.writeValueAsString(stats);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el centro financiero", ex);
        }
    }
}
