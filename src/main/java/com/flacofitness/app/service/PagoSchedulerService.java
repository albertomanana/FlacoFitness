package com.flacofitness.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.pagos.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class PagoSchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagoSchedulerService.class);

    private final PagoService pagoService;

    public PagoSchedulerService(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @Scheduled(cron = "${app.pagos.scheduler.cron:0 0 3 * * *}")
    public void ejecutarGeneracionAutomatica() {
        int pagosGenerados = pagoService.generarPagosMensuales();

        if (pagosGenerados > 0) {
            LOGGER.info("Scheduler de pagos automáticos ejecutado. Pagos creados: {}", pagosGenerados);
        }
    }
}
