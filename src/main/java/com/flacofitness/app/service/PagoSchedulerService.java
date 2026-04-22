package com.flacofitness.app.service;

/**
 * Conservado solo como referencia de compatibilidad historica.
 * La ejecucion programada real vive en {@link SaaSSchedulerService} y delega en
 * {@link FinancialAutomationService} para evitar dobles disparos financieros.
 */
@Deprecated(forRemoval = false)
public final class PagoSchedulerService {

    private PagoSchedulerService() {
    }
}
