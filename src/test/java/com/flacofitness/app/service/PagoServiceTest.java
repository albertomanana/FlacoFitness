package com.flacofitness.app.service;

import com.flacofitness.app.model.entity.Pago;
import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoPago;
import com.flacofitness.app.model.enums.MetodoPago;
import com.flacofitness.app.repository.PagoRepository;
import com.flacofitness.app.repository.PlanRepository;
import com.flacofitness.app.repository.MembresiaUsuarioRepository;
import com.flacofitness.app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private MembresiaUsuarioRepository membresiaUsuarioRepository;

    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        OperationalClockService operationalClockService = new OperationalClockService();
        RecurrenceService recurrenceService = new RecurrenceService();
        pagoService = new PagoService(
                pagoRepository,
                usuarioRepository,
                planRepository,
                membresiaUsuarioRepository,
                operationalClockService,
                recurrenceService);
    }

    @Test
    void generarPagosMensuales_creaPagoVencidoSiElCicloYaPasoYAvanzaFechaProxima() {
        LocalDate fechaVencida = LocalDate.now().minusDays(1);
        Usuario usuario = crearUsuario(1L, fechaVencida);

        when(usuarioRepository.findUsuariosConPagoPendiente(LocalDate.now())).thenReturn(List.of(usuario));
        when(pagoRepository.existsByUsuarioIdAndFechaVencimiento(usuario.getId(), fechaVencida)).thenReturn(false);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int pagosGenerados = pagoService.generarPagosMensuales();

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());

        Pago pagoGenerado = pagoCaptor.getValue();
        assertThat(pagosGenerados).isEqualTo(1);
        assertThat(pagoGenerado.getUsuario()).isSameAs(usuario);
        assertThat(pagoGenerado.getPlan()).isSameAs(usuario.getPlan());
        assertThat(pagoGenerado.getFechaVencimiento()).isEqualTo(fechaVencida);
        assertThat(pagoGenerado.getEstado()).isEqualTo(EstadoPago.VENCIDO);
        assertThat(pagoGenerado.getMetodoPago()).isEqualTo(MetodoPago.TRANSFERENCIA);
        assertThat(usuario.getFechaProximoPago()).isEqualTo(fechaVencida.plusDays(usuario.getPlan().getDuracionDias()));
    }

    @Test
    void actualizarPagosVencidos_usaFechaOperativa() {
        LocalDate hoy = LocalDate.now();
        when(pagoRepository.marcarVencidos(
                List.of(EstadoPago.PROGRAMADO, EstadoPago.PENDIENTE),
                EstadoPago.VENCIDO,
                hoy)).thenReturn(3);

        int actualizados = pagoService.actualizarPagosVencidos();

        assertThat(actualizados).isEqualTo(3);
        verify(pagoRepository).marcarVencidos(
                List.of(EstadoPago.PROGRAMADO, EstadoPago.PENDIENTE),
                EstadoPago.VENCIDO,
                hoy);
    }

    @Test
    void generarPagosMensuales_reutilizaUltimoPagoSiNoHayFechaProximaYEvitaDuplicados() {
        Usuario usuario = crearUsuario(2L, null);
        LocalDate hoy = LocalDate.now();
        LocalDate ultimaFechaPago = hoy.minusDays(35);

        Pago ultimoPago = new Pago();
        ultimoPago.setId(10L);
        ultimoPago.setFechaVencimiento(ultimaFechaPago);

        LocalDate fechaEsperada = ultimaFechaPago.plusDays(usuario.getPlan().getDuracionDias());

        when(usuarioRepository.findUsuariosConPagoPendiente(hoy)).thenReturn(List.of(usuario));
        when(pagoRepository.findTopByUsuarioIdOrderByFechaVencimientoDescIdDesc(usuario.getId())).thenReturn(Optional.of(ultimoPago));
        when(pagoRepository.existsByUsuarioIdAndFechaVencimiento(usuario.getId(), fechaEsperada)).thenReturn(true);

        int pagosGenerados = pagoService.generarPagosMensuales();

        assertThat(pagosGenerados).isZero();
        assertThat(usuario.getFechaProximoPago()).isEqualTo(fechaEsperada.plusDays(usuario.getPlan().getDuracionDias()));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    private Usuario crearUsuario(Long id, LocalDate fechaProximoPago) {
        Plan plan = new Plan();
        plan.setId(1L);
        plan.setNombre("Premium");
        plan.setPrecioMensual(new BigDecimal("49.90"));
        plan.setDuracionDias(30);
        plan.setActivo(true);

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Usuario " + id);
        usuario.setEmail("usuario" + id + "@mail.com");
        usuario.setActivo(true);
        usuario.setPlan(plan);
        usuario.setFechaProximoPago(fechaProximoPago);
        return usuario;
    }

}
