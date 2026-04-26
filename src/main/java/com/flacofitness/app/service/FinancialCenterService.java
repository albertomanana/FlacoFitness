package com.flacofitness.app.service;

import com.flacofitness.app.model.dto.FinancialCenterStatsResponse;
import com.flacofitness.app.model.dto.GastoCategoriaStatsItem;
import com.flacofitness.app.model.enums.EstadoPago;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FinancialCenterService {

    private final PagoService pagoService;
    private final GastoService gastoService;
    private final NominaService nominaService;
    private final OperationalClockService operationalClockService;

    public FinancialCenterService(PagoService pagoService,
                                  GastoService gastoService,
                                  NominaService nominaService,
                                  OperationalClockService operationalClockService) {
        this.pagoService = pagoService;
        this.gastoService = gastoService;
        this.nominaService = nominaService;
        this.operationalClockService = operationalClockService;
    }

    public FinancialCenterStatsResponse buildStats() {
        BigDecimal ingresosMes = pagoService.calcularIngresosMesActual();
        BigDecimal gastosMes = gastoService.calcularGastoMesActual();
        long pagosProgramados = pagoService.contarPagosProgramados();
        long pagosPendientes = pagoService.contarPagosPendientes();
        long pagosVencidos = pagoService.contarPagosVencidos();
        long pagosPagados = pagoService.contarPagosPagados();

        return new FinancialCenterStatsResponse(
                YearMonth.from(operationalClockService.today()).toString(),
                ingresosMes,
                gastosMes,
                ingresosMes.subtract(gastosMes),
                pagoService.calcularDeudaTotal(),
                nominaService.calcularTotalPendiente(),
                pagosProgramados,
                pagosPendientes,
                pagosVencidos,
                pagosPagados,
                nominaService.contarBorradores(),
                nominaService.contarPendientes(),
                nominaService.contarPagadas(),
                nominaService.contarCanceladas(),
                gastoService.contarCriticos(),
                gastoService.contarVencimientosProximos(7),
                gastoService.contarRecurrentesProximos(7),
                pagoService.obtenerIngresosMensuales(),
                gastoService.obtenerGastosMensuales(),
                gastoService.obtenerGastosPorCategoriaMesActual(),
                List.of(
                        new GastoCategoriaStatsItem(EstadoPago.PROGRAMADO.name(), BigDecimal.valueOf(pagosProgramados)),
                        new GastoCategoriaStatsItem(EstadoPago.PENDIENTE.name(), BigDecimal.valueOf(pagosPendientes)),
                        new GastoCategoriaStatsItem(EstadoPago.VENCIDO.name(), BigDecimal.valueOf(pagosVencidos)),
                        new GastoCategoriaStatsItem(EstadoPago.PAGADO.name(), BigDecimal.valueOf(pagosPagados))
                ),
                nominaService.obtenerNominasPorEstado()
        );
    }
}
