package com.flacofitness.app.config;

import com.flacofitness.app.model.enums.CategoriaGasto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class ProductIntelligenceSettings {

    private final int riskDays;
    private final int inactiveDays;
    private final int membershipExpiringDays;
    private final int machineReviewDays;
    private final BigDecimal expenseAnomalyFactor;
    private final Map<CategoriaGasto, BigDecimal> categoryFallbackThresholds;

    public ProductIntelligenceSettings(@Value("${app.product.risk-days:14}") int riskDays,
                                       @Value("${app.product.inactive-days:30}") int inactiveDays,
                                       @Value("${app.product.membership-expiring-days:7}") int membershipExpiringDays,
                                       @Value("${app.product.machine-review-days:7}") int machineReviewDays,
                                       @Value("${app.product.expense-anomaly-factor:1.5}") BigDecimal expenseAnomalyFactor) {
        this.riskDays = Math.max(1, riskDays);
        this.inactiveDays = Math.max(this.riskDays, inactiveDays);
        this.membershipExpiringDays = Math.max(1, membershipExpiringDays);
        this.machineReviewDays = Math.max(1, machineReviewDays);
        this.expenseAnomalyFactor = expenseAnomalyFactor == null || expenseAnomalyFactor.compareTo(BigDecimal.ONE) < 0
                ? new BigDecimal("1.5")
                : expenseAnomalyFactor;
        this.categoryFallbackThresholds = buildFallbackThresholds();
    }

    public int getRiskDays() {
        return riskDays;
    }

    public int getInactiveDays() {
        return inactiveDays;
    }

    public int getMembershipExpiringDays() {
        return membershipExpiringDays;
    }

    public int getMachineReviewDays() {
        return machineReviewDays;
    }

    public BigDecimal getExpenseAnomalyFactor() {
        return expenseAnomalyFactor;
    }

    public BigDecimal fallbackThresholdFor(CategoriaGasto categoria) {
        return categoryFallbackThresholds.getOrDefault(categoria, new BigDecimal("300.00"));
    }

    private Map<CategoriaGasto, BigDecimal> buildFallbackThresholds() {
        Map<CategoriaGasto, BigDecimal> thresholds = new EnumMap<>(CategoriaGasto.class);
        thresholds.put(CategoriaGasto.ALQUILER, new BigDecimal("1200.00"));
        thresholds.put(CategoriaGasto.LUZ, new BigDecimal("250.00"));
        thresholds.put(CategoriaGasto.AGUA, new BigDecimal("140.00"));
        thresholds.put(CategoriaGasto.INTERNET, new BigDecimal("90.00"));
        thresholds.put(CategoriaGasto.NOMINA, new BigDecimal("1300.00"));
        thresholds.put(CategoriaGasto.MATERIAL, new BigDecimal("450.00"));
        thresholds.put(CategoriaGasto.MAQUINA, new BigDecimal("900.00"));
        thresholds.put(CategoriaGasto.MANTENIMIENTO, new BigDecimal("350.00"));
        thresholds.put(CategoriaGasto.SOFTWARE, new BigDecimal("180.00"));
        thresholds.put(CategoriaGasto.MARKETING, new BigDecimal("500.00"));
        thresholds.put(CategoriaGasto.LIMPIEZA, new BigDecimal("180.00"));
        thresholds.put(CategoriaGasto.IMPUESTOS, new BigDecimal("600.00"));
        thresholds.put(CategoriaGasto.OTROS, new BigDecimal("300.00"));
        return thresholds;
    }
}
