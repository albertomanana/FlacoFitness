package com.flacofitness.app.model.dto;

import com.flacofitness.app.model.entity.Trial;
import com.flacofitness.app.model.enums.EstadoTrial;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class TrialForm {

    private Long id;

    @NotBlank(message = "Debes indicar el nombre del lead.")
    private String nombre;

    private String apellidos;

    @Email(message = "El email no tiene un formato valido.")
    private String email;

    private String telefono;

    @NotNull(message = "Debes indicar la fecha del dia de prueba.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPrueba;

    @NotNull(message = "Debes indicar el estado del trial.")
    private EstadoTrial estado;

    private String origen;
    private Long staffResponsableId;
    private String observaciones;

    public static TrialForm from(Trial trial) {
        TrialForm form = new TrialForm();
        if (trial == null) {
            return form;
        }
        form.setId(trial.getId());
        form.setNombre(trial.getNombre());
        form.setApellidos(trial.getApellidos());
        form.setEmail(trial.getEmail());
        form.setTelefono(trial.getTelefono());
        form.setFechaPrueba(trial.getFechaPrueba());
        form.setEstado(trial.getEstado());
        form.setOrigen(trial.getOrigen());
        form.setObservaciones(trial.getObservaciones());
        if (trial.getStaffResponsable() != null) {
            form.setStaffResponsableId(trial.getStaffResponsable().getId());
        }
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaPrueba() {
        return fechaPrueba;
    }

    public void setFechaPrueba(LocalDate fechaPrueba) {
        this.fechaPrueba = fechaPrueba;
    }

    public EstadoTrial getEstado() {
        return estado;
    }

    public void setEstado(EstadoTrial estado) {
        this.estado = estado;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public Long getStaffResponsableId() {
        return staffResponsableId;
    }

    public void setStaffResponsableId(Long staffResponsableId) {
        this.staffResponsableId = staffResponsableId;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
