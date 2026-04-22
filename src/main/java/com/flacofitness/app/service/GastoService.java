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
import com.flacofitness.app.model.dto.GastoCategoriaStatsItem;
import com.flacofitness.app.model.dto.IngresoMensualStatsItem;
import com.flacofitness.app.model.entity.Gasto;
import com.flacofitness.app.model.entity.GastoRecurrente;
import com.flacofitness.app.model.entity.Maquina;
import com.flacofitness.app.model.entity.Material;
import com.flacofitness.app.model.entity.StaffPerfil;
import com.flacofitness.app.model.enums.CategoriaGasto;
import com.flacofitness.app.model.enums.EstadoGasto;
import com.flacofitness.app.model.enums.TipoGasto;
import com.flacofitness.app.repository.GastoRecurrenteRepository;
import com.flacofitness.app.repository.GastoRepository;
import com.flacofitness.app.repository.MaquinaRepository;
import com.flacofitness.app.repository.MaterialRepository;
import com.flacofitness.app.repository.StaffPerfilRepository;

@Service
@Transactional(readOnly = true)
public class GastoService {

    private static final List<EstadoGasto> ESTADOS_ABIERTOS = List.of(
            EstadoGasto.PROGRAMADO,
            EstadoGasto.PENDIENTE,
            EstadoGasto.VENCIDO
    );

    private final GastoRepository gastoRepository;
    private final GastoRecurrenteRepository gastoRecurrenteRepository;
    private final StaffPerfilRepository staffPerfilRepository;
    private final MaquinaRepository maquinaRepository;
    private final MaterialRepository materialRepository;
    private final OperationalClockService operationalClockService;
    private final RecurrenceService recurrenceService;

    public GastoService(GastoRepository gastoRepository,
                        GastoRecurrenteRepository gastoRecurrenteRepository,
                        StaffPerfilRepository staffPerfilRepository,
                        MaquinaRepository maquinaRepository,
                        MaterialRepository materialRepository,
                        OperationalClockService operationalClockService,
                        RecurrenceService recurrenceService) {
        this.gastoRepository = gastoRepository;
        this.gastoRecurrenteRepository = gastoRecurrenteRepository;
        this.staffPerfilRepository = staffPerfilRepository;
        this.maquinaRepository = maquinaRepository;
        this.materialRepository = materialRepository;
        this.operationalClockService = operationalClockService;
        this.recurrenceService = recurrenceService;
    }

    public List<Gasto> listarFiltrados(LocalDate desde,
                                       LocalDate hasta,
                                       CategoriaGasto categoria,
                                       EstadoGasto estado,
                                       TipoGasto tipoGasto,
                                       Long staffId,
                                       Long maquinaId,
                                       Long materialId,
                                       Boolean recurrente,
                                       String proveedor) {
        return gastoRepository.buscarFiltrados(desde, hasta, categoria, estado, tipoGasto,
                staffId, maquinaId, materialId, recurrente, proveedor);
    }

    public List<Gasto> listarFiltrados(LocalDate desde, LocalDate hasta, CategoriaGasto categoria) {
        return listarFiltrados(desde, hasta, categoria, null, null, null, null, null, null, null);
    }

    public Gasto buscarPorId(Long id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado con id: " + id));
    }

    public long contarActivos() {
        return gastoRepository.countByActivoTrue();
    }

    public long contarCriticos() {
        return gastoRepository.countCriticos(List.of(EstadoGasto.PENDIENTE, EstadoGasto.PROGRAMADO, EstadoGasto.VENCIDO),
                operationalClockService.today());
    }

    public long contarRecurrentesProximos(int dias) {
        LocalDate hoy = operationalClockService.today();
        return gastoRecurrenteRepository.countByActivoTrueAndFechaProximoCargoBetween(hoy, hoy.plusDays(Math.max(1, dias)));
    }

    public long contarVencimientosProximos(int dias) {
        LocalDate hoy = operationalClockService.today();
        return gastoRepository.countVencimientosEntre(ESTADOS_ABIERTOS, hoy, hoy.plusDays(Math.max(1, dias)));
    }

