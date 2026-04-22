package com.flacofitness.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SaaSSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SaaSSchedulerService.class);

    private final MembresiaService membresiaService;
    private final MaquinaService maquinaService;
    private final MaterialService materialService;
    private final SesionClaseService sesionClaseService;
    private final OperationalClockService operationalClockService;
    private final FinancialAutomationService financialAutomationService;

    public SaaSSchedulerService(MembresiaService membresiaService,
                                MaquinaService maquinaService,
                                MaterialService materialService,
                                SesionClaseService sesionClaseService,
                                OperationalClockService operationalClockService,
                                FinancialAutomationService financialAutomationService) {
        this.membresiaService = membresiaService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.sesionClaseService = sesionClaseService;
        this.operationalClockService = operationalClockService;
        this.financialAutomationService = financialAutomationService;
    }

    // Se ejecuta cada día a la medianoche
    @Scheduled(cron = "0 0 0 * * *")
    public void ejecutarTareasDiariasSaaS() {
        log.info("Iniciando tareas programadas SaaS diarias - {}", operationalClockService.today());

        try {
            int membresiasVencidas = membresiaService.procesarMembresiasVencidas();
            log.info("SaaS - Membresías vencidas actualizadas: {}", membresiasVencidas);
        } catch (Exception e) {
            log.error("Error al procesar membresías vencidas", e);
        }

        try {
            int maquinasMantenimiento = maquinaService.procesarMaquinasEnMantenimiento();
            log.info("SaaS - Máquinas que requieren mantenimiento: {}", maquinasMantenimiento);
        } catch (Exception e) {
            log.error("Error al procesar máquinas en mantenimiento", e);
        }

        try {
            var financialResult = financialAutomationService.run("DAILY_SCHEDULER");
            log.info("SaaS - Resultado financiero diario: {}", financialResult.toHumanSummary());
        } catch (Exception e) {
            log.error("Error al ejecutar automatizacion financiera diaria", e);
        }

        try {
            int sesionesCerradas = sesionClaseService.cerrarSesionesFinalizadas();
            log.info("SaaS - Sesiones cerradas automaticamente: {}", sesionesCerradas);
        } catch (Exception e) {
            log.error("Error al cerrar sesiones automaticamente", e);
        }

        log.info("Finalizadas tareas programadas SaaS diarias");
    }

    // Se ejecuta cada 12 horas
    @Scheduled(cron = "0 0 */12 * * *")
    public void verificarStockMateriales() {
        log.info("Verificando stock de materiales...");
        try {
            // Actualmente notificaría o enviaría un aviso.
            // Se asume que en una versión posterior esto podría enviar un email.
            int materialesBajoStock = materialService.procesarStockBajo();
            log.info("SaaS - Materiales bajo stock detectados: {}", materialesBajoStock);
        } catch (Exception e) {
            log.error("Error al verificar stock de materiales", e);
        }
    }
}
