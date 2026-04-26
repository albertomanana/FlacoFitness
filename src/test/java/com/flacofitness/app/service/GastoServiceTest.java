package com.flacofitness.app.service;

import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.GastoRecurrente;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.EstadoGasto;
import com.flacofitness.app.model.enums.FrecuenciaGasto;
import com.flacofitness.app.model.enums.TipoGasto;
import com.flacofitness.app.repository.GastoRecurrenteRepository;
import com.flacofitness.app.repository.GastoRepository;
import com.flacofitness.app.repository.MaquinaRepository;
import com.flacofitness.app.repository.MaterialRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GastoServiceTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private GastoRecurrenteRepository gastoRecurrenteRepository;

    @Mock
    private StaffPerfilRepository staffPerfilRepository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @Mock
    private MaterialRepository materialRepository;

        private GastoService gastoService;

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
    }

    @Test
    void procesarGastosRecurrentes_generaCiclosPendientesSinDuplicarYAvanzaProximoCargo() {
                LocalDate hoy = LocalDate.now();

        GastoRecurrente plantilla = new GastoRecurrente();
        plantilla.setId(7L);
        plantilla.setConcepto("Alquiler local");
        plantilla.setCategoria(CategoriaGasto.ALQUILER);
        plantilla.setTipoGasto(TipoGasto.FIJO);
        plantilla.setImporte(new BigDecimal("1200.00"));
        plantilla.setFrecuencia(FrecuenciaGasto.MENSUAL);
        plantilla.setFechaInicio(LocalDate.of(2026, 3, 1));
        plantilla.setFechaProximoCargo(LocalDate.of(2026, 3, 1));
        plantilla.setActivo(true);

        when(gastoRecurrenteRepository
                .findByActivoTrueAndFechaProximoCargoLessThanEqualOrderByFechaProximoCargoAscIdAsc(hoy))
                .thenReturn(List.of(plantilla));
        when(gastoRepository.existsByGastoRecurrenteIdAndFechaVencimiento(7L, LocalDate.of(2026, 3, 1)))
                .thenReturn(false);
        when(gastoRepository.existsByGastoRecurrenteIdAndFechaVencimiento(7L, LocalDate.of(2026, 4, 1)))
                .thenReturn(true);
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int generados = gastoService.procesarGastosRecurrentes();

        ArgumentCaptor<Gasto> gastoCaptor = ArgumentCaptor.forClass(Gasto.class);
        verify(gastoRepository).save(gastoCaptor.capture());
        verify(gastoRecurrenteRepository).save(plantilla);

        assertThat(generados).isEqualTo(1);
        assertThat(gastoCaptor.getValue().getFechaVencimiento()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(gastoCaptor.getValue().getEstado()).isEqualTo(EstadoGasto.VENCIDO);
        assertThat(plantilla.getFechaProximoCargo()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    @Test
    void actualizarGastosVencidos_usaFechaOperativa() {
                LocalDate hoy = LocalDate.now();
        when(gastoRepository.marcarVencidos(
                List.of(EstadoGasto.PROGRAMADO, EstadoGasto.PENDIENTE),
                EstadoGasto.VENCIDO,
                hoy)).thenReturn(2);

        int actualizados = gastoService.actualizarGastosVencidos();

        assertThat(actualizados).isEqualTo(2);
        verify(gastoRepository).marcarVencidos(
                List.of(EstadoGasto.PROGRAMADO, EstadoGasto.PENDIENTE),
                EstadoGasto.VENCIDO,
                hoy);
    }

    @Test
    void crearGastoCompraMaterial_creaGastoPagadoConImporteTotal() {
        Material material = new Material();
        material.setId(5L);
        material.setNombre("Bandas");
        material.setStock(4);
        material.setCosteUnitario(new BigDecimal("12.50"));
        material.setProveedor("Proveedor Fit");

        when(materialRepository.findById(5L)).thenReturn(Optional.of(material));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Gasto gasto = gastoService.crearGastoCompraMaterial(material);

        assertThat(gasto.getCategoria()).isEqualTo(CategoriaGasto.MATERIAL);
        assertThat(gasto.getTipoGasto()).isEqualTo(TipoGasto.VARIABLE);
        assertThat(gasto.getEstado()).isEqualTo(EstadoGasto.PAGADO);
        assertThat(gasto.getImporte()).isEqualByComparingTo("50.00");
        assertThat(gasto.getMaterial()).isSameAs(material);
    }

    @Test
    void crearGastoCompraMaquina_creaGastoPagadoVinculado() {
        Maquina maquina = new Maquina();
        maquina.setId(6L);
        maquina.setNombre("Bike Pro");
        maquina.setMarca("FitTech");
        maquina.setCosteCompra(new BigDecimal("899.00"));

        when(maquinaRepository.findById(6L)).thenReturn(Optional.of(maquina));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Gasto gasto = gastoService.crearGastoCompraMaquina(maquina);

        assertThat(gasto.getCategoria()).isEqualTo(CategoriaGasto.MAQUINA);
        assertThat(gasto.getTipoGasto()).isEqualTo(TipoGasto.VARIABLE);
        assertThat(gasto.getEstado()).isEqualTo(EstadoGasto.PAGADO);
        assertThat(gasto.getImporte()).isEqualByComparingTo("899.00");
        assertThat(gasto.getMaquina()).isSameAs(maquina);
    }

}
