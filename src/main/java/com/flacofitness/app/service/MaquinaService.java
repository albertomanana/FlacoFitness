package com.flacofitness.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.flacofitness.app.exception.BusinessValidationException;
import com.flacofitness.app.exception.DuplicateResourceException;
import com.flacofitness.app.exception.ResourceNotFoundException;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.enums.CategoriaMaquina;
import com.flacofitness.app.model.enums.EstadoMaquina;
import com.flacofitness.app.repository.MaquinaRepository;

@Service
@Transactional(readOnly = true)
public class MaquinaService {

    private final MaquinaRepository maquinaRepository;
    private final OperationalClockService operationalClockService;

    public MaquinaService(MaquinaRepository maquinaRepository,
                          OperationalClockService operationalClockService) {
        this.maquinaRepository = maquinaRepository;
        this.operationalClockService = operationalClockService;
    }

    public List<Maquina> listarFiltradas(EstadoMaquina estado, CategoriaMaquina categoria) {
        if (estado != null && categoria != null) {
            return maquinaRepository.findByActivoTrueAndEstadoAndCategoriaOrderByNombreAsc(estado, categoria);
        }

        if (estado != null) {
            return maquinaRepository.findByActivoTrueAndEstadoOrderByNombreAsc(estado);
        }

        if (categoria != null) {
            return maquinaRepository.findByActivoTrueAndCategoriaOrderByNombreAsc(categoria);
        }

        return maquinaRepository.findByActivoTrueOrderByNombreAsc();
    }

    public Maquina buscarPorId(Long id) {
        return maquinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maquina no encontrada con id: " + id));
    }

    public long contarActivas() {
        return maquinaRepository.countByActivoTrue();
    }

    public long contarFueraDeServicio() {
        return maquinaRepository.countByActivoTrueAndEstadoIn(List.of(EstadoMaquina.AVERIADA, EstadoMaquina.MANTENIMIENTO));
    }

    public long contarRevisionProxima(int dias) {
        return maquinaRepository.countByActivoTrueAndProximaRevisionLessThanEqual(operationalClockService.today().plusDays(Math.max(1, dias)));
    }

    public List<Maquina> listarRevisionProxima(int dias) {
        return maquinaRepository.findByActivoTrueAndProximaRevisionLessThanEqualOrderByProximaRevisionAscNombreAsc(
                operationalClockService.today().plusDays(Math.max(1, dias)));
    }

    public List<Maquina> listarFueraDeServicio() {
        return maquinaRepository.findByActivoTrueAndEstadoInOrderByNombreAsc(
                List.of(EstadoMaquina.AVERIADA, EstadoMaquina.MANTENIMIENTO));
    }

    @Transactional
    public int procesarMaquinasEnMantenimiento() {
        LocalDate hoy = operationalClockService.today();
        List<Maquina> candidatas = maquinaRepository.findByActivoTrueAndProximaRevisionLessThanEqualOrderByProximaRevisionAscNombreAsc(hoy);
        int actualizadas = 0;
        Set<EstadoMaquina> estadosNoOperativos = Set.of(EstadoMaquina.AVERIADA, EstadoMaquina.RETIRADA, EstadoMaquina.MANTENIMIENTO);

        for (Maquina maquina : candidatas) {
            if (estadosNoOperativos.contains(maquina.getEstado())) {
                continue;
            }

            maquina.setEstado(EstadoMaquina.MANTENIMIENTO);
            maquinaRepository.save(maquina);
            actualizadas++;
        }

        return actualizadas;
    }

    @Transactional
    public Maquina guardar(Maquina maquina) {
        validarNumeroSerieUnico(maquina.getNumeroSerie(), null);
        normalizar(maquina);
        return maquinaRepository.save(maquina);
    }

    @Transactional
    public Maquina actualizar(Long id, Maquina maquinaActualizada) {
        Maquina maquina = buscarPorId(id);

        validarNumeroSerieUnico(maquinaActualizada.getNumeroSerie(), id);

        maquina.setNombre(maquinaActualizada.getNombre());
        maquina.setCategoria(maquinaActualizada.getCategoria());
        maquina.setMarca(maquinaActualizada.getMarca());
        maquina.setModelo(maquinaActualizada.getModelo());
        maquina.setNumeroSerie(maquinaActualizada.getNumeroSerie());
        maquina.setUbicacion(maquinaActualizada.getUbicacion());
        maquina.setEstado(maquinaActualizada.getEstado());
        maquina.setFechaCompra(maquinaActualizada.getFechaCompra());
        maquina.setUltimaRevision(maquinaActualizada.getUltimaRevision());
        maquina.setProximaRevision(maquinaActualizada.getProximaRevision());
        maquina.setObservaciones(maquinaActualizada.getObservaciones());
        maquina.setActivo(maquinaActualizada.getActivo());
        normalizar(maquina);
        return maquinaRepository.save(maquina);
    }

    @Transactional
    public void desactivar(Long id) {
        Maquina maquina = buscarPorId(id);
        maquina.setActivo(false);
        maquinaRepository.save(maquina);
    }

    @Transactional
    public void activar(Long id) {
        Maquina maquina = buscarPorId(id);
        maquina.setActivo(true);
        if (maquina.getEstado() == EstadoMaquina.RETIRADA) {
            maquina.setEstado(EstadoMaquina.OPERATIVA);
        }
        maquinaRepository.save(maquina);
    }

    @Transactional
    public Maquina actualizarFotoPath(Long id, String fotoPath) {
        Maquina maquina = buscarPorId(id);
        maquina.setFotoPath(fotoPath);
        return maquinaRepository.save(maquina);
    }

    private void validarNumeroSerieUnico(String numeroSerie, Long idExcluir) {
        if (!StringUtils.hasText(numeroSerie)) {
            return;
        }

        String serialNormalizado = numeroSerie.trim();
        boolean duplicado = idExcluir == null
                ? maquinaRepository.existsByNumeroSerie(serialNormalizado)
                : maquinaRepository.existsByNumeroSerieAndIdNot(serialNormalizado, idExcluir);

        if (duplicado) {
            throw new DuplicateResourceException("Ya existe una maquina con ese numero de serie");
        }
    }

    private void normalizar(Maquina maquina) {
        if (!StringUtils.hasText(maquina.getNombre())) {
            throw new BusinessValidationException("Debes indicar el nombre de la maquina");
        }
        if (maquina.getCategoria() == null) {
            maquina.setCategoria(CategoriaMaquina.FUERZA);
        }
        if (maquina.getEstado() == null) {
            maquina.setEstado(EstadoMaquina.OPERATIVA);
        }
        if (maquina.getActivo() == null) {
            maquina.setActivo(true);
        }
        if (maquina.getProximaRevision() != null
                && maquina.getUltimaRevision() != null
                && maquina.getProximaRevision().isBefore(maquina.getUltimaRevision())) {
            throw new BusinessValidationException("La proxima revision no puede ser anterior a la ultima revision");
        }

        if (StringUtils.hasText(maquina.getNumeroSerie())) {
            maquina.setNumeroSerie(maquina.getNumeroSerie().trim());
        }
    }
}
