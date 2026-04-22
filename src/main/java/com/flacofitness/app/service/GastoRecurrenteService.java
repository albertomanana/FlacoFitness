package com.flacofitness.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.GastoRecurrente;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.FrecuenciaGasto;
import com.flacofitness.app.model.enums.TipoGasto;
import com.flacofitness.app.repository.GastoRecurrenteRepository;
import com.flacofitness.app.repository.MaquinaRepository;
import com.flacofitness.app.repository.MaterialRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;

@Service
@Transactional(readOnly = true)
public class GastoRecurrenteService {

    private final GastoRecurrenteRepository gastoRecurrenteRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final MaquinaRepository maquinaRepository;
    private final MaterialRepository materialRepository;
    private final OperationalClockService operationalClockService;

    public GastoRecurrenteService(GastoRecurrenteRepository gastoRecurrenteRepository,
                                  StaffPerfilRepository staffPerfilRepository,
                                  MaquinaRepository maquinaRepository,
                                  MaterialRepository materialRepository,
                                  OperationalClockService operationalClockService) {
        this.gastoRecurrenteRepository = gastoRecurrenteRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.maquinaRepository = maquinaRepository;
        this.materialRepository = materialRepository;
        this.operationalClockService = operationalClockService;
    }

    public List<GastoRecurrente> listarActivos() {
        return gastoRecurrenteRepository.findByActivoTrueOrderByFechaProximoCargoAscIdAsc();
    }

    public GastoRecurrente buscarPorId(Long id) {
        return gastoRecurrenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla recurrente no encontrada con id: " + id));
    }

    public long contarActivos() {
        return gastoRecurrenteRepository.countByActivoTrue();
    }

    public long contarProximos(int dias) {
        LocalDate hoy = operationalClockService.today();
        return gastoRecurrenteRepository.countByActivoTrueAndFechaProximoCargoBetween(hoy, hoy.plusDays(Math.max(1, dias)));
    }

    @Transactional
    public GastoRecurrente guardar(GastoRecurrente gastoRecurrente) {
        normalizarYValidar(gastoRecurrente);
        return gastoRecurrenteRepository.save(gastoRecurrente);
    }

    @Transactional
    public GastoRecurrente actualizar(Long id, GastoRecurrente actualizado) {
        GastoRecurrente existente = buscarPorId(id);
        existente.setConcepto(actualizado.getConcepto());
        existente.setCategoria(actualizado.getCategoria());
        existente.setTipoGasto(actualizado.getTipoGasto());
        existente.setImporte(actualizado.getImporte());
        existente.setFrecuencia(actualizado.getFrecuencia());
        existente.setFechaInicio(actualizado.getFechaInicio());
        existente.setFechaProximoCargo(actualizado.getFechaProximoCargo());
        existente.setProveedor(actualizado.getProveedor());
        existente.setObservaciones(actualizado.getObservaciones());
        existente.setStaffResponsable(actualizado.getStaffResponsable());
        existente.setMaquina(actualizado.getMaquina());
        existente.setMaterial(actualizado.getMaterial());
        existente.setActivo(actualizado.getActivo());
        normalizarYValidar(existente);
        return gastoRecurrenteRepository.save(existente);
    }

    @Transactional
    public void activar(Long id) {
        GastoRecurrente gastoRecurrente = buscarPorId(id);
        gastoRecurrente.setActivo(true);
        gastoRecurrenteRepository.save(gastoRecurrente);
    }

    @Transactional
    public void desactivar(Long id) {
        GastoRecurrente gastoRecurrente = buscarPorId(id);
        gastoRecurrente.setActivo(false);
        gastoRecurrenteRepository.save(gastoRecurrente);
    }

    private void normalizarYValidar(GastoRecurrente gastoRecurrente) {
        if (!StringUtils.hasText(gastoRecurrente.getConcepto())) {
            throw new BusinessValidationException("Debes indicar el concepto recurrente");
        }
        if (gastoRecurrente.getImporte() == null || gastoRecurrente.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("El importe recurrente debe ser mayor que 0");
        }
        if (gastoRecurrente.getFrecuencia() == null) {
            gastoRecurrente.setFrecuencia(FrecuenciaGasto.MENSUAL);
        }
        if (gastoRecurrente.getFechaInicio() == null) {
            gastoRecurrente.setFechaInicio(operationalClockService.today());
        }
        if (gastoRecurrente.getFechaProximoCargo() == null) {
            gastoRecurrente.setFechaProximoCargo(gastoRecurrente.getFechaInicio());
        }
        if (gastoRecurrente.getCategoria() == null) {
            gastoRecurrente.setCategoria(CategoriaGasto.OTROS);
        }
        if (gastoRecurrente.getTipoGasto() == null) {
            gastoRecurrente.setTipoGasto(TipoGasto.FIJO);
        }
        if (gastoRecurrente.getActivo() == null) {
            gastoRecurrente.setActivo(true);
        }

        gastoRecurrente.setStaffResponsable(validarStaff(gastoRecurrente.getStaffResponsable()));
        gastoRecurrente.setMaquina(validarMaquina(gastoRecurrente.getMaquina()));
        gastoRecurrente.setMaterial(validarMaterial(gastoRecurrente.getMaterial()));
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
