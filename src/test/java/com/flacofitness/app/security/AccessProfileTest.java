package com.flacofitness.app.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessProfileTest {

    @Test
    void clienteSoloAccedeAPanelYDetallesPermitidos() {
        assertThat(AccessProfile.CLIENTE.canAccess("/cliente", "GET")).isTrue();
        assertThat(AccessProfile.CLIENTE.canAccess("/", "GET")).isFalse();
        assertThat(AccessProfile.CLIENTE.canAccess("/usuarios", "GET")).isFalse();
        assertThat(AccessProfile.CLIENTE.canAccess("/rutinas/12", "GET")).isTrue();
        assertThat(AccessProfile.CLIENTE.canAccess("/rutinas/nueva", "GET")).isFalse();
        assertThat(AccessProfile.CLIENTE.canAccess("/pagos/4", "POST")).isFalse();
    }

    @Test
    void gerenteConsultaOperacionPeroNoCreaClasesRutinasOSesiones() {
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/clases", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/sesiones/3", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/rutinas/nueva", "GET")).isFalse();
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/clases", "POST")).isFalse();
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/gastos", "POST")).isTrue();
    }

    @Test
    void recepcionPuedeOperarReservasPeroNoCrearSesiones() {
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/usuarios/nuevo", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/pagos", "POST")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/sesiones", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/sesiones/nueva", "GET")).isFalse();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/sesiones/2/reservas", "POST")).isTrue();
    }
}
