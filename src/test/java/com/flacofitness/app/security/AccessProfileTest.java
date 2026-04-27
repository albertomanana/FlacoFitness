package com.flacofitness.app.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessProfileTest {

    @Test
    void clienteSoloAccedeASuPortalYNoANavegacionGenerica() {
        assertThat(AccessProfile.CLIENTE.canAccess("/cliente", "GET")).isTrue();
        assertThat(AccessProfile.CLIENTE.canAccess("/cliente/dashboard", "GET")).isTrue();
        assertThat(AccessProfile.CLIENTE.canAccess("/cliente/checkin", "POST")).isTrue();
        assertThat(AccessProfile.CLIENTE.canAccess("/api/cliente/check-in", "POST")).isTrue();
        assertThat(AccessProfile.CLIENTE.canAccess("/", "GET")).isFalse();
        assertThat(AccessProfile.CLIENTE.canAccess("/usuarios", "GET")).isFalse();
        assertThat(AccessProfile.CLIENTE.canAccess("/api/staff/usuarios-activos", "GET")).isFalse();
        assertThat(AccessProfile.CLIENTE.canAccess("/rutinas/12", "GET")).isFalse();
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
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/staff/dashboard", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/staff/nominas/1", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/api/staff/usuarios-activos", "GET")).isTrue();
    }

    @Test
    void recepcionPuedeOperarReservasPeroNoCrearSesiones() {
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/usuarios/nuevo", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/pagos", "POST")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/sesiones", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/sesiones/nueva", "GET")).isFalse();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/sesiones/2/reservas", "POST")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/staff/dashboard", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/api/staff/registrar-asistencia", "POST")).isTrue();
    }

    @Test
    void entrenadorPuedeOperarRutinasYClasesPeroNoMaquinas() {
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/rutinas", "POST")).isTrue();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/clases/nueva", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/sesiones/2/asistencias", "POST")).isTrue();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/maquinas/1", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/maquinas", "POST")).isFalse();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/usuarios/1/editar", "GET")).isFalse();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/staff/dashboard", "GET")).isTrue();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/staff/nominas/1/pdf", "GET")).isTrue();
    }

    @Test
    void adminTieneAccesoTotal() {
        assertThat(AccessProfile.ADMIN.canAccess("/cualquier-ruta", "GET")).isTrue();
        assertThat(AccessProfile.ADMIN.canAccess("/otra-ruta", "POST")).isTrue();
    }

    @Test
    void chatInternoDisponibleParaPerfilesAutenticados() {
        for (AccessProfile profile : AccessProfile.values()) {
            assertThat(profile.canAccess("/chat", "GET")).isTrue();
            assertThat(profile.canAccess("/api/chat/consulta", "POST")).isTrue();
        }
    }

    @Test
    void automatizacionesSoloParaAdminYGerente() {
        assertThat(AccessProfile.ADMIN.canAccess("/automatizaciones/ejecutar", "POST")).isTrue();
        assertThat(AccessProfile.STAFF_GERENTE.canAccess("/automatizaciones/ejecutar", "POST")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canAccess("/automatizaciones/ejecutar", "POST")).isFalse();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canAccess("/automatizaciones/ejecutar", "POST")).isFalse();
        assertThat(AccessProfile.CLIENTE.canAccess("/automatizaciones/ejecutar", "POST")).isFalse();
    }

    @Test
    void todosPuedenAccederASalirYNotificaciones() {
        for (AccessProfile profile : AccessProfile.values()) {
            assertThat(profile.canAccess("/salir", "POST")).isTrue();
            assertThat(profile.canAccess("/notificaciones/1", "GET")).isTrue();
        }
    }

    @Test
    void canSeeSectionFuncionaCorrectamente() {
        assertThat(AccessProfile.CLIENTE.canSeeSection("cliente")).isTrue();
        assertThat(AccessProfile.CLIENTE.canSeeSection("usuarios")).isFalse();
        assertThat(AccessProfile.STAFF_GERENTE.canSeeSection("gastos")).isTrue();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canSeeSection("rutinas")).isTrue();
        assertThat(AccessProfile.STAFF_ENTRENADOR.canSeeSection("gastos")).isFalse();
        assertThat(AccessProfile.STAFF_RECEPCION.canSeeSection("pagos")).isTrue();
        assertThat(AccessProfile.STAFF_RECEPCION.canSeeSection("staff")).isFalse();
    }
}