    public BigDecimal calcularGastoMesActual() {
        LocalDate hoy = operationalClockService.today();
        BigDecimal total = gastoRepository.sumImporteByPeriodo(hoy.getYear(), hoy.getMonthValue());
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calcularGastoFijoMesActual() {
        LocalDate hoy = operationalClockService.today();
        BigDecimal total = gastoRepository.sumImporteByPeriodoAndTipo(hoy.getYear(), hoy.getMonthValue(), TipoGasto.FIJO);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calcularGastoVariableMesActual() {
        LocalDate hoy = operationalClockService.today();
        BigDecimal total = gastoRepository.sumImporteByPeriodoAndTipo(hoy.getYear(), hoy.getMonthValue(), TipoGasto.VARIABLE);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calcularGastoNominasMesActual() {
        LocalDate hoy = operationalClockService.today();
        BigDecimal total = gastoRepository.sumImporteByPeriodoAndCategoria(hoy.getYear(), hoy.getMonthValue(), CategoriaGasto.NOMINA);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<IngresoMensualStatsItem> obtenerGastosMensuales() {
        return gastoRepository.sumImporteGroupedByMes().stream()
                .map(item -> new IngresoMensualStatsItem(
                        YearMonth.of(item.getAnio(), item.getMes()).toString(),
                        item.getTotal()))
                .toList();
    }

    public List<GastoCategoriaStatsItem> obtenerGastosPorCategoriaMesActual() {
        LocalDate hoy = operationalClockService.today();
        return gastoRepository.sumImporteGroupedByCategoria(hoy.getYear(), hoy.getMonthValue()).stream()
                .map(item -> new GastoCategoriaStatsItem(item.getCategoria().name(), item.getTotal()))
                .toList();
    }

    public List<Gasto> listarProximos(int dias) {
        LocalDate hoy = operationalClockService.today();
        return gastoRepository.findProximosVencimientos(ESTADOS_ABIERTOS, hoy, hoy.plusDays(Math.max(1, dias)));
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
    public int actualizarGastosVencidos() {
        return gastoRepository.marcarVencidos(
                List.of(EstadoGasto.PROGRAMADO, EstadoGasto.PENDIENTE),
                EstadoGasto.VENCIDO,
                operationalClockService.today());
    }

    @Transactional
    public int procesarGastosRecurrentes() {
        LocalDate hoy = operationalClockService.today();
        List<GastoRecurrente> plantillas = gastoRecurrenteRepository
                .findByActivoTrueAndFechaProximoCargoLessThanEqualOrderByFechaProximoCargoAscIdAsc(hoy);
        int generados = 0;

        for (GastoRecurrente plantilla : plantillas) {
            LocalDate cursor = plantilla.getFechaProximoCargo();
            while (cursor != null && !cursor.isAfter(hoy)) {
                if (!gastoRepository.existsByGastoRecurrenteIdAndFechaVencimiento(plantilla.getId(), cursor)) {
                    gastoRepository.save(construirDesdePlantilla(plantilla, cursor));
                    generados++;
                }
                cursor = recurrenceService.nextByFrequency(cursor, plantilla.getFrecuencia());
            }
            plantilla.setFechaProximoCargo(cursor);
            gastoRecurrenteRepository.save(plantilla);
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
        gasto.setTipoGasto(gastoActualizado.getTipoGasto());
        gasto.setImporte(gastoActualizado.getImporte());
        gasto.setFecha(gastoActualizado.getFecha());
        gasto.setFechaVencimiento(gastoActualizado.getFechaVencimiento());
        gasto.setEstado(gastoActualizado.getEstado());
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
    public void marcarPagado(Long id) {
        Gasto gasto = buscarPorId(id);
        gasto.setEstado(EstadoGasto.PAGADO);
        gastoRepository.save(gasto);
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

    Gasto construirGastoNomina(StaffPerfil staffPerfil,
                              String concepto,
                              BigDecimal importe,
                              LocalDate fecha,
                              String referencia) {
        Gasto gasto = new Gasto();
        gasto.setConcepto(concepto);
        gasto.setCategoria(CategoriaGasto.NOMINA);
        gasto.setTipoGasto(TipoGasto.FIJO);
        gasto.setImporte(importe);
        gasto.setFecha(fecha);
        gasto.setFechaVencimiento(fecha);
        gasto.setEstado(EstadoGasto.PENDIENTE);
        gasto.setProveedor("Nomina interna");
        gasto.setObservaciones("Gasto generado desde nomina experimental " + referencia + ".");
        gasto.setStaffResponsable(staffPerfil);
        gasto.setActivo(true);
        normalizarYValidar(gasto);
        return gastoRepository.save(gasto);
    }

    private Gasto construirDesdePlantilla(GastoRecurrente plantilla, LocalDate fechaVencimiento) {
        Gasto gasto = new Gasto();
        gasto.setConcepto(plantilla.getConcepto());
        gasto.setCategoria(plantilla.getCategoria());
        gasto.setTipoGasto(plantilla.getTipoGasto());
        gasto.setImporte(plantilla.getImporte());
        gasto.setFecha(fechaVencimiento);
        gasto.setFechaVencimiento(fechaVencimiento);
        gasto.setEstado(fechaVencimiento.isAfter(operationalClockService.today())
                ? EstadoGasto.PROGRAMADO
                : EstadoGasto.PENDIENTE);
        gasto.setProveedor(plantilla.getProveedor());
        gasto.setObservaciones(plantilla.getObservaciones());
        gasto.setStaffResponsable(plantilla.getStaffResponsable());
        gasto.setMaquina(plantilla.getMaquina());
        gasto.setMaterial(plantilla.getMaterial());
        gasto.setGastoRecurrente(plantilla);
        gasto.setActivo(true);
        normalizarYValidar(gasto);
        return gasto;
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
        if (gasto.getFechaVencimiento() == null) {
            gasto.setFechaVencimiento(gasto.getFecha());
        }
        if (gasto.getCategoria() == null || gasto.getCategoria() == CategoriaGasto.OTROS) {
            gasto.setCategoria(clasificarCategoria(gasto));
        }
        if (gasto.getTipoGasto() == null) {
            gasto.setTipoGasto(inferirTipo(gasto));
        }
        if (gasto.getEstado() == null) {
            gasto.setEstado(gasto.getFechaVencimiento().isAfter(operationalClockService.today())
                    ? EstadoGasto.PROGRAMADO
                    : EstadoGasto.PENDIENTE);
        }
        if (gasto.getEstado() != EstadoGasto.PAGADO
                && gasto.getEstado() != EstadoGasto.CANCELADO
                && gasto.getFechaVencimiento().isBefore(operationalClockService.today())) {
            gasto.setEstado(EstadoGasto.VENCIDO);
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
            return CategoriaGasto.MATERIAL;
        }
        if (gasto.getMaquina() != null && gasto.getMaquina().getId() != null) {
            return CategoriaGasto.MAQUINA;
        }

        String concepto = gasto.getConcepto() == null ? "" : gasto.getConcepto().toLowerCase(Locale.ROOT);
        if (concepto.contains("alquiler") || concepto.contains("renta") || concepto.contains("local")) {
            return CategoriaGasto.ALQUILER;
        }
        if (concepto.contains("nomina") || concepto.contains("salario") || concepto.contains("sueldo")) {
            return CategoriaGasto.NOMINA;
        }
        if (concepto.contains("luz")) {
            return CategoriaGasto.LUZ;
        }
        if (concepto.contains("agua")) {
            return CategoriaGasto.AGUA;
        }
        if (concepto.contains("internet") || concepto.contains("fibra")) {
            return CategoriaGasto.INTERNET;
        }
        if (concepto.contains("limpieza")) {
            return CategoriaGasto.LIMPIEZA;
        }
        if (concepto.contains("software") || concepto.contains("suscripcion")) {
            return CategoriaGasto.SOFTWARE;
        }
        if (concepto.contains("marketing") || concepto.contains("redes")) {
            return CategoriaGasto.MARKETING;
        }

        return CategoriaGasto.OTROS;
    }

    private TipoGasto inferirTipo(Gasto gasto) {
        return switch (gasto.getCategoria()) {
            case ALQUILER, LUZ, AGUA, INTERNET, NOMINA, SOFTWARE, LIMPIEZA -> TipoGasto.FIJO;
            default -> TipoGasto.VARIABLE;
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
