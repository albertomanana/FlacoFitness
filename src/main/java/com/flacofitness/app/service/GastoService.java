package com.flacofitness.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.dto.IngresoMensualStatsItem;
import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.repository.GastoRepository;
import com.flacofitness.app.repository.MaquinaRepository;
import com.flacofitness.app.repository.MaterialRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;

@Service
@Transactional(readOnly = true)
public class GastoService {

    private final GastoRepository gastoRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final MaquinaRepository maquinaRepository;
    private final MaterialRepository materialRepository;
    private final OperationalClockService operationalClockService;

    public GastoService(GastoRepository gastoRepository,
                        StaffPerfilRepository staffPerfilRepository,
                        MaquinaRepository maquinaRepository,
                        MaterialRepository materialRepository,
                        OperationalClockService operationalClockService) {
        this.gastoRepository = gastoRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.maquinaRepository = maquinaRepository;
        this.materialRepository = materialRepository;
        this.operationalClockService = operationalClockService;
    }

    public List<Gasto> listarFiltrados(LocalDate desde, LocalDate hasta, CategoriaGasto categoria) {
        return gastoRepository.buscarFiltrados(desde, hasta, categoria);
    }

    public Gasto buscarPorId(Long id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado con id: " + id));
    }

    public long contarActivos() {
        return gastoRepository.countByActivoTrue();
    }

    public long contarCriticos() {
        return gastoRepository.countByActivoTrueAndPagadoFalseAndFechaLessThanEqual(operationalClockService.today());
    }

    public long contarRecurrentesProximos(int dias) {
        LocalDate hoy = operationalClockService.today();
        return gastoRepository.countByActivoTrueAndRecurrenteTrueAndPagadoFalseAndFechaBetween(hoy, hoy.plusDays(Math.max(1, dias)));
    }

    public BigDecimal calcularGastoMesActual() {
        LocalDate hoy = operationalClockService.today();
        return gastoRepository.sumImporteByPeriodo(hoy.getYear(), hoy.getMonthValue());
    }

    public List<IngresoMensualStatsItem> obtenerGastosMensuales() {
        return gastoRepository.sumImporteGroupedByMes().stream()
                .map(item -> new IngresoMensualStatsItem(
                        YearMonth.of(item.getAnio(), item.getMes()).toString(),
                        item.getTotal()))
                .toList();
    }

    public List<Gasto> listarProximos(int dias) {
        LocalDate hoy = operationalClockService.today();
        return gastoRepository.findByActivoTrueAndPagadoFalseAndFechaBetweenOrderByFechaAscIdAsc(
                hoy,
                hoy.plusDays(Math.max(1, dias)));
    }

    public List<Gasto> listarPorStaff(Long staffId) {
        return gastoRepository.findByActivoTrueAndStaffResponsableIdOrderByFechaDescIdDesc(staffId);
    }

    public List<Gasto> listarPorMaquina(Long maquinaId) {
        return gastoRepository.findByActivoTrueAndMaquinaIdOrderByFechaDescIdDesc(maquinaId);
    }

    public List<Gasto> listarPorMaterial(Long materialId) {
        return gastoRepository.findByActivoTrueAndMaterialIdOrderByFechaDescIdDesc(materialId);
    }

    @Transactional
    public int procesarGastosRecurrentes() {
        LocalDate hoy = operationalClockService.today();
        List<Gasto> base = gastoRepository.findByActivoTrueAndRecurrenteTrueAndFechaLessThanEqualOrderByFechaAscIdAsc(hoy);
        int generados = 0;

        for (Gasto gasto : base) {
            if (gasto.getFrecuencia() == null) {
                continue;
            }

            LocalDate proximaFecha = calcularSiguienteFecha(gasto.getFecha(), gasto.getFrecuencia());
            if (proximaFecha == null) {
                continue;
            }

            if (gastoRepository.existsByConceptoAndCategoriaAndFechaAndActivoTrue(
                    gasto.getConcepto(), gasto.getCategoria(), proximaFecha)) {
                continue;
            }

            Gasto clon = new Gasto();
            clon.setConcepto(gasto.getConcepto());
            clon.setCategoria(gasto.getCategoria());
            clon.setImporte(gasto.getImporte());
            clon.setFecha(proximaFecha);
            clon.setRecurrente(true);
            clon.setFrecuencia(gasto.getFrecuencia());
            clon.setPagado(false);
            clon.setProveedor(gasto.getProveedor());
            clon.setObservaciones(gasto.getObservaciones());
            clon.setStaffResponsable(gasto.getStaffResponsable());
            clon.setMaquina(gasto.getMaquina());
            clon.setMaterial(gasto.getMaterial());
            clon.setActivo(true);
            normalizarYValidar(clon);
            gastoRepository.save(clon);
            generados++;
        }

        return generados;
    }

    @Transactional
    public Gasto guardar(Gasto gasto) {
        normalizarYValidar(gasto);
        return gastoRepository.save(gasto);
    }

