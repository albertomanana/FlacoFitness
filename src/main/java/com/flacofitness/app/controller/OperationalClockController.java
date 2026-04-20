package com.flacofitness.app.controller;

import com.flacofitness.app.service.OperationalClockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/reloj-operativo")
public class OperationalClockController {

    private final OperationalClockService operationalClockService;

    public OperationalClockController(OperationalClockService operationalClockService) {
        this.operationalClockService = operationalClockService;
    }

    @PostMapping
    public String actualizar(@RequestParam(name = "accion", defaultValue = "set") String accion,
                             @RequestParam(name = "fechaHora", required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                             @RequestParam(name = "returnTo", required = false) String returnTo,
                             RedirectAttributes redirectAttributes) {
        switch (accion) {
            case "minusMonth" -> {
                operationalClockService.shiftMonths(-1);
                redirectAttributes.addFlashAttribute("mensajeExito", "Reloj operativo movido un mes hacia atras.");
            }
            case "plusMonth" -> {
                operationalClockService.shiftMonths(1);
                redirectAttributes.addFlashAttribute("mensajeExito", "Reloj operativo movido un mes hacia adelante.");
            }
            case "reset" -> {
                operationalClockService.resetToSystem();
                redirectAttributes.addFlashAttribute("mensajeExito", "Reloj operativo sincronizado con la fecha real.");
            }
            default -> {
                if (fechaHora == null) {
                    redirectAttributes.addFlashAttribute("mensajeError", "Debes indicar fecha y hora para fijar el reloj.");
                    return "redirect:" + sanitizeReturnTo(returnTo);
                }
                operationalClockService.setFixed(fechaHora);
                redirectAttributes.addFlashAttribute("mensajeExito", "Reloj operativo actualizado.");
            }
        }

        return "redirect:" + sanitizeReturnTo(returnTo);
    }

    private String sanitizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/";
        }
        return returnTo;
    }
}
