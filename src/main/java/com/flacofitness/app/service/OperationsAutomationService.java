package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.FinancialAutomationResult;
import com.flacofitness.app.model.dto.OperationsAutomationResult;
import org.springframework.stereotype.Service;

@Service
public class OperationsAutomationService {

    private final FinancialAutomationService financialAutomationService;
    private final MembresiaService membresiaService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;

    public OperationsAutomationService(FinancialAutomationService financialAutomationService,
                                       MembresiaService membresiaService,
                                       MaquinaService maquinaService,
                                       MaterialService materialService) {
        this.financialAutomationService = financialAutomationService;
        this.membresiaService = membresiaService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
    }

    public OperationsAutomationResult runAll() {
        OperationsAutomationResult result = new OperationsAutomationResult();
        runFinancial(result);
        runSafely(result, "Membresias vencidas", () -> membresiaService.procesarMembresiasVencidas());
        runSafely(result, "Maquinas en revision", () -> maquinaService.procesarMaquinasEnMantenimiento());
        runSafely(result, "Material bajo stock", () -> materialService.procesarStockBajo());
        return result;
    }

    private void runFinancial(OperationsAutomationResult result) {
        try {
            FinancialAutomationResult financial = financialAutomationService.run("OPERATIONS_MANUAL");
            result.addMovement("Pagos generados", financial.getPagosGenerados());
            result.addMovement("Pagos vencidos", financial.getPagosVencidos());
            result.addMovement("Gastos generados", financial.getGastosGenerados());
            result.addMovement("Gastos vencidos", financial.getGastosVencidos());
            result.addMovement("Nominas generadas", financial.getNominasGeneradas());
            financial.getErrors().forEach(error -> result.addError("Finanzas", error));
        } catch (RuntimeException ex) {
            result.addError("Finanzas", safeMessage(ex));
        }
    }

    private void runSafely(OperationsAutomationResult result, String label, AutomationTask task) {
        try {
            result.addMovement(label, task.run());
        } catch (RuntimeException ex) {
            result.addError(label, safeMessage(ex));
        }
    }

    private String safeMessage(RuntimeException ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "No se pudo completar este bloque"
                : ex.getMessage();
    }

    @FunctionalInterface
    private interface AutomationTask {
        int run();
    }
}