    @Transactional
    public Gasto actualizar(Long id, Gasto gastoActualizado) {
        Gasto gasto = buscarPorId(id);
        gasto.setConcepto(gastoActualizado.getConcepto());
        gasto.setCategoria(gastoActualizado.getCategoria());
        gasto.setImporte(gastoActualizado.getImporte());
        gasto.setFecha(gastoActualizado.getFecha());
        gasto.setRecurrente(gastoActualizado.getRecurrente());
        gasto.setFrecuencia(gastoActualizado.getFrecuencia());
        gasto.setPagado(gastoActualizado.getPagado());
        gasto.setProveedor(gastoActualizado.getProveedor());
        gasto.setObservaciones(gastoActualizado.getObservaciones());
        gasto.setStaffResponsable(gastoActualizado.getStaffResponsable());
        gasto.setMaquina(gastoActualizado.getMaquina());
        gasto.setMaterial(gastoActualizado.getMaterial());
        gasto.setActivo(gastoActualizado.getActivo());
        normalizarYValidar(gasto);
        return gastoRepository.save(gasto);
    }

    @Transactional
    public void desactivar(Long id) {
        Gasto gasto = buscarPorId(id);
        gasto.setActivo(false);
        gastoRepository.save(gasto);
    }

    @Transactional
    public void activar(Long id) {
        Gasto gasto = buscarPorId(id);
        gasto.setActivo(true);
        gastoRepository.save(gasto);
    }

    private void normalizarYValidar(Gasto gasto) {
        if (!StringUtils.hasText(gasto.getConcepto())) {
            throw new BusinessValidationException("Debes indicar el concepto del gasto");
        }
        if (gasto.getImporte() == null || gasto.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("El importe del gasto debe ser mayor que 0");
        }
        if (gasto.getFecha() == null) {
            throw new BusinessValidationException("Debes indicar la fecha del gasto");
        }
        if (gasto.getCategoria() == null || gasto.getCategoria() == CategoriaGasto.OTROS) {
            gasto.setCategoria(clasificarCategoria(gasto));
        }
        if (gasto.getRecurrente() == null) {
            gasto.setRecurrente(false);
        }
        if (!Boolean.TRUE.equals(gasto.getRecurrente())) {
            gasto.setFrecuencia(null);
        } else if (gasto.getFrecuencia() == null) {
            throw new BusinessValidationException("Debes indicar la frecuencia cuando el gasto es recurrente");
        }
        if (gasto.getPagado() == null) {
            gasto.setPagado(false);
        }
        if (gasto.getActivo() == null) {
            gasto.setActivo(true);
        }

        gasto.setStaffResponsable(validarStaff(gasto.getStaffResponsable()));
        gasto.setMaquina(validarMaquina(gasto.getMaquina()));
        gasto.setMaterial(validarMaterial(gasto.getMaterial()));
    }

    private CategoriaGasto clasificarCategoria(Gasto gasto) {
        if (gasto.getStaffResponsable() != null && gasto.getStaffResponsable().getId() != null) {
            return CategoriaGasto.NOMINA;
        }
        if (gasto.getMaterial() != null && gasto.getMaterial().getId() != null) {
            return CategoriaGasto.COMPRA_MATERIAL;
        }
        if (gasto.getMaquina() != null && gasto.getMaquina().getId() != null) {
            return CategoriaGasto.MANTENIMIENTO;
        }

        String concepto = gasto.getConcepto() == null ? "" : gasto.getConcepto().toLowerCase(Locale.ROOT);
        if (concepto.contains("alquiler") || concepto.contains("renta") || concepto.contains("local")) {
            return CategoriaGasto.ALQUILER;
        }
        if (concepto.contains("nomina") || concepto.contains("salario") || concepto.contains("sueldo")) {
            return CategoriaGasto.NOMINA;
        }
        if (concepto.contains("luz") || concepto.contains("agua") || concepto.contains("internet")) {
            return CategoriaGasto.SUMINISTROS;
        }

        return CategoriaGasto.OTROS;
    }

    private LocalDate calcularSiguienteFecha(LocalDate fechaBase, com.flacofitness.app.model.enums.FrecuenciaGasto frecuencia) {
        if (fechaBase == null || frecuencia == null) {
            return null;
        }

        return switch (frecuencia) {
            case SEMANAL -> fechaBase.plusWeeks(1);
            case QUINCENAL -> fechaBase.plusDays(15);
            case MENSUAL -> fechaBase.plusMonths(1);
            case TRIMESTRAL -> fechaBase.plusMonths(3);
            case ANUAL -> fechaBase.plusYears(1);
        };
    }

    private StaffPerfil validarStaff(StaffPerfil staffPerfil) {
        if (staffPerfil == null || staffPerfil.getId() == null) {
            return null;
        }

        return staffPerfilRepository.findById(staffPerfil.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff no encontrado con id: " + staffPerfil.getId()));
    }

    private Maquina validarMaquina(Maquina maquina) {
        if (maquina == null || maquina.getId() == null) {
            return null;
        }

        return maquinaRepository.findById(maquina.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Maquina no encontrada con id: " + maquina.getId()));
    }

    private Material validarMaterial(Material material) {
        if (material == null || material.getId() == null) {
            return null;
        }

        return materialRepository.findById(material.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con id: " + material.getId()));
    }
}
