package com.flacofitness.app.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GastoService {

    private final GastoRepository gastoRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final MaquinaRepository maquinaRepository;
    private final MaterialRepository materialRepository;

    public GastoService(GastoRepository gastoRepository,
                        StaffPerfilRepository staffPerfilRepository,
                        MaquinaRepository maquinaRepository,
                        MaterialRepository materialRepository) {
        this.gastoRepository = gastoRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.maquinaRepository = maquinaRepository;
        this.materialRepository = materialRepository;
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
        return gastoRepository.countByActivoTrueAndPagadoFalseAndFechaLessThanEqual(LocalDate.now());
    }

    public long contarRecurrentesProximos(int dias) {
        LocalDate hoy = LocalDate.now();
        return gastoRepository.countByActivoTrueAndRecurrenteTrueAndPagadoFalseAndFechaBetween(hoy, hoy.plusDays(Math.max(1, dias)));
    }

    public BigDecimal calcularGastoMesActual() {
        LocalDate hoy = LocalDate.now();
        return gastoRepository.sumImporteByPeriodo(hoy.getYear(), hoy.getMonthValue());
    }

    public List<IngresoMensualStatsItem> obtenerGastosMensuales() {
        return gastoRepository.sumImporteGroupedByMes().stream()
                .map(item -> new IngresoMensualStatsItem(
                        YearMonth.of(item.getAnio(), item.getMes()).toString(),
                        item.getTotal()))
                .toList();
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
        if (gasto.getCategoria() == null) {
            gasto.setCategoria(CategoriaGasto.OTROS);
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