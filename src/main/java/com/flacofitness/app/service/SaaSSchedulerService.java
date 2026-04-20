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
    private final GastoService gastoService;
    private final SesionClaseService sesionClaseService;
    private final OperationalClockService operationalClockService;

    public SaaSSchedulerService(MembresiaService membresiaService,
                                MaquinaService maquinaService,
                                MaterialService materialService,
                                GastoService gastoService,
                                SesionClaseService sesionClaseService,
                                OperationalClockService operationalClockService) {
        this.membresiaService = membresiaService;
        this.maquinaService = maquinaService;
        this.materialService = materialService;
        this.gastoService = gastoService;
        this.sesionClaseService = sesionClaseService;
        this.operationalClockService = operationalClockService;
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
            int gastosGenerados = gastoService.procesarGastosRecurrentes();
            log.info("SaaS - Gastos recurrentes generados: {}", gastosGenerados);
        } catch (Exception e) {
            log.error("Error al procesar gastos recurrentes", e);
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
