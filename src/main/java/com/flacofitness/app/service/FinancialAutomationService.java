package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.FinancialAutomationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FinancialAutomationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinancialAutomationService.class);

    private final PagoService pagoService;
    private final GastoService gastoService;
    private final NominaService nominaService;

    public FinancialAutomationService(PagoService pagoService,
                                      GastoService gastoService,
                                      NominaService nominaService) {
        this.pagoService = pagoService;
        this.gastoService = gastoService;
        this.nominaService = nominaService;
    }

    public FinancialAutomationResult run(String trigger) {
        FinancialAutomationResult result = new FinancialAutomationResult();
        actualizarEstadosVencidos(result, trigger);
        generarPagos(result, trigger);
        generarGastos(result, trigger);
        generarNominas(result, trigger);
        LOGGER.info("Automatizacion financiera ejecutada por {}. {}", trigger, result.toHumanSummary());
        return result;
    }

    public FinancialAutomationResult runExpensesOnly(String trigger) {
        FinancialAutomationResult result = new FinancialAutomationResult();
        actualizarGastosVencidos(result, trigger);
        generarGastos(result, trigger);
        LOGGER.info("Automatizacion de gastos ejecutada por {}. {}", trigger, result.toHumanSummary());
        return result;
    }

    public FinancialAutomationResult runPayrollsOnly(String trigger) {
        FinancialAutomationResult result = new FinancialAutomationResult();
        actualizarGastosVencidos(result, trigger);
        generarNominas(result, trigger);
        LOGGER.info("Automatizacion de nominas ejecutada por {}. {}", trigger, result.toHumanSummary());
        return result;
    }

    private void actualizarEstadosVencidos(FinancialAutomationResult result, String trigger) {
        actualizarPagosVencidos(result, trigger);
        actualizarGastosVencidos(result, trigger);
    }

    private void actualizarPagosVencidos(FinancialAutomationResult result, String trigger) {
        try {
            result.addPagosVencidos(pagoService.actualizarPagosVencidos());
        } catch (RuntimeException ex) {
            result.addError("pagos vencidos: " + ex.getMessage());
            LOGGER.warn("Error actualizando pagos vencidos durante {}", trigger, ex);
        }
    }

    private void actualizarGastosVencidos(FinancialAutomationResult result, String trigger) {
        try {
            result.addGastosVencidos(gastoService.actualizarGastosVencidos());
        } catch (RuntimeException ex) {
            result.addError("gastos vencidos: " + ex.getMessage());
            LOGGER.warn("Error actualizando gastos vencidos durante {}", trigger, ex);
        }
    }

    private void generarPagos(FinancialAutomationResult result, String trigger) {
        try {
            result.addPagosGenerados(pagoService.generarPagosMensuales());
        } catch (RuntimeException ex) {
            result.addError("pagos: " + ex.getMessage());
            LOGGER.warn("Error generando pagos automaticos durante {}", trigger, ex);
        }
    }

    private void generarGastos(FinancialAutomationResult result, String trigger) {
        try {
            result.addGastosGenerados(gastoService.procesarGastosRecurrentes());
        } catch (RuntimeException ex) {
            result.addError("gastos: " + ex.getMessage());
            LOGGER.warn("Error generando gastos recurrentes durante {}", trigger, ex);
        }
    }

    private void generarNominas(FinancialAutomationResult result, String trigger) {
        try {
            result.addNominasGeneradas(nominaService.generarNominasMensuales());
        } catch (RuntimeException ex) {
            result.addError("nominas: " + ex.getMessage());
            LOGGER.warn("Error generando nominas automaticas durante {}", trigger, ex);
        }
    }
}
