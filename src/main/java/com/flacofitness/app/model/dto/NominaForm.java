package com.flacofitness.app.model.dto;

import com.flacofitness.app.model.entity.Nomina;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class NominaForm {

    @NotNull(message = "Debes seleccionar un staff")
    private Long staffPerfilId;

    @NotBlank(message = "El periodo es obligatorio")
    private String periodo;

    @DecimalMin(value = "0.01", message = "El salario base debe ser mayor que 0")
    private BigDecimal salarioBase;

    @DecimalMin(value = "0.00", message = "El bonus no puede ser negativo")
    private BigDecimal bonus = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Las deducciones no pueden ser negativas")
    private BigDecimal deducciones = BigDecimal.ZERO;

    public static NominaForm from(Nomina nomina) {
        NominaForm form = new NominaForm();
        if (nomina == null) {
            return form;
        }
        if (nomina.getStaffPerfil() != null) {
            form.setStaffPerfilId(nomina.getStaffPerfil().getId());
        }
        form.setPeriodo(nomina.getPeriodo());
        form.setSalarioBase(nomina.getSalarioBase());
        form.setBonus(nomina.getBonus());
        form.setDeducciones(nomina.getDeducciones());
        return form;
    }

    public Long getStaffPerfilId() {
        return staffPerfilId;
    }

    public void setStaffPerfilId(Long staffPerfilId) {
        this.staffPerfilId = staffPerfilId;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(BigDecimal salarioBase) {
        this.salarioBase = salarioBase;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getDeducciones() {
        return deducciones;
    }

    public void setDeducciones(BigDecimal deducciones) {
        this.deducciones = deducciones;
    }
}
