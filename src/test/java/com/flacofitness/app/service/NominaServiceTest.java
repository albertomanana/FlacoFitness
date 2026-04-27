package com.flacofitness.app.service;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.Nomina;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.entity.Usuario;
import com.flacofitness.app.model.enums.EstadoNomina;
import com.flacofitness.app.repository.GastoRecurrenteRepository;
import com.flacofitness.app.repository.GastoRepository;
import com.flacofitness.app.repository.MaquinaRepository;
import com.flacofitness.app.repository.MaterialRepository;
import com.flacofitness.app.repository.NominaRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NominaServiceTest {

    @Mock
    private NominaRepository nominaRepository;

    @Mock
    private StaffPerfilRepository staffPerfilRepository;

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private GastoRecurrenteRepository gastoRecurrenteRepository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @Mock
    private MaterialRepository materialRepository;

    private NominaService nominaService;
    private GastoService gastoService;
    private StaffPerfil staffPerfil;

    @BeforeEach
    void setUp() {
        OperationalClockService operationalClockService = new OperationalClockService();
        gastoService = new GastoService(
                gastoRepository,
                gastoRecurrenteRepository,
                staffPerfilRepository,
                maquinaRepository,
                materialRepository,
                operationalClockService,
                new RecurrenceService());
        nominaService = new NominaService(nominaRepository, staffPerfilRepository, gastoService, operationalClockService);

        Usuario usuario = new Usuario();
        usuario.setNombre("Laura");
        usuario.setApellidos("Navas");

        staffPerfil = new StaffPerfil();
        staffPerfil.setId(7L);
        staffPerfil.setUsuario(usuario);
        staffPerfil.setSalarioBaseMensual(new BigDecimal("1200.00"));
    }

    @Test
    void guardarBorrador_calculaNetoYNoGeneraGasto() {
        when(staffPerfilRepository.findById(7L)).thenReturn(Optional.of(staffPerfil));
        when(nominaRepository.existsByStaffPerfilIdAndPeriodo(7L, "2026-04")).thenReturn(false);
        when(nominaRepository.save(any(Nomina.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Nomina nomina = nominaService.guardarBorrador(
                7L,
                "2026-04",
                new BigDecimal("1000.00"),
                new BigDecimal("150.00"),
                new BigDecimal("50.00"));

        assertThat(nomina.getEstado()).isEqualTo(EstadoNomina.BORRADOR);
        assertThat(nomina.getGasto()).isNull();
        assertThat(nomina.getSalarioNeto()).isEqualByComparingTo("1100.00");
        assertThat(nomina.getReferencia()).isEqualTo("NOM-202604-7");
        assertThat(nomina.getFechaEmision()).isEqualTo(LocalDate.now());
    }

    @Test
    void generar_emiteNominaYConstruyeGastoNomina() {
        when(staffPerfilRepository.findById(7L)).thenReturn(Optional.of(staffPerfil));
        when(nominaRepository.existsByStaffPerfilIdAndPeriodo(7L, "2026-04")).thenReturn(false);
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> {
            Gasto gasto = invocation.getArgument(0);
            gasto.setId(55L);
            return gasto;
        });
        when(nominaRepository.save(any(Nomina.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Nomina nomina = nominaService.generar(
                7L,
                "2026-04",
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO);

        assertThat(nomina.getEstado()).isEqualTo(EstadoNomina.EMITIDA);
        assertThat(nomina.getGasto()).isNotNull();
        assertThat(nomina.getGasto().getId()).isEqualTo(55L);
        assertThat(nomina.getGasto().getConcepto()).isEqualTo("Nómina Laura Navas 2026-04");
    }

    @Test
    void generar_bloqueaDuplicadoPorStaffYPeriodo() {
        when(staffPerfilRepository.findById(7L)).thenReturn(Optional.of(staffPerfil));
        when(nominaRepository.existsByStaffPerfilIdAndPeriodo(7L, "2026-04")).thenReturn(true);

        assertThatThrownBy(() -> nominaService.generar(7L, "2026-04", BigDecimal.TEN, null, null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Ya existe una nómina");
    }

    @Test
    void actualizarBorrador_rechazaNominaEmitida() {
        Nomina nomina = new Nomina();
        nomina.setId(1L);
        nomina.setEstado(EstadoNomina.EMITIDA);
        when(nominaRepository.findById(1L)).thenReturn(Optional.of(nomina));

        assertThatThrownBy(() -> nominaService.actualizarBorrador(
                1L,
                7L,
                "2026-04",
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ZERO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("borrador");
    }

    @Test
    void marcarPagada_emiteBorradorYMarcaGastoAsociado() {
        AtomicReference<Gasto> gastoGuardado = new AtomicReference<>();

        Nomina nomina = new Nomina();
        nomina.setId(1L);
        nomina.setStaffPerfil(staffPerfil);
        nomina.setPeriodo("2026-04");
        nomina.setSalarioNeto(new BigDecimal("1200.00"));
        nomina.setReferencia("NOM-202604-7");
        nomina.setEstado(EstadoNomina.BORRADOR);

        when(staffPerfilRepository.findById(7L)).thenReturn(Optional.of(staffPerfil));
        when(nominaRepository.findById(1L)).thenReturn(Optional.of(nomina));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> {
            Gasto gasto = invocation.getArgument(0);
            if (gasto.getId() == null) {
                gasto.setId(99L);
            }
            gastoGuardado.set(gasto);
            return gasto;
        });
        when(gastoRepository.findById(99L)).thenAnswer(invocation -> Optional.ofNullable(gastoGuardado.get()));
        when(nominaRepository.save(any(Nomina.class))).thenAnswer(invocation -> invocation.getArgument(0));

        nominaService.marcarPagada(1L);

        assertThat(nomina.getEstado()).isEqualTo(EstadoNomina.PAGADA);
        assertThat(nomina.getGasto()).isSameAs(gastoGuardado.get());
        verify(gastoRepository).findById(99L);
    }

    @Test
    void generar_rechazaNetoNoPositivo() {
        when(staffPerfilRepository.findById(7L)).thenReturn(Optional.of(staffPerfil));
        when(nominaRepository.existsByStaffPerfilIdAndPeriodo(7L, "2026-04")).thenReturn(false);

        assertThatThrownBy(() -> nominaService.generar(
                7L,
                "2026-04",
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00")))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("salario neto");
    }
}
