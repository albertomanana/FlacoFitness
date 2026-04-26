package com.flacofitness.app.model.dto;

import java.util.ArrayList;
import java.util.List;

public class FinancialAutomationResult {

    private int pagosGenerados;
    private int pagosVencidos;
    private int gastosGenerados;
    private int gastosVencidos;
    private int nominasGeneradas;
    private final List<String> errors = new ArrayList<>();

    public int getPagosGenerados() {
        return pagosGenerados;
    }

    public void addPagosGenerados(int pagosGenerados) {
        this.pagosGenerados += pagosGenerados;
    }

    public int getPagosVencidos() {
        return pagosVencidos;
    }

    public void addPagosVencidos(int pagosVencidos) {
        this.pagosVencidos += pagosVencidos;
    }

    public int getGastosGenerados() {
        return gastosGenerados;
    }

    public void addGastosGenerados(int gastosGenerados) {
        this.gastosGenerados += gastosGenerados;
    }

    public int getGastosVencidos() {
        return gastosVencidos;
    }

    public void addGastosVencidos(int gastosVencidos) {
        this.gastosVencidos += gastosVencidos;
    }

    public int getNominasGeneradas() {
        return nominasGeneradas;
    }

    public void addNominasGeneradas(int nominasGeneradas) {
        this.nominasGeneradas += nominasGeneradas;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        if (error != null && !error.isBlank()) {
            errors.add(error);
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasMovements() {
        return pagosGenerados > 0
                || pagosVencidos > 0
                || gastosGenerados > 0
                || gastosVencidos > 0
                || nominasGeneradas > 0;
    }

    public String toHumanSummary() {
        return "pagos generados=" + pagosGenerados
                + ", pagos vencidos=" + pagosVencidos
                + ", gastos generados=" + gastosGenerados
                + ", gastos vencidos=" + gastosVencidos
                + ", nominas generadas=" + nominasGeneradas
                + ".";
    }
}
